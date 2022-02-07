package com.cgi.ms.assessment.data.repo.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Ingredient;
import com.cgi.ms.assessment.common.model.Recipe;
import com.cgi.ms.assessment.data.repo.IORecipeCacheReposiotry;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IORecipeCacheReposiotryImpl implements IORecipeCacheReposiotry {

	private final static ObjectMapper mapper = new ObjectMapper();

	@Value("classpath:data/receipe.json")
	private Resource recipeFile;

	@Cacheable(key = "'allIngredients'", value = "allIngredientCache")
	public Map<String, List<Recipe>> getRecipesByIngredients() {
		try {
			log.info(" loading from origial data source ");
			return this.readRecipesFromFile().stream().map(recipe -> {
				return recipe.getIngredients().stream().map(ing -> new Ingredient(ing, recipe))
						.collect(Collectors.toList());
			}).flatMap(ingredients -> ingredients.stream()).collect(Collectors.groupingBy(Ingredient::getTitle,
					Collectors.mapping(Ingredient::getRecipe, Collectors.toList())));
		} catch (AppProcessingException ex) {
			log.error("AppProcessingException while looking up into data file, ", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("Exception while processing the recipe data.");
			throw new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE, ex);
		}
	}

	@Cacheable(key = "'allRecipes'", value = "allRecipeCache")
	public List<Recipe> readRecipesFromFile() {
		try {
			return mapper.readValue(recipeFile.getInputStream(), new TypeReference<List<Recipe>>() {
			});
		} catch (IOException ex) {
			log.error("IO Exception occured while reading data from recipe.json file ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR);
		} catch (Exception ex) {
			log.error("Unkown Exception occured while reading data from recipe.json file ", ex);
			throw new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE, ex);
		}
	}

}