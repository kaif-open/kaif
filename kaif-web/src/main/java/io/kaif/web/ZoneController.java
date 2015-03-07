package io.kaif.web;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import io.kaif.flake.FlakeId;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticlePage;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateList;
import io.kaif.model.zone.Zone;
import io.kaif.model.zone.ZoneInfo;
import io.kaif.service.ArticleService;
import io.kaif.service.ZoneService;

@Controller
@RequestMapping("/z")
public class ZoneController {

  @Autowired
  private ZoneService zoneService;
  @Autowired
  private ArticleService articleService;

  @RequestMapping("/{zone}")
  public Object hotArticles(@PathVariable("zone") String rawZone,
      @RequestParam(value = "start", required = false) String start,
      HttpServletRequest request) throws IOException {
    FlakeId startArticleId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
    return resolveZone(request, rawZone, zoneInfo -> {
      return new ModelAndView("zone/zone-page")//
          .addObject("zoneInfo", zoneInfo)
          .addObject("recommendZones", zoneService.listRecommendZones())
          .addObject("articlePage",
              new ArticlePage(articleService.listHotZoneArticles(zoneInfo.getZone(),
                  startArticleId)));
    });
  }

  @RequestMapping("/{zone}/hot.rss")
  public Object rssFeed(@PathVariable("zone") String rawZone, HttpServletRequest request) {
    return resolveZone(request, rawZone, zoneInfo -> {
      request.getRequestURL();
      ModelAndView modelAndView = new ModelAndView().addObject("zoneInfo", zoneInfo)
          .addObject("articlePage", articleService.listCachedHotZoneArticles(zoneInfo.getZone()));
      modelAndView.setView(new HotArticleRssContentView());
      return modelAndView;
    });
  }

  private Object resolveZone(HttpServletRequest request,
      String decodedRawZone,
      Function<ZoneInfo, ModelAndView> onZoneInfo) {
    // note that decodedRawZone already do http url decode, and PathVariable already trim()
    // space of value
    return Zone.tryFallback(decodedRawZone).map(zone -> {
      if (!zone.value().equals(decodedRawZone)) {
        String orgUrl = request.getRequestURL().toString();
        // replace pattern is combine of fallback pattern and valid pattern
        // TODO refactor replace rule to Zone
        String location = orgUrl.replaceFirst("/z/[a-zA-Z0-9_\\-]+", "/z/" + zone);
        //check if fallback success, this prevent infinite redirect loop
        if (!location.equals(orgUrl)) {
          RedirectView redirectView = new RedirectView(location);
          redirectView.setPropagateQueryParams(true);
          redirectView.setExpandUriTemplateVariables(false);
          redirectView.setExposeModelAttributes(false);
          redirectView.setExposeContextBeansAsAttributes(false);
          redirectView.setExposePathVariables(false);
          redirectView.setContextRelative(true);
          redirectView.setStatusCode(HttpStatus.PERMANENT_REDIRECT);
          return redirectView;
        }
      }
      return onZoneInfo.apply(zoneService.loadZone(zone));
    }).orElseThrow(() -> new EmptyResultDataAccessException("no such zone: " + decodedRawZone, 1));

  }

  @RequestMapping("/{zone}/new")
  public Object newArticles(@PathVariable("zone") String rawZone,
      @RequestParam(value = "start", required = false) String start,
      HttpServletRequest request) {
    return resolveZone(request, rawZone, zoneInfo -> {
      FlakeId startArticleId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
      return new ModelAndView("zone/zone-page")//
          .addObject("zoneInfo", zoneInfo)
          .addObject("recommendZones", zoneService.listRecommendZones())
          .addObject("articlePage",
              new ArticlePage(articleService.listLatestZoneArticles(zoneInfo.getZone(),
                  startArticleId)));
    });
  }

  @RequestMapping("/{zone}/new-debate")
  public Object newDebates(@PathVariable("zone") String rawZone,
      @RequestParam(value = "start", required = false) String start,
      HttpServletRequest request) {
    return resolveZone(request, rawZone, zoneInfo -> {
      FlakeId startDebateId = Optional.ofNullable(start).map(FlakeId::fromString).orElse(null);
      List<Debate> debates = articleService.listLatestZoneDebates(zoneInfo.getZone(),
          startDebateId);
      List<Article> articles = articleService.listArticlesByDebates(debates.stream()
          .map(Debate::getDebateId)
          .collect(Collectors.toList()));
      return new ModelAndView("zone/zone-page")//
          .addObject("zoneInfo", zoneInfo)
          .addObject("recommendZones", zoneService.listRecommendZones())
          .addObject("debateList", new DebateList(debates, articles));
    });
  }

  @RequestMapping({ "/{zone}/article/create-link", "/{zone}/article/create-speak" })
  public Object createArticle(@PathVariable("zone") String rawZone, HttpServletRequest request) {
    return resolveZone(request,
        rawZone,
        zoneInfo -> new CreateArticleModelAndView(zoneService, zoneInfo));
  }

  @RequestMapping("/{zone}/debates/{articleId}")
  public Object articleDebates(@PathVariable("zone") String rawZone,
      @PathVariable("articleId") String articleId,
      HttpServletRequest request) throws IOException {
    return resolveZone(request, rawZone, zoneInfo -> {
      FlakeId articleFlakeId = FlakeId.fromString(articleId);
      return new ModelAndView("article/debates")//
          .addObject("zoneInfo", zoneInfo)
          .addObject("recommendZones", zoneService.listRecommendZones())
          .addObject("article", articleService.loadArticle(articleFlakeId))
          .addObject("debateTree", articleService.listBestDebates(articleFlakeId, null));
    });
  }

  @RequestMapping("/{zone}/debates/{articleId}/{parentDebateId}")
  public Object childDebates(@PathVariable("zone") String rawZone,
      @PathVariable("articleId") String articleId,
      @PathVariable("parentDebateId") String parentDebateId,
      HttpServletRequest request) throws IOException {
    return resolveZone(request, rawZone, zoneInfo -> {
      FlakeId articleFlakeId = FlakeId.fromString(articleId);
      FlakeId debateFlakeId = FlakeId.fromString(parentDebateId);
      return new ModelAndView("article/debates")//
          .addObject("zoneInfo", zoneInfo)
          .addObject("article", articleService.loadArticle(articleFlakeId))
          .addObject("recommendZones", zoneService.listRecommendZones())
          .addObject("parentDebate", articleService.loadDebate(debateFlakeId))
          .addObject("debateTree", articleService.listBestDebates(articleFlakeId, debateFlakeId));
    });
  }
}
