package com.cgi.ms.assessment.common.util.validation;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.cgi.ms.assessment.common.enums.LogLevel;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ValidLogTypeValidator implements ConstraintValidator<ValidLogLevel, String> {

	private static final Set<String> allowedValues = Stream.of(LogLevel.values()).map(e -> e.name())
			.collect(Collectors.toSet());;

	@Override
	public boolean isValid(String logLevel, ConstraintValidatorContext context) {
		log.debug("Validator Log Level--- {}, isAllowed - {} ", logLevel, allowedValues.contains(logLevel.toUpperCase()));
		return (!logLevel.isEmpty() && allowedValues.contains(logLevel.toUpperCase()));
	}

}