package com.cgi.ms.assessment.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.compare;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Receipe implements Comparable<Receipe>, Serializable {

	private static final long serialVersionUID = -1240783856584073644L;

	private String title;

	private String href;

	private List<String> ingredients;

	private String thumbnail;

	@Override
	public int compareTo(Receipe o) {
		return Objects.isNull(o) ? 1 : compare(this.getTitle(), o.getTitle());
	}

}
