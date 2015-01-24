package io.kaif.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.account.Account;

@Controller
@RequestMapping("/account")
public class AccountController {

  @RequestMapping("/sign-up")
  public ModelAndView signUp() {
    ModelAndView modelAndView = new ModelAndView("account/sign-up");
    modelAndView.addObject("accountNamePattern", Account.NAME_PATTERN);
    return modelAndView;
  }

  @RequestMapping("/sign-in")
  public String signIn() {
    return "account/sign-in";
  }

  @RequestMapping("/activation")
  public String activation(@RequestParam("key") String key) {
    return "account/activation";
  }
}