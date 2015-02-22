<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />
<#import "util.ftl" as util/>

<#macro createArticle zoneInfo>
<div class="aside-card">
    <h4>張貼新文</h4>

    <div class="aside-card-box">
        <p>
            好文難尋，知音難遇。分享你發掘到的好文吧！
        </p>
        <a href="/z/${zoneInfo.name}/article/create-link"
           class="pure-button pure-button-primary create-article">
            <i class="fa fa-caret-right"></i> 分享新連結</a>
    </div>
    <div class="aside-card-box">
        <p>
            分享你的想法、提出你的質疑、說出你的困惑！
        </p>
        <a href="/z/${zoneInfo.name}/article/create-speak"
           class="pure-button pure-button-primary create-article">
            <i class="fa fa-caret-right"></i> 我有話要說</a>
    </div>
</div>
</#macro>

<#macro recommendZones zoneInfos>
<div class="aside-card aside-zone-list">
    <h4>熱門討論區</h4>

    <div class="aside-card-box">
        <ul>
            <#list zoneInfos as zoneInfo>
                <li>
                    <a class="plain" href="<@url.zone data=zoneInfo/>">
                        <@url.zone data=zoneInfo/> - ${zoneInfo.aliasName}
                    </a>
                </li>
            </#list>
        </ul>
    </div>
</div>
</#macro>