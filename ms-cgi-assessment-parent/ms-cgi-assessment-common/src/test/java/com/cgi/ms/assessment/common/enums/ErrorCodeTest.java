package com.cgi.ms.assessment.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

public class ErrorCodeTest {

	@Test
	public void testEnumKeysTest() {
		assertThat(ErrorCode.valueOf("INTERNAL_SERVER_ERROR").equals(ErrorCode.INTERNAL_SERVER_ERROR)).isTrue();
		assertThat(ErrorCode.valueOf("SERVICE_UNAVAILABLE").equals(ErrorCode.SERVICE_UNAVAILABLE)).isTrue();
		assertThat(ErrorCode.valueOf("NO_ENDPOINT_FOUND").equals(ErrorCode.NO_ENDPOINT_FOUND)).isTrue();
		assertThat(ErrorCode.valueOf("DATA_NOT_FOUND").equals(ErrorCode.DATA_NOT_FOUND)).isTrue();
		assertThat(ErrorCode.valueOf("BAD_REQUEST").equals(ErrorCode.BAD_REQUEST)).isTrue();

	}

	@Test
	public void testEnumMessages() {
		assertThat(ErrorCode.valueOf("INTERNAL_SERVER_ERROR").getMessage()
				.equals("There was some technical issue for this operation, Please try after sometime.")).isTrue();
		assertThat(ErrorCode.valueOf("SERVICE_UNAVAILABLE").getMessage()
				.equals("Other down-stream systems are down, Please try after sometime.")).isTrue();
		assertThat(ErrorCode.valueOf("NO_ENDPOINT_FOUND").getMessage()
				.equals("Please correct your endpoint, You might be looking for diff service.")).isTrue();
		assertThat(ErrorCode.valueOf("DATA_NOT_FOUND").getMessage()
				.equals("No data found with the given information, Please try by changing your input.")).isTrue();
		assertThat(ErrorCode.valueOf("BAD_REQUEST").getMessage()
				.equals("Looks to be invalid payload passed, Please check your input data once.")).isTrue();
	}

	@Test
	public void testEnumHttpStatus() {
		assertThat(ErrorCode.valueOf("INTERNAL_SERVER_ERROR").getStatus().equals(HttpStatus.INTERNAL_SERVER_ERROR))
				.isTrue();
		assertThat(ErrorCode.valueOf("SERVICE_UNAVAILABLE").getStatus().equals(HttpStatus.SERVICE_UNAVAILABLE))
				.isTrue();
		assertThat(ErrorCode.valueOf("NO_ENDPOINT_FOUND").getStatus().equals(HttpStatus.NOT_FOUND)).isTrue();
		assertThat(ErrorCode.valueOf("DATA_NOT_FOUND").getStatus().equals(HttpStatus.NOT_FOUND)).isTrue();
		assertThat(ErrorCode.valueOf("BAD_REQUEST").getStatus().equals(HttpStatus.BAD_REQUEST)).isTrue();
	}

	@Test
	public void forceUserToaddNewTests() {
		assertThat(ErrorCode.values().length).isEqualTo(5)
				.withFailMessage("you got add newly added enum in ErrorCodeTest for unit testing.");
	}

}
