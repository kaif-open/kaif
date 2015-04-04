package io.kaif.web.v1;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import io.kaif.web.support.SingleWrapper;

/**
 * wrap all json response as
 * <pre>
 *  {
 *    "data": ...
 *  }
 * </pre>
 */
@ControllerAdvice(basePackageClasses = V1ResponseWrapperAdvice.class)
public class V1ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {
  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return converterType.equals(MappingJackson2HttpMessageConverter.class);
  }

  @Override
  public Object beforeBodyWrite(Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body instanceof SingleWrapper) {
      return body;
    }
    return SingleWrapper.of(body);
  }
}
