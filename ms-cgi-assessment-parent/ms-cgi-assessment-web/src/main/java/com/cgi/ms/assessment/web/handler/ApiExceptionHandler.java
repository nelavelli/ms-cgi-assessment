package com.cgi.ms.assessment.web.handler;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.ErrorMessage;

import java.util.List;
import java.util.stream.Collectors;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler({ AppProcessingException.class })
	public ResponseEntity<ErrorMessage> handleApplicationException(AppProcessingException ex) {
		HttpStatus httpStatus = ex.getHttpsStatus();
		ErrorMessage error = new ErrorMessage(httpStatus.value(), httpStatus.name(), ex.getMessage(), null);
		return new ResponseEntity<ErrorMessage>(error, httpStatus);
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {

		List<ErrorMessage> invalidFields = ex.getConstraintViolations().stream()
				.map(cv -> new ErrorMessage(HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(),
						cv.getMessage(),
						cv.getPropertyPath().toString()
								.substring(cv.getPropertyPath().toString().lastIndexOf(".") + 1)))
				.collect(Collectors.toList());
		return new ResponseEntity<>(invalidFields, HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<?> handleGlobalException(Exception ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		ErrorMessage error = new ErrorMessage(errorCode.getStatus().value(), errorCode.getStatus().name(),
				errorCode.getMessage(), null);
		return new ResponseEntity<ErrorMessage>(error, errorCode.getStatus());
	}
}
