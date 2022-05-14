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
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.uwh.model.types.ListType;
import org.uwh.model.types.MapType;
import org.uwh.model.types.Type;
import org.uwh.model.types.UnionType;
import org.uwh.model.validation.Rule;
import org.uwh.model.validation.Rules;
import org.yaml.snakeyaml.Yaml;

public class SchemaRegistry {
  public static SchemaRegistry parse(Path file) throws IOException {
    Vocabulary vocab = new Vocabulary();
    Map<Name,Schema> schemas = new HashMap<>();
    parse(file, vocab, schemas);

    return new SchemaRegistry(vocab, schemas);
  }

  private static void parse(Path file, Vocabulary outVocab, Map<Name, Schema> outSchemas) throws IOException {
    Yaml yaml = new Yaml();
    InputStream fis = Files.newInputStream(file);
    Map map = yaml.load(fis);
    if (map.containsKey("require")) {
      Object v = map.get("require");
      if (v instanceof String) {
        parse(file.resolveSibling((String) v), outVocab, outSchemas);
      } else {
        for (String p : (List<String>) v) {
          parse(file.resolveSibling(p), outVocab, outSchemas);
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
    Schema schema = new Schema(vocab, name);
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

  private static Predicate<Record> parseCondition(Map attrs, Vocabulary vocab, String defaultNamespace) {
    String type = (String) attrs.get("type");
    if ("equals".equals(type)) {
      Function<Record, Object> lhs = parseAccessor(attrs.get("left"), vocab, defaultNamespace);
      Function<Record, Object> rhs = parseAccessor(attrs.get("left"), vocab, defaultNamespace);
      return (rec) -> {
        Object l = lhs.apply(rec);
        Object r = rhs.apply(rec);
        return (l == null && r == null) || (l != null && l.equals(r));
      };
    } else {
      throw new IllegalArgumentException("Unknown predicate type: " + type);
    }
  }

  private static Function<Record, Object> parseAccessor(Object v, Vocabulary vocab, String defaultNamespace) {
    if (v instanceof String && ((String) v).matches("<<.*>>")) {
      Matcher m = Pattern.compile("<<(.*)>>").matcher((String) v);
      m.find();
      String name = m.group(1);
      Name n = parseName(defaultNamespace, name);
      Term t = vocab.lookupTerm(n).orElseThrow(() -> new IllegalArgumentException("Could not find term " + n + " in vocabulary."));
      return (rec) -> rec.get(t);
    } else {
      return (rec) -> v;
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

    return new Term<>(
        (Integer) attrs.get("tag"),
        parseName(defaultNamespace, (String) attrs.get("name")),
        type,
        aliases
    );
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
        List<Type> variants = (List) ((List<Object>) attrs.get("variants")).stream().map(SchemaRegistry::parseType).toList();
        return new UnionType(variants.toArray(new Type[0]));
      } else {
        throw new IllegalArgumentException("Unknown type definition "+type);
      }
    }
  }

  private final Vocabulary vocabulary;
  private final Map<Name, Schema> schemas;

  public SchemaRegistry(Vocabulary vocabulary, Map<Name, Schema> schemas) {
    this.vocabulary = vocabulary;
    this.schemas = new HashMap<>(schemas);
  }

  public Vocabulary getVocabulary() {
    return vocabulary;
  }

  public Map<Name, Schema> getSchemas() {
    return schemas;
  }
}
