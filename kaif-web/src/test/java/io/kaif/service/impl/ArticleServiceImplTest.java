package io.kaif.service.impl;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountStats;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleContentType;
import io.kaif.model.article.ArticleDao;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateContentType;
import io.kaif.model.debate.DebateDao;
import io.kaif.model.vote.RotateVoteStats;
import io.kaif.model.vote.RotateVoteStatsDao;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.AccountService;
import io.kaif.test.DbIntegrationTests;
import io.kaif.web.support.AccessDeniedException;

public class ArticleServiceImplTest extends DbIntegrationTests {

  @Autowired
  private ArticleServiceImpl service;

  @Autowired
  private AccountService accountService;

  @Autowired
  private DebateDao debateDao;

  @Autowired
  private ArticleDao articleDao;

  @Autowired
  private RotateVoteStatsDao rotateVoteStatsDao;

  private ZoneInfo zoneInfo;
  private Article article;
  private Account citizen;

  @Before
  public void setUp() throws Exception {
    zoneInfo = savedZoneDefault("pic");
    citizen = savedAccountCitizen("citizen1");
    article = savedArticle(zoneInfo, citizen, "art-1");
  }

  @Test
  public void debate() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate created = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater,
        "pixel art is *better*");

    Debate debate = service.loadDebate(created.getDebateId());
    assertEquals(DebateContentType.MARK_DOWN, debate.getContentType());
    assertEquals(citizen.getAccountId(), debate.getReplyToAccountId());
    assertEquals("debater1", debate.getDebaterName());
    assertEquals(debater.getAccountId(), debate.getDebaterId());
    assertFalse(debate.hasParent());
    assertFalse(debate.isMaxLevel());
    assertEquals(1, debate.getLevel());
    assertEquals("pixel art is *better*", debate.getContent());
    assertEquals(0L, debate.getDownVote());
    assertEquals(0L, debate.getUpVote());
    assertNotNull(debate.getCreateTime());
    assertNotNull(debate.getLastUpdateTime());

    assertEquals(1, service.findArticle(article.getArticleId()).get().getDebateCount());
    assertEquals(1, accountService.loadAccountStats(debater.getUsername()).getDebateCount());
    assertEquals(1,
        rotateVoteStatsDao.findRotateVoteStats(debater.getAccountId(),
            debate.getZone(),
            debate.getCreateTime()).get().getDebateCount());
  }

  @Test
  public void listLatestDebates() throws Exception {
    assertEquals(0, service.listLatestDebates(null).size());
    Debate d1 = savedDebate(null);
    Debate d2 = savedDebate(null);

    assertEquals(asList(d2, d1), service.listLatestDebates(null));
    assertEquals(asList(d1), service.listLatestDebates(d2.getDebateId()));
  }

  @Test
  public void listLatestZoneDebates() throws Exception {

    assertEquals(0, service.listLatestZoneDebates(zoneInfo.getZone(), null).size());
    Debate d1 = savedDebate(article, "foo-12345", null);
    Debate d2 = savedDebate(article, "foo-12345", null);

    assertEquals(asList(d2, d1), service.listLatestZoneDebates(zoneInfo.getZone(), null));
    assertEquals(asList(d1), service.listLatestZoneDebates(zoneInfo.getZone(), d2.getDebateId()));
  }

  @Test
  public void listArticlesByDebates() throws Exception {
    assertEquals(0, service.listArticlesByDebates(Collections.emptyList()).size());

    Article a2 = savedArticle(zoneInfo, citizen, "another article");
    Debate d1 = savedDebate(article, "foo-12345", null);
    Debate d2 = savedDebate(a2, "foo-about", null);
    Debate d3 = savedDebate(a2, "foo-duplicate", null);

    List<Article> articles = service.listArticlesByDebates(asList(d1.getDebateId(),
        d2.getDebateId(),
        d3.getDebateId()));
    assertEquals(2, articles.size());
    assertTrue(articles.containsAll(asList(article, a2)));
  }

  @Test
  public void listArticlesByDebates_cache() throws Exception {
    assertEquals(0, service.listArticlesByDebates(Collections.emptyList()).size());

    Article a2 = savedArticle(zoneInfo, citizen, "another article");
    Debate d1 = savedDebate(article, "foo-12345", null);
    Debate d2 = savedDebate(a2, "foo-about", null);

    List<Article> articles = service.listArticlesByDebates(asList(d1.getDebateId(),
        d2.getDebateId()));
    List<Article> articles2 = service.listArticlesByDebates(asList(d1.getDebateId(),
        d2.getDebateId()));
    assertSame(articles2.get(0), articles.get(0));
    assertSame(articles2.get(1), articles.get(1));

    articleDao.evictAllCaches();
    List<Article> refreshed = service.listArticlesByDebates(asList(d2.getDebateId()));
    assertNotSame(refreshed.get(0), articles2.get(1));
  }

  @Test
  public void listCachedHotZoneArticles() throws Exception {
    List<Article> articleList = service.listCachedHotZoneArticles(zoneInfo.getZone());
    assertSame(articleList, service.listCachedHotZoneArticles(zoneInfo.getZone()));

    Zone otherZone = savedZoneDefault("others").getZone();
    assertNotSame(articleList, service.listCachedHotZoneArticles(otherZone));
    assertNotSame(articleList, service.listCachedTopArticles());

  }

  @Test
  public void loadDebaterId_cache() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate created = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater,
        "pixel art is better");

    UUID debaterId = debateDao.loadDebaterId(created.getDebateId());
    assertEquals(debater.getAccountId(), debaterId);
    assertSame("cached should be same instance",
        debaterId,
        debateDao.loadDebaterId(created.getDebateId()));
  }

  @Test
  public void listBestDebates_one_level() throws Exception {
    Zone zone = zoneInfo.getZone();
    FlakeId articleId = article.getArticleId();
    assertEquals(0, service.listBestDebates(articleId, null).depthFirst().count());

    List<Debate> debates = IntStream.rangeClosed(1, 3)
        .mapToObj(i -> service.debate(zone,
            articleId,
            Debate.NO_PARENT,
            citizen,
            "debate-content-" + i))
        .collect(toList());

    assertEquals(debates, service.listBestDebates(articleId, null).depthFirst().collect(toList()));
  }

  @Test
  public void listBestDebates_tree() throws Exception {
    Debate d1 = savedDebate(null);
    Debate d1_1 = savedDebate(d1);
    Debate d1_1_1 = savedDebate(d1_1);
    Debate d2 = savedDebate(null);
    Debate d2_1 = savedDebate(d2);
    Debate d2_2 = savedDebate(d2);

    // begin out or order
    Debate d1_1_2 = savedDebate(d1_1);
    Debate d3 = savedDebate(null);
    Debate d2_1_1 = savedDebate(d2_1);
    Debate d3_1 = savedDebate(d3);
    Debate d1_2 = savedDebate(d1);

    //make d3_1 best
    debateDao.changeTotalVote(d3_1.getDebateId(), 100, 0);

    List<Debate> expect = asList(//
        d3, d3_1,//
        d1, d1_1, d1_1_1, d1_1_2, d1_2, //
        d2, d2_1, d2_1_1, d2_2 //
    );

    assertEquals(expect,
        service.listBestDebates(article.getArticleId(), null).depthFirst().collect(toList()));
  }

  @Test
  public void listChildDebates() throws Exception {
    Debate d1 = savedDebate(null);
    Debate d1_1 = savedDebate(d1);
    Debate d1_1_1 = savedDebate(d1_1);
    // begin out or order
    Debate d1_1_2 = savedDebate(d1_1);

    List<Debate> expect = asList(d1_1_1, d1_1_2);

    assertEquals(expect,
        service.listBestDebates(article.getArticleId(), d1_1.getDebateId())
            .depthFirst()
            .collect(toList()));
  }

  private Debate savedDebate(Debate parent) {
    return service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Optional.ofNullable(parent).map(Debate::getDebateId).orElse(Debate.NO_PARENT),
        citizen,
        "debate-content-" + Math.random());
  }

  @Test
  public void debate_max_level() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    FlakeId parentId = Debate.NO_PARENT;
    Debate last = null;
    for (int i = 0; i < 10; i++) {
      last = service.debate(zoneInfo.getZone(),
          article.getArticleId(),
          parentId,
          debater,
          "nested");
      parentId = last.getDebateId();
    }
    assertTrue(last.isMaxLevel());
    try {
      service.debate(zoneInfo.getZone(), article.getArticleId(), parentId, debater, "failed");
      fail("IllegalArgumentException expected");
    } catch (IllegalArgumentException expected) {
    }
  }

  @Test
  public void debate_reply() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate l1 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater,
        "pixel art is better");
    Debate l2 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        l1.getDebateId(),
        debater,
        "i think so");
    assertEquals(2, l2.getLevel());
    assertTrue(l2.hasParent());
    assertTrue(l2.isParent(l1));
    assertFalse(l1.isParent(l2));
    assertEquals(l1.getDebaterId(), l2.getReplyToAccountId());

    assertEquals(2, service.findArticle(article.getArticleId()).get().getDebateCount());
    Debate l3 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        l2.getDebateId(),
        debater,
        "no no no");

    assertEquals(3, l3.getLevel());
    assertTrue(l3.hasParent());
    assertTrue(l3.isParent(l2));
    assertFalse(l2.isParent(l3));

    assertEquals(3, service.findArticle(article.getArticleId()).get().getDebateCount());
  }

  @Test
  public void listReplyToDebates() throws Exception {
    assertEquals(0, service.listReplyToDebates(citizen, null).size());
    //    Account debater = savedAccountCitizen("debater1");
    Debate l1 = savedDebate(article, "reply to my article", null);
    assertEquals(asList(l1), service.listReplyToDebates(citizen, null));

    Debate authorReply = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        citizen,
        "article author reply self is ignored");

    savedDebate(article, "not reply to me is ignored", l1);

    Debate l2 = savedDebate(article, "a debate reply to me", authorReply);
    assertEquals(asList(l2, l1), service.listReplyToDebates(citizen, null));
    assertEquals(asList(l1), service.listReplyToDebates(citizen, l2.getDebateId()));
  }

  @Test
  public void debate_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Article article = savedArticle(zoneRequireCitizen, citizen, "fun-no1");
    Account tourist = savedAccountTourist("notActivated");
    try {
      service.debate(zoneRequireCitizen.getZone(),
          article.getArticleId(),
          Debate.NO_PARENT,
          tourist,
          "pixel art is better");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void listLatestZoneArticles() throws Exception {
    ZoneInfo zone1 = savedZoneDefault("foo");
    ZoneInfo zone2 = savedZoneDefault("zoo");
    Article a1 = service.createExternalLink(citizen, zone1.getZone(), "title1", "http://foo1.com");
    Article a2 = service.createExternalLink(citizen, zone1.getZone(), "title2", "http://foo2.com");
    Article a3 = service.createExternalLink(citizen, zone2.getZone(), "title3", "http://foo2.com");
    Article a4 = service.createExternalLink(citizen, zone2.getZone(), "title4", "http://foo2.com");
    articleDao.markAsDeleted(a1);

    assertEquals(asList(a4, a3, a2, article), service.listLatestArticles(null));
    assertEquals(asList(a2, article), service.listLatestArticles(a3.getArticleId()));
  }

  @Test
  public void listLatestArticles() throws Exception {
    Account author = savedAccountCitizen("citizen");
    ZoneInfo fooZone = savedZoneDefault("foo");
    Article a1 = service.createExternalLink(author, fooZone.getZone(), "title1", "http://foo1.com");
    Article a2 = service.createExternalLink(author, fooZone.getZone(), "title2", "http://foo2.com");
    Article a3 = service.createExternalLink(author, fooZone.getZone(), "title2", "http://foo2.com");

    assertEquals(asList(a3, a2, a1), service.listLatestZoneArticles(fooZone.getZone(), null));
    assertEquals(singletonList(a1),
        service.listLatestZoneArticles(fooZone.getZone(), a2.getArticleId()));
    articleDao.markAsDeleted(a1);
    assertEquals("listLatest should exclude deleted",
        asList(a3, a2),
        service.listLatestZoneArticles(fooZone.getZone(), null));
  }

  @Test
  public void listHotZoneArticles() throws Exception {
    Account author = savedAccountCitizen("citizen");
    ZoneInfo fooZone = savedZoneDefault("foo");
    List<Article> articles = IntStream.rangeClosed(1, 100).mapToObj(i -> {
      Article a = savedArticle(fooZone, author, "title-" + i);
      articleDao.changeTotalVote(a.getArticleId(), i * 10, 0);
      return a;
    }).collect(toList());

    Collections.reverse(articles);

    List<Article> firstPage = articles.stream().limit(25).collect(toList());
    assertEquals(firstPage, service.listHotZoneArticles(fooZone.getZone(), null));

    List<Article> secondPage = articles.stream().skip(25).limit(25).collect(toList());
    assertEquals(secondPage,
        service.listHotZoneArticles(fooZone.getZone(), firstPage.get(24).getArticleId()));

    articleDao.markAsDeleted(articles.get(0));
    articleDao.markAsDeleted(articles.get(1));

    List<Article> firstPageWithoutDeleted = articles.stream().skip(2).limit(25).collect(toList());
    assertEquals(firstPageWithoutDeleted,
        service.listHotZoneArticles(fooZone.getZone(), null));
  }

  @Test
  public void listCachedTopArticles() throws Exception {
    List<Article> articles = service.listCachedTopArticles();
    assertSame(articles, service.listCachedTopArticles());
  }

  @Test
  public void listTopArticles() throws Exception {
    Account author = savedAccountCitizen("citizen");
    ZoneInfo defaultZone = savedZoneDefault("foo");
    List<Article> articles = IntStream.rangeClosed(1, 30).mapToObj(i -> {
      Article a = savedArticle(defaultZone, author, "title-" + i);
      articleDao.changeTotalVote(a.getArticleId(), i * 10, 0);
      return a;
    }).collect(toList());

    ZoneInfo kaifZone = savedZoneKaif("faq");
    //hideFromTop articles will be ignored
    savedArticle(kaifZone, author, "title-faq");

    Collections.reverse(articles);

    //deleted will be ignored
    articleDao.markAsDeleted(articles.get(0));

    //add article from setUp()
    articles.add(article);

    List<Article> firstPage = articles.stream().skip(1).limit(25).collect(toList());
    List<Article> secondPage = articles.stream().skip(26).collect(toList());

    assertEquals(firstPage, service.listTopArticles(null));
    assertEquals(secondPage, service.listTopArticles(firstPage.get(24).getArticleId()));
  }

  @Test
  public void createExternalLink() throws Exception {
    Article created = service.createExternalLink(citizen,
        zoneInfo.getZone(),
        "title1",
        "http://foo.com");
    Article article = service.findArticle(created.getArticleId()).get();
    assertEquals(zoneInfo.getZone(), article.getZone());
    assertEquals("title1", article.getTitle());
    assertNotNull(article.getCreateTime());
    assertEquals("http://foo.com", article.getLink());
    assertNull(article.getContent());
    assertEquals(ArticleContentType.NONE, article.getContentType());
    assertEquals(citizen.getUsername(), article.getAuthorName());
    assertEquals(citizen.getAccountId(), article.getAuthorId());
    assertFalse(article.isDeleted());
    assertEquals(0, article.getUpVote());
    assertEquals(0, article.getDownVote());
    assertEquals(0, article.getDebateCount());

    assertTrue(article.isExternalLink());
    AccountStats stats = accountService.loadAccountStats(citizen.getUsername());
    assertEquals(1, stats.getArticleCount());
  }

  @Test
  public void accountStatsOnlyForCitizenZone() throws Exception {
    ZoneInfo touristZone = savedZoneTourist("test");
    Account tourist = savedAccountTourist("guestB");
    Article article = service.createExternalLink(tourist,
        touristZone.getZone(),
        "title1",
        "http://foo.com");

    service.debate(touristZone.getZone(), article.getArticleId(), null, tourist, "test article");
    AccountStats stats = accountService.loadAccountStats(tourist.getUsername());
    assertEquals(0, stats.getArticleCount());
    assertEquals(0, stats.getDebateCount());
  }

  @Test
  public void createSpeak() throws Exception {
    Article created = service.createSpeak(citizen, zoneInfo.getZone(), "title1", "laugh out loud");
    Article article = service.findArticle(created.getArticleId()).get();
    assertEquals(zoneInfo.getZone(), article.getZone());
    assertEquals("title1", article.getTitle());
    assertNotNull(article.getCreateTime());
    assertEquals("laugh out loud", article.getContent());
    assertNull(article.getLink());
    assertEquals(ArticleContentType.MARK_DOWN, article.getContentType());
    assertEquals(citizen.getUsername(), article.getAuthorName());
    assertEquals(citizen.getAccountId(), article.getAuthorId());
    assertFalse(article.isDeleted());
    assertEquals(0, article.getUpVote());
    assertEquals(0, article.getDownVote());
    assertEquals(0, article.getDebateCount());
    assertFalse(article.isExternalLink());
    AccountStats stats = accountService.loadAccountStats(citizen.getUsername());
    assertEquals(1, stats.getArticleCount());

    RotateVoteStats rotateVoteStats = rotateVoteStatsDao.findRotateVoteStats(citizen.getAccountId(),
        article.getZone(),
        article.getCreateTime())
        .get();
    assertEquals(1, rotateVoteStats.getArticleCount());
  }

  @Test
  public void createArticle_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Account tourist = savedAccountTourist("notActivated");
    try {
      service.createExternalLink(tourist, zoneRequireCitizen.getZone(), "title1", "http://foo.com");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }

    try {
      service.createSpeak(tourist, zoneRequireCitizen.getZone(), "title1", "my content 12345");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void canCreateArticle() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Account tourist = savedAccountTourist("notActivated");
    assertFalse(service.canCreateArticle(zoneRequireCitizen.getZone(), tourist));
    assertTrue(service.canCreateArticle(zoneRequireCitizen.getZone(), citizen));
  }

  @Test
  public void updateDebateContent() throws Exception {
    Debate d1 = savedDebate(null);
    Thread.sleep(200);
    String result = service.updateDebateContent(d1.getDebateId(),
        citizen,
        "pixel art is better<evil>hi</evil>*hi*");
    assertEquals("<p>pixel art is better&lt;evil&gt;hi&lt;/evil&gt;<em>hi</em></p>\n", result);
    Debate updated = service.loadDebate(d1.getDebateId());
    assertTrue(updated.isEdited());
    assertTrue(updated.getLastUpdateTime().isAfter(d1.getLastUpdateTime()));
  }

  @Test
  public void loadEditableDebate() throws Exception {
    Debate d1 = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        citizen,
        "> a quote");
    String content = service.loadEditableDebateContent(d1.getDebateId(), citizen);
    assertEquals("&gt; a quote", content);
  }

  @Test
  public void loadEditableDebate_not_editor() throws Exception {
    Debate d1 = savedDebate(null);
    try {
      service.loadEditableDebateContent(d1.getDebateId(), savedAccountCitizen("not-editor"));
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }
}