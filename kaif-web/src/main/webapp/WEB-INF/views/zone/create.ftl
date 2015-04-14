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
<nav class="zone-menu pure-menu pure-menu-open pure-menu-horizontal">
    <@util.menuLink '/zone/create' '建立討論區'/>
</nav>
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
                    <li>你需要具備一定的聲望才能成立新討論區</li>
                    <li>禁止腥色羶類型的討論區</li>
                    <li>討論區成立後建立者即為版主</li>
                    <li>討論區的文章不一定會納入首頁的綜合排行裡</li>
                </ul>
            </div>
        </div>
    </aside>
</div>
</@template.page>

<#macro zoneForm>
<form class="pure-form pure-form-stacked zone-form" zone-form>

    <div class="pure-control-group">
        <label for="zoneInput">討論區代號 <span class="hint zoneHint"></span></label>
        <input id="zoneInput"
               class="pure-input-1"
               type="text"
               placeholder="英文字小寫、dash(-)隔開、3~20 個字"
               maxlength="20"
               pattern="${zonePattern}"
               required
               title="英文字小寫、dash(-)隔開、3~20 個字必填">

        <div class="pure-form-message-inline">
            * 代號將為網址的一部份，且不能變更，請謹慎選擇。
        </div>
    </div>

    <div class="pure-control-group">
        <label for="aliasNameInput">討論區別名</label>
        <input id="aliasNameInput"
               class="pure-input-1"
               type="text"
               placeholder="清楚的名稱"
               maxlength="20"
               required
               title="討論區名稱必填">

        <div class="pure-form-message-inline">
            * 別名通常是中文，4 到 10 字為宜。別名未來不能變更，請謹慎選擇。
        </div>
    </div>
    <p></p>

    <div class="pure-controls">
        <button type="submit" disabled
                class="pure-button pure-button-primary">
            建立
        </button>
    </div>

    <p></p>

    <div class="pure-controls hidden" can-not-create-zone-hint>
        <div class="alert alert-warning">
            您無法建立討論區，請確認：
            <ul>
                <li>您已經登入帳號，而且帳號已經啟用</li>
                <li>建立討論區需累積一定聲望，建立越多需要越高的聲望，詳情請見
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
