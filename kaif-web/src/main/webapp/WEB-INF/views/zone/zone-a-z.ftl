<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/aside.ftl" as aside>
<#import "../macros/url.ftl" as url>

<#assign headContent>

<title>討論區列表 | kaif.io</title>
<meta name="description" content="討論區按字母排序">

</#assign>

<@template.page
layout='full'
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
                                        <a class="plain"
                                           href="<@url.zone data=zoneInfo/>">/z/${zoneInfo.zone}</a>
                                    </td>
                                    <td>
                                    ${zoneInfo.aliasName}
                                    </td>
                                </tr>
                            </#list>
                        </table>
                    </section>
                </#list>
                <p></p>
            </div>
        </div>
        <aside class="grid-aside">
            <@aside.createZone />
        </aside>
    </div>

    </@template.home>

</@template.page>
