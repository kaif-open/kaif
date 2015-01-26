<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page {
'layout':'small'
}>

<form class="pure-form pure-form-aligned" forget-password-form>
    <fieldset>
        <legend>申請重置密碼</legend>

        <div class="pure-controls">
            <p class="pure-form-message">
                請確認你的帳號與當初申請的 email
            </p>
        </div>
        <div class="pure-control-group">
            <label for="nameInput">帳號</label>
            <input id="nameInput" type="text" placeholder="coder" required class="pure-input-1-2">
        </div>
        <div class="pure-control-group">
            <label for="emailInput">Email</label>
            <input id="emailInput" type="email" placeholder="email" required class="pure-input-1-2">
        </div>
        <div class="pure-controls">
            <button type="submit" class="pure-button pure-button-primary">
                申請
            </button>
        </div>
    </fieldset>
</form>

</@template.page>
