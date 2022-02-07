package com.cgi.ms.assessment.data.repo;

import java.util.List;
import java.util.Map;

import com.cgi.ms.assessment.common.model.Recipe;

public interface IORecipeCacheReposiotry {

	public Map<String, List<Recipe>> getRecipesByIngredients();

	public List<Recipe> readRecipesFromFile();

}
