<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page layout='small'>

<div class="pure-g">
    <div class="pure-u-1-2">
        <form class="pure-form pure-form-stacked" oauth-authorize-form
              method="POST"
              enctype="application/x-www-form-urlencoded">
            <input type="hidden" name="oauthDirectAuthorize" value="">
            <input type="hidden" name="grantDeny" value="">
            <fieldset>
                <legend>授權應用程式 ${clientApp.appName} 使用您的帳號 ?</legend>
                <div class="pure-control-group">
                    <label for="nameInput">帳號</label>
                    <input id="nameInput" type="text" required class="pure-input-1">
                </div>
                <div class="pure-control-group hidden" password-group>
                    <label for="passwordInput">密碼</label>
                    <input id="passwordInput" type="password" placeholder="你的密碼" required
                           class="pure-input-1">
                </div>
                <div class="pure-controls">
                    <button id="grantSubmit" type="submit" class="pure-button pure-button-primary">
                        授權
                    </button>
                    <button id="denySubmit"
                            type="button" class="pure-button">
                        取消
                    </button>
                </div>
            </fieldset>
            <fieldset>
                <legend>
                    您將授權 ${clientApp.appName} 的權限：
                </legend>
                <div class="pure-controls">
                    <ul>
                        <#list clientAppScopes as clientAppScope>
                            <li><@spring.messageText clientAppScope.i18nKey clientAppScope.name() /></li>
                        </#list>
                    </ul>
                </div>
            </fieldset>
        </form>
    </div>
    <div class="pure-u-1-2">
        <div class="client-app-card">
            <h3>${clientApp.appName}</h3>

            <div class="client-app-card-box">
            ${clientApp.description}
            </div>
        </div>
    </div>
</div>
</@template.page>
