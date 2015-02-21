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

     TODO menuLink in part template should use dart to render and highlight
  -->
    <#local selected = url.isCurrentPath(href, true) />
<li class="${selected?string('pure-menu-selected','')}"><a href="${href}">${name}</a></li>
</#macro>

<#--
 show relative time of Instant. the output is wrapped by html5 time tag
 -->
<#macro time instant><#compress>
<time datetime="${instant}">${relativeTime(instant)}</time>
</#compress>
</#macro>