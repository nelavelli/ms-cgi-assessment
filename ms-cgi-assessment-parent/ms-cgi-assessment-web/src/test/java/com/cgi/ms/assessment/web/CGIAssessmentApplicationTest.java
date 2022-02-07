package com.cgi.ms.assessment.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Fail;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.cache.CacheManager;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.model.LogAnalyser;
import com.cgi.ms.assessment.common.model.LogInfo;
import com.cgi.ms.assessment.common.model.Recipe;
import com.cgi.ms.assessment.web.constants.WebConstants;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

// This class implements the functional test cases for all the APIs.
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@TestInstance(Lifecycle.PER_CLASS)
@Slf4j
public class CGIAssessmentApplicationTest {

	private final static ObjectMapper mapper = new ObjectMapper();

	@LocalServerPort
	private int port;

	@Autowired
	private CacheManager cacheManager;

	@Autowired
	private TestRestTemplate restTemplate;

	private String buildUrl(String uri) {
		return new StringJoiner("").add("http://localhost:" + port).add("/assessment/web/api").add(uri).toString();
	}

	private void validateResponseEntity(ResponseEntity<?> response, HttpStatus status) {
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getStatusCode()).isEqualTo(status);
		assertThat(response.getStatusCodeValue()).isEqualTo(status.value());
	}

	private static Stream<Arguments> getLogAnalyserAPITestData() {
		String invaldiLogType = "Log Level can't be empty and only TRACE | DEBUG | INFO | WARN | ERROR | FATAL are allowed log levels.";
		String invalidPageSize = "Page Size should be Minimum 1";
		return Stream.of(Arguments.of("TRACE", 20, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("DEBUG", 15, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("INFO", 25, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("WARN", 25, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("ERROR", 25, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("FATAL", 25, HttpMethod.GET, Boolean.FALSE, null, HttpStatus.OK),
				Arguments.of("DEBUG", 0, HttpMethod.GET, Boolean.TRUE, invalidPageSize, HttpStatus.BAD_REQUEST),
				Arguments.of("DEBUG", -1, HttpMethod.GET, Boolean.TRUE, invalidPageSize, HttpStatus.BAD_REQUEST),
				Arguments.of("ERRORFATAL", 1, HttpMethod.GET, Boolean.TRUE, invaldiLogType, HttpStatus.BAD_REQUEST),
				Arguments.of("UNKNOWN", 1, HttpMethod.GET, Boolean.TRUE, invaldiLogType, HttpStatus.BAD_REQUEST),
				Arguments.of("123456", 1, HttpMethod.GET, Boolean.TRUE, invaldiLogType, HttpStatus.BAD_REQUEST),
				Arguments.of("alpha123", 1, HttpMethod.GET, Boolean.TRUE, invaldiLogType, HttpStatus.BAD_REQUEST));
	}

	@ParameterizedTest(name = "{index} => logType={0}, pageSize={1}, httpMethod={2}, isErrorScenario={3}, errorMessage={4}, httpStatusCD={5}")
	@MethodSource("getLogAnalyserAPITestData")
	public void testGetLogAnalyserAPI(String logType, int pageSize, HttpMethod httpMethod, boolean isErrorScenario,
			String errorMessage, HttpStatus httpStatusCD) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LogAnalyser> requestEntity = new HttpEntity<>(null, headers);

			ResponseEntity<LogAnalyser> response = restTemplate
					.exchange(buildUrl(WebConstants.LOG_ANALYSER_ENDPOINT + "?pageSize=" + pageSize)
							.replace("{logType}", logType), httpMethod, requestEntity, LogAnalyser.class);
			this.validateResponseEntity(response, httpStatusCD);
			// validate body && response payload..
			LogAnalyser logAnalyser = response.getBody();
			if (!isErrorScenario) {
				assertThat(logAnalyser.getLogType()).isEqualTo(logType);
				assertThat(logAnalyser.getTotalRecords()).isGreaterThanOrEqualTo(0);
				assertThat(logAnalyser.getLogInfo()).size().isLessThanOrEqualTo(pageSize);
			} else {
				assertThat(logAnalyser.getMessage()).isEqualTo(errorMessage);
			}
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@Test
	public void testGetLogAnalyserAPIWihtoutPageSizeDefaultValue() {
		try {
			String logType = "DEBUG";
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LogAnalyser> requestEntity = new HttpEntity<>(null, headers);

			ResponseEntity<LogAnalyser> response = restTemplate.exchange(
					buildUrl(WebConstants.LOG_ANALYSER_ENDPOINT).replace("{logType}", logType), HttpMethod.GET,
					requestEntity, LogAnalyser.class);
			this.validateResponseEntity(response, HttpStatus.OK);
			// validate body && response payload..
			LogAnalyser logAnalyser = response.getBody();
			assertThat(logAnalyser.getLogType()).isEqualTo(logType);
			assertThat(logAnalyser.getTotalRecords()).isGreaterThanOrEqualTo(0);
			assertThat(logAnalyser.getLogInfo()).size().isLessThanOrEqualTo((int) logAnalyser.getTotalRecords());
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@Test
	public void testGetLogAnalyserAPISortByLogOccranceDescendingOrder() {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<LogAnalyser> requestEntity = new HttpEntity<>(null, headers);

		ResponseEntity<LogAnalyser> response = restTemplate.exchange(
				buildUrl(WebConstants.LOG_ANALYSER_ENDPOINT.replace("{logType}", "DEBUG")), HttpMethod.GET,
				requestEntity, LogAnalyser.class);
		this.validateResponseEntity(response, HttpStatus.OK);

		LogAnalyser logAnalyser = response.getBody();

		assertThat(logAnalyser.getLogInfo().stream().sorted(Comparator.comparing(LogInfo::getCount).reversed())
				.collect(Collectors.toList()).equals(logAnalyser.getLogInfo())).isTrue();
	}

	@Test
	public void testGetRecipesAPI() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LogAnalyser> requestEntity = new HttpEntity<>(null, headers);
			ResponseEntity<List<Recipe>> response = restTemplate.exchange(buildUrl(WebConstants.RECIPE_ENDPOINT),
					HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Recipe>>() {
					});
			this.validateResponseEntity(response, HttpStatus.OK);
			assertThat(response.getBody()).size().isGreaterThanOrEqualTo(0);
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@Test
	public void testGetRecipesAPISortByTitle() {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<LogAnalyser> requestEntity = new HttpEntity<>(null, headers);

			ResponseEntity<List<Recipe>> response = restTemplate.exchange(buildUrl(WebConstants.RECIPE_ENDPOINT),
					HttpMethod.GET, requestEntity, new ParameterizedTypeReference<List<Recipe>>() {
					});
			this.validateResponseEntity(response, HttpStatus.OK);

			assertThat(response.getBody().stream().sorted(Comparator.comparing(Recipe::getTitle))
					.collect(Collectors.toList()).equals(response.getBody())).isTrue();
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@Test
	public void testGetRecipesByIngredientsAPIIngredientsValidation() {

		try {

			String[] ingredients = { "salt", "garlic", "rice", "onions", "salt,rice,garlic,onions" };

			List<Integer> resultsList = Arrays.stream(ingredients).map(str -> getRecipesByIngredients(str))
					.collect(Collectors.toList());

			// validate sum of n-1 elements is always greater than or equal to nth element.
			assertThat(resultsList.stream().limit(resultsList.size() - 1).mapToInt(i -> i.intValue()).sum())
					.isGreaterThanOrEqualTo(resultsList.get(resultsList.size() - 1).intValue());
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@Test
	public void testGetRecepesByIngredientsSortedByTitle() {
		try {
			String ingredient = "onions,gralic,water";

			List<Recipe> originalRecepes = callService(ingredient).getBody();

			List<Recipe> recepes = callService(ingredient).getBody();

			Collections.sort(recepes, Comparator.comparing(Recipe::getTitle));

			assertThat(originalRecepes.equals(recepes)).isTrue();

		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	private ResponseEntity<List<Recipe>> callService(String ingredients) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		final HttpEntity<Recipe> requestEntity = new HttpEntity<>(null, headers);

		return restTemplate.exchange(
				buildUrl(WebConstants.RECIPES_FILTER_ENDPOINT.replace("{ingredients}", ingredients)), HttpMethod.GET,
				requestEntity, new ParameterizedTypeReference<List<Recipe>>() {
				});
	}

	private int getRecipesByIngredients(String ingredients) {

		ResponseEntity<List<Recipe>> response = callService(ingredients);
		this.validateResponseEntity(response, HttpStatus.OK);

		List<List<String>> ingredientResponse = response.getBody().stream().map(recipe -> recipe.getIngredients())
				.collect(Collectors.toList());

		List<String> inputs = Arrays.asList(ingredients.split(",")).stream().collect(Collectors.toList());

		// validate in each recipe there is a selected ingredient.
		assertThat(ingredientResponse.size()).isEqualTo(
				ingredientResponse.stream().filter(res -> res.removeAll(inputs)).collect(Collectors.toList()).size());

		/*
		 * assertThat(response.getBody().stream().map(recipe ->
		 * recipe.getIngredients()).collect(Collectors.toList()))
		 * .containsAnyOf(Arrays.asList(ingredients.split(",")));
		 */

		return response.getBody().size();
	}

	private static Stream<Arguments> getRecipesByIngredientsErrorScenaiosTestData() {
		String invalidIngredient = "Ingredients can only be of String types.";
		return Stream.of(
				Arguments.of("NotFoundRecipe", ErrorCode.DATA_NOT_FOUND, ErrorCode.DATA_NOT_FOUND.getMessage()),
				Arguments.of("123456Numberic", ErrorCode.BAD_REQUEST, invalidIngredient),
				Arguments.of("@#!@)SpecialCharacter", ErrorCode.BAD_REQUEST, invalidIngredient),
				Arguments.of("@#$%1234", ErrorCode.BAD_REQUEST, invalidIngredient));
	}

	@ParameterizedTest(name = "{index} => ingredientTitle={0}, errorCd={1}, message={2}")
	@MethodSource("getRecipesByIngredientsErrorScenaiosTestData")
	public void getRecipesByIngredientsErrorScenaios(String ingredientTitle, ErrorCode errorCd, String message) {

		try {
			String response = restTemplate.getForObject(
					buildUrl(WebConstants.RECIPES_FILTER_ENDPOINT.replace("{ingredients}", ingredientTitle)),
					String.class);

			Recipe recipe = mapper.readValue(response, Recipe.class);
			assertThat(recipe.getMessage()).isEqualTo(message);
			assertThat(recipe.getStatusCode()).isEqualTo(errorCd.getStatus().value());
		} catch (Exception ex) {
			Fail.fail("Failed as we are not expecting any exception in this flow", ex);
		}
	}

	@AfterAll
	public void testCacheIsNotEmpty() {
		assertThat(cacheManager.getCache("allRecipeCache")).withFailMessage("Have not found Recipes entries in cache, please check once.").isNotNull();
		assertThat(cacheManager.getCache("allIngredientCache")).withFailMessage("Have not found Ingredients entries in cache, please check once.").isNotNull();
		assertThat(cacheManager.getCache("logAnalyserCache")).withFailMessage("Have not found logAnalyser entries in cache, please check once.").isNotNull();
	}

}
