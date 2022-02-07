package com.cgi.ms.assessment.business.service;

import java.util.Collection;
import java.util.List;

import com.cgi.ms.assessment.common.model.Recipe;

public interface RecipeService {

	Collection<Recipe> getRecipes();

	Collection<Recipe> getRecipesByIngredients(List<String> ingredients);

}
