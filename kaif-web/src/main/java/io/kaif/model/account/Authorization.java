package io.kaif.model.account;

import java.util.Collection;
import java.util.UUID;

public interface Authorization {

  UUID authenticatedId();

  default boolean belongToAccounts(Collection<UUID> accountIds) {
    return accountIds.contains(authenticatedId());
  }

  boolean containsAuthority(Authority authority);

  /**
   * matches() only check if any data changed, it has no security meaning. the actual protection is
   * base on SecureTokenCodec or database, not this method.
   */
  boolean matches(Account account);
}
