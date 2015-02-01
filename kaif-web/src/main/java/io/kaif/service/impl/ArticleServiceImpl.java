package io.kaif.service.impl;

import java.time.Instant;
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
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ArticleService;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ArticleFlakeIdGenerator articleFlakeIdGenerator;

  @Autowired
  private ArticleDao articleDao;

  public Article createExternalLink(ZoneInfo zoneInfo, UUID accountId, String title, String url) {
    //TODO no write authority throw AccessDenied
    Account author = accountDao.findById(accountId).get();
    FlakeId flakeId = articleFlakeIdGenerator.next();
    Article article = Article.createExternalLink(zoneInfo.getZone(),
        flakeId,
        author,
        title,
        url,
        Instant.now());
    return articleDao.createArticle(article);
  }

  public Optional<Article> findArticle(Zone zone, FlakeId articleId) {
    return articleDao.findArticle(zone, articleId);
  }
}
