package io.kaif.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.service.ClientAppService;

@Controller
@RequestMapping("/developer")
public class DeveloperController {

  @Autowired
  private ClientAppService clientAppService;

  @RequestMapping("/client-app")
  public String clientApp() {
    return "developer/client-app";
  }

  @RequestMapping("/client-app.part")
  public ModelAndView clientAppPart(AccountAccessToken accountAccessToken) {
    return new ModelAndView("developer/client-app.part").addObject("clientApps",
        clientAppService.listClientApps(accountAccessToken));
  }
}