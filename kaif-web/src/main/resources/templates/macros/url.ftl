<#import "/spring.ftl" as spring />

<#--
  return current request path (without exist query string)

  for example:

    given current page at /z/programming?abc=def
    the result of <@url.current> is:

       /z/programming

  if you add arguments, it will append to current path (exist query string ignored)

    given current page at /z/programming?abc=def
    the result of <@url.current start=123 end=456> is:

       /z/programming?start=123&end=456

-->
<#macro current params...><#compress>
    <#assign currentUrl = springMacroRequestContext.getRequestUri() />
    <#if (params?size > 0) >
        <#assign currentUrl = currentUrl + '?' />
        <#list params?keys as name>
            <#assign currentUrl = currentUrl + name + '=' + params[name]/>
            <#if name_has_next>
                <#assign currentUrl = currentUrl + '&'/>
            </#if>
        </#list>
    ${currentUrl}
    <#else>
    ${currentUrl}
    </#if>
</#compress></#macro>

<#-- check if current url match input

     includePart=true will treat current path `/foo/bar.part` same as target path `/foo/bar`
  -->
<#function isCurrentPath targetPath includePart=false matchEndSlash=true>
    <#local currentPath = springMacroRequestContext.getRequestUri() />
    <#return currentPath == targetPath
    || (matchEndSlash && (currentPath == targetPath + '/'))
    || (includePart && currentPath == targetPath + '.part') />
</#function>

<#-- zone url,
     data can be ZoneInfo, Article, or Debate -->
<#macro zone data>/z/${data.zone}</#macro>

<#-- article's debate url,
     data can be Article or Debate -->
<#macro article data>/z/${data.zone}/debates/${data.articleId}</#macro>

<#-- debate's permanent url,
     data is Debate -->
<#macro debate data parent=false><#compress>
    <#if parent>
    /z/${data.zone}/debates/${data.articleId}/${data.parentDebateId}
    <#else>
    /z/${data.zone}/debates/${data.articleId}/${data.debateId}
    </#if>
</#compress>
</#macro>

<#-- account's public url, data can be
     Account, Debate (debaterName), Article (article author name) -->
<#macro account data>
    <#compress>
        <#if (data.debaterName)??>
        /u/${data.debaterName}
        <#elseif (data.authorName)??>
        /u/${data.authorName}
        <#else>
        /u/${data.username}
        </#if>
    </#compress>
</#macro>

<#-- implicit variable zoneInfo -->
<#macro createArticleLink><#compress>
    <#if zoneInfo??>
        <#assign prefix='/z/${zoneInfo.name}' />
    <#else>
        <#assign prefix='' />
    </#if>
${prefix}/article/create-link
</#compress>
</#macro>

<#-- implicit variable zoneInfo -->
<#macro createArticleSpeak><#compress>
    <#if zoneInfo??>
        <#assign prefix='/z/${zoneInfo.name}' />
    <#else>
        <#assign prefix='' />
    </#if>
${prefix}/article/create-speak
</#compress>
</#macro>