package org.uwh.model;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;
import org.uwh.model.validation.Rules;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TermTest {
  @Test
  public void testEquality() {
    Term sut = Term.of("myns/a", Type.STRING);
    assertNotEquals(sut, Term.of("myns/b", Type.STRING));
    assertNotEquals(sut, Term.of("myns/a", Type.STRING));
    assertEquals(sut, sut);
  }

  @Test
  public void testNameMatching() {
    assertTrue(Term.of("myns/a", Type.STRING).matchesName(Name.of("myns", "a")));
    assertTrue(Term.of("myns/a", Type.STRING).matchesName(Name.of("MyNs", "a")));
    assertTrue(Term.of("myns/a", Type.STRING).matchesName(Name.of("myns", "A")));
    assertFalse(Term.of("myns/a", Type.STRING).matchesName(Name.of("myns", "b")));
    assertTrue(new Term(Name.of("myns", "a"), Type.STRING, Set.of(Name.of("myns", "b")), List.of()).matchesName(Name.of("myns", "b")));
  }

  @Test
  public void testValidations() {
    Term<Double> term = new Term(Name.ofQualified("myns/double"), Type.DOUBLE, Set.of(), List.of());
    assertTrue(term.isValid(3.14));
    assertFalse(((Term) term).isValid("hello"));

    Term<Double> sut = term.withValidation(Rules.bounded(0.0,100.0, true, true));
    assertTrue(sut.isValid(3.14));
    assertTrue(sut.isValid(0.0));
    assertTrue(sut.isValid(100.0));
    assertFalse(sut.isValid(-1.0));

    sut = term.withValidation(Rules.all(
        Rules.min(0.0, true),
        Rules.max(100.0, true)
    ));
    assertTrue(sut.isValid(3.14));
    assertTrue(sut.isValid(0.0));
    assertTrue(sut.isValid(100.0));
    assertFalse(sut.isValid(-1.0));

    sut = term.withValidation(Rules.any(
        Rules.min(100.0, false),
        Rules.max(0.0, false)
    ));
    assertFalse(sut.isValid(3.14));
    assertFalse(sut.isValid(0.0));
    assertFalse(sut.isValid(100.0));
    assertTrue(sut.isValid(200.0));
    assertTrue(sut.isValid(-1.0));
  }
}
