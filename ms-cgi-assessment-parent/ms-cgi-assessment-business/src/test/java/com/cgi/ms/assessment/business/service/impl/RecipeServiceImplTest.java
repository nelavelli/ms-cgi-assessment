package com.cgi.ms.assessment.business.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Ingredient;
import com.cgi.ms.assessment.common.model.Recipe;
import com.cgi.ms.assessment.data.repo.impl.IORecipeCacheReposiotryImpl;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RecipeServiceImplTest {

	@Mock
	private IORecipeCacheReposiotryImpl ioRecipeCacheReposiotry;

	@InjectMocks
	private RecipeServiceImpl recipeServiceImpl;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
	}

	private static Stream<Arguments> getRecipesTestData() {
		return Stream.of(Arguments.of(Boolean.FALSE, null, recipesMockData(), null),
				Arguments.of(Boolean.TRUE, null, null, ErrorCode.INTERNAL_SERVER_ERROR),
				Arguments.of(Boolean.TRUE, new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR), null,
						ErrorCode.INTERNAL_SERVER_ERROR),
				Arguments.of(Boolean.TRUE, new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE), null,
						ErrorCode.SERVICE_UNAVAILABLE),
				Arguments.of(Boolean.TRUE, new RuntimeException(), null, ErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ParameterizedTest(name = "{index} => isErrorScenario={0}, Exception={1}, recipes={2}, errorCode={3}")
	@MethodSource("getRecipesTestData")
	public void getRecipesTest(boolean isErrorScenario, Exception ex, List<Recipe> expectedRecipes,
			ErrorCode errorCode) {

		try {
			if (Objects.isNull(ex)) {
				when(ioRecipeCacheReposiotry.readRecipesFromFile()).thenReturn(expectedRecipes);
			} else {
				when(ioRecipeCacheReposiotry.readRecipesFromFile()).thenThrow(ex);
			}
			Collection<Recipe> recipes = recipeServiceImpl.getRecipes();

			if (isErrorScenario) {
				Fail.fail("Expecting excpetion and this line should not execute.");
			}

			assertThat(recipes).isEqualTo(expectedRecipes.stream().sorted().collect(Collectors.toList()));
			assertThat(recipes.size()).isEqualTo(expectedRecipes.size());

		} catch (AppProcessingException e) {
			log.error("AppProcessingException ", e);
			if (!isErrorScenario) {
				Fail.fail("Not expecting any excpetion and something gone wrong.");
			}
			assertThat(e.getMessage()).isEqualTo(errorCode.getMessage());
			assertThat(e.getHttpsStatus()).isEqualTo(errorCode.getStatus());
		} catch (Exception e) {
			Fail.fail("In all the cases, only AppProcessingException should be thrown, so leak in the code", ex);
		}
		verify(ioRecipeCacheReposiotry, times(1)).readRecipesFromFile();
		verify(ioRecipeCacheReposiotry, never()).getRecipesByIngredients();
	}

	private static Stream<Arguments> getRecipesByIngredientsTestData() {
		return Stream.of(
				Arguments.of(Arrays.asList("onions".split(",")), mockIngredientRecipes("onions"), false, null, null),
				Arguments.of(Arrays.asList("salmon,onions,chicken".split(",")),
						mockIngredientRecipes("salmon,onions,chicken"), false, null, null),
				Arguments.of(null, null, true, new AppProcessingException(ErrorCode.DATA_NOT_FOUND),
						ErrorCode.DATA_NOT_FOUND),
				Arguments.of(Arrays.asList("notavaiableRecipe".split(",")), null, true,
						new AppProcessingException(ErrorCode.DATA_NOT_FOUND), ErrorCode.DATA_NOT_FOUND),
				Arguments.of(null, null, true, new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR),
						ErrorCode.INTERNAL_SERVER_ERROR),
				Arguments.of(null, null, true, new AppProcessingException(ErrorCode.SERVICE_UNAVAILABLE),
						ErrorCode.SERVICE_UNAVAILABLE),
				Arguments.of(null, null, true, new RuntimeException("Unkonwn Exception"),
						ErrorCode.INTERNAL_SERVER_ERROR));
	}

	@ParameterizedTest(name = "{index} => ingredients={0}, expectedRecipes={1}, isErrorScenario={2}, Exception={3}, errorCode={4}")
	@MethodSource("getRecipesByIngredientsTestData")
	public void getRecipesByIngredients(List<String> ingredients, List<Recipe> expectedRecipes, boolean isErrorScenario,
			Exception ex, ErrorCode errorCode) {

		try {
			if (Objects.isNull(ex)) {
				when(ioRecipeCacheReposiotry.getRecipesByIngredients()).thenReturn(ingredientMockData());
			} else {
				when(ioRecipeCacheReposiotry.getRecipesByIngredients()).thenThrow(ex);
			}
			Collection<Recipe> recipes = recipeServiceImpl.getRecipesByIngredients(ingredients);

			if (isErrorScenario) {
				Fail.fail("Expecting excpetion and this line should not execute.");
			}

			assertThat(recipes)
					.isEqualTo(expectedRecipes.stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new)));
			assertThat(recipes.size()).isLessThanOrEqualTo(expectedRecipes.size());

		} catch (AppProcessingException e) {
			if (!isErrorScenario) {
				Fail.fail("Not expecting any excpetion and something gone wrong.");
			}
			assertThat(e.getMessage()).isEqualTo(errorCode.getMessage());
			assertThat(e.getHttpsStatus()).isEqualTo(errorCode.getStatus());
		} catch (Exception e) {
			Fail.fail("In all the cases, only AppProcessingException should be thrown, so leak in the code", ex);
		}
		verify(ioRecipeCacheReposiotry, times(1)).getRecipesByIngredients();
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
