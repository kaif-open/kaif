package io.kaif.web;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import io.kaif.web.support.AccessDeniedException;

@ControllerAdvice(annotations = Controller.class)
@Order(Ordered.LOWEST_PRECEDENCE)
public class PageExceptionHandler {

  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ExceptionHandler(AccessDeniedException.class)
  public ModelAndView handleAccessDeniedException(final AccessDeniedException ex) {
    return new ModelAndView("access-denied");
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ExceptionHandler(EmptyResultDataAccessException.class)
  public ModelAndView handleEmptyResultDataAccessException(EmptyResultDataAccessException e,
      HttpServletRequest request) {
    //see error.ftl and spring's BasicErrorController
    request.setAttribute("status", HttpStatus.NOT_FOUND.value());
    return new ModelAndView("error");
  }

}
