<#import "/spring.ftl" as spring />
<#import "../macros/comp.ftl" as comp>

<div account-settings>
    <div class="pure-form pure-form-aligned">
        <fieldset>

            <legend>基本資料</legend>

            <div class="pure-control-group">
                <label for="accountName">帳號名</label>
                <input id="accountName" type="text" value="${account.username}" readonly>
            </div>

            <div class="pure-control-group">
                <label for="accountEmail">Email</label>
                <input id="accountEmail" type="text" value="${account.email}" readonly>
            </div>

            <div class="pure-control-group about-me-control">
                <label>關於我</label>

                <div class="psuedo-input">
                    <div description-content-edit class="hidden"></div>
                    <div class="about-me kmark" description-content>
                    <#if account.renderDescription?has_content>
                    ${account.renderDescription}
                    <#else>
                        <p>(尚無自介)</p>
                    </#if>
                    </div>
                    <p>
                        <a href="#" class="pure-button" description-content-editor>編輯</a>
                    </p>
                </div>
            </div>
        </fieldset>
    </div>
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
                       pattern=".{6,100}" required title="最少六個字">
            </div>

            <div class="pure-control-group">
                <label for="passwordInput">新密碼</label>
                <input id="passwordInput" type="password"
                       pattern=".{6,100}" required title="最少六個字">
            </div>

            <div class="pure-control-group">
                <label for="confirmPasswordInput">確認新密碼</label>
                <input id="confirmPasswordInput" type="password"
                       pattern=".{6,100}" required title="最少六個字">
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
        <textarea name="contentInput" class="pure-input-1 kmark-input" maxlength="4096"
                  rows="3"></textarea>
    </div>
    <div class="form-action-bar">
        <button type="submit" class="pure-button pure-button-primary">修改</button>
        <button class="pure-button" kmark-cancel>取消</button>
        <button class="pure-button"
                kmark-preview><@spring.messageText "kmark.preview" "Preview" /></button>
        <button type="button" class="pure-button" kmark-help-toggle>
        <@spring.messageText "kmark.help" "Format Help" />
        </button>
    </div>
<@comp.kmarkHelp />
</form>

