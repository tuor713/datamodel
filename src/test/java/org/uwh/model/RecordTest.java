package org.uwh.model;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.uwh.model.io.DeSerUtil;
import org.uwh.model.types.ListType;
import org.uwh.model.types.MapType;
import org.uwh.model.types.Type;
import org.uwh.model.types.UnionType;
import org.uwh.model.validation.Predicate;
import org.uwh.model.validation.Rules;

import static org.junit.jupiter.api.Assertions.*;

public class RecordTest {
  @Test
  public void testInsertRetrieve() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertNull(sut.get(fieldA));

    sut.put(fieldA, "string");
    assertEquals("string", sut.get(fieldA));
  }

  @Test
  public void testFieldSugar() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertNull(sut.get("myns/a"));
    assertNull(sut.get(Name.ofQualified("myns/a")));

    sut.put("myns/a", "string");
    String res = sut.get("myns/a");
    assertEquals("string", res);
  }

  @Test
  public void testCannotInsertTermNotInVocab() {
    Vocabulary vocab = new Vocabulary(List.of(Term.of(1, "myns", "a", Type.STRING)));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
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
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    sut.put(fString, "string");
    sut.put(fInt, 3);
    sut.put(fDouble, 2.1);

    byte[] bytes = DeSerUtil.serialize(sut);
    Record roundTrip = DeSerUtil.deserialize(ns.toContext(), schema, bytes);
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
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    sut.put(fString, "string");
    sut.put(fInt, -3);
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
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    sut.put(fieldA, "string");

    byte[] bytes = DeSerUtil.serialize(sut);

    Term<String> fieldB = new Term<>(1, Name.of("myns", "aprime"), Type.STRING, Set.of(Name.of("myns", "a")), List.of());
    Vocabulary vocab2 = new Vocabulary(List.of(fieldB));
    Schema schema2 = new Schema(Name.of("myns", "schema"));
    Namespace ns2 = new Namespace("myns", SemVer.of("1.1.0"), vocab2, Map.of(schema2.getName(), schema2));

    Record roundTrip = DeSerUtil.deserialize(ns2.toContext(), schema2, bytes);
    assertEquals("string", roundTrip.get(fieldB));
  }

  @Test
  public void testSerializeWithNulls() throws IOException {
    Term<String> fString = Term.of(1, "myns", "string_field", Type.STRING);
    Term<Integer> fInt = Term.of(2, "myns", "int_field", Type.INT);
    Term<Double> fDouble = Term.of(3, "myns", "double_field", Type.DOUBLE);
    Vocabulary vocab = new Vocabulary(List.of(fString, fInt, fDouble));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    sut.put(fString, null);
    sut.put(fInt, null);
    sut.put(fDouble, null);

    byte[] bytes = DeSerUtil.serialize(sut);
    // only the length (0)
    assertEquals(1, bytes.length);

    Record sut2 = DeSerUtil.deserialize(ns.toContext(), schema, bytes);
    assertNull(sut2.get(fString));
    assertNull(sut2.get(fInt));
    assertNull(sut2.get(fDouble));
  }

  @Test
  public void testValidation() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Term<Integer> fieldB = Term.of(2, "myns", "b", Type.INT);
    Vocabulary vocab = new Vocabulary(List.of(fieldA, fieldB));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertTrue(sut.isValid());

    schema.require(fieldA);
    sut = new Record(ns.toContext(), schema);
    assertFalse(sut.isValid());
    sut.put(fieldA, null);
    assertFalse(sut.isValid());
    sut.put(fieldA, "string");
    assertTrue(sut.isValid());

    schema = new Schema(Name.of("myns", "schema"));
    schema.require(fieldA).allowNoOtherTerms();
    ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));
    final Record sut2 = new Record(ns.toContext(), schema);
    assertThrows(IllegalArgumentException.class, () -> {
      sut2.put(fieldB, 3);
    });

    schema.allow(fieldB);
    sut.put(fieldB, 3);
    assertTrue(sut.isValid());

    schema = new Schema(Name.of("myns", "schema"));
    schema.require(fieldA);
    ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));
    sut = new Record(ns.toContext(), schema);
    sut.put(fieldA, "string");
    sut.put(fieldB, 3);
    assertEquals(3, sut.get(fieldB));
  }

  @Test
  public void testOneOfRule() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Term<Integer> fieldB = Term.of(2, "myns", "b", Type.INT);
    Vocabulary vocab = new Vocabulary(List.of(fieldA, fieldB));
    Schema schema = new Schema(Name.ofQualified("myns/schema"));
    schema.requireOneOf(fieldA, fieldB);
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertFalse(sut.isValid());

    sut.put(fieldA, "world");
    assertTrue(sut.isValid());

    sut.put(fieldB, 2);
    assertFalse((sut.isValid()));

    sut.put(fieldA, null);
    assertTrue(sut.isValid());
  }

  @Test
  public void testAtLeastOneOfRule() {
    Term<String> fieldA = Term.of(1, "myns", "a", Type.STRING);
    Term<Integer> fieldB = Term.of(2, "myns", "b", Type.INT);
    Vocabulary vocab = new Vocabulary(List.of(fieldA, fieldB));
    Schema schema = new Schema(Name.ofQualified("myns/schema"));
    schema.requireOneOrMoreOf(fieldA, fieldB);
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertFalse(sut.isValid());

    sut.put(fieldA, "hello");
    assertTrue(sut.isValid());

    sut.put(fieldB, 2);
    assertTrue(sut.isValid());

    sut = new Record(ns.toContext(), schema);
    sut.put(fieldB, 2);
    assertTrue(sut.isValid());
  }

  @Test
  public void testConditionalRules() {
    Term<String> fProductType = Term.of(1, "myns/product_type", Type.STRING);
    Term<Double> fNotional = Term.of(2, "myns/notional", Type.DOUBLE);
    Term<Double> fPayNotional = Term.of(3, "myns/pay_notional", Type.DOUBLE);
    Term<Double> fReceiveNotional = Term.of(4, "myns/receive_notional", Type.DOUBLE);
    Vocabulary vocab = new Vocabulary(List.of(fProductType, fNotional, fPayNotional, fReceiveNotional));
    Schema schema = new Schema(Name.ofQualified("myns/trade"));
    schema.require(Rules.conditionally(
        new Predicate<>() {
          @Override
          public boolean test(Record rec) {
            return "SWAP".equals(rec.get(fProductType));
          }

          @Override
          public Predicate<Record> withTagTranslation(Function<Integer, Integer> mapper) {
            return this;
          }
        },
        Rules.all(Rules.require(fPayNotional), Rules.require(fReceiveNotional)),
        Rules.require(fNotional)));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));

    Record sut = new Record(ns.toContext(), schema);
    assertFalse(sut.isValid());
    sut.put(fProductType, "BOND");
    sut.put(fNotional, 1000.0);
    assertTrue(sut.isValid());

    sut = new Record(ns.toContext(), schema);
    sut.put(fProductType, "SWAP");
    sut.put(fNotional, 1000.0);
    assertFalse(sut.isValid());
    sut.put(fNotional, null);
    sut.put(fPayNotional, 1000.0);
    assertFalse(sut.isValid());
    sut.put(fReceiveNotional, 500.0);
    assertTrue(sut.isValid());
  }

  @Test
  public void testMultiNsContext() throws Exception {
    Term<String> fFieldA = Term.of(1, "ns1/a", Type.STRING);
    Term<Double> fFieldB = Term.of(2, "ns1/b", Type.DOUBLE);
    Namespace ns1 = new Namespace("ns1", SemVer.of("1.0.0"), new Vocabulary(List.of(fFieldA, fFieldB)), Map.of());
    Term<Integer> fFieldC = Term.of(1, "ns2/c", Type.INT);
    Term<String> fFieldD = Term.of(2, "ns2/d", Type.STRING);
    Schema schema = new Schema(Name.ofQualified("ns2/record"));
    Namespace ns2 = new Namespace("ns2", SemVer.of("1.0.0"), new Vocabulary(List.of(fFieldC, fFieldD)), Map.of(schema.getName(), schema));
    Context ctx = new Context(Set.of(ns1, ns2));

    Record rec = new Record(ctx, ctx.getSchema("ns2/record").orElseThrow());
    rec.put("ns1/a", "string1");
    rec.put("ns1/b", 3.14);
    rec.put("ns2/c", 42);
    rec.put("ns2/d", "string2");

    byte[] bytes = DeSerUtil.serialize(rec);
    rec = DeSerUtil.deserialize(ctx, ctx.getSchema("ns2/record").orElseThrow(), bytes);
    assertEquals("string1", rec.get("ns1/a"));
    assertEquals(3.14, (double) rec.get("ns1/b"), 0.1);
    assertEquals(42, (int) rec.get("ns2/c"));
    assertEquals("string2", rec.get("ns2/d"));
  }

  @Test
  public void testTypeSupport() throws IOException {
    // one byte for list length, two 5 char strings + 2 byte length
    assertTypeRoundTrips(new ListType<>(Type.STRING), List.of("hello", "world"), 15);
    assertTypeRoundTrips(Type.DATE, LocalDate.now(), 3);
    // Instant.now() generates a more precise timestamp than milliseconds
    assertTypeRoundTrips(Type.TIMESTAMP, Instant.ofEpochMilli(System.currentTimeMillis()), 6);
    assertTypeRoundTrips(Type.LONG, 4L, 1);
    assertTypeRoundTrips(Type.FLOAT, 3.2f, 4);
    assertTypeRoundTrips(Type.BYTES, new byte[] {0,1,2,3}, 5);
    assertTypeRoundTrips(new MapType<>(Type.STRING, Type.LONG), Map.of("one", 1L, "two", 2L), 13);

    Type unionType = new UnionType(new Type[] {Type.LONG, Type.STRING, new ListType(Type.STRING)});
    assertTypeRoundTrips(unionType, 1L, 2);
    assertTypeRoundTrips(unionType, "abc", 6);
    assertTypeRoundTrips(unionType, List.of("hello", "world"), 16);
  }

  private <T> void assertTypeRoundTrips(Type<T> type, T value, int persistedSize) throws IOException {
    Term<T> fieldA = Term.of(1, "myns", "a", type);
    Vocabulary vocab = new Vocabulary(List.of(fieldA));
    Schema schema = new Schema(Name.of("myns", "schema"));
    Namespace ns = new Namespace("myns", SemVer.of("1.0.0"), vocab, Map.of(schema.getName(), schema));
    Record sut = new Record(ns.toContext(), schema);
    sut.put(fieldA, value);
    assertEquals(value, sut.get(fieldA));

    byte[] bytes = DeSerUtil.serialize(sut);
    // two extra bytes: num fields, tag
    assertEquals(persistedSize+2, bytes.length);

    sut = DeSerUtil.deserialize(ns.toContext(), schema, bytes);
    if (value instanceof byte[]) {
      assertArrayEquals((byte[]) value, (byte[]) sut.get(fieldA));
    } else {
      assertEquals(value, sut.get(fieldA));
    }
  }
}
