<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<#assign headContent>
<title>News Feed | kaif.io</title>
</#assign>

<@template.page layout='full' head=headContent>

<nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
    <@util.menuLink '/account/news-feed' '新消息'/>
    <@util.menuLink '/account/debate-replies' '最近回應給我'/>
</nav>

<div class="grid">
    <div class="grid-body">
    <#-- dart will search #__part_template and set innerHtml with part
       check ServerPartLoader in main.dart
        -->
        <div id="__part_template"></div>
    </div>
    <aside class="grid-aside">
    </aside>
</div>
    <@comp.debateForm/>
</@template.page>
