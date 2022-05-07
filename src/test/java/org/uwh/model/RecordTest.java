package org.uwh.model;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.uwh.model.io.DeSerUtil;
import org.uwh.model.types.ListType;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {
  @Test
  public void testInsertRetrieve() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    assertNull(sut.get(fieldA));

    sut.put(fieldA, "string");
    assertEquals("string", sut.get(fieldA));
  }

  @Test
  public void testFieldSugar() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    assertNull(sut.get("myns/a"));
    assertNull(sut.get(Name.ofQualified("myns/a")));

    sut.put("myns/a", "string");
    assertEquals("string", sut.get("myns/a"));
  }

  @Test
  public void testCannotInsertTermNotInVocab() {
    Vocabulary vocab = new Vocabulary(List.of(Term.of(1, "myns", "a", Type.STRING)));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    assertThrows(IllegalArgumentException.class, () -> {
      sut.put(Term.of(2, "myns", "b", Type.STRING), "string");
    });

    assertThrows(IllegalArgumentException.class, () -> {
      sut.put(Term.of(1, "myns", "b", Type.STRING), "string");
    });
  }

  @Test
  public void testSerialization() throws IOException {
    Term<String> fString = Term.of(1, "myns", "string_field", Type.STRING);
    Term<Integer> fInt = Term.of(2, "myns", "int_field", Type.INT);
    Term<Double> fDouble = Term.of(3, "myns", "double_field", Type.DOUBLE);
    Vocabulary vocab = new Vocabulary(List.of(fString, fInt, fDouble));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    sut.put(fString, "string");
    sut.put(fInt, 3);
    sut.put(fDouble, 2.1);

    byte[] bytes = DeSerUtil.serialize(sut);
    Record roundTrip = DeSerUtil.deserialize(schema, bytes);
    assertEquals("string", roundTrip.get(fString));
    assertEquals(3, roundTrip.get(fInt));
    assertEquals(2.1, (double) roundTrip.get(fDouble), 0.00001);
  }

  @Test
  public void testSerializationSize() throws Exception {
    Term<String> fString = Term.of(1, "myns", "string_field", Type.STRING);
    Term<Integer> fInt = Term.of(2, "myns", "int_field", Type.INT);
    Term<Double> fDouble = Term.of(3, "myns", "double_field", Type.DOUBLE);
    Vocabulary vocab = new Vocabulary(List.of(fString, fInt, fDouble));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    sut.put(fString, "string");
    sut.put(fInt, 3);
    sut.put(fDouble, 2.1);

    byte[] bytes = DeSerUtil.serialize(sut);
    System.out.print(bytes.length + " => ");
    for (byte b : bytes) {
      System.out.print(b + " ");
    }
    System.out.println();
    // 24 = field num:1, tag:1, string length:1, string:6, tag:1, int:4, tag:1, double:8
    assertTrue(bytes.length <= 25, "Too long: " + bytes.length);
  }

  @Test
  public void testDeSerWithDifferentSchemaVersions() throws IOException {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    sut.put(fieldA, "string");

    byte[] bytes = DeSerUtil.serialize(sut);

    Term<String> fieldB = new Term<>(1, Name.of("myns", "aprime"), Type.STRING, Set.of(Name.of("myns", "a")));
    Vocabulary vocab2 = new Vocabulary(List.of(fieldB));
    Schema schema2 = new Schema(vocab2, Name.of("myns", "schema"));
    Record roundTrip = DeSerUtil.deserialize(schema2, bytes);
    assertEquals("string", roundTrip.get(fieldB));
  }

  @Test
  public void testSerializeWithNulls() throws IOException {
    Term<String> fString = Term.of(1, "myns", "string_field", Type.STRING);
    Term<Integer> fInt = Term.of(2, "myns", "int_field", Type.INT);
    Term<Double> fDouble = Term.of(3, "myns", "double_field", Type.DOUBLE);
    Vocabulary vocab = new Vocabulary(List.of(fString, fInt, fDouble));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));

    Record sut = new Record(schema);
    sut.put(fString, null);
    sut.put(fInt, null);
    sut.put(fDouble, null);

    byte[] bytes = DeSerUtil.serialize(sut);
    // only the length (0)
    assertEquals(1, bytes.length);

    Record sut2 = DeSerUtil.deserialize(schema, bytes);
    assertNull(sut2.get(fString));
    assertNull(sut2.get(fInt));
    assertNull(sut2.get(fDouble));
  }

  @Test
  public void testValidation() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Term<Integer> fieldB = Term.of(2, "myns", "b", Type.INT);
    Vocabulary vocab = new Vocabulary(List.of(fieldA, fieldB));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));
    Record sut = new Record(schema);
    assertTrue(sut.isValid());

    schema.require(fieldA);
    sut = new Record(schema);
    assertFalse(sut.isValid());
    sut.put(fieldA, "string");
    assertTrue(sut.isValid());

    schema = new Schema(vocab, Name.of("myns", "schema"));
    schema.require(fieldA).allowNoOtherTerms();
    final Record sut2 = new Record(schema);
    assertThrows(IllegalArgumentException.class, () -> {
      sut2.put(fieldB, 3);
    });

    schema.allow(fieldB);
    sut.put(fieldB, 3);
    assertTrue(sut.isValid());

    schema = new Schema(vocab, Name.of("myns", "schema"));
    schema.require(fieldA);
    sut = new Record(schema);
    sut.put(fieldA, "string");
    sut.put(fieldB, 3);
    assertEquals(3, sut.get(fieldB));
  }

  @Test
  public void testListType() throws IOException {
    Term<List<String>> fieldA = Term.of(1, "myns", "a", new ListType<>(Type.STRING));
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(vocab, Name.of("myns", "schema"));
    Record sut = new Record(schema);
    sut.put(fieldA, List.of("hello", "world"));
    assertEquals(List.of("hello", "world"), sut.get(fieldA));
    sut = DeSerUtil.deserialize(schema, DeSerUtil.serialize(sut));
    assertEquals(List.of("hello", "world"), sut.get(fieldA));
  }
}
