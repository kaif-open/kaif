package io.kaif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.service.ZoneService;

@Controller
@RequestMapping("/article")
public class ArticleController {

  @Autowired
  private ZoneService zoneService;

  @RequestMapping({ "/create-link", "/create-speak" })
  public ModelAndView createArticle() {
    return new CreateArticleModelAndView(zoneService, null);
  }
}
