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

    <div class="zone-body">
        <div class="article-list" article-list>
            <input type="hidden" name="zoneInput" value="${zoneInfo.name}">
            <input type="hidden" name="startArticleIdInput" value="${articlePage.startArticleId}">
            <input type="hidden" name="endArticleIdInput" value="${articlePage.endArticleId}">
            <#list articlePage.articles as article>
                <@comp.article data=article />
            </#list>
        </div>
        <aside class="zone-aside">
        <#-- TODO enable when ajax permission load -->
            <a href="/z/${zoneInfo.name}/article/create"
               class="pure-button pure-button-primary create-article"
               create-article>建立文章 &gt; </a>
        </aside>
    </div>

    </@template.zone>
</@template.page>
