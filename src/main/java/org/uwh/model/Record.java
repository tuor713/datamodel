package org.uwh.model;

import java.util.HashMap;
import java.util.Map;

public class Record {
  private final Map<Integer,Object> values;
  private final Schema schema;

  public Record(Schema schema) {
    values = new HashMap<>();
    this.schema = schema;
  }

  public Record(Schema schema, Map<Integer,Object> values) {
    this.schema = schema;
    this.values = values;
  }

  public<T> void put(Term<T> t, T value) {
    if (!schema.isValidTerm(t)) {
      throw new IllegalArgumentException("Term " + t + " is not valid for schema " + schema);
    }
    if (value == null) {
      values.remove(t.getTag());
    } else {
      values.put(t.getTag(), value);
    }
  }

  public void put(Name n, Object value) {
    Term t = schema.getVocabulary().lookupTerm(n).orElseThrow();
    put(t, value);
  }

  public void put(String qualifiedName, Object value) {
    put(Name.ofQualified(qualifiedName), value);
  }

  public <T> T get(Term<T> t) {
    return (T) values.get(t.getTag());
  }

  public <T> T get(Name n) {
    Term t = schema.getVocabulary().lookupTerm(n).orElseThrow();
    return (T) get(t);
  }

  public <T> T get(String qualifiedName) {
    return get(Name.ofQualified(qualifiedName));
  }

  public Map<Integer,Object> getValues() {
    return values;
  }

  public Schema getSchema() {
    return schema;
  }

  public boolean isValid() {
    return schema.isValid(this);
  }
}
