package io.kaif.web.v1;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiOperation;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.zone.Zone;
import io.kaif.service.ArticleService;
import io.kaif.service.VoteService;
import io.kaif.web.v1.dto.V1ArticleDto;

@Api(value = "article", description = "Articles")
@RestController
@RequestMapping(value = "/v1/article", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1ArticleResource {

  static class ExternalLinkEntry {

    @Size(max = Article.URL_MAX)
    @NotNull
    @URL(regexp = Article.URL_PATTERN)
    @ApiModelProperty(required = true)
    public String url;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    @ApiModelProperty(value = "title of article, min 3 chars", required = true)
    public String title;

    @NotNull
    @ApiModelProperty(required = true)
    public Zone zone;

  }

  static class SpeakEntry {

    @Size(max = Article.CONTENT_MAX, min = Article.CONTENT_MIN)
    @NotNull
    @ApiModelProperty(value = "content of article, min 10 chars", required = true)
    public String content;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    @ApiModelProperty(value = "title of article, min 3 chars", required = true)
    public String title;

    @NotNull
    @ApiModelProperty(required = true)
    public Zone zone;

  }

  @Autowired
  private ArticleService articleService;

  @Autowired
  private VoteService voteService;

  @ApiOperation(value = "[public] Get an article", notes = "Get an article by articleId")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
  public V1ArticleDto article(ClientAppUserAccessToken accessToken,
      @PathVariable("articleId") FlakeId articleId) {
    return articleService.loadArticle(articleId).toV1Dto();
  }

  @ApiOperation(value = "[public] List hot articles of all zones",
      notes = "List hot articles of all zones, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/hot", method = RequestMethod.GET)
  public List<V1ArticleDto> hot(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listTopArticles(startArticleId));
  }

  private List<V1ArticleDto> toDtos(List<Article> articles) {
    return articles.stream().map(Article::toV1Dto).collect(toList());
  }

  @ApiOperation(value = "[public] List latest articles of all zones",
      notes = "List latest articles of all zones, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/latest", method = RequestMethod.GET)
  public List<V1ArticleDto> latest(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listLatestArticles(startArticleId));
  }

  @ApiOperation(value = "[public] List latest articles for the zone",
      notes = "List latest articles for the zone, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/latest", method = RequestMethod.GET)
  public List<V1ArticleDto> latestByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listLatestZoneArticles(Zone.valueOf(zone), startArticleId));
  }

  @ApiOperation(value = "[public] List hot articles for the zone",
      notes = "List hot articles for the zone, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/hot", method = RequestMethod.GET)
  public List<V1ArticleDto> hotByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listHotZoneArticles(Zone.valueOf(zone), startArticleId));
  }

  @ApiOperation(value = "[public] List submitted articles of the user",
      notes = "List submitted articles of the user, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/user/{username}/submitted", method = RequestMethod.GET)
  public List<V1ArticleDto> submitted(ClientAppUserAccessToken accessToken,
      @PathVariable("username") String username,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listArticlesByAuthor(username, startArticleId));
  }

  @ApiOperation(value = "[vote] List voted articles of the user",
      notes = "List voted articles of the user, 25 articles a page. "
          + "To retrieve next page, passing last article id of previous page in parameter start-article-id")
  @RequiredScope(ClientAppScope.VOTE)
  @RequestMapping(value = "/voted", method = RequestMethod.GET)
  public List<V1ArticleDto> voted(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(voteService.listUpVotedArticles(accessToken, startArticleId));
  }

  @ApiOperation(value = "[article] Create an article with external link",
      notes = "Create an article with external link in specified zone. Before calling the method, "
          + "it is recommended check the link already submitted by other users. check API - "
          + "'GET /zone/{zone}/external-link'")
  @ResponseStatus(HttpStatus.CREATED)
  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/external-link", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public V1ArticleDto externalLink(ClientAppUserAccessToken token,
      @Valid @RequestBody ExternalLinkEntry entry) {
    return articleService.createExternalLink(token,
        entry.zone,
        entry.title.trim(),
        entry.url.trim()).toV1Dto();
  }

  @ApiOperation(value = "[public] List articles for external link",
      notes = "List most 3 external-url-articles for a specific url."
          + "You are encouraged to show exist articles to end user before creating the article "
          + "to prevent article duplication in zone.")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/external-link", method = RequestMethod.GET)
  public List<V1ArticleDto> externalLink(ClientAppUserAccessToken accessToken,
      @PathVariable(value = "zone") String zone,
      @RequestParam("url") String url) {
    return toDtos(articleService.listArticlesByExternalLink(Zone.valueOf(zone), url));
  }

  @ApiOperation(value = "[public] check url already submitted in zone",
      notes = "Check url already submitted as external-url-article in zone")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/external-link/exist", method = RequestMethod.GET)
  public boolean isExternalLinkExist(ClientAppUserAccessToken accessToken,
      @PathVariable(value = "zone") String zone,
      @RequestParam("url") String url) {
    return articleService.isExternalLinkExist(Zone.valueOf(zone), url);
  }

  @ApiOperation(value = "[public] check the article can be deleted",
      notes = "Check the article can be deleted by user. Note that there is time-constraint "
          + "on article deletion. If article is old enough, the method return false.")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{articleId}/can-delete", method = RequestMethod.GET)
  public boolean canDeleteArticle(ClientAppUserAccessToken accessToken,
      @PathVariable(value = "articleId") FlakeId articleId,
      @RequestParam("username") String username) {
    return articleService.canDeleteArticle(username, articleId);
  }

  @ApiOperation(value = "[article] Create an article with content",
      notes = "Create an article with content in specified zone")
  @ResponseStatus(HttpStatus.CREATED)
  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/speak", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public V1ArticleDto speak(ClientAppUserAccessToken token, @Valid @RequestBody SpeakEntry entry) {
    return articleService.createSpeak(token, entry.zone, entry.title.trim(), entry.content.trim())
        .toV1Dto();
  }

  @ApiOperation(value = "[article] Delete an article",
      notes = "Delete an article, return status 200 if success, response as error if sign-in user "
          + "not allow to delete the article.")
  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/{articleId}", method = RequestMethod.DELETE, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void deleteArticle(ClientAppUserAccessToken token,
      @PathVariable("articleId") FlakeId articleId) {
    articleService.deleteArticle(token, articleId);
  }
}
