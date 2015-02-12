<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>

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
<div class="zone ${zoneInfo.theme}">
    <div class="zone-header">
        <div class="zone-title">${zoneInfo.aliasName} -
            /z/${zoneInfo.name}</div>
    </div>
    <nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <@template.menuLink '/z/${zoneInfo.name}' '熱門'/>
        <@template.menuLink '/z/${zoneInfo.name}/new' '最新'/>
    </nav>
    <div class="zone-content">
        <div class="article-list" article-list>
            <input type="hidden" name="zoneInput" value="${zoneInfo.name}">
            <input type="hidden" name="startArticleIdInput" value="${articlePage.startArticleId}">
            <input type="hidden" name="endArticleIdInput" value="${articlePage.endArticleId}">
            <#list articlePage.articles as article>
                <div class="article">
                    <div class="article-vote-box votable" article-vote-box
                         data-article-id="${article.articleId}"
                         data-article-vote-count="${article.upVote}">
                        <span article-vote-count>${article.upVote}</span>
                        <a href="#" article-up-vote>
                            <div class="up-vote"></div>
                        </a>
                    </div>
                    <div class="article-body">
                        <div class="article-title">
                            <a href="${article.content}" target="_blank">${article.title}</a>
                            <span class="article-link-hint">(${article.linkHint})</span>
                        </div>
                        <div class="article-info">
                            <span>
                                <a href="/z/${zoneInfo.name}/debates/${article.articleId}">
                                ${article.debateCount} 則討論</a>
                            </span>
                            <span>
                                <a class="article-author"
                                   href="/u/${article.authorName}">/u/${article.authorName}</a>
                                張貼於 ${relativeTime(article.createTime)}
                            </span>
                        </div>
                    </div>
                </div>
            </#list>
        </div>
        <aside class="zone-aside">
        <#-- TODO enable when ajax permission load -->
            <a href="/z/${zoneInfo.name}/article/create"
               class="pure-button pure-button-primary create-article"
               create-article>建立文章 &gt; </a>
        </aside>
    </div>
</div>
</@template.page>
