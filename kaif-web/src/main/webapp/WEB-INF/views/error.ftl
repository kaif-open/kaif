<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<#assign headContent>
<title>Error | kaif.io</title>
</#assign>

<@template.page layout='small' head=headContent errorPage=true >

<#-- TODO show stacktrace in dev mode
     TODO beautify with a little error detail
     TODO error page should not use template or external css/font/js,
         everything should inline (css, js...etc)
  -->
    <#assign resStatus = (Request['status'])!500 />
    <#if resStatus == 404>
    <div style="text-align: center">
        <h1>404 Not Found 查無此頁</h1>
    </div>

    <style>body {
        margin: 0
    }</style>
    <iframe src="http://404page.missingkids.org.tw/api?key=zhhw-kiPRaLa4QEEDdeK" width="100%"
            height="635" frameborder="0"></iframe>
    <#else>
    <h1>
        Error ${resStatus}
    </h1>
    </#if>
</@template.page>
