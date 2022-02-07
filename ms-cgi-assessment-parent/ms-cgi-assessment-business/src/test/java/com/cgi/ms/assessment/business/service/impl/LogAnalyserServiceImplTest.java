package com.cgi.ms.assessment.business.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.never;

import static org.assertj.core.api.Assertions.assertThat;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.model.LogInfo;
import com.cgi.ms.assessment.data.repo.IOLogDataCacheReposiotry;
import com.cgi.ms.assessment.data.repo.impl.IORecipeCacheReposiotryImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LogAnalyserServiceImplTest {

	@Mock
	private IOLogDataCacheReposiotry ioLogDataCacheReposiotry;

	@InjectMocks
	private LogAnalyserServiceImpl logAnalyserServiceImpl;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(logAnalyserServiceImpl, "invalidLogType", "invalidLogType");
	}

	private static Stream<Arguments> getLogAnalysersTestData() {
		// should display all 6 records
		return Stream.of(Arguments.of("DEBUG", Optional.ofNullable(null), mockData()), 
				// should display all 6 records as only 6 available.
				Arguments.of("DEBUG", Optional.ofNullable(100), mockData()), 
				// should display zero records as TRACE has no recrods present.					 
				Arguments.of("TRACE", Optional.ofNullable(null), mockData()), 
				// should display only 2 records																
				Arguments.of("DEBUG", Optional.of(2), mockData()), 
				// should display only 5 records
				Arguments.of("INFO", Optional.of(5), mockData()),
				// should display only 4 records
				Arguments.of("WARN", Optional.of(4), mockData()), 
				// should display only 3 records
				Arguments.of("ERROR", Optional.of(3), mockData()), 
				// should display only 1 records
				Arguments.of("FATAL", Optional.of(1), mockData()), 
				// should display no records
				Arguments.of("UNKNOWN", Optional.of(0), mockData()), 
				// should display only 6 records
				Arguments.of("debug", Optional.of(6), mockData()), 
				// should display only 0 records
				Arguments.of(null, Optional.of(0), mockData())); 
	}
	
	@ParameterizedTest(name = "{index} => logType={0}, pageSize={1}")
	@MethodSource("getLogAnalysersTestData")
	public void getLogDetailsAllLogsTest(String logType, Optional<Integer> pageSize) {
		try {

			when(ioLogDataCacheReposiotry.getLongAnalysers()).thenReturn(mockData());
			LogAnalyser logData = logAnalyserServiceImpl.getLogDetails(logType, pageSize);

			List<LogInfo> mockLogInfo = getLogInfoObjects(logType, pageSize);

			assertThat(logData.getLogType()).isEqualTo(logType); // verify requested Log Type.
			assertThat(logData.getTotalRecords()).isGreaterThanOrEqualTo(0); // verify total number available records.
			assertThat(logData.getLogInfo().size())
					.isLessThanOrEqualTo(pageSize.orElse((int) logData.getTotalRecords())); // verify results list size
			assertThat(logData.getLogInfo().equals(mockLogInfo)).isTrue(); // verify sorting

		} catch (Exception ex) {
			ex.printStackTrace();
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
		verify(ioLogDataCacheReposiotry, times(1)).getLongAnalysers();

	}

	// service method should not throw any exception other than
		// AppProcessingException, otherwise there is a code leak.
		private static Stream<Arguments> getLogAnalysersExceptionTestData() {
			return Stream.of(
					Arguments.of("TRACE", 1, new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR),
							ErrorCode.INTERNAL_SERVER_ERROR),
					Arguments.of("DEBUG", 2, new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE),
							ErrorCode.SERVICE_UNAVAILABLE),
					Arguments.of("ERROR", 4, new IllegalArgumentException(), ErrorCode.INTERNAL_SERVER_ERROR),
					Arguments.of("ERROR", 4, new RuntimeException(), ErrorCode.INTERNAL_SERVER_ERROR),
					Arguments.of(null, 0, new NullPointerException(), ErrorCode.INTERNAL_SERVER_ERROR));
		}

	@ParameterizedTest(name = "{index} => logType={0}, pageSize={1}, AppProcessingException={2}, errorCode={3}")
	@MethodSource("getLogAnalysersExceptionTestData")
	public void getLogAnalysersExceptionTest(String logType, int pageSize, Exception ex, ErrorCode errorCode) {
		try {
			when(ioLogDataCacheReposiotry.getLongAnalysers()).thenThrow(ex);
			logAnalyserServiceImpl.getLogDetails(logType, Optional.of(pageSize));

			Fail.fail("Should not reach to this line, as we are expecting exception in this flow");
		} catch (AppProcessingException e) {
			assertThat(e.getMessage()).isEqualTo(errorCode.getMessage());
			assertThat(e.getHttpsStatus()).isEqualTo(errorCode.getStatus());
		} catch (Exception e) {
			Fail.fail("In all the cases, only AppProcessingException should be thrown, so leak in the code", ex);
		}
		verify(ioLogDataCacheReposiotry, times(1)).getLongAnalysers();
	}

	private static List<LogInfo> getLogInfoObjects(String logType, Optional<Integer> pageSize) {
		Map<String, Long> logMap = new HashMap<String, Long>();
		switch ((Optional.ofNullable(logType).orElse("invalidLogType")).toUpperCase()) {
		case "TRACE":
			break;
		case "DEBUG":
			logMap = debugLogMap();
			break;
		case "INFO":
			logMap = infoLogMap();
			break;
		case "WARN":
			logMap = warnLogMap();
			break;
		case "ERROR":
			logMap = errorLogMap();
			break;
		case "FATAL":
			logMap = fatalLogMap();
			break;
		}
		return logMap.entrySet().stream().map(e -> new LogInfo(e.getValue(), e.getKey())).sorted()
				.limit(pageSize.orElse(logMap.size())).collect(Collectors.toList());
	}

	private static Map<String, Long> debugLogMap() {
		Map<String, Long> debugMessagesMap = new HashMap<String, Long>();
		debugMessagesMap.put("DEBUG test data 10", 10l);
		debugMessagesMap.put("DEBUG test data 11", 11l);
		debugMessagesMap.put("DEBUG test data 12", 12l);
		debugMessagesMap.put("DEBUG test data 13", 13l);
		debugMessagesMap.put("DEBUG test data 14", 14l);
		debugMessagesMap.put("DEBUG test data 15", 15l);
		return debugMessagesMap;
	}

	private static Map<String, Long> infoLogMap() {
		Map<String, Long> infoMessagesMap = new HashMap<String, Long>();
		infoMessagesMap.put("INFO test data 20", 20l);
		infoMessagesMap.put("INFO test data 21", 21l);
		infoMessagesMap.put("INFO test data 22", 22l);
		infoMessagesMap.put("INFO test data 23", 23l);
		infoMessagesMap.put("INFO test data 24", 24l);
		return infoMessagesMap;
	}

	private static Map<String, Long> warnLogMap() {
		Map<String, Long> warnMessagesMap = new HashMap<String, Long>();
		warnMessagesMap.put("WARN test data 30", 30l);
		warnMessagesMap.put("WARN test data 31", 31l);
		warnMessagesMap.put("WARN test data 32", 32l);
		warnMessagesMap.put("WARN test data 33", 33l);
		return warnMessagesMap;

	}

	private static Map<String, Long> errorLogMap() {
		Map<String, Long> errorMessagesMap = new HashMap<String, Long>();
		errorMessagesMap.put("ERROR test data 40", 40l);
		errorMessagesMap.put("ERROR test data 41", 41l);
		errorMessagesMap.put("ERROR test data 42", 42l);
		return errorMessagesMap;
	}

	private static Map<String, Long> fatalLogMap() {
		Map<String, Long> fatalMessagesMap = new HashMap<String, Long>();
		fatalMessagesMap.put("FATAL test data 5", 5l);
		return fatalMessagesMap;
	}

	private static Map<String, Map<String, Long>> mockData() {
		Map<String, Map<String, Long>> logMapper = new HashMap<>();
		logMapper.put("DEBUG", debugLogMap());
		logMapper.put("INFO", infoLogMap());
		logMapper.put("WARN", warnLogMap());
		logMapper.put("ERROR", errorLogMap());
		logMapper.put("FATAL", fatalLogMap());
		// no records for TRACE
		return logMapper;
	}

}
