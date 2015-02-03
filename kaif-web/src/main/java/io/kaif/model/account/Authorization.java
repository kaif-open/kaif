package io.kaif.model.account;

import java.util.Collection;
import java.util.UUID;

public interface Authorization {

  public boolean belongToAccounts(Collection<UUID> accountIds);

  public boolean containsAuthority(Authority authority);
}
