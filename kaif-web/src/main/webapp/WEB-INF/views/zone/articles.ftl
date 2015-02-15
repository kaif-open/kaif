<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>

<#assign headContent>

<title>${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description" content="${zoneInfo.aliasName} ${zoneInfo.name} | kaif.io">

<link rel="stylesheet" href="<@url.dynamicRes/>/css/${zoneInfo.theme}.css">

</#assign>

<@template.page
config={
'layout':'full'
}
head=headContent
>
    <@template.zone data=zoneInfo>

    <div class="grid">
        <div class="grid-body">
            <@comp.articleList data=articlePage></@comp.articleList>
        </div>

        <aside class="grid-aside">
        <#-- TODO enable when ajax permission load -->
            <a href="/z/${zoneInfo.name}/article/create"
               class="pure-button pure-button-primary create-article"
               create-article><i class="fa fa-caret-right"></i> 建立文章</a>
        </aside>
    </div>

    </@template.zone>
</@template.page>
