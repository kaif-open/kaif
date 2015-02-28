<#import "/spring.ftl" as spring />
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<div class="grid">

    <div class="grid-body">
        <div class="debate-tree">
        <#list debates as debate>
            <@comp.debate data=debate parentMode=true/>
        </#list>
        </div>
        <aside class="grid-aside">
        </aside>
    </div>