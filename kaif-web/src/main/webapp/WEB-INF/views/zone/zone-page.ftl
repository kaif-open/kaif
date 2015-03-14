<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/aside.ftl" as aside>

<#assign headContent>

<title>${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description" content="${zoneInfo.aliasName} - <@url.zone data=zoneInfo/>">
    <#if url.isCurrentPath('/z/${zoneInfo.name}')>
    <link rel="alternate" type="application/rss+xml" title="RSS"
          href="https://kaif.io/z/${zoneInfo.name}/hot.rss"/>
    </#if>
</#assign>

<@template.page
layout='full'
head=headContent
applyZoneTheme=true
>
    <@template.zone data=zoneInfo>

    <div class="grid">
        <div class="grid-body">
            <#if articleList??>
                <@comp.articleList data=articleList></@comp.articleList>
            </#if>

        <#if debateList??>
            <@comp.debateList data=debateList showZone=false></@comp.debateList>
            <@comp.debateForm />
        </#if>
        </div>

        <aside class="grid-aside">
            <@aside.createArticle />
            <@aside.recommendZones zoneInfos=recommendZones />
            <@aside.honorRoll data=honorRollList/>
            <@aside.rss/>
        </aside>
    </div>

    </@template.zone>
</@template.page>
