<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<div class="zone">

    <div class="pure-g">
        <div class="pure-u-1">
            <ul class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
            <@template.menuLink '/z/${zoneInfo.name}/article/create' '分享好連結'/>
            <@template.menuLink '/z/${zoneInfo.name}/article/create-self' '寫文章'/>
            </ul>
        </div>
        <div class="pure-u-1 pure-u-md-3-4">
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
                        建立
                    </button>
                </div>
            </form>
        </div>
        <div class="pure-u-1 pure-u-md-1-4">
            發文規則... TODO
        </div>
    </div>

</div>