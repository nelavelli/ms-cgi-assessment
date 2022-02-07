package com.cgi.ms.assessment.data.repo.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogBuilder;
import com.cgi.ms.assessment.data.repo.IOLogDataCacheReposiotry;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IOLogDataCacheReposiotryImpl implements IOLogDataCacheReposiotry {

	@Value("classpath:data/logFile-2018-09-10.log")
	private Resource logFile;

	@Value("${com.cgi.ms.assessment.data.log.pattern}")
	private String logPattern;

	@Cacheable(key = "'logAnalyser'", value = "logAnalyserCache")
	@Override
	public Map<String, Map<String, Long>> getLongAnalysers() {

		try {
			Pattern pattern = Pattern.compile(logPattern);
			return new BufferedReader(new InputStreamReader(logFile.getInputStream())).lines()
					.filter(line -> line.matches(logPattern)).map(line -> {
						Matcher matcher = pattern.matcher(line);
						if (!matcher.matches()) {
							log.warn("Bad log or problem with RE? ");
							log.info(line);
						}
						return LogBuilder.builder().logType(matcher.group(3)).description(matcher.group(7)).build();
					}).collect(Collectors.groupingBy(LogBuilder::getLogType,
							Collectors.groupingBy(LogBuilder::getDescription, Collectors.counting())));

		} catch (IOException ex) {
			log.error("IO Exception occured while reading data from logFile file ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		} catch (Exception ex) {
			log.error("Unkown Exception occured while reading data from logFile file ", ex);
			throw new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE, ex);
		}
	}


}
