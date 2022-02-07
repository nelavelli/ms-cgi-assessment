package com.cgi.ms.assessment.data.repo.impl;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Recipe;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class IORecipeCacheReposiotryImplTest {

	@InjectMocks
	private IORecipeCacheReposiotryImpl ioRecipeCacheReposiotryImpl;

	@BeforeEach
	public void setup() {
		MockitoAnnotations.openMocks(this);
		ReflectionTestUtils.setField(ioRecipeCacheReposiotryImpl, "recipeFile",
				new ClassPathResource("data/receipe.json"));
	}

	@Test
	public void getRecipesByIngredientsTest() {
		Map<String, List<Recipe>> ingredientRecipes = ioRecipeCacheReposiotryImpl.getRecipesByIngredients();

		assertThat(ingredientRecipes.entrySet().stream().map(e -> e.getKey()).collect(Collectors.toList()))
				.containsAll(Arrays.asList("chicken", "butter", "salt", "onions", "ham", "salmon")).doesNotContainNull()
				.doesNotContainAnyElementsOf(Arrays.asList("rice,garlic")).hasSize(6);

		assertThat(ingredientRecipes.get("rice"))
				.withFailMessage("there is no item with ingredient rice in the given file.").isNull();

		assertThat(ingredientRecipes.get("chicken"))
				.withFailMessage("only 1 item with chicken ingredient avaiable in the file").hasSize(1);

		assertThat(ingredientRecipes.get("butter"))
				.withFailMessage("only 1 item with chicken ingredient avaiable in the file").hasSize(1);

		assertThat(ingredientRecipes.get("onions"))
				.withFailMessage("only 10 items with chicken ingredient avaiable in the file").hasSize(10);

		assertThat(ingredientRecipes.get("ham"))
				.withFailMessage("only 1 item with chicken ingredient avaiable in the file").hasSize(1);

		assertThat(ingredientRecipes.get("salt"))
				.withFailMessage("only 1 item with chicken ingredient avaiable in the file").hasSize(1);

	}

	@Test
	public void readRecipesFromFileTest() {
		List<Recipe> recipes = ioRecipeCacheReposiotryImpl.readRecipesFromFile();
		assertThat(recipes).withFailMessage("Only 10 recipes avaiable in the recipe file.").hasSize(10);
	}

	@Test
	public void testInvalidRecipeException() {
		try {

			ReflectionTestUtils.setField(ioRecipeCacheReposiotryImpl, "recipeFile",
					new ClassPathResource("data/logFile-2018-09-10.log"));
			ioRecipeCacheReposiotryImpl.readRecipesFromFile();
			Fail.fail("this line should not reach as we have inject wrong file.");

		} catch (AppProcessingException apEx) {
			apEx.printStackTrace();
		}
	}
}
