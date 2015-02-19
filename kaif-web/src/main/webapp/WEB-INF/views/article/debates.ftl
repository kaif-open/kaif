<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

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
    <#assign debateMenus>
        <@util.menuLink '/z/${zoneInfo.name}/debates/${article.articleId}' '討論'/>
    </#assign>

    <@template.zone data=zoneInfo menus=debateMenus>

    <div class="grid">

        <div class="grid-body">
            <div class="debate-tree" debate-tree>
                <#if parentDebate??>

                <#-- tree parent is Debate, we still create article component for dart,
                     but it is invisible to user -->
                    <@comp.article data=article hidden=true />
                    <@comp.debate data=parentDebate parentMode=true/>
                <#else>

                <#-- tree parent is Article -->
                    <@comp.article data=article />
                    <div class="grid-center-row debate-form-container">
                    <#-- place holder debate-form will replace by comp-template
                         keep place holder looks the same as comp-template
                         -->
                        <div class="pure-form" debate-form>
                            <textarea rows="3"></textarea>
                            <button type="submit" class="pure-button pure-button-primary">留言
                            </button>
                        </div>
                    </div>
                </#if>
                <#list debateTree.children as debateNode>
                    <@comp.debateNode data=debateNode />
                </#list>
            </div>
        </div>
        <aside class="grid-aside">
            Side bar
        </aside>
    </div>
    </@template.zone>

<#-- component templates -->
<form class="pure-form hidden" comp-template="debate-form">
    <input type="hidden" name="zoneInput" value="${article.zone}">
    <input type="hidden" name="articleInput" value="${article.articleId}">

    <div kmark-previewer class="hidden"></div>
    <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
    <button type="submit" class="pure-button pure-button-primary">留言</button>
    <button class="pure-button" kmark-preview>預覽</button>
</form>

<form class="pure-form hidden" comp-template="edit-debate-form">
    <div kmark-previewer class="hidden"></div>
    <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
    <button type="submit" class="pure-button pure-button-primary">修改</button>
    <button class="pure-button" kmark-preview>預覽</button>
</form>
</@template.page>
