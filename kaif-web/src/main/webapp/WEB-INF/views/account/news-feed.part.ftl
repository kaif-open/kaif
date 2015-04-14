<#import "/spring.ftl" as spring />
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>


<div class="news-feed" news-feed debate-list data-first-page="${isFirstPage?string}">
<#list newsFeed.feedAssets as asset>
    <#if asset.assetType.debate>
    <div class="feed-asset" feed-asset data-asset-id="${asset.assetId}"
         data-asset-acked="${asset.acked?string}">
        <#assign debate=newsFeed.getDebate(asset)/>
        <@comp.debateStandAlone data=debate article=newsFeed.getArticle(debate) />
    <#else>
        TODO other asset
    </#if>
</div>
</#list>

    <div class="news-feed-pager grid-center-row">
    <#if newsFeed.feedAssets?size == 0>
        <p>沒有消息了</p>
    <#else>
        <a href="#" ajax-pager class="pure-button">
            <i class="fa fa-caret-right"></i> 下一頁
        </a>
    </#if>
    </div>
</div>