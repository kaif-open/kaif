<#import "/spring.ftl" as spring />
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>


<div granted-client-app class="grid-center-row granted-client-app">
<#if clientApps?size == 0 >
    尚無授權的應用程式
<#else>
    <table class="pure-table pure-table-horizontal">
        <thead>
        <tr>
            <th>應用程式</th>
            <th>說明</th>
            <th></th>
        </tr>
        </thead>
        <#list clientApps as clientApp>
            <tr>
                <td>
                ${clientApp.appName}
                </td>
                <td class="client-app-description">
                ${clientApp.description}
                </td>
                <td>
                    <button class="pure-button button-danger"
                            data-client-id="${clientApp.clientId}">撤消
                    </button>
                </td>
            </tr>
        </#list>
    </table>
</#if>
</div>