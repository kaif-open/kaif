package io.kaif.config;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.*;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mangofactory.swagger.configuration.SpringSwaggerConfig;
import com.mangofactory.swagger.models.dto.ApiInfo;
import com.mangofactory.swagger.models.dto.AuthorizationCodeGrant;
import com.mangofactory.swagger.models.dto.AuthorizationScope;
import com.mangofactory.swagger.models.dto.AuthorizationType;
import com.mangofactory.swagger.models.dto.OAuth;
import com.mangofactory.swagger.models.dto.TokenEndpoint;
import com.mangofactory.swagger.models.dto.TokenRequestEndpoint;
import com.mangofactory.swagger.models.dto.builder.ApiInfoBuilder;
import com.mangofactory.swagger.paths.SwaggerPathProvider;
import com.mangofactory.swagger.plugin.EnableSwagger;
import com.mangofactory.swagger.plugin.SwaggerSpringMvcPlugin;

import io.kaif.model.clientapp.ClientAppScope;
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

  public static final String API_VERSION = "1.0.0-alpha";

  @Autowired
  private SpringSwaggerConfig springSwaggerConfig;

  @Autowired
  private MessageSource messageSource;

  @Bean //Don't forget the @Bean annotation
  public SwaggerSpringMvcPlugin customImplementation() {
    return new SwaggerSpringMvcPlugin(this.springSwaggerConfig).apiInfo(apiInfo())
        .includePatterns("/v1/.*")
        .apiVersion(API_VERSION)
        .swaggerGroup("v1")
        .alternateTypeRules()
        .useDefaultResponseMessages(false)
        .authorizationTypes(authorizationTypes())
        .ignoredParameterTypes(ClientAppUserAccessToken.class)
        .pathProvider(new CustomSwaggerPathProvider());
  }

  private List<AuthorizationType> authorizationTypes() {
    List<AuthorizationScope> authorizationScopes = Stream.of(ClientAppScope.values())
        .map(scope -> new AuthorizationScope(scope.toString(),
            messageSource.getMessage(scope.getI18nKey(), null, Locale.ENGLISH)))
        .collect(toList());
    //TODO oauth authorization in swagger ui not configured yet (how?)
    return asList(new OAuth(authorizationScopes,
        asList(new AuthorizationCodeGrant(new TokenRequestEndpoint("https://kaif.io/oauth/authorize",
            "client_id",
            "client_secret"),
            new TokenEndpoint("https://kaif.io/oauth/access-token", "access_token")))));
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
