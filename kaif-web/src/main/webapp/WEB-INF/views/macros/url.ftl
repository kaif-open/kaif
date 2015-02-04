<#import "/spring.ftl" as spring />

<#--

  kaif.appBuild
     from app.build in WebConfiguration.java
        from application.yml
           from application-prod.yml in production
              from gradle build replace token @app.build@

  kaif.appBuild by default is `snapshot`
  -->
<#macro dynamicRes>/${kaif.appBuild}</#macro>