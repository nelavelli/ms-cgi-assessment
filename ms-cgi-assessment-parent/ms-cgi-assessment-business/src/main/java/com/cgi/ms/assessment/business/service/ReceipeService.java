package com.cgi.ms.assessment.business.service;

import java.util.Collection;
import java.util.List;

import com.cgi.ms.assessment.common.model.Receipe;

public interface ReceipeService {

	Collection<Receipe> getReceipes();

	Collection<Receipe> getReceipesByIngredients(List<String> ingredients);

}
