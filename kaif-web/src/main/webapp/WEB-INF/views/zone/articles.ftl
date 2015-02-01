<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<#assign headContent>

<title>${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description" content="${zoneInfo.aliasName} ${zoneInfo.name} | kaif.io">

<link rel="stylesheet" href="/css/${zoneInfo.theme}.css?${(kaif.deployServerTime)!0}">

</#assign>

<@template.page
config={
'layout':'full'
}
head=headContent
>

<div class="zone ${zoneInfo.theme}">
    <ul class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <a class="pure-menu-heading">${zoneInfo.aliasName}</a>
        <@template.menuLink '/z/${zoneInfo.name}' '熱門'/>
        <@template.menuLink '/z/${zoneInfo.name}/new' '最新'/>
    </ul>
</div>

</@template.page>
