package com.cgi.ms.assessment.business.service.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cgi.ms.assessment.business.service.LogAnalyserService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.model.LogInfo;
import com.cgi.ms.assessment.data.repo.IOLogDataCacheReposiotry;
import com.cgi.ms.assessment.data.repo.IORecipeCacheReposiotry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class LogAnalyserServiceImpl implements LogAnalyserService {

	private @Autowired IOLogDataCacheReposiotry ioLogDataCacheReposiotry;

	@Value("${com.cgi.ms.assessment.business.invalid.logType}")
	private String invalidLogType;

	@Override
	public LogAnalyser getLogDetails(String type, Optional<Integer> pageSize) {

		try {
			log.debug("type --> {}, pageSize present ---> {}", type, pageSize.isPresent());
			Map<String, Long> loggerMap = ioLogDataCacheReposiotry.getLongAnalysers()
					.get(Optional.ofNullable(type).orElse(invalidLogType).toUpperCase());

			if (Objects.isNull(loggerMap) || loggerMap.isEmpty()) {
				return LogAnalyser.builder().logType(type).totalRecords(0).logInfo(Collections.<LogInfo>emptyList()).build();
			}

			List<LogInfo> logInfo = loggerMap.entrySet().stream().map(e -> new LogInfo(e.getValue(), e.getKey()))
					.sorted().limit(pageSize.orElse(loggerMap.size())).collect(Collectors.toList());

			return LogAnalyser.builder().logType(type).totalRecords(loggerMap.size()).logInfo(logInfo).build();

		} catch (AppProcessingException apex) {
			log.error("Exception while building the log object", apex);
			throw apex;
		} catch (Exception ex) {
			log.error("Unknown error while looking for log information", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}
}
