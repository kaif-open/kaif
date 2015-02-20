<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#assign headContent>
<title>新文章 | kaif.io</title>
</#assign>

<@template.page
layout='full'
applyZoneTheme=true
head=headContent
>

    <#assign createLinkPath='/z/${zoneInfo.name}/article/create-link' />
    <#assign createSpeakPath='/z/${zoneInfo.name}/article/create-speak' />

<div class="zone ${zoneInfo.theme}">
    <@template.zoneHeader subTitle=" - 新文章發表"/>
    <nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <@util.menuLink createLinkPath '分享好連結'/>
        <@util.menuLink createSpeakPath '我有話要說'/>
    </nav>
    <div class="grid">
        <div class="grid-body">
            <div class="grid-center-row">
                <@articleForm />
            </div>
        </div>
        <aside class="grid-aside">
            發文規則... TODO
        </aside>
    </div>
</div>
</@template.page>

<#macro articleForm>
<form class="pure-form pure-form-stacked" article-form>

    <input type="hidden" value="${zoneInfo.name}" name="zoneInput">

    <div class="pure-control-group">
        <label for="titleInput">標題</label>
        <textarea id="titleInput"
                  class="pure-input-1"
                  rows="2"
                  placeholder="吸引人的標題..."
                  maxlength="128"
                  required
                  title="標題必填"
                ></textarea>
    </div>

    <#if url.isCurrentPath(createLinkPath)>
        <div class="pure-control-group">
            <label for="urlInput">文章連結</label>
            <input id="urlInput"
                   class="pure-input-1"
                   type="url"
                   placeholder="http://..."
                   maxlength="512"
                   required
                   title="連結必填, 必須以 http 開頭"
                    >
        </div>
    <#elseif url.isCurrentPath(createSpeakPath)>
        <div class="pure-control-group">
            <label for="contentInput">想法</label>
            <textarea id="contentInput"
                      class="pure-input-1"
                      rows="5"
                      placeholder="您的想法或問題..."
                      maxlength="16384"
                      required
                      title="內文必填"
                    ></textarea>
        </div>
    </#if>
    <div class="pure-controls">
        <button type="submit" disabled
                class="pure-button pure-button-primary">
            分享
        </button>
    </div>

    <p></p>

    <div class="pure-controls hidden" can-not-create-article-hint>
        <div class="alert alert-warning">
            您沒有權限發表文章，請確認：
            <ul>
                <li>您已經登入帳號，而且帳號已經啟用</li>
                <li>有些討論區需要特別身份的人才能發新文，例如官方討論區
                    <a href="/z/kaif-faq">常見問題</a></li>
            </ul>
        </div>
    </div>
    <div class="pure-controls hidden" not-sign-in-hint>
        <div class="alert alert-warning">
            您尚未登入，登入後才能發文喔！
            <p>
                <a href="/account/sign-in" class="pure-button">
                    <i class="fa fa-caret-right"></i> 馬上登入</a>
            </p>

            <p>
                <a href="/account/sign-up" class="pure-button">
                    <i class="fa fa-caret-right"></i> 申請帳號</a>
            </p>
        </div>
    </div>
</form>
</#macro>
