<#import "/spring.ftl" as spring />
<#import "../macros/template.ftl" as template>
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>
<#import "../macros/aside.ftl" as aside>

<#assign headContent>

<title>${article.title} | ${zoneInfo.aliasName} | kaif.io</title>

<#-- TODO description and open graph, twitter card...etc -->

<meta name="description"
      content="${article.title} | ${zoneInfo.aliasName}">

</#assign>

<@template.page
layout='full'
head=headContent
applyZoneTheme=true
>
    <@template.zone data=zoneInfo showMenu=false>

    <div class="grid">

        <div class="grid-body">
            <div class="debate-tree" debate-tree>
                <#if parentDebate??>

                <#-- tree parent is Debate, we still create article component for dart,
                     but it is invisible to user -->
                    <@comp.article data=article hidden=true />
                    <@comp.debate data=parentDebate parentMode=true/>
                <#else>

                <#-- tree parent is Article -->
                    <@comp.article data=article parentMode=true />
                    <div class="grid-center-row debate-form-container">
                    <#-- place holder debate-form will replace by comp-template
                         keep place holder looks the same as <@comp.debateForm>
                         (but without cancel button)
                         -->
                        <div class="pure-form debate-form" debate-form>
                            <div>
                                <div class="hidden kmark kmark-preview"></div>
                                <textarea class="pure-input-1 kmark-input" rows="3"></textarea>
                            </div>
                            <div class="form-action-bar">
                                <button type="submit" class="pure-button pure-button-primary">留言
                                </button>
                                <button type="button" class="pure-button">
                                    <@spring.messageText "kmark.preview" "Preview" /></button>
                                <button type="button" class="pure-button">
                                    <@spring.messageText "kmark.help" "Format Help" /></button>
                            </div>
                        </div>
                    </div>
                </#if>
                <#list debateTree.children as debateNode>
                    <@comp.debateNode data=debateNode />
                </#list>
            </div>
        </div>
        <aside class="grid-aside">
            <#if parentDebate??>
                <@aside.shortUrl data=parentDebate />
            <#else>
                <@aside.shortUrl data=article />
            </#if>
            <@aside.createArticle />
            <@aside.recommendZones zoneInfos=recommendZones />
        </aside>
    </div>
    </@template.zone>

<#-- component templates -->
    <@comp.debateForm />

<form class="pure-form hidden debate-form" comp-template="edit-kmark-form">
    <div>
        <div kmark-previewer class="hidden kmark kmark-preview"></div>
        <textarea name="contentInput" class="pure-input-1 kmark-input" maxlength="4096"
                  rows="3"></textarea>
    </div>
    <div class="form-action-bar">
        <button type="submit" class="pure-button pure-button-primary">修改</button>
        <button type="button" class="pure-button" kmark-cancel>取消</button>
        <button type="button" class="pure-button"
                kmark-preview><@spring.messageText "kmark.preview" "Preview" /></button>
        <button type="button" class="pure-button" kmark-help-toggle>
            <@spring.messageText "kmark.help" "Format Help" />
        </button>
    </div>
    <@comp.kmarkHelp />
</form>

</@template.page>
