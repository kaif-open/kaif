package io.kaif.model.clientapp;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.google.common.base.Strings;

public enum ClientAppScope {
  PUBLIC,
  USER,
  FEED,
  ARTICLE,
  DEBATE,
  VOTE;

  /**
   * if any one of scope is invalid, return empty set
   */
  public static Set<ClientAppScope> tryParse(String rawScopes) {
    return Optional.ofNullable(rawScopes)
        .map(scopes -> Pattern.compile("\\s+")
            .splitAsStream(scopes)
            .filter(s -> !Strings.isNullOrEmpty(s)))
        .map(nameStream -> {
          Set<ClientAppScope> parsed = nameStream.map(name -> {
            try {
              return ClientAppScope.valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
              return null;
            }
          }).collect(Collectors.toSet());
          if (parsed.contains(null)) {
            return null;
          }
          return parsed;
        })
        .orElse(Collections.emptySet());
  }

  public static String toCanonicalString(Set<ClientAppScope> nonEmptyScopes) {
    return nonEmptyScopes.stream()
        .map(ClientAppScope::toString)
        .sorted()
        .collect(Collectors.joining(" "));
  }

  @Override
  public String toString() {
    return name().toLowerCase();
  }

  public String getI18nKey() {
    return "client-app-scope." + name().toLowerCase();
  }
}
