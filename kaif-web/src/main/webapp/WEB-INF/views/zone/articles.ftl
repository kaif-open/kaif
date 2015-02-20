<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>

<#assign headContent>

<title>${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description" content="${zoneInfo.aliasName} ${zoneInfo.name} | kaif.io">

</#assign>

<@template.page
layout='full'
head=headContent
applyZoneTheme=true
>
    <@template.zone data=zoneInfo>

    <div class="grid">
        <div class="grid-body">
            <@comp.articleList data=articlePage></@comp.articleList>
        </div>

        <aside class="grid-aside">
            <a href="/z/${zoneInfo.name}/article/create-link"
               class="pure-button pure-button-primary create-article">
                <i class="fa fa-caret-right"></i> 分享新文章</a>
            <a href="/z/${zoneInfo.name}/article/create-speak"
               class="pure-button pure-button-primary create-article">
                <i class="fa fa-caret-right"></i> 我有話要說</a>
        </aside>
    </div>

    </@template.zone>
</@template.page>
