<#import "/spring.ftl" as spring />
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>


<div class="debate-list">
<#list newsFeed.feedAssets as asset>
    <#if asset.assetType.debate>
        <#assign debateItem=newsFeed.getDebate(asset)/>

    <#-- TODO extra macro -->
        <div class="debate-standalone">
            <@comp.debate data=debateItem editable=false />
            <div class="grid-row">
                <div class="grid-center-row debate-navigation">
                    <a href="<@url.article data=debateItem />${'#debate-'+debateItem.debateId}">
                        <i class="fa fa-caret-right"></i>
                    ${util.abbreviate(newsFeed.getArticle(debateItem).title, 20)}
                    </a>
                    <a href="<@url.zone data=debateItem />">
                        <i class="fa fa-caret-right"></i>
                        <@url.zone data=debateItem />
                    </a>
                </div>
            </div>
        </div>
    <#else>
        TODO other asset
    </#if>

</#list>

    <div class="news-feed-pager grid-center-row">
    <#if newsFeed.feedAssets?size == 0>
        <p>沒有消息了</p>
    <#else>
        <a href="#" news-feed-pager class="pure-button">
            <i class="fa fa-caret-right"></i> 下一頁
        </a>
    </#if>
    </div>
    <div next-news-feed></div>
