<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<#assign headContent>
<title>開發者</title>
</#assign>

<@template.page layout='full' head=headContent>

<nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
    <@util.menuLink '/developer/doc' '開發者文件'/>
    <@util.menuLink '/developer/client-app' '管理應用程式'/>
</nav>
<div class="grid">
    <div class="grid-body">
        <div class="grid-center-row">
            <h3>
                kaif Open API 開發說明
            </h3>

            <p>
                kaif Open API 提供開發者在應用程式內串接 API。簡要來說 API 認證的方式採用
                <a href="https://speakerdeck.com/chitsaou/jian-dan-yi-dong-de-oauth-2-dot-0"
                   target="_blank">OAuth2</a>
                ，而協定則是 RESTFul JSON，
                目前的設計適用於手機應用程式串接。注意 Open API 仍然在測試中，未來仍會再改變。
            </p>
            <h4>相關連結</h4>
            <ul>
                <li>
                    <a href="https://github.com/kaif-open/kaif-api" target="_blank">
                        開發指南 (github) <i class="fa fa-external-link"></i>
                    </a>

                    <p>
                        如果 API 有任何使用上問題，請在該 github 專案上發 issue。
                    </p>
                </li>
                <li>
                    <a href="/sdoc.jsp" target="_blank">
                        API 文件 <i class="fa fa-external-link"></i>
                    </a>

                    <p>
                        API 文件以 <a href="http://swagger.io" target="_blank">Swagger</a>
                        的方式呈現，不過只能當文件查詢，Swagger UI 上提供的測試功能還不能使用。
                    </p>
                    <#--
                    <p>你可以利用 Swagger 提供的工具自動
                        <a href="https://github.com/swagger-api/swagger-codegen" target="_blank">
                            產生呼叫 API 的程式</a>。
                        kaif Open API 的 Swagger 定義網址為 <code><a href="/api-docs">https://kaif.io/api-docs</a></code>
                    </p>
                    -->
                </li>
                <li>
                    <a href="/developer/client-app" rel="nofollow">
                        管理你的應用程式
                    </a>

                    <p>
                        新增與修改你的應用程式，API 串接時需要使用的 <code>client_id</code> 與 <code>client_secret</code>
                        可在此處查詢。
                    </p>
                </li>
            </ul>
        </div>
    </div>
    <div class="grid-aside">

    </div>
</div>
</@template.page>
