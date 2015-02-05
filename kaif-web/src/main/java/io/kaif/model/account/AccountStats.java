package io.kaif.model.account;

import java.util.UUID;

public class AccountStats {

  public static AccountStats zero(UUID accountId) {
    return new AccountStats(accountId, 0, 0, 0, 0, 0);
  }

  private final UUID accountId;

  //total count of created debate
  private final long debateCount;

  //total count of created article (exclude any zone that hide from top)
  private final long articleCount;

  //total count of debate up voted
  private final long debateUpVoted;

  //total count of debate down voted
  private final long debateDownVoted;

  //total count of article up voted
  private final long articleUpVoted;

  AccountStats(UUID accountId,
      long debateCount,
      long articleCount,
      long debateUpVoted,
      long debateDownVoted,
      long articleUpVoted) {
    this.accountId = accountId;
    this.debateCount = debateCount;
    this.articleCount = articleCount;
    this.debateUpVoted = debateUpVoted;
    this.debateDownVoted = debateDownVoted;
    this.articleUpVoted = articleUpVoted;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public long getDebateCount() {
    return debateCount;
  }

  public long getArticleCount() {
    return articleCount;
  }

  public long getDebateUpVoted() {
    return debateUpVoted;
  }

  public long getDebateDownVoted() {
    return debateDownVoted;
  }

  public long getArticleUpVoted() {
    return articleUpVoted;
  }

  @Override
  public String toString() {
    return "AccountStats{" +
        "accountId=" + accountId +
        ", debateCount=" + debateCount +
        ", articleCount=" + articleCount +
        ", debateUpVoted=" + debateUpVoted +
        ", debateDownVoted=" + debateDownVoted +
        ", articleUpVoted=" + articleUpVoted +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    AccountStats that = (AccountStats) o;

    if (articleCount != that.articleCount) {
      return false;
    }
    if (articleUpVoted != that.articleUpVoted) {
      return false;
    }
    if (debateCount != that.debateCount) {
      return false;
    }
    if (debateDownVoted != that.debateDownVoted) {
      return false;
    }
    if (debateUpVoted != that.debateUpVoted) {
      return false;
    }
    if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = accountId != null ? accountId.hashCode() : 0;
    result = 31 * result + (int) (debateCount ^ (debateCount >>> 32));
    result = 31 * result + (int) (articleCount ^ (articleCount >>> 32));
    result = 31 * result + (int) (debateUpVoted ^ (debateUpVoted >>> 32));
    result = 31 * result + (int) (debateDownVoted ^ (debateDownVoted >>> 32));
    result = 31 * result + (int) (articleUpVoted ^ (articleUpVoted >>> 32));
    return result;
  }
}
