<#import "/spring.ftl" as spring />

<#--

  kaif.appBuild
     from app.build in WebConfiguration.java
        from application.yml
           from application-prod.yml in production
              from gradle build replace token @app.build@

  kaif.appBuild by default is `snapshot`
  -->
<#macro dynamicRes>/${kaif.appBuild}</#macro>

<#--
  return current request path (without exist query string)

  for example:

    given current page at /z/programming?abc=def
    the result of <@url current> is:

       /z/programming

  if you add arguments, it will append to current path (exist query string ignored)

    given current page at /z/programming?abc=def
    the result of <@url current start=123 end=456> is:

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
