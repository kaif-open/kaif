<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page {
'layout':'full'
}>
<div class="zone ${zoneInfo.theme}">
    <ul class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <a class="pure-menu-heading">${zoneInfo.aliasName}</a>
        <@template.menuLink '/z/${zoneInfo.zone}' '熱門'/>
        <@template.menuLink '/z/${zoneInfo.zone}/new' '最新'/>
    </ul>
</div>

</@template.page>
