package io.kaif.web;

import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.flake.FlakeId;
import io.kaif.model.account.Account;
import io.kaif.model.account.AccountAccessToken;
import io.kaif.model.account.AccountOnceToken;
import io.kaif.model.article.Article;
import io.kaif.model.article.ArticleList;
import io.kaif.model.clientapp.ClientApp;
import io.kaif.model.debate.Debate;
import io.kaif.model.feed.FeedAsset;
import io.kaif.model.feed.NewsFeed;
import io.kaif.service.AccountService;
import io.kaif.service.ArticleService;
import io.kaif.service.ClientAppService;
import io.kaif.service.FeedService;
import io.kaif.service.VoteService;

@Controller
@RequestMapping("/account")
public class AccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private ArticleService articleService;

  @Autowired
  private FeedService feedService;

  @Autowired
  private VoteService voteService;

  @Autowired
  private ClientAppService clientAppService;

  @RequestMapping("/sign-up")
  public ModelAndView signUp() {
    return new ModelAndView("account/sign-up").addObject("accountNamePattern",
        Account.NAME_PATTERN);
  }

  @RequestMapping("/sign-in")
  public String signIn() {
    return "account/sign-in";
  }

  @RequestMapping("/forget-password")
  public String forgetPassword() {
    return "account/forget-password";
  }

  @RequestMapping("/reset-password")
  public ModelAndView resetPassword(@RequestParam("key") String key) {
    Optional<AccountOnceToken> token = accountService.findValidResetPasswordToken(key);
    return new ModelAndView("account/reset-password").addObject("valid", token.isPresent());
  }

  @RequestMapping("/settings")
  public String settings() {
    return "account/account";
  }

  @RequestMapping("/settings.part")
  public ModelAndView settingsPart(AccountAccessToken accountAccessToken) {
    Account account = accountService.findMe(accountAccessToken).orElse(null);
    return new ModelAndView("account/settings.part").addObject("account", account);
  }

  @RequestMapping("/client-app")
  public String clientApp() {
    return "account/account";
  }

  @RequestMapping("/client-app.part")
  public ModelAndView clientAppPart(AccountAccessToken accountAccessToken) {
    List<ClientApp> clientApps = clientAppService.listGrantedApps(accountAccessToken);
    return new ModelAndView("account/client-app.part").addObject("clientApps", clientApps);
  }

  @RequestMapping("/news-feed")
  public String newsFeed() {
    return "account/account";
  }

  @RequestMapping("/up-voted")
  public String upVoted() {
    return "account/account";
  }

  @RequestMapping("/news-feed.part")
  public ModelAndView newsFeedPart(AccountAccessToken accountAccessToken,
      @RequestParam(value = "start", required = false) FlakeId startAssetId) {
    List<FeedAsset> feedAssets = feedService.listFeeds(accountAccessToken, startAssetId);
    List<Debate> debates = articleService.listDebatesByIdWithCache(feedAssets.stream()
        .filter(f -> f.getAssetType().isDebate())
        .map(FeedAsset::getAssetId)
        .collect(toList()));
    List<Article> articles = articleService.listArticlesByDebatesWithCache(debates.stream()
        .map(Debate::getDebateId)
        .collect(toList()));

    return new ModelAndView("account/news-feed.part").addObject("newsFeed",
        new NewsFeed(feedAssets, debates, articles)).addObject("isFirstPage", startAssetId == null);
  }

  @RequestMapping("/up-voted.part")
  public ModelAndView upVotedPart(AccountAccessToken accountAccessToken,
      @RequestParam(value = "start", required = false) FlakeId startArticleId) {
    List<Article> articles = voteService.listUpVotedArticles(accountAccessToken, startArticleId);
    return new ModelAndView("account/up-voted.part").addObject("articleList",
        new ArticleList(articles));
  }

  @RequestMapping("/activation")
  public ModelAndView activation(@RequestParam("key") String key, HttpServletResponse response) {
    boolean success = accountService.activate(key);
    if (success) {
      //see AccountSession.dart#detectForceLogout();
      Cookie cookie = new Cookie("force-logout", "true");
      cookie.setPath("/");
      cookie.setSecure(true);
      response.addCookie(cookie);
    }
    return new ModelAndView("account/activation").addObject("success", success);
  }
}