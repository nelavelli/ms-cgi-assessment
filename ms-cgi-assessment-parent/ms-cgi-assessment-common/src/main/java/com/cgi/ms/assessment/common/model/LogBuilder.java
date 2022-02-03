package com.cgi.ms.assessment.common.model;

import java.io.Serializable;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LogBuilder implements Serializable {

	private static final long serialVersionUID = -2891441540584191375L;

	private String description;
	
	private String logType;
}
