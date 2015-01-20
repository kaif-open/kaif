<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page {}>
<div class="pure-g">
  <div class="pure-u-1-5"></div>
  <div class="pure-u-3-5">
    <form class="pure-form pure-form-stacked" sign-up-form-controller>
        <fieldset>
            <legend>Register new account</legend>

            <label for="nameInput">Your Name</label>
            <input id="nameInput" type="text" placeholder="letter or number">

            <label for="emailInput">Your Email</label>
            <input id="emailInput" type="email" placeholder="foo@gmail.com">

            <label for="passwordInput">Your Password</label>
            <input id="passwordInput" type="password" placeholder="Your Password">

            <label for="confirmPasswordInput">Confirm Password</label>
            <input id="confirmPasswordInput" type="password" placeholder="Type Again">

            <button type="submit" class="pure-button">Sign Up</button>
        </fieldset>
    </form>
  </div>
  <div class="pure-u-3-5"></div>
</div>
</@template.page>
