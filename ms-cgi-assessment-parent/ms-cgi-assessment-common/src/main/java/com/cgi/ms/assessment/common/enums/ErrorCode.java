package com.cgi.ms.assessment.common.enums;

import org.springframework.http.HttpStatus;

public enum ErrorCode {

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR,
			"There was some technical issue for this operation, Please try after sometime."),
	DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "No data found with the given information, Please try by changing your input."),
	BAD_REQUEST(HttpStatus.BAD_REQUEST, "Looks to be invalid payload passed, Please check your input data once.");

	private HttpStatus status;

	private String message;

	private ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

	public String getMessage() {
		return this.message;
	}

	public HttpStatus getStatus() {
		return this.status;
	}
}
