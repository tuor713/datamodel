package org.uwh.model.validation;

import org.uwh.model.Record;
import org.uwh.model.TagTranslation;


public interface Rule extends TagTranslation<Rule> {
  boolean isSatisfied(Record rec);
}
