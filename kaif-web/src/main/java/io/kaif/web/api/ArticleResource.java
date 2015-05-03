package io.kaif.web.api;

import static java.util.stream.Collectors.*;

import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.article.Article;
import io.kaif.model.debate.Debate;
import io.kaif.model.zone.Zone;
import io.kaif.service.ArticleService;
import io.kaif.web.support.SingleWrapper;

@RestController
@RequestMapping("/api/article")
public class ArticleResource {

  static class CreateExternalLink {

    @Size(max = Article.URL_MAX)
    @NotNull
    @URL(regexp = Article.URL_PATTERN)
    public String url;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    public String title;

    @NotNull
    public Zone zone;

  }

  static class DeleteArticle {
    @NotNull
    public FlakeId articleId;
  }

  static class CreateSpeak {

    @Size(max = Article.CONTENT_MAX, min = Article.CONTENT_MIN)
    @NotNull
    public String content;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    public String title;

    @NotNull
    public Zone zone;

  }

  static class CreateDebate {
    @NotNull
    public FlakeId articleId;

    public FlakeId parentDebateId;

    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    public String content;
  }

  static class UpdateDebate {
    @NotNull
    public FlakeId debateId;

    @Size(min = Debate.CONTENT_MIN, max = Debate.CONTENT_MAX)
    @NotNull
    public String content;
  }

  static class Previewer {
    @NotNull
    public String content;
  }

  @Autowired
  private ArticleService articleService;

  @RequestMapping(value = "/external-link", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void createExternalLink(AccountAccessToken token,
      @Valid @RequestBody CreateExternalLink request) {
    articleService.createExternalLink(token,
        request.zone,
        request.title.trim(),
        request.url.trim());
  }

  @RequestMapping(value = "/", method = RequestMethod.DELETE, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void deleteArticle(AccountAccessToken token, @Valid @RequestBody DeleteArticle request) {
    articleService.deleteArticle(token, request.articleId);
  }

  @RequestMapping(value = "/speak", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void createSpeak(AccountAccessToken token, @Valid @RequestBody CreateSpeak request) {
    articleService.createSpeak(token, request.zone, request.title.trim(), request.content.trim());
  }

  @RequestMapping(value = "/debate", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public SingleWrapper<String> create(AccountAccessToken token,
      @Valid @RequestBody CreateDebate request) {
    return SingleWrapper.of(articleService.debate(request.articleId,
        request.parentDebateId,
        token,
        request.content.trim()).getDebateId().toString());
  }

  @RequestMapping(value = "/debate/content", method = RequestMethod.POST, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String editDebateContent(AccountAccessToken token,
      @Valid @RequestBody UpdateDebate request) {
    return articleService.updateDebateContent(request.debateId, token, request.content.trim());
  }

  //no need authenticate
  @RequestMapping(value = "/debate/content/preview", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String previewDebateContent(@Valid @RequestBody Previewer request) {
    return Debate.renderContentPreview(request.content);
  }

  @RequestMapping(value = "/speak/preview", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public String previewSpeakContent(@Valid @RequestBody Previewer request) {
    return Article.renderSpeakPreview(request.content);
  }

  @RequestMapping(value = "/debate/content", method = RequestMethod.GET)
  public String loadEditableDebate(AccountAccessToken token,
      @RequestParam("debateId") FlakeId debateId) {
    return articleService.loadEditableDebateContent(debateId, token);
  }

  @RequestMapping(value = "/can-create", method = RequestMethod.GET)
  public SingleWrapper<Boolean> canCreateArticle(AccountAccessToken token,
      @RequestParam("zone") String rawZone) {
    return SingleWrapper.of(articleService.canCreateArticle(Zone.valueOf(rawZone), token));
  }

  @RequestMapping(value = "/external-link/exist", method = RequestMethod.GET)
  public SingleWrapper<Boolean> isExternalUrlExist(@RequestParam("zone") String rawZone,
      @RequestParam("url") String url) {
    return SingleWrapper.of(articleService.isExternalLinkExist(Zone.valueOf(rawZone), url));
  }

  @RequestMapping(value = "/external-link", method = RequestMethod.GET)
  public List<FlakeId> listArticleIdsByExternalLink(@RequestParam("zone") String rawZone,
      @RequestParam("url") String url) {
    return articleService.listArticlesByExternalLink(Zone.valueOf(rawZone), url)
        .stream()
        .map(Article::getArticleId)
        .collect(toList());
  }

  @RequestMapping(value = "/can-delete", method = RequestMethod.GET)
  public SingleWrapper<Boolean> canDeleteArticle(AccountAccessToken token,
      @RequestParam("username") String username,
      @RequestParam("articleId") FlakeId articleId) {
    return SingleWrapper.of(articleService.canDeleteArticle(username, articleId));
  }
}
