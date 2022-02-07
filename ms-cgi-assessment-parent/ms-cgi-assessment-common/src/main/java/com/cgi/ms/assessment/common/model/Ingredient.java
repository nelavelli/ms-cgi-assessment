package com.cgi.ms.assessment.common.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Ingredient implements Serializable {

	private static final long serialVersionUID = -2160824486579130049L;

	String title;

	Recipe recipe;

	public Ingredient(String title, Recipe recipe) {
		this.title = title;
		this.recipe = recipe;
	}

}
