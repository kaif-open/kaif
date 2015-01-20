package io.kaif.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class HomeController {

  @RequestMapping("/")
  public String index() {
    return "index";
  }

  @RequestMapping("/sign-up")
  public String signUp() {
    return "sign-up";
  }

  @RequestMapping("/login")
  public String login() {
    return "login";
  }
}