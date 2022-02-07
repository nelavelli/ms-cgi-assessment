package com.cgi.ms.assessment.common.util;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public final class AppHttpHeaders {

	private AppHttpHeaders() {
	}

	public static HttpHeaders getHttpHeaders() {
		final HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		return httpHeaders;
	}
}
