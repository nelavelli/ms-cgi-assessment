package com.cgi.ms.assessment.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorMessage implements Serializable {
	
	private static final long serialVersionUID = 7564551719118441163L;
	
	private Integer statusCode;
	
	private String status;
	
	private String message;
	
	private String property;

	public ErrorMessage() {

	}

	public ErrorMessage(Integer statusCode, String status, String message, String property) {
		this.statusCode = statusCode;
		this.status = status;
		this.message = message;
		this.property = property;
	}

}
