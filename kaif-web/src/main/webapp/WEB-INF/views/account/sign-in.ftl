<#import "/spring.ftl" as spring />
<#import "/macros/template.ftl" as template>

<@template.page {}>
<div class="pure-g">
  <div class="pure-u-1-5"></div>
  <div class="pure-u-3-5">
    <#-- check sign_up_form.dart for string `sign-up-success` -->
    <#if springMacroRequestContext.getQueryString()!?contains('sign-up-success') >
      <p class="alert alert-info">
        <#-- TODO i18n -->
        Verification email sent, please check your email to activate your account.
      </p>
    </#if>
    <form class="pure-form pure-form-stacked" sign-in-form>
        <fieldset>
            <legend>Login</legend>

            <label for="nameInput">Your Name</label>
            <input id="nameInput" type="text" placeholder="letter or number" required>

            <label for="passwordInput">Password</label>
            <input id="passwordInput" type="password" placeholder="Your Password" required>

            <label for="rememberMeInput" class="pure-checkbox">
                <input id="rememberMeInput" type="checkbox" checked> Remember me
            </label>

            <button type="submit" class="pure-button pure-button-primary">Login</button>
        </fieldset>
        <p class="alert alert-danger hidden"></p>
    </form>
  </div>
  <div class="pure-u-3-5"></div>
</div>
</@template.page>
