package io.kaif.web;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.base.Strings;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountStats;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticlePage;
import io.kaif.model.debate.Debate;
import io.kaif.model.debate.DebateList;
import io.kaif.service.AccountService;
import io.kaif.service.ArticleService;

@Controller
public class UserController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private ArticleService articleService;

  @RequestMapping("/u/{username}")
  public ModelAndView userProfile(@PathVariable("username") String username) {
    Account account = accountService.loadAccount(username);
    AccountStats accountStats = accountService.loadAccountStats(account.getUsername());
    return new ModelAndView("account/user-profile")//
        .addObject("account", account).addObject("accountStats", accountStats);
  }

  @RequestMapping("/u/{username}/articles")
  public ModelAndView createdArticles(@PathVariable("username") String username,
      @RequestParam(value = "start", required = false) String start) {
    FlakeId startArticleId = Optional.ofNullable(start)
        .map(Strings::emptyToNull)
        .map(FlakeId::fromString)
        .orElse(null);
    List<Article> articles = articleService.listArticlesByAuthor(username, startArticleId);
    return new ModelAndView("account/user-articles")//
        .addObject("articlePage", new ArticlePage(articles)).addObject("username", username);
  }

  @RequestMapping("/u/{username}/debates")
  public ModelAndView createdDebates(@PathVariable("username") String username,
      @RequestParam(value = "start", required = false) String start) {
    FlakeId startDebateId = Optional.ofNullable(start)
        .map(Strings::emptyToNull)
        .map(FlakeId::fromString)
        .orElse(null);
    List<Debate> debates = articleService.listDebatesByDebater(username, startDebateId);
    List<Article> articles = articleService.listArticlesByDebates(debates.stream()
        .map(Debate::getDebateId)
        .collect(toList()));
    return new ModelAndView("account/user-debates")//
        .addObject("debateList", new DebateList(debates, articles)).addObject("username", username);
  }
}
