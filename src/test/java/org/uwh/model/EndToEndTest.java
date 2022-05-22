package org.uwh.model;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.uwh.model.io.DeSerUtil;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class EndToEndTest {
  @Test
  public void testE2EWithYAML() throws Exception {
    Context ctx = Parser.parseNamespace(Path.of("src/test/resources/e2e/trading.yaml")).toContext();
    Schema schema = ctx.getSchema("trading/trade").orElseThrow();

    Record t = new Record(ctx, schema);
    t.put("trading/id", "123");
    t.put("trading/notional", 1000.0);
    t.put("trading/book", "MYBOOK");
    assertTrue(t.isValid());
  }

  @Test
  public void testProgressiveEnrichmentSharedVocab() throws Exception {
    // Producer - schemaA
    Term<String> fTradeId = Term.of("trading/id", Type.STRING);
    Term<Double> fNotional = Term.of("trading/notional", Type.DOUBLE);
    Term<String> fSecurityId = Term.of("trading/security_id", Type.STRING);
    Term<String> fTicker = Term.of("security/ticker", Type.STRING);
    Term<String> fBook = Term.of("trading/book", Type.STRING);
    Vocabulary voc = new Vocabulary(List.of(fTradeId, fNotional, fSecurityId, fTicker, fBook));
    Schema schemaA = new Schema(Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).require(fBook);
    Namespace ns_v2 = new Namespace("myns", SemVer.of("1.2.0"), voc, Map.of(schemaA.getName(), schemaA));

    Record original = new Record(ns_v2.toContext(), schemaA);
    original.put(fTradeId, "123");
    original.put(fNotional, 1000000.0);
    original.put(fSecurityId, "1234567E9");
    original.put(fBook, "MYBOOK");

    byte[] bytes = DeSerUtil.serialize(original);


    // Enricher - schemaB
    Schema schemaB = new Schema(Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).allow(fSecurityId).allow(fTicker);
    Namespace ns_v1 = new Namespace("myns", SemVer.of("1.1.0"), voc, Map.of(schemaB.getName(), schemaB));

    Record enriched = DeSerUtil.deserialize(ns_v1.toContext(), schemaB, bytes);
    enriched.put(fTicker, "IBM");

    bytes = DeSerUtil.serialize(enriched);


    // Consumer - schemaC
    Schema schemaC = new Schema(Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).allow(fSecurityId).allow(fTicker);
    Namespace ns_v3 = new Namespace("myns", SemVer.of("1.3.0"), voc, Map.of(schemaC.getName(), schemaC));
    Record finalRec = DeSerUtil.deserialize(ns_v3.toContext(), schemaC, bytes);
    assertEquals("IBM", finalRec.get(fTicker));
    assertEquals("MYBOOK", finalRec.get(fBook));
  }
}
