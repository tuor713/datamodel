package org.uwh.model;

import java.util.Set;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.*;

public class TermTest {
  @Test
  public void testEqualityByTag() {
    assertEquals(Term.of(1, "myns", "a", Type.STRING), Term.of(1, "myns", "b", Type.STRING));
    assertNotEquals(Term.of(1, "myns", "a", Type.STRING), Term.of(2, "myns", "a", Type.STRING));
  }

  @Test
  public void testNameMatching() {
    assertTrue(Term.of(1, "myns", "a", Type.STRING).matchesName(Name.of("myns", "a")));
    assertTrue(Term.of(1, "myns", "a", Type.STRING).matchesName(Name.of("MyNs", "a")));
    assertTrue(Term.of(1, "myns", "a", Type.STRING).matchesName(Name.of("myns", "A")));
    assertFalse(Term.of(1, "myns", "a", Type.STRING).matchesName(Name.of("myns", "b")));
    assertTrue(new Term(1, Name.of("myns", "a"), Type.STRING, Set.of(Name.of("myns", "b"))).matchesName(Name.of("myns", "b")));
  }
}
