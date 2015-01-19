package io.kaif.web.api;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;

import io.kaif.web.support.AbstractRestExceptionHandler;

@ControllerAdvice(annotations = RestController.class)
public class RestExceptionHandler extends AbstractRestExceptionHandler {
}
