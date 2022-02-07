package com.cgi.ms.assessment.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.ms.assessment.business.service.LogAnalyserService;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.util.validation.ValidLogLevel;

import static com.cgi.ms.assessment.web.constants.WebConstants.LOG_ANALYSER_ENDPOINT;
import static com.cgi.ms.assessment.common.util.AppHttpHeaders.getHttpHeaders;

import java.util.Optional;

import javax.validation.constraints.Min;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@Validated
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class LogAnalyserController {

	private @Autowired LogAnalyserService analyserService;

	@GetMapping(value = LOG_ANALYSER_ENDPOINT)
	public ResponseEntity<LogAnalyser> getLogDetails(@PathVariable(required = true) @ValidLogLevel String logType,  @RequestParam(value = "pageSize") Optional<@Min(value = 1, message = "{com.cgi.ms.web.log.valid.pagesize.value}") Integer> pageSize) {
		try {
			log.info("requested log Type --> {} , pageSize --> {} ", logType, pageSize.orElse(null));
			LogAnalyser analyser = analyserService.getLogDetails(logType, pageSize);
			return new ResponseEntity<LogAnalyser>(analyser, getHttpHeaders(), HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for recipes", ex);
			throw ex;
		}
	}
}
