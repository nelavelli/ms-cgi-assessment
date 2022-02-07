package com.cgi.ms.assessment.common.enums;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class LogLevelTest {

	@Test
	public void testEnumKeysTest() {
		assertThat(LogLevel.valueOf("TRACE").equals(LogLevel.TRACE)).isTrue();
		assertThat(LogLevel.valueOf("DEBUG").equals(LogLevel.DEBUG)).isTrue();
		assertThat(LogLevel.valueOf("INFO").equals(LogLevel.INFO)).isTrue();
		assertThat(LogLevel.valueOf("WARN").equals(LogLevel.WARN)).isTrue();
		assertThat(LogLevel.valueOf("ERROR").equals(LogLevel.ERROR)).isTrue();
		assertThat(LogLevel.valueOf("FATAL").equals(LogLevel.FATAL)).isTrue();
	}

	@Test
	public void forceUserToaddNewTests() {
		assertThat(LogLevel.values().length).isEqualTo(6)
				.withFailMessage("you got add newly added enum in ErrorCodeTest for unit testing.");
	}
}
