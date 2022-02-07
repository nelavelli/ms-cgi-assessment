package com.cgi.ms.assessment.web.controller;

import org.assertj.core.api.Fail;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cgi.ms.assessment.business.service.LogAnalyserService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.model.LogInfo;
import com.cgi.ms.assessment.common.model.Response;
import com.cgi.ms.assessment.web.constants.WebConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(LogAnalyserController.class)
@Slf4j
public class LogAnalyserControllerTest {

	private static final ObjectMapper mapper = new ObjectMapper();
	
	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private LogAnalyserService logAnalyserService;

	@ParameterizedTest(name = "{index} => logType={0}, pageSize={1}, logDetails={2}")
	@MethodSource("getLogAnalysersTestData")
	public void getLogDetails(String logType, Optional<Integer> pageSize, LogAnalyser logDetails) {

		try {
			when(logAnalyserService.getLogDetails(logType, pageSize)).thenReturn(logDetails);

			String uri = WebConstants.LOG_ANALYSER_ENDPOINT.replace("{logType}",
					"/" + logType + (pageSize.isPresent() ? "?pageSize=" + pageSize.get() : ""));
			mockMvc.perform(get(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(content().json(mapper.writeValueAsString(logDetails)))
					.andExpect(content().json("{'logType':" + logType + "}"))
					.andExpect(content().json("{'totalRecords':" + logDetails.getTotalRecords() + "}"));
		} catch (Exception ex) {
			Fail.fail("control should reach to exception block, something went wrong.");
		}
		verify(logAnalyserService, times(1)).getLogDetails(logType, pageSize);
		verifyNoMoreInteractions(logAnalyserService);
	}

	private static Stream<Arguments> getLogAnalysersTestData() {
		return Stream.of(
				// should display all 6 records
				Arguments.of("DEBUG", Optional.ofNullable(null), getLogAnalyser("DEBUG", Optional.ofNullable(null))),
				// should display all 6 records as only 6 available.
				Arguments.of("DEBUG", Optional.of(100), getLogAnalyser("DEBUG", Optional.of(100))),
				// should display zero records as TRACE has no records present.
				Arguments.of("TRACE", Optional.ofNullable(null), getLogAnalyser("TRACE", Optional.ofNullable(null))),
				// should display only 2 records
				Arguments.of("DEBUG", Optional.of(2), getLogAnalyser("DEBUG", Optional.of(2))),
				// should display only 5 records
				Arguments.of("INFO", Optional.of(5), getLogAnalyser("INFO", Optional.of(5))),
				// should display only 4 records
				Arguments.of("WARN", Optional.of(4), getLogAnalyser("WARN", Optional.of(4))),
				// should display only 3 records
				Arguments.of("ERROR", Optional.of(3), getLogAnalyser("ERROR", Optional.of(3))),
				// should display only 1 records
				Arguments.of("FATAL", Optional.of(1), getLogAnalyser("FATAL", Optional.of(1))),
				// should display no records
				Arguments.of("debug", Optional.of(6), getLogAnalyser("debug", Optional.of(6)))
		// should display only 0 records
		);
	}

	@ParameterizedTest(name = "{index} => logType={0}, pageSize={1}, errorMessage={2}, matcher={3}, errorCode={4}")
	@MethodSource("testGetLogAnalyserAPIErrorsTestData")
	public void testGetLogAnalyserAPIErrors(String logType, Optional<Integer> pageSize, String errorMessage,
			ResultMatcher matcher, ErrorCode errorCode) {

		try {
			String uri = WebConstants.LOG_ANALYSER_ENDPOINT.replace("{logType}",
					"/" + logType + (pageSize.isPresent() ? "?pageSize=" + pageSize.get() : ""));

			if (Objects.nonNull(errorCode)) {
				when(logAnalyserService.getLogDetails(logType, pageSize))
						.thenThrow(new AppProcessingException(errorCode));
			}
			MvcResult results = mockMvc
					.perform(get(uri).contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(matcher).andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andReturn();

			assertThat(mapper.readValue(results.getResponse().getContentAsString(), Response.class).getMessage()
					.equals(errorMessage)).isTrue();

		} catch (Exception ex) {
			ex.printStackTrace();
			Fail.fail("control should not reach to exception block, something went wrong.");
		}
	}

	private static Stream<Arguments> testGetLogAnalyserAPIErrorsTestData() {
		String invaldiLogType = "Log Level can't be empty and only TRACE | DEBUG | INFO | WARN | ERROR | FATAL are allowed log levels.";
		String invalidPageSize = "Page Size should be Minimum 1";
		return Stream.of(
				// invalid log type.
				Arguments.of("unknownLogType", Optional.of(10), invaldiLogType, status().isBadRequest(), null),
				// 2 logs in one request.
				Arguments.of("ERROR, FATAL", Optional.of(10), invaldiLogType, status().isBadRequest(), null),
				// number types not allowed records
				Arguments.of("123456", Optional.of(10), invaldiLogType, status().isBadRequest(), null),
				// Empty not allowed records
				Arguments.of("   ", Optional.of(10), invaldiLogType, status().isBadRequest(), null),
				// alphanumeric not allowed 4 records
				Arguments.of("alpha123", Optional.of(10), invaldiLogType, status().isBadRequest(), null),
				// invalid page number - zero
				Arguments.of("DEBUG", Optional.of(0), invalidPageSize, status().isBadRequest(), null),
				// invalid page number minus one.
				Arguments.of("DEBUG", Optional.of(-1), invalidPageSize, status().isBadRequest(), null),
				// internal server error
				Arguments.of("DEBUG", Optional.of(10), ErrorCode.INTERNAL_SERVER_ERROR.getMessage(),
						status().isInternalServerError(), ErrorCode.INTERNAL_SERVER_ERROR),
				// service unavailable.
				Arguments.of("DEBUG", Optional.of(10), ErrorCode.SERVICE_UNAVAILABLE.getMessage(),
						status().isServiceUnavailable(), ErrorCode.SERVICE_UNAVAILABLE));
	}

	private static LogAnalyser getLogAnalyser(String logType, Optional<Integer> pageSize) {

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

		List<LogInfo> logInfo = logMap.entrySet().stream().map(e -> new LogInfo(e.getValue(), e.getKey())).sorted()
				.limit(pageSize.orElse(logMap.size())).collect(Collectors.toList());
		return LogAnalyser.builder().logType(logType).totalRecords(logMap.size()).logInfo(logInfo).build();
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
}
