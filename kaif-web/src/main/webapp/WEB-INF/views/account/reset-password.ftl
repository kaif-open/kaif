<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page {
'layout':'small'
}>

    <#if valid >
    <form class="pure-form pure-form-aligned" reset-password-form>
        <fieldset>
            <legend>重置密碼</legend>

            <div class="pure-controls">
                <p class="pure-form-message">
                    請輸入你的新密碼
                </p>
            </div>
            <div class="pure-control-group">
                <label for="passwordInput">密碼</label>
                <input id="passwordInput" type="password" placeholder="最少六個字"
                       pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
            </div>
            <div class="pure-control-group">
                <label for="confirmPasswordInput">確認密碼</label>
                <input id="confirmPasswordInput" type="password" placeholder="再次確認"
                       pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
            </div>
            <div class="pure-controls">
                <button type="submit" class="pure-button pure-button-primary">
                    重置
                </button>
            </div>
        </fieldset>
    </form>
    <#else >
    <p class="alert alert-danger">
        重置連結已經失效，請重新再申請一次。
    </p>
    </#if>




</@template.page>
