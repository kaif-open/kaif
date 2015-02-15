<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>
<#import "macros/comp.ftl" as comp>
<#import "macros/url.ftl" as url>

<#assign headContent>

<title>kaif.io - 討論區列表</title>
<meta name="討論區按字母排序" content="kaif.io">

</#assign>

<@template.page
config={
'layout':'full'
}
head=headContent
>

    <@template.home>
    <div class="home-body">
        <#list zoneAtoZ?keys as cat>
        ${cat}
            <#list zoneAtoZ[cat] as zoneInfo>
            ${zoneInfo.zone}
            </#list>
        </#list>
        <aside class="home-aside">
            side bar
        </aside>
    </div>
    </@template.home>

</@template.page>
