<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />
<#import "util.ftl" as util/>

<#-- implicit variable zoneInfo -->
<#macro createArticle>
<div class="aside-card">
    <h4>張貼新文</h4>

    <div class="aside-card-box">
        <p>
            好文難尋，知音難遇。分享你發掘到的好文吧！
        </p>
        <a href="<@url.createArticleLink />"
           class="pure-button pure-button-primary create-article">
            <i class="fa fa-caret-right"></i> 分享新連結</a>
    </div>
    <div class="aside-card-box">
        <p>
            分享你的想法、提出你的質疑、說出你的困惑！
        </p>
        <a href="<@url.createArticleSpeak />"
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

<#-- data can be Debate or Article -->
<#macro shortUrl data>
<div class="aside-card aside-short-url">
    <h4>短網址</h4>

    <div class="aside-card-box">
        <input short-url-input type="text" value="https://kaif.io${data.shortUrlPath}">
    </div>
</div>
</#macro>

<#macro search>
<div class="aside-card">
    <h4>站內搜尋</h4>

<#-- google search input style is stranged, try to fix it, we add min-height
     in card-box to prevent size bounce when page loaded -->
    <div class="aside-card-box" style="min-height: 71px;">
        <style>
            .gsc-input-box {
                min-height: 34px;
            }

            input.gsc-search-button-v2 {
                width: auto;
                height: 32px;
                margin-top: 8px;
            }
            form.gsc-search-box {
                margin-bottom: 0;
            }
        </style>
        <script>
            (function () {
                var cx = '005595287635600419689:mkvbjm66l84';
                var gcse = document.createElement('script');
                gcse.type = 'text/javascript';
                gcse.async = true;
                gcse.src = (document.location.protocol == 'https:' ? 'https:' : 'http:') +
                '//www.google.com/cse/cse.js?cx=' + cx;
                var s = document.getElementsByTagName('script')[0];
                s.parentNode.insertBefore(gcse, s);
            })();
        </script>
        <gcse:searchbox-only></gcse:searchbox-only>
    </div>
</div>

</#macro>