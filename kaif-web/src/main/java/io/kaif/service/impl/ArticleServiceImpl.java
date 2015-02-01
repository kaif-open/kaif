package io.kaif.service.impl;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.article.ArticleFlakeIdGenerator;
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
  private ArticleFlakeIdGenerator articleFlakeIdGenerator;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private ZoneDao zoneDao;

  @Override
  public Article createExternalLink(UUID accountId, Zone zone, String title, String url) {
    //creating article should not use cache
    ZoneInfo zoneInfo = zoneDao.getZoneWithoutCache(zone);
    Account author = accountDao.findById(accountId)
        .filter(account -> zoneInfo.canWriteArticle(account.getAccountId(),
            account.getAuthorities()))
        .orElseThrow(() -> new AccessDeniedException("no write to create article at zone:" + zone));

    FlakeId flakeId = articleFlakeIdGenerator.next();
    Article article = Article.createExternalLink(zone, flakeId, author, title, url, Instant.now());
    return articleDao.createArticle(article);
  }

  public Optional<Article> findArticle(Zone zone, FlakeId articleId) {
    return articleDao.findArticle(zone, articleId);
  }

  @Override
  public List<Article> listLatestArticles(Zone zone, int page) {
    return articleDao.listArticlesDesc(zone, page * PAGE_SIZE, PAGE_SIZE);
  }
}
