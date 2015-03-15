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

  //total count of article up voted
  private final long articleUpVoted;

  //total count of debate up voted
  private final long debateUpVoted;

  //total count of debate down voted
  private final long debateDownVoted;

  AccountStats(UUID accountId,
      long debateCount,
      long articleCount,
      long articleUpVoted,
      long debateUpVoted,
      long debateDownVoted) {
    this.accountId = accountId;
    this.debateCount = debateCount;
    this.articleCount = articleCount;
    this.articleUpVoted = articleUpVoted;
    this.debateUpVoted = debateUpVoted;
    this.debateDownVoted = debateDownVoted;
  }

  @Override
  public String toString() {
    return "AccountStats{" +
        "accountId=" + accountId +
        ", debateCount=" + debateCount +
        ", articleCount=" + articleCount +
        ", articleUpVoted=" + articleUpVoted +
        ", debateUpVoted=" + debateUpVoted +
        ", debateDownVoted=" + debateDownVoted +
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
    result = 31 * result + (int) (articleUpVoted ^ (articleUpVoted >>> 32));
    result = 31 * result + (int) (debateUpVoted ^ (debateUpVoted >>> 32));
    result = 31 * result + (int) (debateDownVoted ^ (debateDownVoted >>> 32));
    return result;
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

  //聲望
  public long getHonorScore() {
    return articleUpVoted + debateUpVoted - debateDownVoted;
  }

  //積分
  public long getDebateTotalVoted() {
    return debateUpVoted - debateDownVoted;
  }
}
