package io.kaif.web.v1;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiModelProperty;
import com.wordnik.swagger.annotations.ApiParam;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.zone.Zone;

@Api(value = "article", description = "Articles")
@RestController
@RequestMapping(value = "/v1/article", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1ArticleResource {

  static class ExternalLinkEntry {

    @Size(max = Article.URL_MAX)
    @NotNull
    @URL(regexp = Article.URL_PATTERN)
    public String url;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    public String title;

    @NotNull
    @ApiModelProperty(required = true)
    public Zone zone;

  }

  static class SpeakEntry {

    @Size(max = Article.CONTENT_MAX, min = Article.CONTENT_MIN)
    @NotNull
    public String content;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    public String title;

    @NotNull
    @ApiModelProperty(required = true)
    public Zone zone;

  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{articleId}", method = RequestMethod.GET)
  public void article(ClientAppUserAccessToken accessToken,
      @PathVariable("articleId") FlakeId articleId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/hot", method = RequestMethod.GET)
  public void hot(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/latest", method = RequestMethod.GET)
  public void latest(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/latest", method = RequestMethod.GET)
  public void latestByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/zone/{zone}/hot", method = RequestMethod.GET)
  public void hotByZone(ClientAppUserAccessToken accessToken,
      @PathVariable("zone") String zone,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/submitted", method = RequestMethod.GET)
  public void submitted(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.VOTE)
  @RequestMapping(value = "/voted", method = RequestMethod.GET)
  public void voted(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "start-article-id", required = false) FlakeId startArticleId) {
  }

  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/external-link", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void externalLink(ClientAppUserAccessToken token,
      @Valid @RequestBody ExternalLinkEntry entry) {
  }

  @RequiredScope(ClientAppScope.ARTICLE)
  @RequestMapping(value = "/speak", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void speak(ClientAppUserAccessToken token, @Valid @RequestBody SpeakEntry entry) {
  }
}
