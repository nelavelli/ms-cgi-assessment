package com.cgi.ms.assessment.business.service;

import java.util.Optional;

import com.cgi.ms.assessment.common.model.LogAnalyser;

public interface LogAnalyserService {

	LogAnalyser getLogDetails(String type,  Optional<Integer> pageSize);
}
