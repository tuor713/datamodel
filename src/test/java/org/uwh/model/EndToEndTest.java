package org.uwh.model;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.uwh.model.io.DeSerUtil;
import org.uwh.model.types.Type;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EndToEndTest {
  @Test
  public void testProgressiveEnrichmentSharedVocab() throws Exception {
    // Producer - schemaA
    Term<String> fTradeId = Term.of(1, "trading", "id", Type.STRING);
    Term<Double> fNotional = Term.of(2, "trading", "notional", Type.DOUBLE);
    Term<String> fSecurityId = Term.of(3, "trading", "security_id", Type.STRING);
    Term<String> fTicker = Term.of(4, "security", "ticker", Type.STRING);
    Term<String> fBook = Term.of(5, "trading", "book", Type.STRING);
    Vocabulary voc = new Vocabulary(List.of(fTradeId, fNotional, fSecurityId, fTicker, fBook));
    Schema schemaA = new Schema(voc, Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).require(fBook);

    Record original = new Record(schemaA);
    original.put(fTradeId, "123");
    original.put(fNotional, 1000000.0);
    original.put(fSecurityId, "1234567E9");
    original.put(fBook, "MYBOOK");

    byte[] bytes = DeSerUtil.serialize(original);


    // Enricher - schemaB
    Schema schemaB = new Schema(voc, Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).allow(fSecurityId).allow(fTicker);

    Record enriched = DeSerUtil.deserialize(schemaB, bytes);
    enriched.put(fTicker, "IBM");

    bytes = DeSerUtil.serialize(enriched);


    // Consumer - schemaC
    Schema schemaC = new Schema(voc, Name.of("trading", "trade"))
        .require(fTradeId).require(fNotional).allow(fSecurityId).allow(fTicker);
    Record finalRec = DeSerUtil.deserialize(schemaC, bytes);
    assertEquals("IBM", finalRec.get(fTicker));
    assertEquals("MYBOOK", finalRec.get(fBook));
  }
}
