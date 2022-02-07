package com.cgi.ms.assessment.common.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class LogAnalyser extends Response implements Serializable {

	private static final long serialVersionUID = -2142602704589813193L;

	private String logType;

	private long totalRecords;

	private List<LogInfo> logInfo;
	

}
