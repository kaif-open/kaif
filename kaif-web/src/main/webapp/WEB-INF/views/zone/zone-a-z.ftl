<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/url.ftl" as url>

<#assign headContent>

<title>kaif.io - 討論區列表</title>
<meta name="討論區按字母排序" content="kaif.io">

</#assign>

<@template.page
config={
'layout':'full'
}
head=headContent>

    <@template.home>
    <div class="grid">
        <div class="grid-body">
            <div class="grid-center-row zone-list">
                <#list zoneAtoZ?keys as cat>
                    <section>
                        <h3 class="zone-category">
                        ${cat}
                        </h3>
                    <table class="zone-table">
                        <#list zoneAtoZ[cat] as zoneInfo>
                            <tr>
                                <td class="zone-name">
                                    <a href="/z/${zoneInfo.zone}">/z/${zoneInfo.zone}</a>
                                </td>
                                <td>
                                ${zoneInfo.aliasName}
                                </td>
                            </tr>
                        </table>
                        </#list>
                    </section>
                </#list>
            </div>
        </div>
        <aside class="grid-aside">
            side bar
        </aside>
    </div>

    </@template.home>

</@template.page>
