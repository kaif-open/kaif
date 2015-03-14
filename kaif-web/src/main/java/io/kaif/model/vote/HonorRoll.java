package io.kaif.model.vote;

import java.time.YearMonth;
import java.util.UUID;

import io.kaif.model.zone.Zone;

public class HonorRoll {
  /**
   * create monthly ranking now
   */
  public static HonorRoll zero(UUID accountId,
      Zone zone,
      YearMonth yearMonth,
      String username) {
    return new HonorRoll(accountId,
        zone,
        yearMonth.atDay(1).toString(),
        username,
        0,
        0,
        0);
  }

  private final UUID accountId;

  private final Zone zone;

  private final String bucket;

  private final String username;

  private final long articleUpVoted;

  private final long debateUpVoted;

  private final long debateDownVoted;

  public HonorRoll(UUID accountId,
      Zone zone,
      String bucket,
      String username,
      long articleUpVoted,
      long debateUpVoted,
      long debateDownVoted) {
    this.accountId = accountId;
    this.zone = zone;
    this.bucket = bucket;
    this.username = username;
    this.articleUpVoted = articleUpVoted;
    this.debateUpVoted = debateUpVoted;
    this.debateDownVoted = debateDownVoted;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    HonorRoll honorRoll = (HonorRoll) o;

    if (articleUpVoted != honorRoll.articleUpVoted) {
      return false;
    }
    if (debateUpVoted != honorRoll.debateUpVoted) {
      return false;
    }
    if (debateDownVoted != honorRoll.debateDownVoted) {
      return false;
    }
    if (!accountId.equals(honorRoll.accountId)) {
      return false;
    }
    if (zone != null ? !zone.equals(honorRoll.zone) : honorRoll.zone != null) {
      return false;
    }
    if (!bucket.equals(honorRoll.bucket)) {
      return false;
    }
    return username.equals(honorRoll.username);

  }

  @Override
  public int hashCode() {
    int result = accountId.hashCode();
    result = 31 * result + (zone != null ? zone.hashCode() : 0);
    result = 31 * result + bucket.hashCode();
    result = 31 * result + username.hashCode();
    result = 31 * result + (int) (articleUpVoted ^ (articleUpVoted >>> 32));
    result = 31 * result + (int) (debateUpVoted ^ (debateUpVoted >>> 32));
    result = 31 * result + (int) (debateDownVoted ^ (debateDownVoted >>> 32));
    return result;
  }

  public UUID getAccountId() {
    return accountId;
  }

  public Zone getZone() {
    return zone;
  }

  public String getBucket() {
    return bucket;
  }

  public String getUsername() {
    return username;
  }

  public long getArticleUpVoted() {
    return articleUpVoted;
  }

  public long getDebateUpVoted() {
    return debateUpVoted;
  }

  public long getDebateDownVoted() {
    return debateDownVoted;
  }

  public long getScore() {
    return articleUpVoted + debateUpVoted - debateDownVoted;
  }
}
