package io.kaif.web.api;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.URL;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.article.Article;
import io.kaif.model.zone.Zone;
import io.kaif.service.ArticleService;

@RestController
@RequestMapping("/api/article")
public class ArticleResource {

  static class CreateExternalLink {
    @NotNull
    public Zone zone;

    @Size(max = Article.URL_MAX)
    @NotNull
    @URL
    public String url;

    @Size(min = Article.TITLE_MIN, max = Article.TITLE_MAX)
    @NotNull
    public String title;
  }

  @Autowired
  private ArticleService articleService;

  @RequestMapping(value = "/external-link", method = RequestMethod.PUT, consumes = {
      MediaType.APPLICATION_JSON_VALUE })
  public void createExternalLink(AccountAccessToken token,
      @Valid @RequestBody CreateExternalLink request) {
    articleService.createExternalLink(token.getAccountId(),
        request.zone,
        request.title.trim(),
        request.url.trim());
  }
}
