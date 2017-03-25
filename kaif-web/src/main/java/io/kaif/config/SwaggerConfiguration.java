package io.kaif.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.kaif.flake.FlakeId;
import io.kaif.model.clientapp.ClientAppUserAccessToken;
import io.kaif.model.zone.Zone;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.AlternateTypeRule;
import springfox.documentation.schema.AlternateTypeRules;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableSwagger2
@Configuration
public class SwaggerConfiguration {

  private static final String API_VERSION = "1.0.0-alpha";

  @Bean
  public Docket apiDocket() {
    return new Docket(DocumentationType.SWAGGER_2).apiInfo(apiInfo())
        .alternateTypeRules(alternativeTypeRules())
        .useDefaultResponseMessages(false)
        .ignoredParameterTypes(ignoredParameterTypes())
        .groupName("v1")
        .select()
        .apis(RequestHandlerSelectors.basePackage("io.kaif.web.v1"))
        .paths(PathSelectors.any())
        .build();
  }

  private Class<?>[] ignoredParameterTypes() {
    return new Class[] { ClientAppUserAccessToken.class };
  }

  /**
   * convert FlakeId and Zone to String in swagger document
   *
   * @see io.kaif.web.support.WebDataBinderAdvice
   * see json deserializer of FlakeId
   * see json deserializer of Zone
   */
  private AlternateTypeRule[] alternativeTypeRules() {
    return new AlternateTypeRule[] { AlternateTypeRules.newRule(FlakeId.class, String.class),
        AlternateTypeRules.newRule(Zone.class, String.class) };
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("kaif")
        .description("kaif Open API " + API_VERSION)
        .termsOfServiceUrl("https://kaif.io/z/kaif-terms")
        .license("Apache 2.0")
        .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
        .build();
  }
}