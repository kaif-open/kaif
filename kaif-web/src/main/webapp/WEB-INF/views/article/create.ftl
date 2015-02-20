<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#assign headContent>
<title>新文章 | kaif.io</title>
</#assign>

<@template.page
layout='full'
head=headContent
>
<div class="zone">
    <div class="zone-header">
        <div class="zone-title">
            新文章發表於 ${zoneInfo.aliasName} - <@url.zone data=zoneInfo/>
        </div>
    </div>
    <nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
        <@util.menuLink '/z/${zoneInfo.name}/article/create-link' '分享好連結'/>
        <@util.menuLink '/z/${zoneInfo.name}/article/create-speak' '我有話要說'/>
    </nav>
    <div class="grid">
        <div class="grid-body">
            <div class="grid-center-row">

                <form class="pure-form pure-form-stacked" external-link-article-form>

                    <input type="hidden" value="${zoneInfo.name}" id="zoneInput">

                    <div class="pure-control-group">
                        <label for="titleInput">標題</label>
                    <textarea maxlength="128" rows="2" id="titleInput" type="text"
                              placeholder="吸引人的標題..." required
                              title="標題必填"
                              class="pure-input-1"></textarea>
                    </div>

                    <div class="pure-control-group">
                        <label for="urlInput">文章連結</label>
                        <input id="urlInput" type="url" placeholder="http://..."
                               maxlength="512" required
                               title="連結必填, 必須以 http 開頭"
                               class="pure-input-1">
                    </div>

                    <div class="pure-controls">
                        <button type="submit" class="pure-button pure-button-primary">
                            分享
                        </button>
                    </div>
                </form>
            </div>
        </div>

        <aside class="grid-aside">
            發文規則... TODO
        </aside>
    </div>
</div>
</@template.page>
