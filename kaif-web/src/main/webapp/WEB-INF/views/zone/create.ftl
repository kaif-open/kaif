<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/util.ftl" as util>
<#import "../macros/url.ftl" as url>

<#assign headContent>
<title>建立討論區 | kaif.io</title>
</#assign>

<@template.page
layout='full'
head=headContent
>

<div>
    <div class="grid">
        <div class="grid-body">
            <div class="grid-center-row">
                <@zoneForm />
            </div>
        </div>
        <aside class="grid-aside">
            <div class="aside-card">
                <h4>建立討論區規則</h4>

                <div class="aside-card-box">
                    <ul>
                        <li>未定</li>
                    </ul>
                </div>
            </div>
        </aside>
    </div>
</div>
</@template.page>

<#macro zoneForm>
<form class="pure-form pure-form-stacked zone-form" zone-form>

    <div class="pure-control-group">
        <label for="zoneInput">英文名 <span class="hint zoneHint"></span></label>
        <input id="zoneInput"
               class="pure-input-1"
               type="text"
               placeholder="英文字、dash(-)隔開、3~20 個字"
               maxlength="20"
               pattern="${zonePattern}"
               required
               title="英文字、dash(-)隔開、3~20 個字必填">
    </div>

    <div class="pure-control-group">
        <label for="aliasNameInput">中文名</label>
        <input id="aliasNameInput"
               class="pure-input-1"
               type="text"
               placeholder="清楚的名稱"
               maxlength="15"
               required
               title="討論區名稱必填">
    </div>
    <div class="pure-controls">
        <button type="submit" disabled
                class="pure-button pure-button-primary">
            建立
        </button>
    </div>

    <p></p>

    <div class="pure-controls hidden" can-not-create-zone-hint>
        <div class="alert alert-warning">
            您沒有權限建立討論區，請確認：
            <ul>
                <li>您已經登入帳號，而且帳號已經啟用</li>
                <li>建立討論區需累積一定聲望，詳情請見
                    <a href="/z/kaif-faq/">常見問題</a></li>
            </ul>
        </div>
    </div>
    <div class="pure-controls hidden" not-sign-in-hint>
        <div class="alert alert-warning">
            您尚未登入，登入後才能建立討論區喔！
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
