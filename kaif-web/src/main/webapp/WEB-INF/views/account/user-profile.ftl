<#import "/spring.ftl" as spring />
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#import "../macros/template.ftl" as template>

<#assign headContent>
<title>User ${account.username} - Kaif</title>
<link rel="canonical" href="http://kaif.io/<@url.account data=account/>"/>
</#assign>

<@template.page
layout='small'
head=headContent>

<div>
${account.username}
</div>
<div>
    <ul>
        <li>註冊日：${account.createTime?datetime.iso?string.medium}</li>
        <li>留言：${accountStats.debateCount}</li>
        <li>發文：${accountStats.articleCount}</li>
        <li>積分：${accountStats.debateUpVoted - accountStats.debateDownVoted}</li>

    </ul>

</div>


</@template.page>
