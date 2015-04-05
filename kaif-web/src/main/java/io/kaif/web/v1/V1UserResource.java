package io.kaif.web.v1;

import static io.kaif.model.clientapp.ClientAppScope.USER;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import io.kaif.model.account.Account;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.service.AccountService;
import io.kaif.web.v1.dto.RenderMode;
import io.kaif.web.v1.dto.UserBasicDto;

@Api(value = "user", description = "User profile")
@RestController
@RequestMapping(value = "/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
public class V1UserResource {

  @Autowired
  private AccountService accountService;

  @ApiOperation(value = "Get user basic information", notes = "Get authenticated user basic information")
  @RequiredScope(USER)
  @RequestMapping(value = "/basic", method = RequestMethod.GET)
  public UserBasicDto basic(ClientAppUserAccessToken accessToken,
      @RequestParam(value = "renderMode", required = false)
      @ApiParam("render description in mark down or html") RenderMode renderMode) {
    Account account = accountService.findMe(accessToken).get();
    return new UserBasicDto(account.getUsername(),
        renderMode == RenderMode.HTML ? account.getRenderDescription() : account.getDescription(),
        Date.from(account.getCreateTime()));
  }

}
