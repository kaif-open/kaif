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

<div class="zone ${zoneInfo.theme} pure-g">
    <div class="pure-u-1 pure-u-md-3-4">
        <ul class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
            <a class="pure-menu-heading">${zoneInfo.aliasName}</a>
            <@template.menuLink '/z/${zoneInfo.name}' '熱門'/>
            <@template.menuLink '/z/${zoneInfo.name}/new' '最新'/>
        </ul>
    </div>
    <div class="pure-u-1 pure-u-md-1-4">
    <#-- TODO enable when ajax permission load -->
        <a href="/z/${zoneInfo.name}/article/create" class="pure-button pure-button-primary"
           create-article>建立文章 &gt; </a>
    </div>
    <div class="pure-u-1 pure-u-md-3-4">
        <div class="pure-g article-list" article-list>
            <input type="hidden" name="zoneInput" value="${zoneInfo.name}">
            <input type="hidden" name="startArticleIdInput" value="${articlePage.startArticleId}">
            <input type="hidden" name="endArticleIdInput" value="${articlePage.endArticleId}">
            <#list articlePage.articles as article>
                <div class="pure-u-1-6 pure-u-md-1-12">
                    <div class="article-vote-box" article-vote-box
                         data-article-id="${article.articleId}"
                         data-article-vote-count="${article.upVote}">
                        <span article-vote-count>${article.upVote}</span>
                        <a href="#" article-up-vote><i class="fa fa-chevron-up"></i></a>
                    </div>
                </div>
                <div class="pure-u-5-6 pure-u-md-11-12">
                    <div class="article-title">
                        <a href="${article.content}" target="_blank">${article.title}
                            <span class="article-content">(${article.content})</span>
                        </a>
                    </div>
                    <div class="article-info">
                        <span>
                            <a href="/z/${zoneInfo.name}/debates/${article.articleId}">
                                <i class="fa fa-comment"></i> ${article.debateCount} 則討論
                            </a>
                        </span>
                        <span>
                            <a class="article-author"
                               href="/u/${article.authorName}">${article.authorName}</a>
                            張貼於 ${article.createTime}
                        </span>
                    </div>
                </div>
            </#list>
        </div>
    </div>
</div>
</@template.page>
