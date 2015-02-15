package io.kaif.web;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.ArticlePage;
import io.kaif.service.ArticleService;
import io.kaif.service.ZoneService;

@Controller
public class HomeController {

  @Autowired
  private ArticleService articleService;
  @Autowired
  private ZoneService zoneService;

  @RequestMapping("/")
  public ModelAndView index(@RequestParam(value = "start", required = false) String start) {
    FlakeId startArticleId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
    return new ModelAndView("index").addObject("articlePage",
        new ArticlePage(articleService.listTopArticles(startArticleId)));
  }

  @RequestMapping("/new")
  public ModelAndView listLatestArticles(
      @RequestParam(value = "start", required = false) String start) {
    FlakeId startArticleId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
    return new ModelAndView("index").addObject("articlePage",
        new ArticlePage(articleService.listLatestArticles(startArticleId)));
  }

  @RequestMapping("/zones")
  public ModelAndView zoneAtoZ() {
    return new ModelAndView("zones").addObject("zoneAtoZ", zoneService.listZoneAtoZ());
  }
}