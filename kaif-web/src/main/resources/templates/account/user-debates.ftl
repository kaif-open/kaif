<#import "/spring.ftl" as spring />
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/template.ftl" as template>

<#assign headContent>
<title>${username} 參與的討論 | kaif.io</title>
</#assign>

<@template.page
layout='full'
head=headContent>
    <@template.user username=username>
        <@comp.debateList data=debateList />
        <@comp.debateForm />
    </@template.user>
</@template.page>
