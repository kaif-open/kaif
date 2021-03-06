package io.kaif.service.impl;

import static java.util.stream.Collectors.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import javax.annotation.Nullable;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Charsets;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountDao;
import io.kaif.model.account.Authority;
import io.kaif.model.account.Authorization;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.debate.DebateTree;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneDao;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ArticleService;
import io.kaif.service.FeedService;
import io.kaif.web.support.AccessDeniedException;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

  private static final Logger editLog = LoggerFactory.getLogger("EDIT");

  static final int PAGE_SIZE = 25;

  @Autowired
  private AccountDao accountDao;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private ZoneDao zoneDao;

  @Autowired
  private DebateDao debateDao;

  @Autowired
  private FeedService feedService;
  private Clock clock = Clock.systemDefaultZone();

  @VisibleForTesting
  void setClock(Clock clock) {
    this.clock = clock;
  }

  @Override
  public Article createExternalLink(Authorization authorization,
      Zone zone,
      String title,
      String link) {
    return createArticle(authorization,
        zone,
        (zoneInfo, author) -> articleDao.createExternalLink(zoneInfo,
            author,
            title,
            link,
            canonicalizeUrl(link),
            Instant.now(clock)));
  }

  private Article createArticle(Authorization authorization,
      Zone zone,
      BiFunction<ZoneInfo, Account, Article> articleCreator) {
    //creating article should not use cache
    ZoneInfo zoneInfo = zoneDao.loadZoneWithoutCache(zone);

    Account author = accountDao.strongVerifyAccount(authorization)
        .filter(zoneInfo::canWriteArticle)
        .orElseThrow(() -> new AccessDeniedException("no write to create article at zone:" + zone));

    Article article = articleCreator.apply(zoneInfo, author);
    if (zoneInfo.getWriteAuthority() == Authority.CITIZEN) {
      accountDao.increaseArticleCount(author);
    }
    return article;
  }

  @Override
  public boolean canCreateArticle(Zone zone, Authorization auth) {
    ZoneInfo zoneInfo = zoneDao.loadZoneWithCache(zone);
    return accountDao.strongVerifyAccount(auth).filter(zoneInfo::canWriteArticle).isPresent();
  }

  @Override
  public Article createSpeak(Authorization authorization, Zone zone, String title, String content) {
    return createArticle(authorization,
        zone,
        (zoneInfo, author) -> articleDao.createSpeak(zoneInfo,
            author,
            title,
            content,
            Instant.now(clock)));
  }

  @Override
  public Optional<Article> findArticle(FlakeId articleId) {
    return articleDao.findArticle(articleId);
  }

  @Override
  public List<Article> listLatestZoneArticles(Zone zone, @Nullable FlakeId startArticleId) {
    return articleDao.listZoneArticlesDesc(zone, startArticleId, PAGE_SIZE);
  }

  @Override
  public Article loadArticle(FlakeId articleId) throws EmptyResultDataAccessException {
    return articleDao.loadArticleWithoutCache(articleId);
  }

  @Override
  public String loadEditableDebateContent(FlakeId debateId, Authorization editor) {
    Debate debate = debateDao.loadDebateWithoutCache(debateId);
    accountDao.strongVerifyAccount(editor)
        .filter(debate::canEdit)
        .orElseThrow(() -> new AccessDeniedException("no permission to edit debate:"
            + debate.getDebateId()));

    return debate.getEscapeContent();
  }

  @Override
  public String updateDebateContent(FlakeId debateId, Authorization editorAuth, String content) {
    Debate debate = debateDao.loadDebateWithoutCache(debateId);
    accountDao.strongVerifyAccount(editorAuth)
        .filter(debate::canEdit)
        .orElseThrow(() -> new AccessDeniedException("no permission to edit debate:" + debateId));

    debateDao.updateContent(debateId, content, Instant.now(clock));

    editLog.info("user(id:{}) update debate's(id:{}) content:{}",
        editorAuth.authenticatedId(),
        debateId.value(),
        debate.getContent());

    return debateDao.loadDebateWithoutCache(debateId).getRenderContent();
  }

  @Override
  public Debate debate(FlakeId articleId,
      @Nullable FlakeId parentDebateId,
      Authorization debaterAuth,
      String content) {
    //creating debate should not use cache
    Article article = articleDao.loadArticleWithoutCache(articleId);
    ZoneInfo zoneInfo = zoneDao.loadZoneWithoutCache(article.getZone());

    Account debater = accountDao.strongVerifyAccount(debaterAuth)
        .filter(zoneInfo::canDebate)
        .orElseThrow(() -> new AccessDeniedException("no write to debate at zone:"
            + article.getZone()));

    Debate parent = Optional.ofNullable(parentDebateId).flatMap(debateDao::findDebate).orElse(null);
    Debate debate = debateDao.create(article, parent, content, debater, Instant.now(clock));

    //may improve later to make it async, but async has transaction problem
    articleDao.increaseDebateCount(article);

    if (zoneInfo.getDebateAuthority() == Authority.CITIZEN) {
      accountDao.increaseDebateCount(debater);
    }

    if (!debate.getReplyToAccountId().equals(debater.getAccountId())) {
      feedService.createReplyFeed(debate.getDebateId(), debate.getReplyToAccountId());
    }
    return debate;
  }

  @Override
  public DebateTree listBestDebates(FlakeId articleId, @Nullable FlakeId parentDebateId) {
    //TODO cache
    //TODO paging
    return debateDao.listDebateTreeByArticle(articleId, parentDebateId);
  }

  @Override
  public List<Article> listHotZoneArticles(Zone zone, FlakeId startArticleId) {
    //TODO cache
    return articleDao.listZoneHotArticles(zone, startArticleId, PAGE_SIZE);
  }

  @Override
  @Cacheable(value = "rssHotArticles")
  public List<Article> listRssHotZoneArticlesWithCache(Zone zone) {
    return listHotZoneArticles(zone, null);
  }

  @Override
  @Cacheable(value = "rssHotArticles")
  public List<Article> listRssTopArticlesWithCache() {
    return listTopArticles(null);
  }

  @Override
  public List<Article> listLatestArticles(@Nullable FlakeId startArticleId) {
    return articleDao.listArticlesDesc(startArticleId, PAGE_SIZE);
  }

  @Override
  public List<Article> listTopArticles(@Nullable FlakeId startArticleId) {
    //TODO cache
    return articleDao.listHotArticlesExcludeHidden(startArticleId, PAGE_SIZE);
  }

  @Override
  public Debate loadDebateWithoutCache(FlakeId debateId) {
    return debateDao.loadDebateWithoutCache(debateId);
  }

  @Override
  public Debate loadDebateWithCache(FlakeId debateId) {
    return debateDao.loadDebateWithCache(debateId);
  }

  @Override
  public List<Debate> listReplyToDebates(Authorization authorization,
      @Nullable FlakeId startDebateId) {
    return debateDao.listLatestDebateByReplyTo(authorization.authenticatedId(),
        startDebateId,
        PAGE_SIZE);
  }

  @Override
  public List<Debate> listLatestDebates(@Nullable FlakeId startDebateId) {
    return debateDao.listDebatesByTimeDesc(startDebateId, PAGE_SIZE);
  }

  @Override
  public List<Debate> listLatestZoneDebates(Zone zone, @Nullable FlakeId startDebateId) {
    return debateDao.listZoneDebatesByTimeDesc(zone, startDebateId, PAGE_SIZE);
  }

  @Override
  public List<Article> listArticlesByDebatesWithCache(List<FlakeId> debateIds) {
    return articleDao.listArticlesByDebatesWithCache(debateIds);
  }

  @Override
  public List<Debate> listDebatesByIdWithCache(List<FlakeId> debateIds) {
    return debateDao.listDebatesByIdWithCache(debateIds);
  }

  @Override
  public List<Article> listArticlesByAuthor(String username, @Nullable FlakeId startArticleId) {
    // we don't use join because we may use cache to optimize later
    Account author = accountDao.loadByUsername(username);
    return articleDao.listArticlesByAuthor(author.getAccountId(), startArticleId, PAGE_SIZE);
  }

  @Override
  public List<Debate> listDebatesByDebater(String username, @Nullable FlakeId startDebateId) {
    // we don't use join because we may use cache to optimize later
    Account debater = accountDao.loadByUsername(username);
    return debateDao.listDebatesByDebater(debater.getAccountId(), startDebateId, PAGE_SIZE);
  }

  @Override
  public boolean isExternalLinkExist(Zone zone, String externalLink) {
    return articleDao.isExternalLinkExist(zone, canonicalizeUrl(externalLink));
  }

  @VisibleForTesting
  String canonicalizeUrl(String url) {
    //TODO visit target web page and get header:
    //   <link rel="canonical" href="https://blog.example.com/dresses/" />
    String cleaned = url.replaceAll("[\r\n \t]*", "");
    try {
      URI uri = new URI(cleaned);
      List<NameValuePair> params = URLEncodedUtils.parse(uri, Charsets.UTF_8);
      List<NameValuePair> cleanedParams = params.stream()
          .filter(pair -> !pair.getName().startsWith("utm_"))
          .sorted(Comparator.comparing(NameValuePair::getName)
              .thenComparing(NameValuePair::getValue))
          .collect(toList());
      URIBuilder uriBuilder = new URIBuilder(uri);
      if (cleanedParams.isEmpty()) {
        uriBuilder.clearParameters();
      } else {
        //set empty list will cause builder always append `?`
        uriBuilder.setParameters(cleanedParams);
      }
      return uriBuilder.build().toString();
    } catch (URISyntaxException e) {
      //ignore
    }
    return cleaned;
  }

  @Override
  public List<Article> listArticlesByExternalLink(Zone zone, String externalLink) {
    return articleDao.listArticlesByExternalLink(zone, canonicalizeUrl(externalLink), 3);
  }

  @Override
  public void deleteArticle(Authorization authorization, FlakeId articleId) {
    Article target = articleDao.findArticle(articleId).filter(article -> {
      return canDeleteArticle(authorization, article);
    }).orElseThrow(() -> new AccessDeniedException("not allow delete article: " + articleId));

    articleDao.markAsDeleted(target);
  }

  private boolean canDeleteArticle(Authorization authorization, Article article) {
    return article.canDelete(authorization, Instant.now(clock))
        || zoneDao.isZoneAdmin(article.getZone(), authorization.authenticatedId());
  }

  @Override
  public boolean canDeleteArticle(String username, FlakeId articleId) {
    return accountDao.findByUsername(username)
        .flatMap(account -> articleDao.findArticle(articleId)
            .filter(article -> canDeleteArticle(account, article)))
        .isPresent();
  }
}
