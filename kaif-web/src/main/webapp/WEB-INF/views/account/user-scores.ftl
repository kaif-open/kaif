<#import "/spring.ftl" as spring />
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/template.ftl" as template>

<#assign headContent>
<title>${username} 的積分| kaif.io</title>
</#assign>

<@template.page
layout='full'
head=headContent>
    <@template.user username=username>
    <div class="honor-roll-list grid-center-row">
        <#list honorRolls as honorRoll>
            <h3 class="honor-roll-title">
                <a href="<@url.zone data=honorRoll/>">
                    <i class="fa fa-caret-right"></i>/z/${honorRoll.zone}
                </a>
            </h3>

            <div class="pure-g honor-roll-formula">
                <div class="pure-u-3-24 honor-positive-score"><p>
                ${honorRoll.articleUpVoted+honorRoll.debateUpVoted} <span class="up-vote"></span>
                </p>
                </div>
                <div class="pure-u-3-24"><p>-</p></div>
                <div class="pure-u-3-24 honor-negative-score"><p>${honorRoll.debateUpVoted} <span
                        class="down-vote"></span></p></div>
                <div class="pure-u-3-24"><p>=</p></div>
                <div class="pure-u-3-24">
                    <#if honorRoll.score gt 0>
                        <p class="honor-positive-score">${honorRoll.score}</p>
                    <#elseif honorRoll.score lt 0>
                        <p class="honor-negative-score">${honorRoll.score}</p>
                    <#else>
                        <p class="honor-score">${honorRoll.score} </p>
                    </#if>
                </div>
            </div>
        </#list>
    </div>
    </@template.user>
</@template.page>
