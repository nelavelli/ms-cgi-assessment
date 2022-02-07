package com.cgi.ms.assessment.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;

import com.cgi.ms.assessment.business.service.RecipeService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Ingredient;
import com.cgi.ms.assessment.common.model.Recipe;
import com.cgi.ms.assessment.common.model.Response;
import com.cgi.ms.assessment.web.constants.WebConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@WebMvcTest(RecipeController.class)
@Slf4j
public class RecipeControllerTest {

	private static final ObjectMapper mapper = new ObjectMapper();

	@Autowired
	private MockMvc mockMvc;

	@MockBean
	private RecipeService recipeService;

	@Test
	public void testGetRecipesAPIHappyPath() {
		try {
			when(recipeService.getRecipes()).thenReturn(recipesMockData());
			mockMvc.perform(get(WebConstants.RECIPE_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
					.accept(MediaType.APPLICATION_JSON)).andDo(print()).andExpect(status().isOk())
					.andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(content().json(mapper.writeValueAsString(recipesMockData())));
		} catch (Exception e) {
			Fail.fail("control should not reach to exception block, something went wrong.");
		}
		verify(recipeService, times(1)).getRecipes();
		verifyNoMoreInteractions(recipeService);
	}

	private static Stream<Arguments> getRecipesAPIErrorTestData() {
		return Stream.of(
				// throw internal server error with mock and check the status message.
				Arguments.of(null, new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR),
						status().isInternalServerError(), ErrorCode.INTERNAL_SERVER_ERROR),
				// throw service unavailable error with mock and check the status message.
				Arguments.of(null, new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE),
						status().isServiceUnavailable(), ErrorCode.SERVICE_UNAVAILABLE),
				// throw unknown error and check the status message.
				Arguments.of(null, new RuntimeException(), status().isInternalServerError(),
						ErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ParameterizedTest(name = "{index} => responseRecipes={0}, matcher={1}, errorCode={2}")
	@MethodSource("getRecipesAPIErrorTestData")
	public void testGetRecipesAPIErrorScenario(List<Recipe> responseRecipes, Exception ex, ResultMatcher matcher,
			ErrorCode errorCode) {
		try {
			when(recipeService.getRecipes()).thenThrow(ex);
			ResultActions action = mockMvc
					.perform(get(WebConstants.RECIPE_ENDPOINT).contentType(MediaType.APPLICATION_JSON)
							.accept(MediaType.APPLICATION_JSON))
					.andDo(print()).andExpect(matcher).andExpect(content().contentType(MediaType.APPLICATION_JSON));
			assertThat(mapper.readValue(action.andReturn().getResponse().getContentAsString(), Response.class)
					.getMessage().equals(errorCode.getMessage())).isTrue();
		} catch (Exception e) {
			Fail.fail("control should not reach to exception block, something went wrong.");
		}
		verify(recipeService, times(1)).getRecipes();
		verifyNoMoreInteractions(recipeService);
	}
	 
	private static Stream<Arguments> getRecipesByIngredientsTestData() {
		return Stream.of(
		Arguments.of("onions", mockIngredientRecipes("onions").stream().sorted().collect(Collectors.toList())),
		Arguments.of("salmon,onions,chicken",mockIngredientRecipes("salmon,onions,chicken").stream().sorted().collect(Collectors.toList())));
	}

	@ParameterizedTest(name = "{index} => ingredients={0}, expectedRecipes={1}, isErrorScenario={2}, Exception={3}, errorCode={4}")
	@MethodSource("getRecipesByIngredientsTestData")
	public void getRecipesByIngredients(String ingredients, List<Recipe> expectedRecipes) {

		try {
			when(recipeService.getRecipesByIngredients(Arrays.asList(ingredients.split(","))))
					.thenReturn(expectedRecipes);

			mockMvc.perform(get(WebConstants.RECIPES_FILTER_ENDPOINT.replace("{ingredients}", ingredients))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
					.andExpect(status().isOk()).andExpect(content().contentType(MediaType.APPLICATION_JSON))
					.andExpect(content().json(mapper.writeValueAsString(expectedRecipes)));
		} catch (Exception ex) {
			Fail.fail("In all the cases, only AppProcessingException should be thrown, so leak in the code", ex);
		}
		verify(recipeService, times(1)).getRecipesByIngredients(Arrays.asList(ingredients.split(",")));
		verifyNoMoreInteractions(recipeService);
	}

	private static Stream<Arguments> getRecipesByIngredientsErrorTestData() {
		return Stream.of( 
				Arguments.of("onions", new AppProcessingException(ErrorCode.DATA_NOT_FOUND), ErrorCode.DATA_NOT_FOUND, status().isNotFound()),
				Arguments.of("salmon,onions,chicken", new AppProcessingException(ErrorCode.DATA_NOT_FOUND), ErrorCode.DATA_NOT_FOUND, status().isNotFound()),
				Arguments.of("salmon,onions,chicken", new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR), ErrorCode.INTERNAL_SERVER_ERROR, status().isInternalServerError()), 
				Arguments.of("salmon,onions,chicken", new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE), ErrorCode.SERVICE_UNAVAILABLE, status().isServiceUnavailable()), 
				Arguments.of("salmon,onions,chicken", new RuntimeException("Unkonwn Exception"), ErrorCode.INTERNAL_SERVER_ERROR, status().isInternalServerError()));
	}
	
	@ParameterizedTest(name = "{index} => ingredients={0}, exception={1}, errorCode={2}")
	@MethodSource("getRecipesByIngredientsErrorTestData")
	public void testGetRecipesByIngredientsError(String ingredients, Exception exception, ErrorCode errorCode, ResultMatcher matcher) {

		try {
			when(recipeService.getRecipesByIngredients(Arrays.asList(ingredients.split(",")))).thenThrow(exception);

			MvcResult results =  mockMvc.perform(get(WebConstants.RECIPES_FILTER_ENDPOINT.replace("{ingredients}", ingredients))
					.contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)).andDo(print())
					.andExpect(matcher).andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
			assertThat(mapper.readValue(results.getResponse().getContentAsString(), Response.class)
					.getMessage().equals(errorCode.getMessage())).isTrue();	
			
		} catch (Exception ex) {
			Fail.fail("In all the cases, only AppProcessingException should be thrown, so leak in the code", ex);
		}
		verify(recipeService, times(1)).getRecipesByIngredients(Arrays.asList(ingredients.split(",")));
		verifyNoMoreInteractions(recipeService);
	}
	
	private static List<Recipe> mockIngredientRecipes(String ingredient) {
		if (Objects.nonNull(ingredient) && ingredient.split(",").length == 1)
			return ingredientMockData().get(ingredient);

		return Arrays.asList(ingredient.split(",")).stream()
				.map(ele -> Optional.ofNullable(mockIngredientRecipes(ele)).orElse(Collections.<Recipe>emptyList()))
				.flatMap(List::stream).collect(Collectors.toList());
	}

	private static Map<String, List<Recipe>> ingredientMockData() {

		Map<String, List<Recipe>> ingredientsMap = recipesMockData().stream().map(recipe -> {
			return recipe.getIngredients().stream().map(ing -> new Ingredient(ing, recipe))
					.collect(Collectors.toList());
		}).flatMap(ingredients -> ingredients.stream()).collect(Collectors.groupingBy(Ingredient::getTitle,
				Collectors.mapping(Ingredient::getRecipe, Collectors.toList())));

		return ingredientsMap;

	}

	private static List<Recipe> recipesMockData() {

		List<Recipe> recipies = new ArrayList<>();

		recipies.add(new Recipe().setTitle("Creamy Scrambled Eggs Recipe Recipe")
				.setHref("http://www.grouprecipes.com/43522/creamy-scrambled-eggs-recipe.html")
				.setIngredients(Arrays.asList("onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/373064.jpg"));

		recipies.add(
				new Recipe().setTitle("Blue Ribbon Meatloaf").setHref("http://www.eatingwell.com/recipes/meatloaf.html")
						.setIngredients(Arrays.asList("onions".split(",")))
						.setThumbnail("http://img.recipepuppy.com/373064.jpg"));

		recipies.add(new Recipe().setTitle("Spaghetti with Clams & Corn")
				.setHref("http://www.eatingwell.com/recipes/spaghetti_clams_corn.html")
				.setIngredients(Arrays.asList("onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/698569.jpg"));

		recipies.add(new Recipe().setTitle("Green Bean Casserole")
				.setHref("http://www.eatingwell.com/recipes/healthy_green_bean_casserole.html")
				.setIngredients(Arrays.asList("onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/707237.jpg"));

		recipies.add(new Recipe().setTitle("Broccoli Casserole Recipe")
				.setHref("http://cookeatshare.com/recipes/broccoli-casserole-59082")
				.setIngredients(Arrays.asList("onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/780513.jpg"));

		recipies.add(new Recipe().setTitle("Crock Pot Caramelized Onions")
				.setHref("http://www.recipezaar.com/Crock-Pot-Caramelized-Onions-191625")
				.setIngredients(Arrays.asList("butter, onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/338845.jpg"));

		recipies.add(new Recipe().setTitle("Pulled Chicken Sandwiches (Crock Pot)")
				.setHref("http://www.recipezaar.com/Pulled-Chicken-Sandwiches-Crock-Pot-242547")
				.setIngredients(Arrays.asList("chicken, onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/107122.jpg"));

		recipies.add(new Recipe().setTitle("Grilled Chipotle Salmon With Pineapple Cilantro Rice")
				.setHref("http://www.recipezaar.com/Grilled-Chipotle-Salmon-With-Pineapple-Cilantro-Rice-128564")
				.setIngredients(Arrays.asList("salmon, onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/715159.jpg"));

		recipies.add(new Recipe().setTitle("Roast Chicken with Rosemary")
				.setHref("http://allrecipes.com/Recipe/Roast-Chicken-with-Rosemary/Detail.aspx")
				.setIngredients(Arrays.asList("onions, salt".split(",")))
				.setThumbnail("http://img.recipepuppy.com/18294.jpg"));

		recipies.add(new Recipe().setTitle("Boiled Ham").setHref("http://www.recipezaar.com/Boiled-Ham-11162")
				.setIngredients(Arrays.asList("ham, onions".split(",")))
				.setThumbnail("http://img.recipepuppy.com/182730.jpg"));

		return recipies;
	}
}
