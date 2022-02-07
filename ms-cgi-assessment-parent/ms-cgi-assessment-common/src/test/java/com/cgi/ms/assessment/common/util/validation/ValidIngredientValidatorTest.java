package com.cgi.ms.assessment.common.util.validation;

import javax.validation.ConstraintValidatorContext;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import java.util.stream.Stream;

public class ValidIngredientValidatorTest {

	private final static String INVALID_LOG_MSG = "Log Level can't be empty and only TRACE | DEBUG | INFO | WARN | ERROR | FATAL are allowed log levels.";

	@Mock
	private ValidLogLevel validLogLevel;

	@Mock
	private ConstraintValidatorContext constraintValidatorContext;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	private static Stream<Arguments> allowedLogValuesTestData() {
		return Stream.of(Arguments.of("TRACE", true), Arguments.of("trace", true), Arguments.of("DEBUG", true),
				Arguments.of("INFO", true), Arguments.of("WARN", true), Arguments.of("ERROR", true),
				Arguments.of("FATAL", true), Arguments.of("12345", false), Arguments.of("  ", false),
				Arguments.of("abc123", false), Arguments.of("#$%^&*", false), Arguments.of("abc#$%^&123*", false));
	}

	@ParameterizedTest(name = "{index} => pattern={0}, expected={1}")
	@MethodSource("allowedLogValuesTestData")
	public void testAllowedLogValues(String pattern, boolean expected) {

		when(validLogLevel.message()).thenReturn(INVALID_LOG_MSG);

		ValidLogTypeValidator validLogTypeValidator = new ValidLogTypeValidator();
		validLogTypeValidator.initialize(validLogLevel);

		boolean result = validLogTypeValidator.isValid(pattern, constraintValidatorContext);

		assertThat(result).isEqualTo(expected).withFailMessage("Loglevel validator is allowing unrecognazied values.");
	}
}