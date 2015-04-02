package io.kaif.web.v1;

import java.util.List;
import java.util.UUID;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.account.AccountAccessToken;
import io.kaif.web.support.SingleWrapper;

@RestController
@RequestMapping("/v1/oauth")
public class V1OauthResource {

  static class DirectAuthorize {
    @NotNull
    public String clientId;

    @NotNull
    public String state;

    @NotNull
    public List<String> scopes;

    @NotNull
    public String redirectUri;
  }

  static class SignInAuthorize {
    @NotNull
    public String username;

    @NotNull
    public String password;

    @NotNull
    public String clientId;

    @NotNull
    public String state;

    @NotNull
    public List<String> scopes;

    @NotNull
    public String redirectUri;
  }

  //TODO move to controller because need to use server side redirect
  //TODO AccountAccessToken should be submit via form body, not header, but the token still
  //     append by js, not hard-coded in html
  @RequestMapping(value = "/direct-authorize", method = RequestMethod.POST)
  public SingleWrapper<String> directAuthorize(AccountAccessToken token,
      @RequestBody @Valid DirectAuthorize directAuthorize) {
    //TODO create ClientAppUser
    //TODO state needs encode component uri ?
    //TODO redirectUri
    String redirectLocation = directAuthorize.redirectUri
        + "?state="
        + directAuthorize.state
        + "&code="
        + UUID.randomUUID().toString();
    return SingleWrapper.of(redirectLocation);
  }

  @RequestMapping(value = "/sign-in-authorize", method = RequestMethod.POST)
  public SingleWrapper<String> signInAuthorize(
      @RequestBody @Valid SignInAuthorize signInAuthorize) {
    //TODO create ClientAppUser
    //TODO state needs encode component uri ?
    //TODO redirectUri
    String redirectLocation = signInAuthorize.redirectUri
        + "?state="
        + signInAuthorize.state
        + "&code="
        + UUID.randomUUID().toString();
    return SingleWrapper.of(redirectLocation);
  }

}
