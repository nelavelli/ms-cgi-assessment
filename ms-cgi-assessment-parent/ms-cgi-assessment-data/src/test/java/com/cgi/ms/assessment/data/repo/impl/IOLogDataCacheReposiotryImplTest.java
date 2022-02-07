package com.cgi.ms.assessment.data.repo.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import com.cgi.ms.assessment.common.enums.LogLevel;
import com.cgi.ms.assessment.common.exception.AppProcessingException;

public class IOLogDataCacheReposiotryImplTest {

	@InjectMocks
	private IOLogDataCacheReposiotryImpl ioLogDataCacheReposiotryImpl;

	@Value("${com.cgi.ms.assessment.data.log.pattern}")
	private String logPattern;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(ioLogDataCacheReposiotryImpl, "logFile",
				new ClassPathResource("data/logFile-2018-09-10.log"));
		ReflectionTestUtils.setField(ioLogDataCacheReposiotryImpl, "logPattern",
				"^([\\d-]+\\s[\\d:]+,\\d+)(\\s)(\\w+)(\\s+)(\\[\\w.+\\])(\\s+)(\\w.+)");
	}

	@Test
	public void getLongAnalysersTest() {

		Map<String, Map<String, Long>> logMap = ioLogDataCacheReposiotryImpl.getLongAnalysers();

		assertThat(logMap.size()).isEqualTo(4).withFailMessage("there were only 4 log types avaiable in the log file.");

		assertThat(logMap.get(LogLevel.DEBUG.name()).size()).isEqualTo(9)
				.withFailMessage("there were only 9 log DEBUG messages of same text avaiable in the log file.");

		assertThat(logMap.get(LogLevel.INFO.name()).size()).isEqualTo(6)
				.withFailMessage("there were only 6 log INFO messages of same text avaiable in the log file.");

		assertThat(logMap.get(LogLevel.WARN.name()).size()).isEqualTo(2)
				.withFailMessage("there were only 2 log INFO messages of same text avaiable in the log file.");

		assertThat(logMap.get(LogLevel.ERROR.name()).size()).isEqualTo(3)
				.withFailMessage("there were only 3 log ERROR messages of same text avaiable in the log file.");

		assertThat(logMap.get(LogLevel.WARN.name()).size()).isEqualTo(2)
				.withFailMessage("there were only 2 log ERROR messages of same text avaiable in the log file.");

		assertThat(logMap.get(LogLevel.TRACE.name()))
				.withFailMessage("There were no TRACE log types avaiable in the log file.").isNull();

		assertThat(logMap.get(LogLevel.FATAL.name()))
				.withFailMessage("There were no FATAL log types avaiable in the log file.").isNull();

	}

	@Test
	public void testInvalidLogFileException() {
		try {
			ReflectionTestUtils.setField(ioLogDataCacheReposiotryImpl, "logFile",
					new ClassPathResource("data/receipe.json"));
			Map<String, Map<String, Long>> logFile = ioLogDataCacheReposiotryImpl.getLongAnalysers();
			assertThat(logFile.size()).isEqualTo(0);
		} catch (AppProcessingException apEx) {
			apEx.printStackTrace();
		}
	}

}
