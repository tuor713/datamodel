package org.uwh.model;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class SchemaRegistryTest {
  @Test
  public void testYamlRead() throws Exception {
    SchemaRegistry registry = SchemaRegistry.parse(Path.of("src/test/resources/vocab.yaml"));

    Vocabulary vocab = registry.getVocabulary();
    assertEquals(5, vocab.size());
    assertEquals(Type.STRING, vocab.lookupTerm(Name.of("myns", "varianta")).get().getType());
    Term mynsA = vocab.lookupTerm(Name.of("myns", "a")).get();
    assertEquals(1, mynsA.getAliases().size());

    Map<Name, Schema> schemas = registry.getSchemas();
    assertEquals(2, schemas.size());
  }
}
