package com.cgi.ms.assessment.data.repo;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Ingredient;
import com.cgi.ms.assessment.common.model.LogBuilder;
import com.cgi.ms.assessment.common.model.Receipe;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class IOCacheReposiotry {

	private final static String LOG_LINE_REGEX = "^([\\d-]+\\s[\\d:]+,\\d+)(\\s)(\\w+)(\\s+)(\\[\\w.+\\])(\\s+)(\\w.+)";

	private final static Pattern PATTERN = Pattern.compile(LOG_LINE_REGEX);

	private final static ObjectMapper mapper = new ObjectMapper();

	@Value("classpath:data/receipe.json")
	private Resource receipeFile;

	@Value("classpath:data/logFile-2018-09-10.log")
	private Resource logFile;

	@Cacheable(key = "'allIngredients'", value = "allIngredientCache")
	public Map<String, List<Receipe>> getReceipesByIngredients() {
		try {
			log.info(" loading from origial data source ");
			return this.readReceipesFromFile().stream().map(receipe -> {
				return receipe.getIngredients().stream().map(ing -> new Ingredient(ing, receipe))
						.collect(Collectors.toList());
			}).flatMap(ingredients -> ingredients.stream()).collect(Collectors.groupingBy(Ingredient::getTitle,
					Collectors.mapping(Ingredient::getReceipe, Collectors.toList())));
		} catch (AppProcessingException ex) {
			log.error("exeception while looking up into data file, ", ex);
			throw ex;
		}
	}

	@Cacheable(key = "'allReceipes'", value = "allReceipeCache")
	public List<Receipe> readReceipesFromFile() {
		try {
			return mapper.readValue(receipeFile.getInputStream(), new TypeReference<List<Receipe>>() {
			});
		} catch (IOException ex) {
			log.error("IO Exception occured while reading data from receipe.json file ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	@Cacheable(key = "'longAnalyser'", value = "longAnalyserCache")
	public Map<String, Map<String, Long>> getLongAnalysers() {
		try {
			return new BufferedReader(new InputStreamReader(logFile.getInputStream())).lines()
					.filter(line -> line.matches(LOG_LINE_REGEX)).map(line -> {
						Matcher matcher = PATTERN.matcher(line);
						if (!matcher.matches()) {
							log.warn("Bad log or problem with RE? ");
							log.info(line);
						}
						return LogBuilder.builder().logType(matcher.group(3)).description(matcher.group(7)).build();
					}).collect(Collectors.groupingBy(LogBuilder::getLogType,
							Collectors.groupingBy(LogBuilder::getDescription, Collectors.counting())));

		} catch (IOException ex) {
			log.error("IO Exception occured while reading data from logFile file ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		} catch (Exception ex) {
			log.error("Unkown Exception occured while reading data from logFile file ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}

}