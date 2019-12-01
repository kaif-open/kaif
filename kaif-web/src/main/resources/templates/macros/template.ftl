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

<#-- less/kaif.less are compiled by dart transformer
  -->
    <#if kaif.profilesActive?contains('dev')>
        <link rel="stylesheet" href="//localhost:15980/css/kaif.css">
    <#else>
        <link rel="stylesheet" href="<@spring.url '/css/kaif.css' />">
    </#if>
    <#if applyZoneTheme>
        <link rel="stylesheet" href="<@spring.url '/css/${zoneInfo.theme}.css' />">
    </#if>
    <#if head?is_string>
       <title>kaif.io</title>
    <#else>
        ${head}
    </#if>

</head>
<body>

<header class="header">
    <div class="container">
        <div class="top-menu pure-menu pure-menu-open pure-menu-horizontal">
            <a class="pure-menu-heading" href="/">
                <img class="logo"
                     src="<@spring.url '/img/kaif_white@2x.png'/>"
                     alt="kaif"/>
            </a>
            <ul class="pure-menu-children" account-menu>
            <#-- mock
                <li class="pure-menu-can-have-children pure-menu-open">
                    <a class="pure-menu-label" href="#">Top</a>
                    <ul >
                        <li><a href="#">sub 1</a></li>
                        <li><a href="#">sub 2</a></li>
                    </ul >
                </li>
            -->
            </ul>
        </div>
    </div>
</header>

<main>
    <div class="container">
        <#if layout == 'small'>
            <div class="pure-g">
                <div class="pure-u pure-u-lg-1-6"></div>
                <div class="pure-u-1 pure-u-lg-2-3 l-box">
                    <#nested>
                </div>
                <div class="pure-u pure-u-lg-1-6"></div>
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
        <div class="footer-item">
            <span>
                &#169; 2015 All Rights Reserved
            </span>
            <a href="/z/kaif-faq">常見問題</a>
            <a href="/z/kaif-terms">服務條款</a>
            <a href="/developer">開發者</a>
        </div>
    </div>
</footer>
<#-- Error page will not enable js, for security reason -->
    <#if !errorPage >
        <#if kaif.profilesActive?contains('dev')>
        <#-- require dart pub serve, please run `cd kaif-fe; ../gradlew pubPollServe` -->
        <div id="waitingPubServe"
             style="position: fixed; bottom:0; right:0px; padding: 3px 10px; background-color: rgba(92, 0, 0, 0.67); color:white">
            Waiting Pub Serve...
        </div>
        <script src="//localhost:15980/main.dart.js"></script>
        <#else>
        <script src="<@spring.url '/web/main.dart.js'/>"></script>
        </#if>
    </#if>

<#-- google analytics -->
<script>
    (function (i, s, o, g, r, a, m) {
        i['GoogleAnalyticsObject'] = r;
        i[r] = i[r] || function () {
            (i[r].q = i[r].q || []).push(arguments)
        }, i[r].l = 1 * new Date();
        a = s.createElement(o),
                m = s.getElementsByTagName(o)[0];
        a.async = 1;
        a.src = g;
        m.parentNode.insertBefore(a, m)
    })(window, document, 'script', '//www.google-analytics.com/analytics.js', 'ga');

    ga('create', 'UA-60188510-1', 'auto');
    ga('send', 'pageview');
</script>

</body>
</html>

</#macro>

<#--
 zone layout
-->
<#macro zone data showMenu=true>

    <#local zoneInfo=data />

<div class="zone ${zoneInfo.theme}">
    <@zoneHeader/>
    <#if showMenu>
        <nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
            <@util.menuLink '/z/${zoneInfo.name}' '熱門'/>
            <@util.menuLink '/z/${zoneInfo.name}/new' '最新'/>
            <@util.menuLink '/z/${zoneInfo.name}/new-debate' '最新討論'/>
            <@util.menuLink '/z/${zoneInfo.name}/honor' '每月英雄榜'/>
        </nav>
    </#if>
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
        <@util.menuLink '/new-debate' '最新討論'/>
        <@util.menuLink '/honor' '每月英雄榜'/>
        <@util.menuLink '/zone/a-z' '所有討論區'/>
    </nav>
    <#nested/>
</div>
</#macro>

<#--
 user layout
-->
<#macro user username>
<div class="grid">
    <div class="grid-body">
        <div class="user">
            <nav class="user-menu pure-menu pure-menu-open pure-menu-horizontal">
                <@util.menuLink '/u/${username}' '關於'/>
                <@util.menuLink '/u/${username}/articles' '分享的文章'/>
                <@util.menuLink '/u/${username}/debates' '參與的討論'/>
                <@util.menuLink '/u/${username}/honors' '討論區聲望'/>
            </nav>
            <#nested/>
        </div>
    </div>
    <aside class="grid-aside">
    </aside>
</div>

</#macro>