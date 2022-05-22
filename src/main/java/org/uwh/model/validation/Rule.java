package org.uwh.model.validation;

@FunctionalInterface
public interface Rule<T> {
  boolean isSatisfied(T rec);
}
