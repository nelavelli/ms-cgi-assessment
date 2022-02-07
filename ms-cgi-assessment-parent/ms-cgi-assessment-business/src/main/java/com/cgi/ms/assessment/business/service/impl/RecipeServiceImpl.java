package com.cgi.ms.assessment.business.service.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cgi.ms.assessment.business.service.RecipeService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Recipe;
import com.cgi.ms.assessment.data.repo.IORecipeCacheReposiotry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class RecipeServiceImpl implements RecipeService {

	private @Autowired IORecipeCacheReposiotry ioCacheReposiotry;

	@Override
	public Collection<Recipe> getRecipes() {
		try {

			Collection<Recipe> recipes = ioCacheReposiotry.readRecipesFromFile().stream().sorted()
					.collect(Collectors.toList());
			return recipes;

		} catch (AppProcessingException ex) {
			log.error("AppProcessingException occured, ", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("unknown exeception while looking up into data file, ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}

	@Override
	public Collection<Recipe> getRecipesByIngredients(List<String> ingredients) {
		try {

			Map<String, List<Recipe>> ingredientsMap = ioCacheReposiotry.getRecipesByIngredients();

			Collection<Recipe> recipes = Optional.ofNullable(ingredients).orElse(Collections.<String>emptyList())
					.stream().filter(x -> Objects.nonNull(ingredientsMap.get(x))).map(x -> ingredientsMap.get(x))
					.collect(Collectors.toList()).stream().flatMap(List::stream).sorted()
					.collect(Collectors.toCollection(LinkedHashSet::new));

			if (recipes.isEmpty()) {
				throw new AppProcessingException(ErrorCode.DATA_NOT_FOUND);
			}
			return recipes;

		} catch (AppProcessingException ex) {
			log.error("exeception while looking up into data file, ", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("unknown exeception while looking up into data file, ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}

}
