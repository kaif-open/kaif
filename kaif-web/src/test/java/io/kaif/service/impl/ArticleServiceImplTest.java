package io.kaif.service.impl;

import static java.util.Arrays.asList;
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
import io.kaif.model.article.ArticleLinkType;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateContentType;
import io.kaif.model.debate.DebateDao;
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
    assertEquals("debater1", debate.getDebaterName());
    assertEquals(debater.getAccountId(), debate.getDebaterId());
    assertFalse(debate.hasParent());
    assertFalse(debate.isMaxLevel());
    assertEquals(1, debate.getLevel());
    assertEquals("pixel art is *better*", debate.getContent());
    assertEquals("<p>pixel art is <em>better</em></p>\n", debate.getRenderContent());
    assertEquals(0L, debate.getDownVote());
    assertEquals(0L, debate.getUpVote());
    assertNotNull(debate.getCreateTime());
    assertNotNull(debate.getLastUpdateTime());

    assertEquals(1, service.findArticle(article.getArticleId()).get().getDebateCount());

    assertEquals(1, accountService.loadAccountStats(debater.getUsername()).getDebateCount());
  }

  @Test
  public void debate_escape_content() throws Exception {
    Account debater = savedAccountCitizen("debater1");
    Debate created = service.debate(zoneInfo.getZone(),
        article.getArticleId(),
        Debate.NO_PARENT,
        debater,
        "pixel art is better<evil>hi</evil>");

    Debate debate = debateDao.findDebate(created.getDebateId()).get();
    assertEquals(DebateContentType.MARK_DOWN, debate.getContentType());
    assertEquals("pixel art is better<evil>hi</evil>", debate.getContent());
    assertEquals("<p>pixel art is better&lt;evil&gt;hi&lt;/evil&gt;</p>\n",
        debate.getRenderContent());
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
    assertEquals(asList(a1), service.listLatestZoneArticles(fooZone.getZone(), a2.getArticleId()));
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
    assertEquals(firstPageWithoutDeleted, service.listHotZoneArticles(fooZone.getZone(), null));
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
  public void createExternalLink_escape_content() throws Exception {
    Article created = service.createExternalLink(citizen,
        zoneInfo.getZone(),
        "title1<script>alert('123');</script>",
        "http://foo.com<script>alert('123');</script>");
    Article article = service.findArticle(created.getArticleId()).get();
    assertEquals("title1&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;", article.getTitle());
    assertEquals("http://foo.com&lt;script&gt;alert(&#39;123&#39;);&lt;/script&gt;",
        article.getContent());
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
    assertNull(article.getUrlName());
    assertNotNull(article.getCreateTime());
    assertEquals("http://foo.com", article.getContent());
    assertEquals(ArticleContentType.URL, article.getContentType());
    assertEquals(ArticleLinkType.EXTERNAL, article.getLinkType());
    assertEquals(citizen.getUsername(), article.getAuthorName());
    assertEquals(citizen.getAccountId(), article.getAuthorId());
    assertFalse(article.isDeleted());
    assertEquals(0, article.getUpVote());
    assertEquals(0, article.getDownVote());
    assertEquals(0, article.getDebateCount());

    AccountStats stats = accountService.loadAccountStats(citizen.getUsername());
    assertEquals(1, stats.getArticleCount());
  }

  @Test
  public void createExternalLink_not_enough_authority() throws Exception {
    ZoneInfo zoneRequireCitizen = savedZoneDefault("fun");
    Account tourist = savedAccountTourist("notActivated");
    try {
      service.createExternalLink(tourist, zoneRequireCitizen.getZone(), "title1", "http://foo.com");
      fail("AccessDeniedException expected");
    } catch (AccessDeniedException expected) {
    }
  }

  @Test
  public void updateDebateContent() throws Exception {
    Debate d1 = savedDebate(null);
    String result = service.updateDebateContent(d1.getDebateId(),
        citizen,
        "pixel art is better<evil>hi</evil>*hi*");
    assertEquals("<p>pixel art is better&lt;evil&gt;hi&lt;/evil&gt;<em>hi</em></p>\n", result);
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