<#import "/spring.ftl" as spring />
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#import "../macros/template.ftl" as template>

<#assign headContent>
<title>關於 ${account.username} | kaif.io</title>
<link rel="canonical" href="https://kaif.io<@url.account data=account/>"/>
</#assign>

<@template.page
layout='full'
head=headContent>

    <@template.user username=account.username>
    <div class="user-profile grid-center-row">
        <@util.time instant=account.createTime  maxUnit="Day" />
        <h2>關於 ${account.username}</h2>

        <div class="kmark">${account.renderDescription}</div>
        <div class="user-info-list">
            <div>
                文章分享 <span class="user-number">${accountStats.articleCount}</span> 篇
            </div>
            <div>
                爭論激辯 <span class="user-number">${accountStats.debateCount}</span> 回
            </div>
            <div>
                個人聲望 <span
                    class="user-number">${accountStats.honorScore}</span>
                點
            </div>
        </div>
    </div>
    </@template.user>
</@template.page>
