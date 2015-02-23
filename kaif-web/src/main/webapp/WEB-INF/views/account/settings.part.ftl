<#import "/spring.ftl" as spring />

<div account-settings>
    <div class="pure-form pure-form-aligned">
        <fieldset>

            <legend>基本資料</legend>

            <div class="pure-control-group">
                <label for="accountName">帳號名</label>
                <input id="accountName" type="text" value="${account.username}" readonly
                       class="pure-input-1-2">
            </div>

            <div class="pure-control-group">
                <label for="accountEmail">Email</label>
                <input id="accountEmail" type="text" value="${account.email}" readonly
                       class="pure-input-1-2">
            </div>

        </fieldset>
    </div>
    <div class="pure-form">
        <fieldset class="pure-u-1">
            <legend>關於我</legend>
            <div class="pure-control-group">
                <div description-content-edit class="hidden"></div>
                <div class="kmark about-me" description-content>
                <#if account.renderDescription?has_content>
                ${account.renderDescription}
                <#else>
                    <p>(尚無自介)</p>
                </#if>
                </div>
                <a href="#" description-content-editor>編輯</a>

                <div>
        </fieldset>
    </
    >
    <div class="pure-form pure-form-aligned">
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
    </div>
    <form class="pure-form pure-form-aligned" update-new-password-form>
        <fieldset>

            <legend>修改密碼</legend>

            <div class="pure-control-group">
                <label for="oldPasswordInput">舊密碼</label>
                <input id="oldPasswordInput" type="password"
                       pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
            </div>

            <div class="pure-control-group">
                <label for="passwordInput">新密碼</label>
                <input id="passwordInput" type="password"
                       pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
            </div>

            <div class="pure-control-group">
                <label for="confirmPasswordInput">確認新密碼</label>
                <input id="confirmPasswordInput" type="password"
                       pattern=".{6,100}" required title="最少六個字" class="pure-input-1-2">
            </div>

            <div class="pure-controls">
                <button type="submit" class="pure-button pure-button-primary">
                    變更密碼
                </button>
            </div>
        </fieldset>
    </form>
</div>

<form class="pure-form hidden" comp-template="edit-kmark-form">
    <div>
        <div kmark-previewer class="hidden kmark kmark-preview"></div>
        <textarea name="contentInput" class="pure-input-1" maxlength="4096" rows="3"></textarea>
    </div>
    <div>
        <button type="submit" class="pure-button pure-button-primary">修改</button>
        <button class="pure-button" kmark-cancel>取消</button>
        <button class="pure-button"
                kmark-preview><@spring.messageText "kmark.preview" "Preview" /></button>
    </div>
</form>

