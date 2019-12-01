<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<#assign headContent>
<title>開發者</title>
</#assign>

<@template.page layout='full' head=headContent>

<nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
    <@util.menuLink '/developer/doc' '開發者文件'/>
    <@util.menuLink '/developer/client-app' '管理應用程式'/>
</nav>
<div class="grid">
    <div class="grid-body">
        <div id="__part_template"></div>
    </div>
</div>
</@template.page>
