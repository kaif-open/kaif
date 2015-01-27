<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page {
'layout': 'small'
}>

<div class="alert alert-danger">
    <@spring.messageText "part-loader.permission-error" "permission error" />
    <p>
        <a class="pure-button" href="/"> &gt; Home</a>
    </p>
</div>
</@template.page>