package org.uwh.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.uwh.model.types.ListType;
import org.uwh.model.types.MapType;
import org.uwh.model.types.Type;
import org.yaml.snakeyaml.Yaml;

public class SchemaRegistry {
  public static SchemaRegistry parse(Path file) throws IOException {
    Yaml yaml = new Yaml();
    InputStream fis = Files.newInputStream(file);
    Map map = (Map) yaml.load(fis);
    Vocabulary vocab = parseVocab((List) map.get("terms"));

    return new SchemaRegistry(vocab, parseSchemas(vocab, (List) map.get("schemas")));
  }

  private static Map<Name,Schema> parseSchemas(Vocabulary vocab, List<?> attrs) {
    return attrs.stream().map(attr -> parseSchema(vocab, (Map) attr)).collect(Collectors.toMap(s -> s.getName(), s -> s));
  }

  private static Schema parseSchema(Vocabulary vocab, Map attrs) {
    Name name = Name.ofQualified((String) attrs.get("name"));
    Schema schema = new Schema(vocab, name);
    if (attrs.containsKey("required")) {
      ((List<String>) attrs.get("required")).stream().forEach(s -> schema.require(vocab.lookupTerm(Name.ofQualified(s)).orElseThrow()));
    }
    if (attrs.containsKey("allowed")) {
      ((List<String>) attrs.get("allowed")).stream().forEach(s -> schema.allow(vocab.lookupTerm(Name.ofQualified(s)).orElseThrow()));
    }
    if (attrs.containsKey("allow_no_other_terms") && (boolean) attrs.get("allow_no_other_terms")) {
      schema.allowNoOtherTerms();
    }
    return schema;
  }

  private static Vocabulary parseVocab(List terms) {
    Vocabulary vocab = new Vocabulary();
    terms.forEach(v -> vocab.insertTerm(parseTerm(vocab, (Map) v)));
    return vocab;
  }

  private static Term<?> parseTerm(Vocabulary vocab, Map attrs) {
    String parent = (String) attrs.get("inherit");
    Type<?> type;
    if (parent != null) {
      type = vocab.lookupTerm(Name.ofQualified(parent)).get().getType();
    } else {
      type = parseType(attrs.get("type"));
    }

    Set<Name> aliases = (attrs.containsKey("aliases") ? (List<String>) attrs.get("aliases") : List.<String>of())
        .stream()
        .map(Name::ofQualified)
        .collect(Collectors.toSet());

    return new Term<>(
        (Integer) attrs.get("tag"),
        Name.ofQualified((String) attrs.get("name")),
        type,
        aliases
    );
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
