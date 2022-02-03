package com.cgi.ms.assessment.common.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LogInfo implements Comparable<LogInfo>, Serializable {

	private static final long serialVersionUID = -2891441540584191375L;

	private long count;

	private String description;

	public LogInfo(long count, String description) {
		this.count = count;
		this.description = description;
	}

	@Override
	public int compareTo(LogInfo o) {
		return (int) (o.getCount() - this.getCount());
	}

}
