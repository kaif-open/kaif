<#import "/spring.ftl" as spring />
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<div class="grid-center-row">
    <p></p>

    <div developer-client-app class="pure-g">
        <div class="pure-u-1-3 pure-u-md-1-6">
            <nav class="client-app-menu pure-menu pure-menu-open pure-menu-vertical">
                <ul>
                    <li>
                        <a href="#create-client-app" data-toggle="tab">
                            <i class="fa fa-plus"></i> 新增應用程式
                        </a>
                    </li>
                    <li class="pure-menu-separator"></li>
                    <li>
                    <#list clientApps as clientApp>
                        <a href="#edit-client-app_${clientApp.clientId}" data-toggle="tab">
                            <i class="fa fa-cube"></i> ${clientApp.appName}
                        </a>
                    </#list>
                    </li>
                </ul>
            </nav>
        </div>
        <div class="pure-u-2-3 pure-u-md-5-6">
            <div class="tab-content">
                <div id="create-client-app" class="tab-pane">
                    <form class="pure-form pure-form-stacked client-app-form"
                          create-client-app-form>
                        <fieldset>
                            <legend>建立新應用程式</legend>

                            <div class="pure-control-group">
                                <label>名稱*</label>
                                <input name="nameInput" type="text"
                                       pattern=".{3,15}"
                                       maxlength="15" required title="3 ~ 15 字"
                                       class="pure-input-1">
                            </div>

                            <div class="pure-control-group">
                                <label>說明*</label>
                                <input name="descriptionInput" type="text"
                                       pattern=".{5,100}" title="5 ~ 100 字"
                                       maxlength="100" required class="pure-input-1">
                            </div>

                            <div class="pure-control-group">
                                <label>callback_uri* (不能包含 kaif 保留字)</label>
                                <input name="callbackUriInput" type="url"
                                       placeholder="http://... or foo://..."
                                       required
                                       class="pure-input-1">
                            </div>

                            <div class="pure-controls">
                                <button type="submit" class="pure-button pure-button-primary">
                                    建立
                                </button>
                            </div>
                            <p class="pure-form-message">
                                * 最多五個應用程式
                            </p>
                        </fieldset>
                    </form>
                </div>
            <#list clientApps as clientApp >
                <div id="edit-client-app_${clientApp.clientId}" class="tab-pane">
                    <form class="pure-form pure-form-stacked client-app-form"
                          edit-client-app-form>
                        <fieldset>
                            <legend>應用程式 ${clientApp.appName}</legend>
                            <div class="pure-control-group">
                                <label><b>client_id</b></label>
                                <input name="clientIdInput" type="text"
                                       value="${clientApp.clientId}" readonly
                                       class="pure-input-1">
                            </div>
                            <div class="pure-control-group">
                                <label><b>client_secret</b></label>
                                <input name="clientSecretInput" type="text"
                                       value="${clientApp.clientSecret}" readonly
                                       class="pure-input-1">
                            </div>
                        </fieldset>
                        <fieldset>
                            <legend>編輯資訊</legend>
                            <div class="pure-control-group">
                                <label>名稱*</label>
                                <input name="nameInput" type="text"
                                       pattern=".{3,15}"
                                       value="${clientApp.appName}"
                                       maxlength="15" required title="3 ~ 15 字"
                                       class="pure-input-1">
                            </div>

                            <div class="pure-control-group">
                                <label>說明*</label>
                                <input name="descriptionInput" type="text"
                                       value="${clientApp.description}"
                                       pattern=".{5,100}" title="5 ~ 100 字"
                                       maxlength="100" required class="pure-input-1">
                            </div>

                            <div class="pure-control-group">
                                <label>callback_uri* (不能包含 kaif 保留字)</label>
                                <input name="callbackUriInput" type="url"
                                       placeholder="http://... or foo://..."
                                       value="${clientApp.callbackUri}"
                                       required
                                       class="pure-input-1">
                            </div>

                            <div class="pure-controls">
                                <button type="submit" class="pure-button pure-button-primary">
                                    變更
                                </button>
                            </div>

                        </fieldset>
                    </form>
                    <form class="pure-form pure-form-stacked client-app-form debug-client-app-form"
                          debug-client-app-form>
                        <fieldset>
                            <legend>測試區</legend>
                            <input name="clientIdInput" type="hidden" value="${clientApp.clientId}">
                        <#--
                        <div class="pure-control-group">
                            <label><b>授權的 scope*</b></label>
                            <input name="scopeInput" type="text"
                                   value="${allCanonicalScopes}"
                                   required
                                   class="pure-input-1">
                            <div class="pure-form-message-inline">
                                * 所有支援的 scope: <code>${allCanonicalScopes}</code>
                            </div>
                        </div>
                        -->

                            <div class="pure-controls">
                                <button type="submit" class="pure-button">
                                    產生測試用 OAuth2 <code>access_token</code>
                                </button>
                                <div class="pure-form-message">ps. 該 token 由你目前帳號授權，並且包含所有權限
                                    (scope)
                                </div>
                            </div>
                        </fieldset>
                        <fieldset generated-token-group class="hidden">
                            <legend>暫時的 <code>access_token</code>:</legend>
                            <div class="pure-control-group">
                            <textarea name="generatedTokenInput"
                                      class="generated-token-input pure-input-1" rows="6" readonly>
                            </textarea>
                            </div>
                        </fieldset>
                    </form>
                </div>
            </#list>
            </div>
        </div>
    </div>
</div>



