package com.cgi.ms.assessment.business.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cgi.ms.assessment.business.service.LogAnalyserService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.model.LogInfo;
import com.cgi.ms.assessment.data.repo.IOCacheReposiotry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogAnalyserServiceImpl implements LogAnalyserService {

	private @Autowired IOCacheReposiotry ioCacheReposiotry;

	@Override
	public LogAnalyser getLogDetails(String type) {

		try {

			Map<String, Long> loggerMap = ioCacheReposiotry.getLongAnalysers().get(type.toUpperCase());

			if (Objects.isNull(loggerMap) || loggerMap.isEmpty()) {
				return LogAnalyser.builder().logLevel(type).logInfo(Collections.<LogInfo>emptyList()).build();
			}

			List<LogInfo> logInfo = loggerMap.entrySet().stream().map(e -> new LogInfo(e.getValue(), e.getKey()))
					.sorted().collect(Collectors.toList());

			return LogAnalyser.builder().logLevel(type).logInfo(logInfo).build();

		} catch (AppProcessingException apex) {
			log.error("Exception while building the log object", apex);
			throw apex;
		} catch (Exception ex) {
			log.error("Unknown error while looking for log information", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
}
