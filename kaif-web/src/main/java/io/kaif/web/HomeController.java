package io.kaif.web;

import static java.util.stream.Collectors.*;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleList;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateList;
import io.kaif.service.ArticleService;
import io.kaif.service.HonorRollService;
import io.kaif.service.ZoneService;

@Controller
public class HomeController {

  @Autowired
  private ArticleService articleService;

  @Autowired
  private HonorRollService honorRollService;

  @Autowired
  private ZoneService zoneService;

  @RequestMapping("/")
  public ModelAndView index(
      @RequestParam(value = "start", required = false) FlakeId startArticleId) {
    return new ModelAndView("index")//
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("honorRollList", honorRollService.listHonorRollsByZone(null))
        .addObject("articleList", new ArticleList(articleService.listTopArticles(startArticleId)));
  }

  @RequestMapping("/hot.rss")
  public Object rssFeed() {
    ModelAndView modelAndView = new ModelAndView().addObject("articles",
        articleService.listRssTopArticlesWithCache());
    modelAndView.setView(new HotArticleRssContentView());
    return modelAndView;
  }

  @RequestMapping("/new")
  public ModelAndView listLatestArticles(
      @RequestParam(value = "start", required = false) FlakeId startArticleId) {
    return new ModelAndView("index") //
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("honorRollList", honorRollService.listHonorRollsByZone(null))
        .addObject("articleList",
            new ArticleList(articleService.listLatestArticles(startArticleId)));
  }

  @RequestMapping("/new-debate")
  public ModelAndView listLatestDebates(
      @RequestParam(value = "start", required = false) FlakeId startDebateId) {
    List<Debate> debates = articleService.listLatestDebates(startDebateId);
    List<Article> articles = articleService.listArticlesByDebates(debates.stream()
        .map(Debate::getDebateId)
        .collect(toList()));
    return new ModelAndView("index") //
        .addObject("recommendZones", zoneService.listRecommendZones())
        .addObject("honorRollList", honorRollService.listHonorRollsByZone(null))
        .addObject("debateList", new DebateList(debates, articles));
  }

  @RequestMapping("/zone/a-z")
  public ModelAndView zoneAtoZ() {
    return new ModelAndView("zone/zone-a-z").addObject("zoneAtoZ", zoneService.listZoneAtoZ());
  }
}