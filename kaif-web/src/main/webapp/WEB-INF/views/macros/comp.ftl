<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />

<#macro article data>

    <#local article=data />

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
<div class="debate" debate style="margin-left: ${(debate.level-1) * 30}px;">
    <div class="debate-vote-box votable" debate-vote-box
         data-debate-id="${debate.debateId}"
         data-debate-vote-count="${debate.totalVote}">
        <span debate-vote-count>${debate.totalVote}</span>
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
            發表於 ${relativeTime(debate.createTime)}
        </div>
        <div class="debate-content">
            <div class="kmark">
            ${debate.renderContent}
            </div>
        </div>
        <div class="debate-info">
            <#if !debate.maxLevel>
                <a href="#" debate-replier
                   data-debate-id="${debate.debateId}">回應</a>
            </#if>
            <#if debate.hasParent()>
                <a>parent</a>
            </#if>
        </div>
    </div>
</div>
</#macro>