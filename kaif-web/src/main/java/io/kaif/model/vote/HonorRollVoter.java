package io.kaif.model.vote;

import java.util.UUID;

import com.google.common.annotations.VisibleForTesting;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;

public class HonorRollVoter {

  @VisibleForTesting
  public static class Builder {
    private UUID accountId;
    private FlakeId flakeId;
    private Zone zone;
    private String username;
    private long deltaArticleUpVoted;
    private long deltaDebateUpVoted;
    private long deltaDebateDownVoted;

    public Builder(UUID accountId, FlakeId flakeId, Zone zone, String username) {
      this.accountId = accountId;
      this.flakeId = flakeId;
      this.zone = zone;
      this.username = username;
    }

    public Builder withDeltaArticleUpVoted(long deltaArticleUpVoted) {
      this.deltaArticleUpVoted = deltaArticleUpVoted;
      return this;
    }

    public Builder withDeltaDebateUpVoted(long deltaDebateUpVoted) {
      this.deltaDebateUpVoted = deltaDebateUpVoted;
      return this;
    }

    public Builder withDeltaDebateDownVoted(long deltaDebateDownVoted) {
      this.deltaDebateDownVoted = deltaDebateDownVoted;
      return this;
    }

    public HonorRollVoter build() {
      return new HonorRollVoter(accountId,
          flakeId,
          zone,
          username,
          deltaArticleUpVoted,
          deltaDebateUpVoted,
          deltaDebateDownVoted);
    }
  }

  public static HonorRollVoter createByVote(Article article, int upVoteDelta, int downVoteDelta) {
    return new Builder(article.getAuthorId(),
        article.getArticleId(),
        article.getZone(),
        article.getAuthorName()).withDeltaArticleUpVoted(upVoteDelta - downVoteDelta).build();
  }

  public static HonorRollVoter createByVote(Debate debate, int upVoteDelta, int downVoteDelta) {
    return new Builder(debate.getDebaterId(),
        debate.getDebateId(),
        debate.getZone(),
        debate.getDebaterName()).withDeltaDebateUpVoted(upVoteDelta)
        .withDeltaDebateDownVoted(downVoteDelta)
        .build();
  }
  private final UUID accountId;

  private final FlakeId flakeId;

  private final Zone zone;

  private final String username;

  private final long deltaArticleUpVoted;

  private final long deltaDebateUpVoted;

  private final long deltaDebateDownVoted;

  private HonorRollVoter(UUID accountId,
      FlakeId flakeId,
      Zone zone,
      String username,
      long deltaArticleUpVoted,
      long deltaDebateUpVoted,
      long deltaDebateDownVoted) {
    this.accountId = accountId;
    this.flakeId = flakeId;
    this.zone = zone;
    this.username = username;
    this.deltaArticleUpVoted = deltaArticleUpVoted;
    this.deltaDebateUpVoted = deltaDebateUpVoted;
    this.deltaDebateDownVoted = deltaDebateDownVoted;
  }

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

  public long getDeltaArticleUpVoted() {
    return deltaArticleUpVoted;
  }

  public long getDeltaDebateUpVoted() {
    return deltaDebateUpVoted;
  }

  public long getDeltaDebateDownVoted() {
    return deltaDebateDownVoted;
  }

}
