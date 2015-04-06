package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.USER;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientAppScope;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.service.AccountService;
import io.kaif.web.v1.dto.V1UserBasicDto;

@Api(value = "user", description = "User profile")
@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1UserResource {

  @Autowired
  private AccountService accountService;

  @ApiOperation(value = "[user] Get my basic information", notes = "Get authorized user basic information")
  @RequiredScope(USER)
  @RequestMapping(value = "/basic", method = RequestMethod.GET)
  public V1UserBasicDto basic(ClientAppUserAccessToken accessToken) {
    Account account = accountService.findMe(accessToken).get();
    return new V1UserBasicDto(account.getUsername(),
        account.getDescription(),
        Date.from(account.getCreateTime()));
  }

  @ApiOperation(value = "[public] Get basic information of the user", notes = "Get other user's basic information")
  @RequiredScope(ClientAppScope.PUBLIC)
  @RequestMapping(value = "/{username}/basic", method = RequestMethod.GET)
  public V1UserBasicDto basicByUsername(ClientAppUserAccessToken accessToken,
      @PathVariable("username") String username) {
    Account account = accountService.loadAccount(username);
    return new V1UserBasicDto(account.getUsername(),
        account.getDescription(),
        Date.from(account.getCreateTime()));
  }
}
