package com.cgi.ms.assessment.common.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogAnalyser implements Serializable {

	private static final long serialVersionUID = -2142602704589813193L;

	private String logLevel;

	private List<LogInfo> logInfo;

}
