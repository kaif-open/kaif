<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page {}>
<div class="pure-g">
  <div class="pure-u-1-5"></div>
  <div class="pure-u-3-5">
    <form class="pure-form pure-form-stacked" login-form-controller>
        <fieldset>
            <legend>Login</legend>

            <label for="nameInput">Your Name</label>
            <input id="nameInput" type="text" placeholder="letter or number">

            <label for="passwordInput">Password</label>
            <input id="passwordInput" type="password" placeholder="Your Password">

            <label for="rememberMeInput" class="pure-checkbox">
                <input id="rememberMeInput" type="checkbox" checked> Remember me
            </label>

            <button type="submit" class="pure-button pure-button-primary">Login</button>
        </fieldset>
    </form>
  </div>
  <div class="pure-u-3-5"></div>
</div>
</@template.page>
