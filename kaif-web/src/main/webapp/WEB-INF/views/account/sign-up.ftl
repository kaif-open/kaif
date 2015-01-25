<#import "/spring.ftl" as spring />
<#import "/macros/template.ftl" as template>

<@template.page {
  'layout':'small'
}>

    <form class="pure-form pure-form-stacked" sign-up-form>
        <fieldset>
            <legend>Register new account</legend>

            <label for="nameInput">Your Name <span class="hint nameHint"></span></label>
            <input id="nameInput" type="text" placeholder="letter or number or underscore"
                   maxlength="15" pattern="${accountNamePattern}" required title="letter or number only, 3~15 characters">

            <label for="emailInput">Your Email </label>
            <input id="emailInput" type="email" placeholder="foo@gmail.com" required>

            <label for="passwordInput">Your Password</label>
            <input id="passwordInput" type="password" placeholder="Your Password"
                   pattern=".{6,100}" required title="at least 6 characters">

            <label for="confirmPasswordInput">Confirm Password</label>
            <input id="confirmPasswordInput" type="password" placeholder="Type Again"
                   pattern=".{6,100}" required title="at least 6 characters">

            <button type="submit" class="pure-button pure-button-primary">Sign Up</button>
            <i class="fa fa-cog fa-spin loading hidden"></i>
        </fieldset>
        <p class="alert alert-danger hidden"></p>
    </form>

</@template.page>
