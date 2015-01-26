<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>

<@template.page {
  'layout':'small'
}>

    <#if success >
       <p class="alert alert-info">
         帳號啟用成功！
       </p>
       <a class="pure-button pure-button-primary" href="/account/sign-in"> &gt; 馬上登入</a>
    <#else >
       <p class="alert alert-danger">
         啟用連結已經失效，如果你的帳戶尚未啟用，請重新再送一次啟用信。
       </p>
    </#if>

</@template.page>
