<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<#assign headContent>

<title>${article.title} | ${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description"
      content="${article.title} | ${zoneInfo.aliasName} ${zoneInfo.name} | kaif.io">

<link rel="stylesheet" href="/css/${zoneInfo.theme}.css?${(kaif.deployServerTime)!0}">

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

        <form class="pure-form" debate-form>
        <#-- this form has a copy in debate_tree.dart
             any change should review class DebateReplier
        -->
            <input type="hidden" name="zoneInput" value="${zoneInfo.zone}">
            <input type="hidden" name="articleInput" value="${article.articleId}">
        <#-- blank parent input, see debate_form.dart -->
            <input type="hidden" name="parentDebateIdInput" value="">
            <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
            <button type="submit" class="pure-button pure-button-primary">留言</button>
        </form>
        <div class="debate-tree" debate-tree>
            <input type="hidden" name="articleInput" value="${article.articleId}">
            <input type="hidden" name="zoneInput" value="${zoneInfo.zone}">
            <#list debates as debate>
                <div class="debate" style="margin-left: ${(debate.level-1) * 15}px;">
                    <div class="debate-author">
                    ${debate.debaterName}
                    </div>
                    <div class="debate-content">
                    ${debate.content}
                    </div>
                    <div class="debate-info">
                        <#if !debate.maxLevel>
                            <a href="#" debate-replier
                               data-debate-id="${debate.debateId}">回應</a>
                        </#if>
                    </div>
                </div>
            </#list>
        </div>

    </div>
</div>
</@template.page>
