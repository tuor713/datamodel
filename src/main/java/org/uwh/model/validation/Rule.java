package org.uwh.model.validation;

import org.uwh.model.TagTranslation;


public interface Rule<T> extends TagTranslation<Rule<T>> {
  boolean isSatisfied(T rec);
}
