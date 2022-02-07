package com.cgi.ms.assessment.common.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import static org.assertj.core.api.Assertions.assertThat;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AppHttpHeadersTest {

	@Test
	public void testPrivateConstructor() throws Exception {
		IllegalAccessException thrown = Assertions.assertThrows(IllegalAccessException.class, () -> {
			AppHttpHeaders.class.newInstance();
		}, "AppHttpHeaders class constructor should be private");
	}
	
	@Test
	public void testHttpHeaders() throws Exception {
		HttpHeaders headers = AppHttpHeaders.getHttpHeaders();
		assertThat(headers).isNotNull();
		assertThat(headers.get("Content-Type")).isNotNull();
	}

}
