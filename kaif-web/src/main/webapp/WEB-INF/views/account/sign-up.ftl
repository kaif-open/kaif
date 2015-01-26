<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page {
  'layout':'small'
}>

    <form class="pure-form pure-form-stacked" sign-up-form>
        <fieldset>

            <legend>註冊新帳號</legend>

            <div class="pure-control-group">
                <label for="nameInput">帳號  <span class="hint nameHint"></span></label>
                <input id="nameInput" type="text" placeholder="英文字、數字、底線"
                       maxlength="15" pattern="${accountNamePattern}" required title="英文字、數字、底線，3~15 個字">
            </div>

            <div class="pure-control-group">
                <label for="emailInput">Email </label>
                <input id="emailInput" type="email" placeholder="foo@gmail.com" required>
            </div>

            <div class="pure-control-group">
                <label for="passwordInput">密碼</label>
                <input id="passwordInput" type="password" placeholder="最少六個字"
                       pattern=".{6,100}" required title="最少六個字">
            </div>

            <div class="pure-control-group">
                <label for="confirmPasswordInput">確認密碼</label>
                <input id="confirmPasswordInput" type="password" placeholder="再次確認"
                       pattern=".{6,100}" required title="最少六個字">
            </div>

            <div class="pure-controls">
                <label for="consentInput" class="pure-checkbox">
                    <input id="consentInput" type="checkbox" >
                    <span id="consentLabel" >我同意本站使用條款</span>
                </label>
                <button type="submit" class="pure-button pure-button-primary">
                   <@spring.messageText "account-menu.sign_up" "Sign Up" />
                </button>
                <i class="fa fa-cog fa-spin loading hidden"></i>
            </div>

        </fieldset>
        <p class="alert alert-danger hidden"></p>
    </form>

</@template.page>
