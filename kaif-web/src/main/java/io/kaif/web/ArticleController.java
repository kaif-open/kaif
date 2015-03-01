package io.kaif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.service.ZoneService;

@Controller
@RequestMapping("/article")
public class ArticleController {

  @Autowired
  private ZoneService zoneService;

  /**
   * request parameter are used for bookmarklet pre-filled, so name is short.
   */
  @RequestMapping({ "/create-link", "/create-speak" })
  public ModelAndView createArticle(@RequestParam(value = "c", required = false) String content,
      @RequestParam(value = "t", required = false) String title) {
    return new CreateArticleModelAndView(zoneService, null)//
        .preFilled(content, title);
  }
}
