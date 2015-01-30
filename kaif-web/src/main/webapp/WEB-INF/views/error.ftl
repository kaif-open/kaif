<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page {
'layout':'small',
'errorPage': true
}>

<h1>
<#-- TODO show stacktrace in dev mode
     TODO beautify with a little error detail
  -->
    <#assign resStatus = (Request['status'])!500 />
    <#if resStatus == 404>
        404 Not Found
    <#else>
        Error ${resStatus}
    </#if>
</h1>
</@template.page>
