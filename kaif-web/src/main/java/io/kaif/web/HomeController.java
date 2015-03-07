package io.kaif.web;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticlePage;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateList;
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
    return new ModelAndView("index")//
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("articlePage", new ArticlePage(articleService.listTopArticles(startArticleId)));
  }

  @RequestMapping("/hot.rss")
  public Object rssFeed() {
    ModelAndView modelAndView = new ModelAndView().addObject("articlePage",
        articleService.listCachedTopArticles());
    modelAndView.setView(new HotArticleRssContentView());
    return modelAndView;
  }

  @RequestMapping("/new")
  public ModelAndView listLatestArticles(
      @RequestParam(value = "start", required = false) String start) {
    FlakeId startArticleId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
    return new ModelAndView("index") //
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("articlePage",
            new ArticlePage(articleService.listLatestArticles(startArticleId)));
  }

  @RequestMapping("/new-debate")
  public ModelAndView listLatestDebates(
      @RequestParam(value = "start", required = false) String start) {
    FlakeId startDebateId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
    List<Debate> debates = articleService.listLatestDebates(startDebateId);
    List<Article> articles = articleService.listArticlesByDebates(debates.stream()
        .map(Debate::getDebateId)
        .collect(toList()));
    return new ModelAndView("index") //
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("debateList", new DebateList(debates, articles));
  }

  @RequestMapping("/zone/a-z")
  public ModelAndView zoneAtoZ() {
    return new ModelAndView("zone/zone-a-z").addObject("zoneAtoZ", zoneService.listZoneAtoZ());
  }
}