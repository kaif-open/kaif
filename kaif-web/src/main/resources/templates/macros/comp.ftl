<#import "/spring.ftl" as spring />
<#import "url.ftl" as url />
<#import "util.ftl" as util/>

<#-- Article components -->

<#-- data is ArticleList.java -->
<#macro articleList data ajaxPager=false showZone=false>
<div class="article-list" article-list>
    <#list data.articles as article>
        <@comp.article data=article showZone=showZone/>
    </#list>

    <div class="article-list-pager grid-center-row">
        <#if data.articles?size == 0 >
            <p>沒有文章</p>
        <#else>
            <#if ajaxPager>
                <a href="#" ajax-pager class="pure-button">
                    <i class="fa fa-caret-right"></i> 下一頁
                </a>
            <#else>
                <a href="<@url.current start=data.lastArticleId />"
                   class="pure-button"><i class="fa fa-caret-right"></i> 下一頁</a>
            </#if>
        </#if>
    </div>
</div>
</#macro>

<#macro article data hidden=false parentMode=false showZone=false zoneAdmins="">

    <#local article=data />
    <#local hiddenCls=hidden?string("hidden", "") />

<div class="article grid-row ${hiddenCls}" article data-article-id="${article.articleId}"
     data-zone="${article.zone}">
    <div class="article-vote-box votable grid-convex" article-vote-box
         data-article-vote-count="${article.upVote}">
        <span class="vote-count" article-vote-count>${article.upVote}</span>
        <a href="#" article-up-vote>
            <div class="up-vote"></div>
        </a>
    </div>
    <div class="grid-center">
        <div class="article-title">
        <#-- TODO, not to use article content as url directly -->
            <#if article.externalLink>
                <a href="${article.link}" target="_blank">${article.title}</a>
            <#else>
                <a href="<@url.article data=article />">${article.title}</a>
            </#if>
            <span class="article-link-hint">
                <#if article.externalLink>
                    (${article.linkHint})
                <#else>
                    (<span class="speak-hint">${article.linkHint}</span>)
                </#if>
            </span>
        </div>
        <#if parentMode && article.hasMarkDownContent() >
            <div class="article-content">
                <article class="kmark">
                ${article.renderContent}
                </article>
            </div>
        </#if>
        <div class="article-info">
            <span>
                <#if parentMode>
                ${article.debateCount} 則討論
                <#else>
                    <a href="<@url.article data=article/>"><i
                            class="fa fa-caret-right"></i> ${article.debateCount} 則討論</a>
                </#if>
            </span>
            <span>
                <a class="article-author"
                   href="<@url.account data=article/>">${article.authorName}</a>
                張貼於 <@util.time instant=article.createTime />
            </span>
            <#if showZone>
                <a href="<@url.zone data=article/>">
                    <i class="fa fa-caret-right"></i> ${article.aliasName}
                </a>
            </#if>
            <#if article.deleted>
                <span class="label label-warning">文章已刪除</span>
            <#else>
                <#if parentMode>
                    <span class="hidden" article-deletion
                          data-author-name="${article.authorName}"
                          data-zone-admins="${zoneAdmins}">
                        <button type="button" confirm-delete
                                class="pure-button button-warning">
                            刪除文章 ?
                        </button>
                        <button type="button" delete
                                class="pure-button button-danger hidden">
                            確定要刪除 ? 刪除後無法回復.
                        </button>
                    </span>
                </#if>
            </#if>
        </div>
    </div>
</div>

</#macro>

<#-- Debate components -->

<#macro debateNode data >
    <#local theDebateNode=data />
    <@debate data=theDebateNode.value >
        <#list theDebateNode.children as child>
        <#-- recursive macro -->
            <@comp.debateNode data=child />
        </#list>
    </@debate>
</#macro>

<#-- if parentMode set to true, it means this debate is top of the page
     it will handle `parent` link differently. (also show hint)
  -->
<#macro debate data parentMode=false editable=true>

    <#local debate=data />

<div id="debate-${debate.debateId}" class="debate grid-row"
     debate
     data-article-id="${debate.articleId}"
     data-zone="${debate.zone}"
     data-debate-id="${debate.debateId}"
     data-debater-name="${debate.debaterName}">
    <div class="debate-vote-box votable grid-convex" debate-vote-box
         data-debate-vote-count="${debate.totalVote}">
        <a href="#" debate-up-vote>
            <div class="up-vote"></div><#--
         This freemarker comment is required, for remove small spacing between triangle
        --></a><br>
        <a href="#" debate-down-vote>
            <div class="down-vote"></div>
        </a>
    </div>
    <div class="grid-center">
        <div class="debate-title">
            <a class="debate-author"
               href="<@url.account data=debate/>">${debate.debaterName}</a>
            <span>
                積分 <span class="vote-count" debate-vote-count>${debate.totalVote}</span>
            </span>
            <#if debate.edited>
                <span class="debate-edited">編輯於 <@util.time instant=debate.lastUpdateTime /></span>
            </#if>
        </div>
        <div class="debate-content">
            <div class="kmark" debate-content>
            ${debate.renderContent}
            </div>
            <div debate-content-edit class="hidden"></div>
        </div>
        <div class="debate-action">
            <#if !debate.maxLevel>
                <a href="#" debate-replier
                   data-debate-id="${debate.debateId}">回應</a>
            </#if>
        <#--
          permenant link is sub debate tree only, we don't want google index it.
          so rel="nofollow"
          -->
            <a href="<@url.debate data=debate/>" title="永久連結"
               rel="nofollow"><@util.time instant=debate.createTime /></a>
            <#if editable>
                <a href="#" debate-content-editor data-debate-id="${debate.debateId}"
                   class="hidden">編輯</a>
            </#if>
        </div>
        <div class="debate-child">
        <#-- child debate here -->
            <#nested/>
        </div>
    </div>
</div>

    <#if parentMode>
    <div class="grid-center-row child-debate-hint">
        <div class="alert alert-info">
            這是文章的子討論串，你可以回到上層查看所有討論和文章
            <div>
                <a href="<@url.article data=debate/>"
                   class="pure-button button-sm button-info"><i class="fa fa-caret-up"></i> 回上層</a>
            </div>
        </div>
    </div>
    </#if>
</#macro>

<#macro debateStandAlone data article>
    <#local debateItem=data/>
<div class="debate-standalone">
    <@debate data=debateItem editable=false />
    <div class="grid-row">
        <div class="grid-center-row debate-navigation">
            <a href="<@url.article data=debateItem />${'#debate-'+debateItem.debateId}">
                <i class="fa fa-caret-right"></i>
            ${util.abbreviate(article.title, 20)}
            </a>
            <a href="<@url.zone data=debateItem />">
                <i class="fa fa-caret-right"></i>
                <@url.zone data=debateItem />
            </a>
        </div>
    </div>
</div>
</#macro>

<#-- data is DebateList -->
<#macro debateList data ajaxPager=false showZone=true >
    <#local debates=data.debates />
<div class="debate-list" debate-list>
    <#list debates as debateItem>
        <@debateStandAlone data=debateItem article=data.getArticle(debateItem) />
    </#list>
    <div class="debate-list-pager grid-center-row">
        <#if debates?size == 0>
            <p>沒有回應了</p>
        <#else>
            <#if ajaxPager>
                <a href="#" ajax-pager class="pure-button">
                    <i class="fa fa-caret-right"></i> 下一頁
                </a>
            <#else>
                <a href="<@url.current start=debates?last.debateId />"
                   class="pure-button"><i class="fa fa-caret-right"></i> 下一頁</a>
            </#if>
        </#if>
    </div>
</div>
</#macro>


<#macro debateForm>
<form class="pure-form hidden debate-form" comp-template="debate-form">
    <div>
        <div kmark-previewer class="hidden kmark kmark-preview"></div>
        <textarea name="contentInput"
                  class="pure-input-1 kmark-input"
                  maxlength="4096"
                  rows="3"></textarea>
    </div>
    <div class="form-action-bar">
        <button type="submit" class="pure-button pure-button-primary">留言</button>
        <button type="button" class="pure-button" kmark-debate-cancel>取消</button>
        <button type="button" class="pure-button"
                kmark-preview><@spring.messageText "kmark.preview" "Preview" /></button>
        <button type="button" class="pure-button"
                kmark-help-toggle><@spring.messageText "kmark.help" "Format Help" /></button>
    </div>
    <@kmarkHelp />
</form>
</#macro>

<#-- data is List<HonorRoll> -->
<#macro honorRollList data>
<div class="grid-center-row">
    <table class="honor-roll-table">
        <thead>
        <tr>
            <th class="honor-rank">排行</th>
            <th class="honor-score">聲望</th>
            <th class="honor-user">英雄</th>
        </tr>
        </thead>
        <tbody>
            <#list data as honorRoll>
            <tr>
                <td class="honor-rank">
                    <#if honorRoll_index lt 3>
                        <i class="fa fa-trophy fa-trophy-${honorRoll_index}"></i>
                    <#else>
                    ${honorRoll_index+1}
                    </#if>
                </td>
                <td class="honor-score">
                    <#if honorRoll.honorScore gt 0>
                        <span class="honor-positive-score">${honorRoll.honorScore}</span>
                    <#elseif honorRoll.honorScore lt 0>
                        <span class="honor-negative-score">${honorRoll.honorScore}</span>
                    <#else>
                        <span class="honor-zero-score">${honorRoll.honorScore} </span>
                    </#if>
                </td>
                <td class="honor-user">
                    <a href="<@url.account data=honorRoll/>"><@url.account data=honorRoll/></a>
                </td>
            </tr>
            </#list>
        </tbody>
    </table>
</div>

</#macro>

<#macro kmarkHelp>
<div class="kmark-help hidden" kmark-help>
    <h4>Kmark 語法說明</h4>

    <p>Kmark 是一個類似 <a href="http://en.wikipedia.org/wiki/Markdown" target="_blank">Markdown</a>
        語法的格式，以下為提供的功能:</p>
    <table class="pure-table pure-table-horizontal">
        <tr>
            <th>種類</th>
            <th>語法</th>
            <th>呈現</th>
        </tr>
        <tr>
            <td>
                斜體
            </td>
            <td>
                <p>
                    *兩邊加單星*
                </p>
            </td>
            <td>
                <div class="kmark">
                    <em>兩邊加單星</em>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                粗體
            </td>
            <td>
                <p>
                    **兩邊加雙星**
                </p>
            </td>
            <td>
                <div class="kmark">
                    <strong>兩邊加雙星</strong>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                刪除線
            </td>
            <td>
                <p>
                    ~~兩邊加雙曲號~~
                </p>
            </td>
            <td>
                <div class="kmark">
                    <del>兩邊加雙曲號</del>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                引用
            </td>
            <td>
                <p>
                    > 左邊加個大於符號
                </p>
            </td>
            <td>
                <div class="kmark">
                    <blockquote>左邊加個大於符號</blockquote>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                列表
            </td>
            <td>
<pre> * 可用星號
 * 也可以 - 減號
 * 數字加點也可以
</pre>
            </td>
            <td>
                <div class="kmark">
                    <ul>
                        <li>可用星號</li>
                        <li>也可以 - 減號</li>
                        <li>數字加點也可以</li>
                    </ul>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                固定寬字
            </td>
            <td>
                <p>
                    `abcdefghijk`
                </p>

                <p>
                    兩邊用倒引號包住
                </p>
            </td>
            <td>
                <div class="kmark">
                    <code>abcdefghijk</code>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                編碼區塊
            </td>
            <td>
<pre>```
function abc()
```</pre>
                <p>上下都用三個倒引號包住</p>
            </td>
            <td>
                <div class="kmark">
                    <pre><code>function abc()</code></pre>
                </div>
            </td>
        </tr>
        <tr>
            <td>
                連結
            </td>
            <td>
<pre>
[這是連結][1]
[1]: http://example.com
</pre>
                <p>
                    連結第一部份是文字，先用中括號包住，後面再加上 <strong>[編號]</strong>。
                    第二部份是連結本身，放在文末，開頭是 <strong>[編號]: http</strong>
                </p>
            </td>
            <td>
                <div class="kmark">
                    <p><a href="http://example.com" class="reference-link">這是連結</a><span
                            class="reference-link-index">1</span></p>

                    <div class="reference-appendix-block">
                        <div class="reference-appendix-index">1</div>
                        <div class="reference-appendix-wrap"><a href="http://example.com"
                                                                rel="nofollow" target="_blank">http://example.com</a>
                        </div>
                    </div>
                </div>
            </td>
        </tr>
    </table>
</div>
</#macro>