package io.kaif.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.builder.ApiInfoBuilder;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

import io.kaif.model.clientapp.ClientAppUserAccessToken;

@EnableSwagger
@Configuration
public class SwaggerConfiguration {
  static class CustomSwaggerPathProvider extends SwaggerPathProvider {

    @Override
    protected String applicationPath() {
      return "/";
    }

    @Override
    protected String getDocumentationPath() {
      // default is `/`
      return "/";
    }

    public CustomSwaggerPathProvider withApiResourcePrefix(String apiResourcePrefix) {
      setApiResourcePrefix(apiResourcePrefix);
      return this;
    }
  }

  @Autowired
  private SpringSwaggerConfig springSwaggerConfig;

  @Bean //Don't forget the @Bean annotation
  public SwaggerSpringMvcPlugin customImplementation() {
    return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).apiInfo(apiInfo())
        .includePatterns(".*v1.*")
        .apiVersion("1.0 alpha")
        .ignoredParameterTypes(ClientAppUserAccessToken.class)
        .pathProvider(new CustomSwaggerPathProvider());
  }

  private ApiInfo apiInfo() {
    return new ApiInfoBuilder().title("Kaif API")
        .description("Kaif API 1.0 alpha")
        .termsOfServiceUrl("https://kaif.io/z/kaif-terms")
        .license("Licence Apahce 2.0")
        .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
        .build();
  }
}
