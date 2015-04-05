package io.kaif.web.v1;

import org.springframework.core.MethodParameter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import io.kaif.web.support.ErrorResponse;
import io.kaif.web.support.SingleWrapper;

/**
 * wrap all json response as
 * <pre>
 *  {
 *    "data": ...
 *  }
 * </pre>
 * <p>
 * note that if return type is byte array or Resource, it won't do wrapping
 */
@ControllerAdvice(basePackageClasses = V1ResponseWrapperAdvice.class)
public class V1ResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

  /**
   * for POJO or primitive response, the converterType is MappingJackson2HttpMessageConverter, so
   * it can be translated to other object such as SingleWrapper.
   * <p>
   * for String response, the convertType is StringHttpMessageConverter and it is before jackson
   * converter. so we have to manually wrapped as string include "data"
   */
  @Override
  public boolean supports(MethodParameter returnType,
      Class<? extends HttpMessageConverter<?>> converterType) {
    return converterType.equals(MappingJackson2HttpMessageConverter.class) || converterType.equals(
        StringHttpMessageConverter.class);
  }

  @Override
  public Object beforeBodyWrite(Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {
    if (body instanceof CharSequence) {
      //for StringHttpMessageConverter
      response.getHeaders()
          .add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");
      return "{\"data\":\"" + body + "\"}";
    }
    if (body instanceof ErrorResponse) {
      return body;
    }
    if (body instanceof SingleWrapper) {
      return body;
    }
    return SingleWrapper.of(body);
  }
}
