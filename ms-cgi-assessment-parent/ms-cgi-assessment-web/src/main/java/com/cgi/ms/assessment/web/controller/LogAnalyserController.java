package com.cgi.ms.assessment.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.ms.assessment.business.service.LogAnalyserService;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.util.validation.ValidLogLevel;

import static com.cgi.ms.assessment.web.constants.WebConstants.LOG_ANALYSER_ENDPOINT;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping
@Slf4j
@Validated
public class LogAnalyserController {

	private @Autowired LogAnalyserService analyserService;

	@GetMapping(value = LOG_ANALYSER_ENDPOINT, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<LogAnalyser> getLogDetails(@PathVariable(required = true) @ValidLogLevel String logLevel) {
		try {
			log.info(" log type requested  {} ", logLevel);
			LogAnalyser analyser = analyserService.getLogDetails(logLevel);
			return new ResponseEntity<LogAnalyser>(analyser, HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for receipes", ex);
			throw ex;
		}
	}
}
