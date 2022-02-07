package com.cgi.ms.assessment.common.model;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static org.apache.commons.lang3.StringUtils.compare;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@EqualsAndHashCode(callSuper = false)
public class Recipe extends Response implements Comparable<Recipe>, Serializable {

	private static final long serialVersionUID = -1240783856584073644L;

	private String title;

	private String href;

	private List<String> ingredients;

	private String thumbnail;

	@Override
	public int compareTo(Recipe o) {
		return Objects.isNull(o) ? 1 : compare(this.getTitle(), o.getTitle());
	}
	
	public Recipe setTitle(String title) {
		this.title = title;
		return this;
	}
	
	public Recipe setHref(String href) {
		this.href = href;
		return this;
	}
	
	public Recipe setIngredients(List<String> ingredients) {
		this.ingredients = ingredients;
		return this;
	}
	
	public Recipe setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
		return this;
	}
}
