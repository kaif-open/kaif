<#import "/spring.ftl" as spring />
<#import "macros/template.ftl" as template>

<@template.page layout=part.layout >

<#-- dart will search #__part_template and set innerHtml with part
     check ServerPartLoader in main.dart
  -->
<div id="__part_template"></div>

</@template.page>