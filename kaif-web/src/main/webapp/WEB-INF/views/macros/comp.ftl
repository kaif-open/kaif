<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />

<#-- Article components -->

<#macro articleList data>
    <#local articlePage=data />
<div class="article-list" article-list>
    <#list articlePage.articles as article>
        <@comp.article data=article />
    </#list>
    <div class="article-list-pager grid-center-row">
        <#if articlePage.articles?size == 0 >
            沒有文章
        </#if>
        <#if articlePage.hasNext()>
            <a href="<@url.current start=articlePage.lastArticleId />"
               class="pure-button"><i class="fa fa-caret-right"></i> 下一頁</a>
        </#if>
    </div>
</div>
</#macro>

<#macro article data hidden=false>

    <#local article=data />
    <#local hiddenCls=hidden?string("hidden", "") />

<div class="article grid-row ${hiddenCls}" article data-article-id="${article.articleId}"
     data-zone="${article.zone}">
    <div class="article-vote-box votable grid-convex" article-vote-box
         data-article-vote-count="${article.upVote}">
        <span article-vote-count>${article.upVote}</span>
        <a href="#" article-up-vote>
            <div class="up-vote"></div>
        </a>
    </div>
    <div class="grid-center">
        <div class="article-title">
            <a href="${article.content}" target="_blank">${article.title}</a>
            <span class="article-link-hint">(${article.linkHint})</span>
        </div>
        <div class="article-info">
            <span>
                <a href="/z/${article.zone}/debates/${article.articleId}">
                ${article.debateCount} 則討論</a>
            </span>
            <span>
                <a class="article-author"
                   href="/u/${article.authorName}">${article.authorName}</a>
                張貼於 ${relativeTime(article.createTime)}
            </span>
        </div>
    </div>
</div>

</#macro>

<#-- Debate components -->

<#macro debateNode data smallConvex=false >
    <#local theDebateNode=data />
    <@debate data=theDebateNode.value smallConvex=smallConvex>
        <#list theDebateNode.children as child>
        <#-- recursive macro -->
            <@comp.debateNode data=child smallConvex=true />
        </#list>
    </@debate>
</#macro>

<#-- if parentMode set to true, it means this debate is top of the page
     it will handle `parent` link differently. (also show hint)
  -->
<#macro debate data smallConvex=false parentMode=false>

    <#local debate=data />
    <#local gridConvex=smallConvex?string("grid-sm-convex", "grid-convex") />

<div id="debate-${debate.debateId}" class="debate grid-row"
     debate
     data-debate-id="${debate.debateId}"
     data-debater-name="${debate.debaterName}">
    <div class="debate-vote-box votable ${gridConvex}" debate-vote-box
         data-debate-vote-count="${debate.totalVote}">
        <a href="#" debate-up-vote>
            <div class="up-vote"></div><#--
         This freemarker comment is required, for remove small spacing between triangle
        --></a><br>
        <a href="#" debate-down-vote>
            <div class="down-vote"></div>
        </a>
    </div>
    <div class="grid-center">
        <div class="debate-title">
            <a class="debate-author"
               href="/u/${debate.debaterName}">${debate.debaterName}</a>
            積分
            ( <span debate-vote-count>${debate.totalVote}</span> )
        </div>
        <div class="debate-content">
            <div class="kmark" debate-content>
            ${debate.renderContent}
            </div>
            <div debate-content-edit class="hidden"></div>
        </div>
        <div class="debate-info">
            <#if !debate.maxLevel>
                <a href="#" debate-replier
                   data-debate-id="${debate.debateId}">回應</a>
            </#if>
            <#if debate.hasParent() && !parentMode >
                <a href="#debate-${debate.parentDebateId}">上層</a>
            </#if>
        <#--
          permenant link is sub debate tree only, we don't want google index it.
          so rel="nofollow"
          -->
            <a href="/z/${debate.zone}/debates/${debate.articleId}/${debate.debateId}"
               rel="nofollow">${relativeTime(debate.createTime)}</a>

            <a href="#" debate-editor data-debate-id="${debate.debateId}" class="hidden">編輯</a>
        </div>
        <div class="debate-child">
        <#-- child debate here -->
            <#nested/>
        </div>
    </div>
</div>

    <#if parentMode>
    <div class="grid-center-row child-debate-hint">
        <div class="alert alert-info">
            這是文章的子討論串，你可以回到上層查看所有討論和文章
            <div>
                <a href="/z/${debate.zone}/debates/${debate.articleId}">回上層</a>
            </div>
        </div>
    </div>
    </#if>
</#macro>
