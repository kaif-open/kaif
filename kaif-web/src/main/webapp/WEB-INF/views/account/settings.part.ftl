<#import "/spring.ftl" as spring />

<div class="pure-form pure-form-aligned" account-settings>
    <fieldset>

        <legend>基本資料</legend>

        <div class="pure-control-group">
            <label for="accountName">帳號名</label>
            <input id="accountName" type="text" value="${account.name}" readonly
                   class="pure-input-1-2">
        </div>

        <div class="pure-control-group">
            <label for="accountEmail">Email</label>
            <input id="accountEmail" type="text" value="${account.email}" readonly
                   class="pure-input-1-2">
        </div>

    </fieldset>

    <fieldset>

        <legend>權限相關</legend>

        <div class="pure-controls">
        <#if account.activated >
            <p>
                帳號已啟用
            </p>
        <#else>
            <p>尚未啟用 (未啟用帳號不能發文)</p>
            <p>
                <button class="pure-button" id="account-reactivate">重送認證信</button>
            </p>
        </#if>
        <div>

    </fieldset>


    <fieldset>

        <legend>修改密碼</legend>

        <div class="pure-control-group">
            <label for="oldPasswordInput">舊密碼</label>
            <input id="oldPasswordInput" type="password"
                   pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
        </div>

        <div class="pure-control-group">
            <label for="newPasswordInput">新密碼</label>
            <input id="newPasswordInput" type="password"
                   pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
        </div>

        <div class="pure-control-group">
            <label for="confirmNewPasswordInput">確認新密碼</label>
            <input id="confirmNewPasswordInput" type="password"
                   pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
        </div>

        <div class="pure-controls">
            <button type="submit" class="pure-button pure-button-primary">
                變更密碼
            </button>
        </div>
    </fieldset>
</div>

