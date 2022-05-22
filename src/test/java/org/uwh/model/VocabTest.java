package org.uwh.model;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.assertThrows;


public class VocabTest {
  @Test
  public void validatesDupeVocabWithAliases() {
    Vocabulary vocab = new Vocabulary();
    vocab.insertTerm(new Term<>(Name.of("myns", "a"), Type.STRING, Set.of(Name.of("myns", "b")), List.of()));

    assertThrows(IllegalArgumentException.class, () -> {
      vocab.insertTerm(Term.of("myns", "b", Type.STRING));
    });

    assertThrows(IllegalArgumentException.class, () -> {
      vocab.insertTerm(new Term<>(Name.of("myns", "newb"), Type.STRING, Set.of(Name.of("myns", "b")), List.of()));
    });
  }
}
