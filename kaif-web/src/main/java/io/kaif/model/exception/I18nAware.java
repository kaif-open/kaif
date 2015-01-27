package io.kaif.model.exception;

import java.util.Collections;
import java.util.List;

public interface I18nAware {

  String i18nKey();

  default List<String> i18nArgs() {
    return Collections.emptyList();
  }

}
