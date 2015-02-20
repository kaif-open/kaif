<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />
<#import "util.ftl" as util/>
<#--
sample configs:

1)

<@template.page layout='small'>

2)

// if you use `head` you must define <title>
<#assign head>
    <title>foo zone</title>
    <meta name="description" content="foo zone is best">
</#assign>
<@template.page
    layout='full'
    head=headContent>

3) for error page

<@template.page layout='full' errorPage=true >

-->
<#macro page layout head="" applyZoneTheme=false errorPage=false>

<!doctype html>
<html lang="zh-tw">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

<#-- meta data for dart, see ServerType for detail -->
    <#if kaif.profilesActive?contains('dev')>

    <#-- for detect dev mode only, should not leak information to produciton -->
        <meta name="kaifProfilesActive" content="${(kaif.profilesActive)!"prod"}">

    <#-- server locale is only used in dev mode, because the page will be cached for everyone
    -->
        <meta name="kaifLocale" content="${(request.getLocale().toString())!"en_US"}">
    </#if>

    <link rel='stylesheet' href='/webjars/yui-pure/0.5.0/pure-min.css'>
    <link rel='stylesheet' href='/webjars/yui-pure/0.5.0/grids-responsive-min.css'>
    <link rel="stylesheet" href="/webjars/font-awesome/4.2.0/css/font-awesome.min.css">

<#-- use webjars less js, in production it is compile by gradle rhino less plugin
     @see build.gradle lessc task
  -->
    <#if kaif.profilesActive?contains('dev')>
        <link rel="stylesheet/less" type="text/css" href="/less/kaif.less">
        <script>less = {
            env: 'development',
            logLevel: 2,
        };</script>
        <script src="/webjars/less/1.7.0/less.js"></script>
        <script>
            // uncomment if you want auto refresh in browser
            // less.watch();
        </script>
    <#else>
        <link rel="stylesheet" href="<@url.dynamicRes/>/css/kaif.css">
    </#if>
    <#if applyZoneTheme>
        <link rel="stylesheet" href="<@url.dynamicRes/>/css/${zoneInfo.theme}.css">
    </#if>
    <#if head?length == 0>
        <title>kaif.io</title>
    <#else>
    ${head}
    </#if>

</head>
<body>

<header class="header">
    <div class="container">
        <div class="top-menu pure-menu pure-menu-open pure-menu-horizontal">
            <a class="pure-menu-heading" href="/">Kaif.io</a>
            <ul account-menu>
            <#-- mock
            <li><a href="/account/sign-up">Sign Up</a></li>
            <li><a href="/account/sign-in">Sign In</a></li>
            <li><a href="/account/settings">myname</a></li>
            <li><a href="/account/sign-out">Sign Out</a></li>
            -->
            </ul>
        </div>
    </div>
</header>

<main>
    <div class="container">
        <#if layout == 'small'>
            <div class="pure-g">
                <div class="pure-u pure-u-md-1-5"></div>
                <div class="pure-u-1 pure-u-md-3-5 l-box">
                    <#nested>
                </div>
                <div class="pure-u pure-u-md-1-5"></div>
            </div>
        <#elseif layout == 'full'>
        <#-- full layout let nested take full control -->
            <#nested>
        <#else>
        <#-- TODO other grid 24 layout-->
            <#nested>
        </#if>
    </div>
</main>

<footer class="footer l-box">
    <div class="container">
        <ul class="footer-item">
            <li>
                &#169; 2015 All Rights Reserved
            </li>
            <li>
                <a href="/z/kaif-faq">常見問題</a>
            </li>
        </ul>
    </div>
</footer>
<#-- Error page will not enable js, for security reason -->
    <#if !errorPage >
        <#if kaif.profilesActive?contains('dev')>
        <#-- require dart pub serve, please run `./gradlew pubServe` -->
        <div id="waitingPubServe"
             style="position: fixed; bottom:0; right:0px; padding: 3px 10px; background-color: rgba(92, 0, 0, 0.67); color:white">
            Waiting Pub Serve...
        </div>
        <script src="//localhost:15980/main.dart.js"></script>
        <#else>
        <script src="<@url.dynamicRes/>/web/main.dart.js"></script>
        </#if>
    </#if>
</body>
</html>

</#macro>

<#--
 zone layout
-->
<#macro zone data menus="">

    <#local zoneInfo=data />

<div class="zone ${zoneInfo.theme}">
    <@zoneHeader/>
    <nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <@util.menuLink '/z/${zoneInfo.name}' '熱門'/>
        <@util.menuLink '/z/${zoneInfo.name}/new' '最新'/>
        ${menus}
    </nav>
    <#nested/>
</div>
</#macro>

<#--
  zoneHeader under zone
  -->
<#macro zoneHeader subTitle="">
<div class="zone-header">
    <div class="zone-title"><a href="<@url.zone data=zoneInfo/>">
        <i class="fa fa-caret-right"></i> ${zoneInfo.aliasName} <@url.zone data=zoneInfo/>
    </a> ${subTitle}</div>
</div>
</#macro>

<#--
 home layout
-->
<#macro home>

<div class="home">
    <nav class="home-menu pure-menu pure-menu-open pure-menu-horizontal">
        <@util.menuLink '/' '綜合熱門'/>
        <@util.menuLink '/new' '綜合最新'/>
        <@util.menuLink '/zone/a-z' '所有討論區'/>
    </nav>
    <#nested/>
</div>
</#macro>