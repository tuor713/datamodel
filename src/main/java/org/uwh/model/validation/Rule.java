package org.uwh.model.validation;

import org.uwh.model.Record;

@FunctionalInterface
public interface Rule {
  boolean isSatisfied(Record rec);
}
