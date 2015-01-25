package io.kaif.web;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.web.support.AccessDeniedException;

@ControllerAdvice(annotations = Controller.class)
@Order(Ordered.LOWEST_PRECEDENCE)
public class PageExceptionHandler {

  @org.springframework.web.bind.annotation.ExceptionHandler(AccessDeniedException.class)
  public ModelAndView handleRestAccessDeniedException(final AccessDeniedException ex,
      final WebRequest request) {
    final HttpStatus status = HttpStatus.UNAUTHORIZED;
    if (request instanceof ServletWebRequest) {
      ((ServletWebRequest) request).getResponse().setStatus(status.value());
    }
    return new ModelAndView("access-denied");
  }
}
