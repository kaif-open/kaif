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
        <div class="pure-g article">
            <#list articles as article>
                <div class="pure-u-1-6 pure-u-md-1-12">
                    <div class="article-voting">
                        <a href="#"><i class="fa fa-chevron-up"></i></a>
                        <a href="#"><i class="fa fa-chevron-down"></i></a>
                    </div>
                </div>
                <div class="pure-u-5-6 pure-u-md-11-12">
                    <div class="article-title">
                        <a href="${article.content}" target="_blank">${article.title}
                            <span class="article-content">${article.content}</span>
                        </a>
                    </div>
                    <div class="article-info">
                        <span>
                           積分 ${article.upVote - article.downVote}
                        </span>
                        <span>
                            <a href="/z/${zoneInfo.name}/debates/${article.articleId}">
                                討論 (${article.debateCount})
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
