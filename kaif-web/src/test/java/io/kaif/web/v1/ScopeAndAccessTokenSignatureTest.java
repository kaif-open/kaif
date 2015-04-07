package io.kaif.web.v1;

import static java.util.stream.Collectors.*;
import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.model.clientapp.ClientAppUserAccessToken;

public class ScopeAndAccessTokenSignatureTest {

  private Logger logger = LoggerFactory.getLogger(ScopeAndAccessTokenSignatureTest.class);

  @Test
  public void scanAll() throws Exception {
    List<Method> allMethods = ClassScanner.searchAnnotatedClasses("io.kaif.web.v1",
        RestController.class)
        .stream()
        .flatMap(cls -> Stream.of(cls.getDeclaredMethods()))
        .filter(method -> Modifier.isPublic(method.getModifiers()))
        .filter(method -> method.getAnnotation(RequestMapping.class) != null)
        .collect(toList());

    logger.debug("total methods count: " + allMethods.size());
    assertFalse("no method detected, may be scanner bug?", allMethods.isEmpty());

    List<Method> badMethods = allMethods.stream()
        .filter((method) -> !isCorrectAnnotated(method))
        .collect(toList());
    if (!badMethods.isEmpty()) {
      logger.error("method should annotated @"
          + RequiredScope.class.getSimpleName()
          + " and one parameter is "
          + ClientAppUserAccessToken.class.getSimpleName());
      logger.error("==============");
      badMethods.forEach(method -> logger.error(method.toString()));
      logger.error("==============");
    }
    String message =
        "some method missing ClientAppUserAccessToken argument or annotated @RequiredScope: \n"
            + badMethods.stream()
            .map(method -> method.getDeclaringClass().getSimpleName() + "." + method.getName())
            .collect(joining("\n"))
            + "\n";
    assertTrue(message, badMethods.isEmpty());
  }

  private boolean isCorrectAnnotated(Method method) {
    long accessTokenCount = Stream.of(method.getParameterTypes())
        .filter(parameterType -> parameterType.equals(ClientAppUserAccessToken.class))
        .count();
    if (accessTokenCount != 1) {
      // must has single ClientAppUserAccessToken argument
      return false;
    }
    return AnnotationUtils.findAnnotation(method, RequiredScope.class) != null;
  }
}
