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
                        <a href="#edit-client-app_foo" data-toggle="tab">
                            <i class="fa fa-cube"></i> Foo App1
                        </a>
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
                                <label>Callback Uri*</label>
                                <input name="callbackUriInput" type="url" placeholder="http://..."
                                       required
                                       class="pure-input-1">
                            </div>

                            <div class="pure-controls">
                                <button type="submit" class="pure-button pure-button-primary">
                                    建立
                                </button>
                            </div>

                        </fieldset>
                    </form>
                </div>
                <div id="edit-client-app_foo" class="tab-pane">Foo App1
                </div>
            </div>
        </div>
    </div>
</div>



