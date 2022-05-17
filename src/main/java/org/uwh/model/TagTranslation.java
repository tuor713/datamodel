package org.uwh.model;

import java.util.function.Function;


public interface TagTranslation<T> {
  T withTagTranslation(Function<Integer,Integer> mapper);
}
