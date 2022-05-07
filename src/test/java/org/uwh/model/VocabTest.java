package org.uwh.model;

import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.*;


public class VocabTest {
  @Test
  public void validatesDupeVocab() {
    assertThrows(IllegalArgumentException.class, () -> {
      Vocabulary vocab = new Vocabulary(List.of(Term.of(1, "myns", "a", Type.STRING), Term.of(1, "myns", "b", Type.STRING)));
    });
  }

  @Test
  public void validatesDupeVocabWithAliases() {
    Vocabulary vocab = new Vocabulary();
    vocab.insertTerm(new Term<>(1, Name.of("myns", "a"), Type.STRING, Set.of(Name.of("myns", "b"))));

    assertThrows(IllegalArgumentException.class, () -> {
      vocab.insertTerm(Term.of(2, "myns", "b", Type.STRING));
    });

    assertThrows(IllegalArgumentException.class, () -> {
      vocab.insertTerm(new Term<>(2, Name.of("myns", "newb"), Type.STRING, Set.of(Name.of("myns", "b"))));
    });
  }
}
