package com.cgi.ms.assessment.web.handler;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Response;
import static com.cgi.ms.assessment.common.util.AppHttpHeaders.getHttpHeaders;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolationException;

@RestControllerAdvice
public class ApiExceptionHandler {

	@ExceptionHandler({ AppProcessingException.class })
	public ResponseEntity<Response> handleApplicationException(AppProcessingException ex) {
		HttpStatus httpStatus = ex.getHttpsStatus();
		Response error = new Response(httpStatus.value(), httpStatus.name(), ex.getMessage(), null);
		return new ResponseEntity<Response>(error, getHttpHeaders(), httpStatus);
	}

	@ExceptionHandler(value = ConstraintViolationException.class)
	public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {

		Optional<Response> invalidField = ex.getConstraintViolations().stream().map(cv -> new Response(
				HttpStatus.BAD_REQUEST.value(), HttpStatus.BAD_REQUEST.name(), cv.getMessage(),
				cv.getPropertyPath().toString().substring(cv.getPropertyPath().toString().lastIndexOf(".") + 1)))
				.findFirst();
		return new ResponseEntity<>(invalidField.get(), getHttpHeaders(), HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(value = { NoHandlerFoundException.class, MissingServletRequestParameterException.class })
	public ResponseEntity<?> handleError404(HttpServletRequest request, Exception e) {
		ErrorCode errorCode = ErrorCode.NO_ENDPOINT_FOUND;
		Response error = new Response(errorCode.getStatus().value(), errorCode.getStatus().name(),
				errorCode.getMessage(), null);
		return new ResponseEntity<Response>(error, getHttpHeaders(), errorCode.getStatus());
	}

	@ExceptionHandler(value = Exception.class)
	public ResponseEntity<?> handleGlobalException(Exception ex) {
		ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
		Response error = new Response(errorCode.getStatus().value(), errorCode.getStatus().name(),
				errorCode.getMessage(), null);
		return new ResponseEntity<Response>(error, getHttpHeaders(), errorCode.getStatus());
	}

}
