package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import javax.annotation.Nullable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authorization;
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

  private static final int PAGE_SIZE = 25;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private DebateDao debateDao;

  @Override
  public Article createExternalLink(Authorization authorization,
      Zone zone,
      String title,
      String url) {
    //creating article should not use cache
    ZoneInfo zoneInfo = zoneDao.loadZoneWithoutCache(zone);

    Account author = accountDao.strongVerifyAccount(authorization)
        .filter(zoneInfo::canWriteArticle)
        .orElseThrow(() -> new AccessDeniedException("no write to create article at zone:" + zone));

    Article article = articleDao.createExternalLink(zone,
        author,
        HtmlUtils.htmlEscape(title),
        HtmlUtils.htmlEscape(url),
        Instant.now());
    accountDao.increaseArticleCount(author);
    return article;
  }

  public Optional<Article> findArticle(Zone zone, FlakeId articleId) {
    return articleDao.findArticle(zone, articleId);
  }

  @Override
  public List<Article> listLatestArticles(Zone zone, @Nullable FlakeId startArticleId) {
    return articleDao.listZoneArticlesDesc(zone, startArticleId, PAGE_SIZE);
  }

  @Override
  public Article loadArticle(Zone zone, FlakeId articleId) throws EmptyResultDataAccessException {
    return articleDao.loadArticle(zone, articleId);
  }

  @Override
  public Debate debate(Zone zone,
      FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      Authorization debaterAuth,
      String content) {
    //creating debate should not use cache
    ZoneInfo zoneInfo = zoneDao.loadZoneWithoutCache(zone);
    Article article = articleDao.loadArticle(zoneInfo.getZone(), articleId);

    Account debater = accountDao.strongVerifyAccount(debaterAuth)
        .filter(zoneInfo::canDebate)
        .orElseThrow(() -> new AccessDeniedException("no write to debate at zone:"
            + article.getZone()));

    Debate parent = Optional.ofNullable(parentDebateId)
        .flatMap(pId -> debateDao.findDebate(article.getArticleId(), pId))
        .orElse(null);
    Debate debate = debateDao.create(article, parent, content, debater, Instant.now());

    //may improve later to make it async, but async has transaction problem
    articleDao.increaseDebateCount(article);
    accountDao.increaseDebateCount(debater);
    return debate;
  }

  /**
   * tree structure, but flatten to list, with order by vote score
   */
  @Override
  public List<Debate> listHotDebates(Zone zone, FlakeId articleId, int offset) {
    //TODO cache
    //TODO paging
    //TODO order by rank
    //TODO do not use offset, use start item instead
    return debateDao.listTreeByArticle(articleId);
  }

  @Override
  public List<Article> listHotArticles(Zone zone, FlakeId startArticleId) {
    //TODO cache
    return articleDao.listZoneHotArticles(zone, startArticleId, PAGE_SIZE);
  }
}
