package io.kaif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import io.kaif.flake.FlakeId;
import io.kaif.model.debate.Debate;
import io.kaif.service.ArticleService;

@Controller
@RequestMapping("/d")
public class ShortUrlController {

  @Autowired
  private ArticleService articleService;

  @RequestMapping("/{flakeId}")
  public View redirectDebateOrArticle(@PathVariable("flakeId") String flakeId) {
    FlakeId id = FlakeId.fromString(flakeId);
    return articleService.findArticle(id).map(article -> {
      RedirectView redirectView = new RedirectView(String.format("/z/%s/debates/%s",
          article.getZone().value(),
          article.getArticleId()));
      redirectView.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
      return redirectView;
    }).orElseGet(() -> {
      // if not found will throw exception and go to 404
      Debate debate = articleService.loadDebate(id);
      RedirectView redirectView = new RedirectView(String.format("/z/%s/debates/%s/%s",
          debate.getZone().value(),
          debate.getArticleId(),
          debate.getDebateId()));
      redirectView.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
      return redirectView;
    });
  }
}
