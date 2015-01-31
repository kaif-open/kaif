<#import "/spring.ftl" as spring />

<#--
sample configs:

1)

<@template.page {
  'layout':'small'
}>

2)

<@template.page {
  'layout':'full'
}>

3) for error page

<@template.page {
  'layout':'small',
  'errorPage': true
}>
-->
<#macro page config>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="Kaif prototype">

    <title>kaif.io</title>

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
    <link rel="stylesheet" href="/css/kaif.css?${(kaif.deployServerTime)!0}">

    <#if zoneInfo?? >
        <link rel="stylesheet" href="/css/${zoneInfo.theme}.css?${(kaif.deployServerTime)!0}">
    </#if>
</head>
<body>
<header class="header">
    <div class="home-menu pure-menu pure-menu-open pure-menu-horizontal">
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
</header>

<main class="content">
    <#if (config.layout)! == 'small'>
        <div class="pure-g">
            <div class="pure-u pure-u-md-1-5"></div>
            <div class="pure-u-1 pure-u-md-3-5 l-box">
                <#nested>
            </div>
            <div class="pure-u pure-u-md-1-5"></div>
        </div>
    <#elseif (config.layout)! == 'full'>
    <#-- full layout let nested take full control -->
        <#nested>
    <#else>
    <#-- TODO other grid 24 layout-->
        <#nested>
    </#if>
</main>

<footer class="footer l-box">
    <ul class="footer-item">
        <li>
            &#169; 2015
        </li>
        <li>
            <a href="/z/kaif-faq">常見問題</a>
        </li>
    </ul>
</footer>
<#-- Error page will not enable js, for security reason -->
    <#if !((config.errorPage)!false) >
        <#if kaif.profilesActive?contains('dev')>
        <#-- require dart pub serve, please run `./gradlew pubServe` -->
        <div id="waitingPubServe"
             style="position: fixed; bottom:0; right:0px; padding: 3px 10px; background-color: rgba(92, 0, 0, 0.67); color:white">
            Waiting Pub Serve...
        </div>
        <script src="//localhost:15980/main.dart.js"></script>
        <#else>
        <script src="/dart_dist/web/main.dart.js?${kaif.deployServerTime}"></script>
        </#if>
    </#if>
</body>
</html>

</#macro>

