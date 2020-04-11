<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page layout='small'>

<#-- check sign_up_form.dart for string `sign-up-success` -->
    <#if springMacroRequestContext.getQueryString()!?contains('sign-up-success') >
    <p class="alert alert-info">
        認證信已經寄出，請檢查你的信箱並啟用你的帳號
    </p>
    </#if>

<#-- check forget_password_form.dart for string `send-reset-password-successs` -->
    <#if springMacroRequestContext.getQueryString()!?contains('send-reset-password-success') >
    <p class="alert alert-info">
        重置信件已經寄出，請檢查你的信箱並修改你的密碼
    </p>
    </#if>

<#-- check reset_password_form.dart for string `update-password-success -->
    <#if springMacroRequestContext.getQueryString()!?contains('update-password-success') >
    <p class="alert alert-info">
        密碼修改成功，請使用新密碼登入
    </p>
    </#if>
<form class="pure-form pure-form-aligned" sign-in-form method="post">
    <fieldset>

        <legend><@spring.messageText "account-menu.sign-in" "Sign In" /></legend>
        <div class="pure-control-group">
            <label for="nameInput">帳號</label>
            <input id="nameInput" type="text" placeholder="coder" required class="pure-input-1-2">
        </div>
        <div class="pure-control-group">
            <label for="passwordInput">密碼</label>
            <input id="passwordInput" type="password" placeholder="你的密碼" required
                   class="pure-input-1-2">
        </div>
        <div class="pure-controls">
            <label for="rememberMeInput" class="pure-checkbox">
                <input id="rememberMeInput" type="checkbox" checked> 記住我
            </label>

            <button type="submit" class="pure-button pure-button-primary">
                <@spring.messageText "account-menu.sign-in" "Sign In" />
            </button>
        </div>
    </fieldset>
    <fieldset>
        <legend>
            其他
        </legend>
        <div class="pure-controls">
            <a class="pure-button button-sm" href="/account/sign-up">註冊新帳號</a>
            <a class="pure-button button-sm" href="/account/forget-password">忘記密碼 ?</a>
        </div>
    </fieldset>
</form>

</@template.page>
