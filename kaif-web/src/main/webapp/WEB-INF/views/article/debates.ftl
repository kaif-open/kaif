<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>

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
        <@template.menuLink '/z/${zoneInfo.name}/debates/${article.articleId}' '討論'/>
    </#assign>

    <@template.zone data=zoneInfo menus=debateMenus>

    <div class="debate-container">

        <div class="debate-tree" debate-tree>

            <@comp.article data=article />

            <input type="hidden" name="zoneInput" value="${zoneInfo.name}">
            <input type="hidden" name="articleIdInput" value="${article.articleId}">

            <div class="debate-form-container">

            <#-- place holder debate-form will replace by comp-template
             keep place holder looks the same as comp-template
            -->
                <div class="pure-form" debate-form>
                    <textarea rows="3"></textarea>
                    <button type="submit" class="pure-button pure-button-primary">留言</button>
                </div>

            </div>

            <#list debates as debate>
                <@comp.debate data=debate article=article/>
            </#list>
        </div>
        <aside class="debate-aside">
            Side bar
        </aside>
    </div>
    </@template.zone>

<#-- component templates -->
<form class="pure-form hidden" comp-template="debate-form">
    <input type="hidden" name="zoneInput" value="${article.zone}">
    <input type="hidden" name="articleInput" value="${article.articleId}">

    <div kmark-previewer hidden></div>
    <textarea name="contentInput" maxlength="4096" rows="3"></textarea>
    <button type="submit" class="pure-button pure-button-primary">留言</button>
    <button class="pure-button" kmark-preview>預覽</button>
</form>
</@template.page>
