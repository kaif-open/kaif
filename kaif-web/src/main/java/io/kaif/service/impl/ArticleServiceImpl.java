package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ArticleService;
import io.kaif.web.support.AccessDeniedException;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

  private static final int PAGE_SIZE = 30;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private DebateDao debateDao;

  @Override
  public Article createExternalLink(UUID accountId, Zone zone, String title, String url) {
    //creating article should not use cache
    ZoneInfo zoneInfo = zoneDao.getZoneWithoutCache(zone);
    Account author = accountDao.findById(accountId)
        .filter(zoneInfo::canWriteArticle)
        .orElseThrow(() -> new AccessDeniedException("no write to create article at zone:" + zone));

    return articleDao.createExternalLink(zone, author, title, url, Instant.now());
  }

  public Optional<Article> findArticle(Zone zone, FlakeId articleId) {
    return articleDao.findArticle(zone, articleId);
  }

  @Override
  public List<Article> listLatestArticles(Zone zone, int page) {
    return articleDao.listArticlesDesc(zone, page * PAGE_SIZE, PAGE_SIZE);
  }

  @Override
  public Article getArticle(Zone zone, FlakeId articleId) throws EmptyResultDataAccessException {
    return articleDao.getArticle(zone, articleId);
  }

  public Debate debate(Zone zone,
      FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      UUID debaterId,
      String content) {
    //creating debate should not use cache
    ZoneInfo zoneInfo = zoneDao.getZoneWithoutCache(zone);
    Article article = articleDao.getArticle(zoneInfo.getZone(), articleId);

    Account debater = accountDao.findById(debaterId)
        .filter(zoneInfo::canDebate)
        .orElseThrow(() -> new AccessDeniedException("no write to debate at zone:"
            + article.getZone()));

    Debate parent = Optional.ofNullable(parentDebateId)
        .flatMap(pId -> debateDao.findDebate(article.getArticleId(), pId))
        .orElse(null);
    return debateDao.create(article, parent, content, debater, Instant.now());
  }

  /**
   * tree structure, but flatten to list, with order by vote score
   */
  public List<Debate> listHotDebates(Zone zone, FlakeId articleId, int offset) {
    //TODO paging
    //TODO order by rank
    return debateDao.listTreeByArticle(articleId);
  }
}
