<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />
<#import "util.ftl" as util/>

<#-- Article components -->

<#macro articleList data showZone=false>
    <#local articlePage=data />
<div class="article-list" article-list>
    <#list articlePage.articles as article>
        <@comp.article data=article showZone=showZone/>
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

<#macro article data hidden=false parentMode=false showZone=false>

    <#local article=data />
    <#local hiddenCls=hidden?string("hidden", "") />

<div class="article grid-row ${hiddenCls}" article data-article-id="${article.articleId}"
     data-zone="${article.zone}">
    <div class="article-vote-box votable grid-convex" article-vote-box
         data-article-vote-count="${article.upVote}">
        <span class="vote-count" article-vote-count>${article.upVote}</span>
        <a href="#" article-up-vote>
            <div class="up-vote"></div>
        </a>
    </div>
    <div class="grid-center">
        <div class="article-title">
        <#-- TODO, not to use article content as url directly -->
            <#if article.externalLink>
                <a href="${article.link}" target="_blank">${article.title}</a>
            <#else>
                <a href="<@url.article data=article />">${article.title}</a>
            </#if>
            <span class="article-link-hint">
                <#if article.externalLink>
                    (${article.linkHint})
                <#else>
                    (<span class="speak-hint">${article.linkHint}</span>)
                </#if>
            </span>
        </div>
        <#if parentMode && article.hasMarkDownContent() >
            <div class="article-content">
                <article class="kmark">
                ${article.renderContent}
                </article>
            </div>
        </#if>
        <div class="article-info">
            <span>
                <#if parentMode>
                ${article.debateCount} 則討論
                <#else>
                    <a href="<@url.article data=article/>"><i
                            class="fa fa-caret-right"></i> ${article.debateCount} 則討論</a>
                </#if>
            </span>
            <span>
                <a class="article-author"
                   href="<@url.account data=article/>">${article.authorName}</a>
                張貼於 <@util.time instant=article.createTime />
            </span>
            <#if showZone>
                <a href="<@url.zone data=article/>">
                    <i class="fa fa-caret-right"></i> ${article.aliasName}
                </a>
            </#if>
        </div>
    </div>
</div>

</#macro>

<#-- Debate components -->

<#macro debateNode data >
    <#local theDebateNode=data />
    <@debate data=theDebateNode.value >
        <#list theDebateNode.children as child>
        <#-- recursive macro -->
            <@comp.debateNode data=child />
        </#list>
    </@debate>
</#macro>

<#-- if parentMode set to true, it means this debate is top of the page
     it will handle `parent` link differently. (also show hint)
  -->
<#macro debate data parentMode=false>

    <#local debate=data />

<div id="debate-${debate.debateId}" class="debate grid-row"
     debate
     data-article-id="${debate.articleId}"
     data-zone="${debate.zone}"
     data-debate-id="${debate.debateId}"
     data-debater-name="${debate.debaterName}">
    <div class="debate-vote-box votable grid-convex" debate-vote-box
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
               href="<@url.account data=debate/>">${debate.debaterName}</a>
            <span>
                積分 <span class="vote-count" debate-vote-count>${debate.totalVote}</span>
            </span>
            <#if debate.edited>
                <span class="debate-edited">編輯於 <@util.time instant=debate.lastUpdateTime /></span>
            </#if>
        </div>
        <div class="debate-content">
            <div class="kmark" debate-content>
            ${debate.renderContent}
            </div>
            <div debate-content-edit class="hidden"></div>
        </div>
        <div class="debate-action">
            <#if !debate.maxLevel>
                <a href="#" debate-replier
                   data-debate-id="${debate.debateId}">回應</a>
            </#if>
        <#--
          permenant link is sub debate tree only, we don't want google index it.
          so rel="nofollow"
          -->
            <a href="<@url.debate data=debate/>" title="永久連結"
               rel="nofollow"><@util.time instant=debate.createTime /></a>
            <a href="#" debate-content-editor data-debate-id="${debate.debateId}"
               class="hidden">編輯</a>
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
                <a href="<@url.article data=debate/>"
                   class="pure-button button-sm button-info"><i class="fa fa-caret-up"></i> 回上層</a>
            </div>
        </div>
    </div>
    </#if>
</#macro>

<#macro debateForm>
<form class="pure-form hidden debate-form" comp-template="debate-form">
    <div>
        <div kmark-previewer class="hidden kmark kmark-preview"></div>
        <textarea name="contentInput"
                  class="pure-input-1 kmark-input"
                  maxlength="4096"
                  rows="3"></textarea>
    </div>
    <div class="form-action-bar">
        <button type="submit" class="pure-button pure-button-primary">留言</button>
        <button class="pure-button" kmark-debate-cancel>取消</button>
        <button class="pure-button"
                kmark-preview><@spring.messageText "kmark.preview" "Preview" /></button>
    </div>
</form>
</#macro>