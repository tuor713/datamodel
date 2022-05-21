package org.uwh.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.uwh.model.types.ListType;
import org.uwh.model.types.MapType;
import org.uwh.model.types.Type;
import org.uwh.model.types.UnionType;
import org.uwh.model.validation.Predicate;
import org.uwh.model.validation.Rule;
import org.uwh.model.validation.Rules;
import org.yaml.snakeyaml.Yaml;

public class Parser {
  public static Namespace parseNamespace(Path file) throws IOException {
    Vocabulary vocab = new Vocabulary();
    Map<Name,Schema> schemas = new HashMap<>();

    Yaml yaml = new Yaml();
    InputStream fis = Files.newInputStream(file);
    Map map = yaml.load(fis);
    parseContent(file, map, vocab, schemas);

    String name = (String) map.get("namespace");
    SemVer version = SemVer.of((String) map.get("version"));

    return new Namespace(name, version, vocab, schemas);
  }

  private static void parseChild(Path file, Vocabulary outVocab, Map<Name, Schema> outSchemas) throws IOException {
    Yaml yaml = new Yaml();
    InputStream fis = Files.newInputStream(file);
    Map map = yaml.load(fis);
    parseContent(file, map, outVocab, outSchemas);
  }

  private static void parseContent(Path file, Map map, Vocabulary outVocab, Map<Name, Schema> outSchemas) throws IOException {
    if (map.containsKey("require")) {
      Object v = map.get("require");
      if (v instanceof String) {
        parseChild(file.resolveSibling((String) v), outVocab, outSchemas);
      } else {
        for (String p : (List<String>) v) {
          parseChild(file.resolveSibling(p), outVocab, outSchemas);
        }
      }
    }
    String defaultNamespace = (String) map.get("namespace");

    if (map.containsKey("terms")) {
      parseVocab((List) map.get("terms"), defaultNamespace, outVocab);
    }
    if (map.containsKey("schemas")) {
      parseSchemas(outVocab, (List) map.get("schemas"), defaultNamespace, outSchemas);
    }
  }

  private static void parseSchemas(Vocabulary vocab, List<?> attrs, String defaultNamespace, Map<Name,Schema> outSchemas) {
    outSchemas.putAll(attrs.stream().map(attr -> parseSchema(vocab, (Map) attr, defaultNamespace)).collect(Collectors.toMap(s -> s.getName(), s -> s)));
  }

  private static Schema parseSchema(Vocabulary vocab, Map attrs, String defaultNamespace) {
    Name name = parseName(defaultNamespace, (String) attrs.get("name"));
    Schema schema = new Schema(name);
    if (attrs.containsKey("required")) {
      ((List<String>) attrs.get("required"))
          .stream()
          .forEach(s -> schema.require(vocab.lookupTerm(parseName(defaultNamespace, s)).orElseThrow(() -> new IllegalStateException("Attribute "+s+" not found in vocabulary."))));
    }
    if (attrs.containsKey("allowed")) {
      ((List<String>) attrs.get("allowed"))
          .stream()
          .forEach(s -> schema.allow(vocab.lookupTerm(parseName(defaultNamespace, s)).orElseThrow(() -> new IllegalStateException("Attribute "+s+" not found in vocabulary."))));
    }
    if (attrs.containsKey("allow_no_other_terms") && (boolean) attrs.get("allow_no_other_terms")) {
      schema.allowNoOtherTerms();
    }
    if (attrs.containsKey("validation")) {
      ((List) attrs.get("validation")).stream().forEach(m -> schema.require(parseValidationRule((Map) m, vocab, defaultNamespace)));
    }

    return schema;
  }

  private static Rule parseValidationRule(Map attrs, Vocabulary vocab, String defaultNamespace) {
    String type = (String) attrs.get("type");
    if ("one-of".equals(type)) {
      List<Term> terms = parseListOfTerms((List<String>) attrs.get("terms"), vocab, defaultNamespace);
      return Rules.requireOneOf(terms.toArray(new Term[0]));
    } else if ("at-least-one-of".equals(type)) {
      List<Term> terms = parseListOfTerms((List<String>) attrs.get("terms"), vocab, defaultNamespace);
      return Rules.requireOneOrMoreOf(terms.toArray(new Term[0]));
    } else if ("require".equals(type)) {
      List<Term> terms = parseListOfTerms((List<String>) attrs.get("terms"), vocab, defaultNamespace);
      if (terms.size() == 1) {
        return Rules.require(terms.get(0));
      } else {
        return Rules.all(terms.stream().map(t -> Rules.require(t)).collect(Collectors.toList()));
      }
    } else if ("conditionally".equals(type)) {
      Predicate<Record> condition = parseCondition((Map) attrs.get("if"), vocab, defaultNamespace);
      Rule thenRule = parseValidationRule((Map) attrs.get("then"), vocab, defaultNamespace);
      Rule elseRule = attrs.containsKey("else") ? parseValidationRule((Map) attrs.get("else"), vocab, defaultNamespace) : Rules.always();
      return Rules.conditionally(condition, thenRule, elseRule);
    } else {
      throw new IllegalArgumentException("Unknown validation rule type: " + type);
    }
  }

  private static class EqualsPredicate implements Predicate<Record> {
    private final Object lhs;
    private final Object rhs;

    public EqualsPredicate(Object lhs, Object rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }

    @Override
    public boolean test(Record rec) {
      Object l = lhs;
      Object r = rhs;
      if (l instanceof Term<?>) {
        l = rec.get((Term) l);
      }
      if (r instanceof Term<?>) {
        r = rec.get((Term) r);
      }

      return (l == null && r == null) || (l != null && l.equals(r));
    }

    @Override
    public Predicate<Record> withTagTranslation(Function<Integer, Integer> mapper) {
      Object l = (lhs instanceof Term<?>) ? ((Term) lhs).withTagTranslation(mapper) : lhs;
      Object r = (rhs instanceof Term<?>) ? ((Term) rhs).withTagTranslation(mapper) : rhs;
      return new EqualsPredicate(l, r);
    }
  }

  private static Predicate<Record> parseCondition(Map attrs, Vocabulary vocab, String defaultNamespace) {
    String type = (String) attrs.get("type");
    if ("equals".equals(type)) {
      Object lhs = parseAccessor(attrs.get("left"), vocab, defaultNamespace);
      Object rhs = parseAccessor(attrs.get("left"), vocab, defaultNamespace);
      return new EqualsPredicate(lhs, rhs);
    } else {
      throw new IllegalArgumentException("Unknown predicate type: " + type);
    }
  }

  private static Object parseAccessor(Object v, Vocabulary vocab, String defaultNamespace) {
    if (v instanceof String && ((String) v).matches("<<.*>>")) {
      Matcher m = Pattern.compile("<<(.*)>>").matcher((String) v);
      m.find();
      String name = m.group(1);
      Name n = parseName(defaultNamespace, name);
      return vocab.lookupTerm(n).orElseThrow(() -> new IllegalArgumentException("Could not find term " + n + " in vocabulary."));
    } else {
      return v;
    }
  }

  private static List<Term> parseListOfTerms(List<String> names, Vocabulary vocab, String defaultNamespace) {
    return (List) names
        .stream()
        .map(s -> vocab.lookupTerm(parseName(defaultNamespace, s)).get()).toList();
  }

  private static void parseVocab(List terms, String defaultNamespace, Vocabulary outVocab) {
    terms.forEach(v -> outVocab.insertTerm(parseTerm(outVocab, (Map) v, defaultNamespace)));
  }

  private static Term<?> parseTerm(Vocabulary vocab, Map attrs, String defaultNamespace) {
    String parent = (String) attrs.get("inherit");
    Type<?> type;
    if (parent != null) {
      type = vocab.lookupTerm(parseName(defaultNamespace, parent)).get().getType();
    } else {
      type = parseType(attrs.get("type"));
    }

    Set<Name> aliases = (attrs.containsKey("aliases") ? (List<String>) attrs.get("aliases") : List.<String>of())
        .stream()
        .map(s -> parseName(defaultNamespace, s))
        .collect(Collectors.toSet());

    List rules = List.of();
    if (attrs.containsKey("validation")) {
      rules = (List) ((List) attrs.get("validation")).stream().map(v -> parseTermValidation((Map) v, type)).collect(Collectors.toList());
    }

    return new Term<>(
        (Integer) attrs.get("tag"),
        parseName(defaultNamespace, (String) attrs.get("name")),
        type,
        aliases,
        rules
    );
  }

  private static Rule parseTermValidation(Map attrs, Type type) {
    boolean isFloating = type.getClazz().equals(Double.class) || type.getClazz().equals(Float.class);
    if (attrs.containsKey("min")) {
      boolean inclusive = attrs.containsKey("inclusive") ? (boolean) attrs.get("inclusive") : true;
      return isFloating ? Rules.min(((Number) attrs.get("min")).doubleValue(), inclusive) : Rules.min(((Number) attrs.get("min")).longValue(), inclusive);
    } else if (attrs.containsKey("max")) {
      boolean inclusive = attrs.containsKey("inclusive") ? (boolean) attrs.get("inclusive") : true;
      return isFloating ? Rules.max(((Number) attrs.get("max")).doubleValue(), inclusive) : Rules.max(((Number) attrs.get("max")).longValue(), inclusive);
    } else {
      throw new IllegalArgumentException("Unexpected term validation: " + attrs);
    }
  }

  private static Name parseName(String defaultNamespace, String name) {
    if (name.contains("/")) {
      return Name.ofQualified(name);
    } else if (defaultNamespace != null) {
      return Name.of(defaultNamespace, name);
    } else {
      throw new IllegalArgumentException("Expected qualified name or default namespace but found "+ name);
    }
  }

  private static Type<?> parseType(Object type) {
    if (type instanceof String) {
      return Type.forString((String) type);
    } else {
      Map attrs = (Map) type;
      String componentType = (String) attrs.get("type");
      if ("list".equals(componentType)) {
        Type innerType = parseType(attrs.get("item"));
        return new ListType<>(innerType);
      } else if ("map".equals(componentType)) {
        Type keyType = parseType(attrs.get("key"));
        Type valueType = parseType(attrs.get("value"));
        return new MapType<>(keyType, valueType);
      } else if ("union".equals(componentType)) {
        List<Type> variants = (List) ((List<Object>) attrs.get("variants")).stream().map(Parser::parseType).toList();
        return new UnionType(variants.toArray(new Type[0]));
      } else {
        throw new IllegalArgumentException("Unknown type definition "+type);
      }
    }
  }
}
