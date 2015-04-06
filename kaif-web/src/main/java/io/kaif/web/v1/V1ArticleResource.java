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

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
  public V1ArticleDto article(ClientAppUserAccessToken accessToken,
      @PathVariable("articleId") FlakeId articleId) {
    return articleService.loadArticle(articleId).toV1Dto();
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/hot", method = RequestMethod.GET)
  public List<V1ArticleDto> hot(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listTopArticles(startArticleId));
  }

  private List<V1ArticleDto> toDtos(List<Article> articles) {
    return articles.stream().map(Article::toV1Dto).collect(toList());
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/latest", method = RequestMethod.GET)
  public List<V1ArticleDto> latest(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listLatestArticles(startArticleId));
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/latest", method = RequestMethod.GET)
  public List<V1ArticleDto> latestByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listLatestZoneArticles(Zone.valueOf(zone), startArticleId));
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/hot", method = RequestMethod.GET)
  public List<V1ArticleDto> hotByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listHotZoneArticles(Zone.valueOf(zone), startArticleId));
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/user/{username}/submitted", method = RequestMethod.GET)
  public List<V1ArticleDto> submitted(ClientAppUserAccessToken accessToken,
      @PathVariable("username") String username,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(articleService.listArticlesByAuthor(username, startArticleId));
  }

  @RequiredScope(ClientAppScope.VOTE)
  @RequestMapping(value = "/voted", method = RequestMethod.GET)
  public List<V1ArticleDto> voted(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
    return toDtos(voteService.listUpVotedArticles(accessToken, startArticleId));
  }

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

  @ResponseStatus(HttpStatus.CREATED)
  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/speak", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public V1ArticleDto speak(ClientAppUserAccessToken token, @Valid @RequestBody SpeakEntry entry) {
    return articleService.createSpeak(token, entry.zone, entry.title.trim(), entry.content.trim())
        .toV1Dto();
  }
}
