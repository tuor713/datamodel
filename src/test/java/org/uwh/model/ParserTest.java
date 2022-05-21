package org.uwh.model;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class ParserTest {
  @Test
  public void testYamlRead() throws Exception {
    Namespace ns = Parser.parseNamespace(Path.of("src/test/resources/schemas.yaml"));

    Vocabulary vocab = ns.getVocab();
    assertEquals(7, vocab.size());
    assertEquals(Type.STRING, vocab.lookupTerm(Name.of("myns", "varianta")).get().getType());
    assertTrue(vocab.hasTerm(Name.ofQualified("myns/a")));
    assertTrue(vocab.hasTerm(Name.ofQualified("otherns/map")));

    Term mynsA = vocab.lookupTerm(Name.of("myns", "a")).get();
    assertEquals(1, mynsA.getAliases().size());

    Map<Name, Schema> schemas = ns.getSchemas();
    assertEquals(2, schemas.size());
  }

  @Test
  public void testTermValidation() throws Exception {
    Namespace ns = Parser.parseNamespace(Path.of("src/test/resources/schemas.yaml"));
    Vocabulary vocab = ns.getVocab();
    Term<Integer> b = (Term<Integer>) vocab.lookupTerm("myns/b").orElseThrow();
    assertTrue(b.isValid(0));
    assertFalse(b.isValid(100));
    assertTrue(b.isValid(99));
    assertFalse(b.isValid(-10));
  }
}
