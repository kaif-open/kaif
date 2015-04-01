<#import "/spring.ftl" as spring />
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<div class="grid-center-row">
    <p></p>

    <div developer-client-app class="pure-g">
        <div class="pure-u-1-3 pure-u-md-1-6">
            <nav class="pure-menu pure-menu-open pure-menu-vertical">
                <ul>
                    <li class="">
                        <a href="#create-client-app" data-toggle="tab">
                            <i class="fa fa-plus"></i> 新增應用程式
                        </a>
                    </li>
                    <li class="pure-menu-separator"></li>
                    <li class="pure-menu-selected">
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
                    Create App...
                </div>
                <div id="edit-client-app_foo" class="tab-pane">Foo App1
                </div>
            </div>
        </div>
    </div>
</div>



