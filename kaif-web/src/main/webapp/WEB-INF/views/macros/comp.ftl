<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />

<#macro article data>

    <#local article=data />

<div class="article" article data-article-id="${article.articleId}" data-zone="${article.zone}">
    <div class="article-vote-box votable" article-vote-box
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

<#macro debate data article>
    <#local debate=data />
<#-- TODO adjust margin-left -->
<div class="debate" style="margin-left: ${(debate.level-1) * 30}px;"
     debate
     data-debate-id="${debate.debateId}"
     data-debater-name="${debate.debaterName}">
    <div class="debate-vote-box votable" debate-vote-box

         data-debate-vote-count="${debate.totalVote}">
        <a href="#" debate-up-vote>
            <div class="up-vote"></div>
        </a>
        <a href="#" debate-down-vote>
            <div class="down-vote"></div>
        </a>
    </div>
    <div class="debate-body">
        <div class="debate-title">
            <a class="debate-author"
               href="/u/${article.authorName}">${debate.debaterName}</a>
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
                <a href="#" debate-editor
                   data-debate-id="${debate.debateId}" class="hidden">編輯</a>
            </#if>
            <#if debate.hasParent()>
                <a>parent</a>
            </#if>
            <span>${relativeTime(debate.createTime)}</span>
        </div>
    </div>
</div>
</#macro>

<#macro articleList data>
    <#local articlePage=data />
<div class="article-list" article-list>
    <#list articlePage.articles as article>
        <@comp.article data=article />
    </#list>
    <div class="article-list-pager convex-row">
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