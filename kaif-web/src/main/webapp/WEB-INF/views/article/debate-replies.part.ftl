<#import "/spring.ftl" as spring />
<#import "../macros/url.ftl" as url>
<#import "../macros/comp.ftl" as comp>
<#import "../macros/util.ftl" as util>

<div class="debate-list" debate-list>
<#list debates as debate>
    <div class="debate-standalone">
        <@comp.debate data=debate />
        <div class="grid-row">
            <div class="grid-center-row debate-navigation">
                <#if debate.hasParent()>
                    <a href="<@url.debate data=debate parent=true/>">
                        <i class="fa fa-caret-right"></i>
                        回到討論串
                    </a>
                </#if>
                <a href="<@url.article data=debate />">
                    <i class="fa fa-caret-right"></i>
                    回到文章
                </a>
                <a href="<@url.zone data=debate />">
                    <i class="fa fa-caret-right"></i>
                    <@url.zone data=debate />
                </a>
            </div>
        </div>
    </div>

</#list>

    <div class="debate-list-pager grid-center-row">
    <#if debates?size == 0>
        <p>沒有回應了</p>
    <#else>
        <a href="#" debate-list-pager class="pure-button">
            <i class="fa fa-caret-right"></i> 下一頁
        </a>
    </#if>
    </div>
    <div next-debate-list></div>
</div>

