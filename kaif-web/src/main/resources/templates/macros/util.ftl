<#import "url.ftl" as url />

<#--
 menuLink
-->
<#macro menuLink href name>
<#-- here we have a tricky hack
     the landing page is `/z/programm/article/create`
     but part template is `/z/programm/article/create.part`
     if `create.part` use menuLink, it won't work because the url is not same as
     landing page
     thus we compare both url
  -->
    <#local selected = url.isCurrentPath(href, true) />
<li class="${selected?string('pure-menu-selected','')}"><a href="${href}">${name}</a></li>
</#macro>

<#function abbreviate str max>
    <#if str?? >
        <#if str?length <= max>
            <#return str />
        <#else>
            <#return str?substring(0, max)  + '...'/>
        </#if>
    <#else >
        <#return ''/>
    </#if>
</#function>

<#--
 show relative time of Instant. the output is wrapped by html5 time tag
 -->
<#macro time instant maxUnit=""><#compress>
<time datetime="${instant}">
    <#if maxUnit?has_content>
        ${relativeTime(instant, maxUnit)}
    <#else>
    ${relativeTime(instant)}
    </#if>
</time>
</#compress>
</#macro>

<#macro bookmarklet>
<div class="bookmarklet">
    <h3>貼文小幫手: Bookmarklet</h3>

    <p>你可以使用 Bookmarket 一鍵完成分享文章！</p>

    <p>安裝：將下面的連結拖拉瀏覽器的書簽列</p>

    <div class="bookmarklet-container">
        <i class="fa fa-caret-right"></i>
        <a class="bookmarklet-link" rel="nofollow"
           href="javascript:window.location=%22https://kaif.io/article/create-link?c=%22+encodeURIComponent(document.location)+%22&t=%22+encodeURIComponent(document.title)">
            分享到 Kaif
        </a>
        <i class="fa fa-caret-left"></i>
    </div>
    <p>以後看到你覺得很棒的網頁，直接點書簽列上的 <strong style="white-space: nowrap">[ 分享到 Kaif ]</strong> 鈕即可。</p>
</div>
</#macro>