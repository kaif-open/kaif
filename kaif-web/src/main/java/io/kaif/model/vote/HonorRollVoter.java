package io.kaif.model.vote;

import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;

public class HonorRollVoter {

  public static HonorRollVoter create(Debate debate) {
    return new HonorRollVoterBuilder(debate.getDebaterId(),
        debate.getDebateId(),
        debate.getZone(),
        debate.getDebaterName())
        .withDeltaDebateCount(1)
        .build();
  }

  public static HonorRollVoter create(Article article) {
    return new HonorRollVoterBuilder(article.getAuthorId(),
        article.getArticleId(),
        article.getZone(),
        article.getAuthorName())
        .withDeltaArticleCount(1)
        .build();
  }

  @VisibleForTesting
  HonorRollVoter(UUID accountId,
      FlakeId flakeId,
      Zone zone,
      String username,
      long deltaDebateCount,
      long deltaArticleCount,
      long deltaArticleUpVoted,
      long deltaDebateUpVoted, long deltaDebateDownVoted) {
    this.accountId = accountId;
    this.flakeId = flakeId;
    this.zone = zone;
    this.username = username;
    this.deltaDebateCount = deltaDebateCount;
    this.deltaArticleCount = deltaArticleCount;
    this.deltaArticleUpVoted = deltaArticleUpVoted;
    this.deltaDebateUpVoted = deltaDebateUpVoted;
    this.deltaDebateDownVoted = deltaDebateDownVoted;
  }

  private final UUID accountId;

  private final FlakeId flakeId;

  private final Zone zone;

  private final String username;

  private final long deltaDebateCount;

  private final long deltaArticleCount;

  private final long deltaArticleUpVoted;

  private final long deltaDebateUpVoted;

  private final long deltaDebateDownVoted;

  public UUID getAccountId() {
    return accountId;
  }

  public FlakeId getFlakeId() {
    return flakeId;
  }

  public Zone getZone() {
    return zone;
  }

  public String getUsername() {
    return username;
  }

  public long getDeltaDebateCount() {
    return deltaDebateCount;
  }

  public long getDeltaArticleCount() {
    return deltaArticleCount;
  }

  public long getDeltaArticleUpVoted() {
    return deltaArticleUpVoted;
  }

  public long getDeltaDebateUpVoted() {
    return deltaDebateUpVoted;
  }

  public long getDeltaDebateDownVoted() {
    return deltaDebateDownVoted;
  }

  @VisibleForTesting
  public static class HonorRollVoterBuilder {
    private UUID accountId;
    private FlakeId flakeId;
    private Zone zone;
    private String username;
    private long deltaDebateCount;
    private long deltaArticleCount;
    private long deltaArticleUpVoted;
    private long deltaDebateUpVoted;
    private long deltaDebateDownVoted;

    public HonorRollVoterBuilder(UUID accountId,
        FlakeId flakeId,
        Zone zone,
        String username) {
      this.accountId = accountId;
      this.flakeId = flakeId;
      this.zone = zone;
      this.username = username;
    }

    public HonorRollVoterBuilder withDeltaDebateCount(long deltaDebateCount) {
      this.deltaDebateCount = deltaDebateCount;
      return this;
    }

    public HonorRollVoterBuilder withDeltaArticleCount(long deltaArticleCount) {
      this.deltaArticleCount = deltaArticleCount;
      return this;
    }

    public HonorRollVoterBuilder withDeltaArticleUpVoted(long deltaArticleUpVoted) {
      this.deltaArticleUpVoted = deltaArticleUpVoted;
      return this;
    }

    public HonorRollVoterBuilder withDeltaDebateUpVoted(long deltaDebateUpVoted) {
      this.deltaDebateUpVoted = deltaDebateUpVoted;
      return this;
    }

    public HonorRollVoterBuilder withDeltaDebateDownVoted(long deltaDebateDownVoted) {
      this.deltaDebateDownVoted = deltaDebateDownVoted;
      return this;
    }

    public HonorRollVoter build() {
      return new HonorRollVoter(accountId,
          flakeId,
          zone,
          username,
          deltaDebateCount,
          deltaArticleCount,
          deltaArticleUpVoted,
          deltaDebateUpVoted,
          deltaDebateDownVoted);
    }
  }
}
