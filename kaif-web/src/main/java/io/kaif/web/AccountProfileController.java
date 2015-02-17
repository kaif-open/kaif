package io.kaif.web;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.google.common.collect.ImmutableMap;

import io.kaif.model.account.Account;
import io.kaif.model.account.AccountStats;
import io.kaif.service.AccountService;

@Controller
public class AccountProfileController {

  @Autowired
  AccountService accountService;

  @RequestMapping("/u/{username}")
  public ModelAndView accountProfile(
      @PathVariable("username") @Pattern(regexp = Account.NAME_PATTERN) String username) {
    Account account = accountService.loadAccount(username);
    AccountStats accountStats = accountService.loadAccountStats(account.getUsername());
    return new ModelAndView("account/user-profile").addAllObjects(ImmutableMap.of("account",
        account,
        "accountStats",
        accountStats));
  }

}
