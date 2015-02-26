<#import "/spring.ftl" as spring />
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#import "../macros/template.ftl" as template>

<#assign headContent>
<title>User ${account.username} | Kaif</title>
<link rel="canonical" href="https://kaif.io/<@url.account data=account/>"/>
</#assign>

<@template.page
layout='small'
head=headContent>

<div>
    <table class="user-infos">
        <tr>
            <th>帳號</th>
            <td>${account.username}</td>
        </tr>
        <tr>
            <th>註冊於</th>
            <td><@util.time instant=account.createTime  maxUnit="Day" /></td>
        </tr>
        <tr>
            <th>留言</th>
            <td>${accountStats.debateCount}</td>
        </tr>
        <tr>
            <th>發文</th>
            <td>${accountStats.articleCount}</td>
        </tr>
        <tr>
            <th>積分</th>
            <td>${accountStats.debateUpVoted - accountStats.debateDownVoted}</td>
        </tr>
        <tr>
            <th>關於我</th>
            <td>
                <div class="kmark about-me">${account.renderDescription}</div>
            </td>
        </tr>
    </table>
</div>


</@template.page>
