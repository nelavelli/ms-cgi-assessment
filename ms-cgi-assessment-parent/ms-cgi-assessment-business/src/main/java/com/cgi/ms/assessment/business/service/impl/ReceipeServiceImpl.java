package com.cgi.ms.assessment.business.service.impl;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cgi.ms.assessment.business.service.ReceipeService;
import com.cgi.ms.assessment.common.enums.ErrorCode;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Receipe;
import com.cgi.ms.assessment.data.repo.IOCacheReposiotry;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ReceipeServiceImpl implements ReceipeService {

	private @Autowired IOCacheReposiotry ioCacheReposiotry;

	@Override
	public Collection<Receipe> getReceipes() {
		try {
			Collection<Receipe> receipes = ioCacheReposiotry.readReceipesFromFile().stream().sorted().collect(Collectors.toCollection(LinkedHashSet::new));
			if (receipes.isEmpty()) {
				throw new AppProcessingException(ErrorCode.DATA_NOT_FOUND);
			}
			return receipes;
		} catch (AppProcessingException ex) {
			log.error("exeception while looking up into data file, ", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("unknown exeception while looking up into data file, ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}

	@Override
	public Collection<Receipe> getReceipesByIngredients(List<String> ingredients) {
		try {
			Map<String, List<Receipe>> ingredientsMap = ioCacheReposiotry.getReceipesByIngredients();
			Collection<Receipe> receipes = ingredients.stream().filter(x -> Objects.nonNull(ingredientsMap.get(x)))
					.map(x -> ingredientsMap.get(x)).collect(Collectors.toList()).stream().flatMap(List::stream).sorted()
					.collect(Collectors.toCollection(LinkedHashSet::new));

			/*if (receipes.isEmpty()) {
				throw new AppProcessingException(ErrorCode.DATA_NOT_FOUND);
			} */
			return receipes;

		} catch (AppProcessingException ex) {
			log.error("exeception while looking up into data file, ", ex);
			throw ex;
		} catch (Exception ex) {
			log.error("unknown exeception while looking up into data file, ", ex);
			throw new AppProcessingException(ErrorCode.INTERNAL_SERVER_ERROR, ex);
		}
	}

}
