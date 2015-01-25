<#import "/spring.ftl" as spring />
<#import "/macros/template.ftl" as template>

<@template.page {
  'layout':'small'
}>

    <#-- check sign_up_form.dart for string `sign-up-success` -->
    <#if springMacroRequestContext.getQueryString()!?contains('sign-up-success') >
      <p class="alert alert-info">
        認證信已經寄出，請檢查你的信箱並啟用你的帳號
      </p>
    </#if>
    <form class="pure-form pure-form-aligned" sign-in-form>
        <fieldset>
            <legend><@spring.messageText "account-menu.sign_in" "Sign In" /></legend>
            <div class="pure-control-group">
                <label for="nameInput">帳號</label>
                <input id="nameInput" type="text" placeholder="coder" required>
            </div>
            <div class="pure-control-group">
                <label for="passwordInput">密碼</label>
                <input id="passwordInput" type="password" placeholder="你的密碼" required>
            </div>
            <div class="pure-controls">
                <label for="rememberMeInput" class="pure-checkbox">
                    <input id="rememberMeInput" type="checkbox" checked> 記住我
                </label>

                <button type="submit" class="pure-button pure-button-primary">
                    <@spring.messageText "account-menu.sign_in" "Sign In" />
                </button>
            </div>
        </fieldset>
        <p class="alert alert-danger hidden"></p>
    </form>

</@template.page>
