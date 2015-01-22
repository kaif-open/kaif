package io.kaif.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.account.Account;

@Controller
public class HomeController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }

  @RequestMapping("/sign-up")
  public ModelAndView signUp() {
    ModelAndView modelAndView = new ModelAndView("sign-up");
    modelAndView.addObject("accountNamePattern", Account.NAME_PATTERN);
    return modelAndView;
  }

  @RequestMapping("/login")
  public String login() {
    return "login";
  }
}