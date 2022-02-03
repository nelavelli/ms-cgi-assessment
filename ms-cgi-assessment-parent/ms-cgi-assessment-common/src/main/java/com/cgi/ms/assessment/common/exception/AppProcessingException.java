package com.cgi.ms.assessment.common.exception;

import org.springframework.http.HttpStatus;

import com.cgi.ms.assessment.common.enums.ErrorCode;

public class AppProcessingException extends RuntimeException {
	
	private static final long serialVersionUID = -58739879323013824L;
	
	private HttpStatus httpsStatus;

	public AppProcessingException() {
		super();
	}

	public AppProcessingException(ErrorCode errorMessages) {
		super(errorMessages.getMessage());
		this.httpsStatus = errorMessages.getStatus();
	}
	
	
	public AppProcessingException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.httpsStatus = errorCode.getStatus();
	}

	public AppProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

	public HttpStatus getHttpsStatus() {
		return httpsStatus;
	}

}
