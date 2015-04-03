<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page layout='small'>

<form class="pure-form pure-form-aligned" oauth-authorize-form>

    <input type="hidden" name="clientId" value="${clientApp.clientId}">
    <input type="hidden" name="state" value="${state}">
    <input type="hidden" name="redirectUri" value="${redirectUri}">
    <input type="hidden" name="scope" value="${scope}">
    <input type="hidden" name="responseType" value="${responseType}">

    <fieldset>
        <legend>授權應用程式 ${clientApp.appName}</legend>
        <div class="pure-control-group">
            <label for="nameInput">帳號</label>
            <input id="nameInput" type="text" required class="pure-input-1-2">
        </div>
        <div class="pure-control-group hidden" password-group>
            <label for="passwordInput">密碼</label>
            <input id="passwordInput" type="password" placeholder="你的密碼" required
                   class="pure-input-1-2">
        </div>
        <div class="pure-controls">
            <button type="submit" class="pure-button pure-button-primary">
                授權
            </button>
        </div>
    </fieldset>
</form>

</@template.page>
