package com.cgi.ms.assessment.common.model;

import java.io.Serializable;

import lombok.Data;

@Data
public class Ingredient implements Serializable {

	private static final long serialVersionUID = -2160824486579130049L;

	String title;

	Receipe receipe;

	public Ingredient(String title, Receipe receipe) {
		this.title = title;
		this.receipe = receipe;
	}

}
