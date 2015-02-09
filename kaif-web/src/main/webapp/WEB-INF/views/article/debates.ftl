<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>

<#assign headContent>

<title>${article.title} | ${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description"
      content="${article.title} | ${zoneInfo.aliasName} ${zoneInfo.name} | kaif.io">

<link rel="stylesheet" href="<@url.dynamicRes/>/css/${zoneInfo.theme}.css">

</#assign>

<@template.page
config={
'layout':'full'
}
head=headContent
>

<div class="zone ${zoneInfo.theme} pure-g article">
    <div class="pure-u-1 pure-u-md-3-4">
    ${article.title}
        <div class="debate-tree" debate-tree>
            <input type="hidden" name="zoneInput" value="${zoneInfo.name}">
            <input type="hidden" name="articleIdInput" value="${article.articleId}">
        <#-- place holder debate-form will replace by comp-template
         keep place holder looks the same as comp-template
        -->
            <div class="pure-form" debate-form>
                <textarea rows="3"></textarea>
                <button type="submit" class="pure-button pure-button-primary">留言</button>
            </div>
            <#list debates as debate>
                <div class="debate" style="margin-left: ${(debate.level-1) * 20}px;"
                     debate>
                    <div>
                        <span class="debate-author">
                        ${debate.debaterName}
                        </span>
                        <span class="debate-vote-box" debate-vote-box
                              data-debate-id="${debate.debateId}"
                              data-debate-vote-count="${debate.totalVote}">
                            <a href="#" debate-up-vote><i class="fa fa-chevron-up"></i></a>
                            <a href="#" debate-down-vote><i class="fa fa-chevron-down"></i></a>
                            <span debate-vote-count>${debate.totalVote}</span>
                        </span>
                    </div>
                    <div class="debate-content">
                    ${debate.renderContent}
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
            </#list>
        </div>

    </div>
</div>

<#-- component templates -->
<form class="pure-form hidden" comp-template="debate-form">
    <input type="hidden" name="zoneInput" value="${article.zone}">
    <input type="hidden" name="articleInput" value="${article.articleId}">
    <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
    <button type="submit" class="pure-button pure-button-primary">留言</button>
</form>
</@template.page>
