package com.cgi.ms.assessment.web.controller;

import java.util.Collection;
import java.util.List;

import javax.validation.constraints.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cgi.ms.assessment.business.service.RecipeService;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Recipe;

import lombok.extern.slf4j.Slf4j;

import static com.cgi.ms.assessment.web.constants.WebConstants.RECIPE_ENDPOINT;
import static com.cgi.ms.assessment.web.constants.WebConstants.RECIPES_FILTER_ENDPOINT;
import static com.cgi.ms.assessment.common.util.AppHttpHeaders.getHttpHeaders;

@RestController
@Slf4j
@Validated
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class RecipeController {

	private @Autowired RecipeService recipeService;

	@GetMapping(value = RECIPE_ENDPOINT)
	public ResponseEntity<?> getRecipes() {
		try {
			Collection<Recipe> recipes = recipeService.getRecipes();
			return new ResponseEntity<Collection<Recipe>>(recipes, getHttpHeaders(), HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for recipes", ex);
			throw ex;
		}
	}

	@GetMapping(value = RECIPES_FILTER_ENDPOINT)
	public ResponseEntity<Collection<Recipe>> getRecipesByIngredients(
			@RequestBody @PathVariable List<@Pattern(regexp = "^([a-zA-Z\\s])+$", message = "{com.cgi.ms.web.recipe.valid.ingredients.format.value}") String> ingredients) {
		try {
			log.info("Requested ingredients list --> {}", ingredients);
			Collection<Recipe> recipes = recipeService.getRecipesByIngredients(ingredients);
			return new ResponseEntity<Collection<Recipe>>(recipes, getHttpHeaders(), HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for recipes", ex);
			throw ex;
		}
	}
}
