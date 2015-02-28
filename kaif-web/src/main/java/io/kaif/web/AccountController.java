package io.kaif.web;

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
import io.kaif.model.debate.Debate;
import io.kaif.service.AccountService;
import io.kaif.service.ArticleService;
import io.kaif.web.support.PartTemplate;

@Controller
@RequestMapping("/account")
public class AccountController {

  @Autowired
  private AccountService accountService;

  @Autowired
  private ArticleService articleService;

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
  public ModelAndView settings() {
    return PartTemplate.smallLayout();
  }

  @RequestMapping("/settings.part")
  public ModelAndView settingsPart(AccountAccessToken accountAccessToken) {
    Account account = accountService.findMe(accountAccessToken).orElse(null);
    return new ModelAndView("account/settings.part").addObject("account", account);
  }

  @RequestMapping("/debate-replies")
  public String debateReplies() {
    return "article/debate-replies";
  }

  @RequestMapping("/debate-replies.part")
  public ModelAndView debateRepliesPart(AccountAccessToken accountAccessToken,
      @RequestParam(value = "startDebateId", required = false) String startDebateId) {
    List<Debate> debates = articleService.listReplyToDebates(accountAccessToken,
        Optional.ofNullable(startDebateId).map(FlakeId::fromString).orElse(null));
    return new ModelAndView("article/debate-replies.part").addObject("debates", debates);
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