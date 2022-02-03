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

import com.cgi.ms.assessment.business.service.ReceipeService;
import com.cgi.ms.assessment.common.exception.AppProcessingException;
import com.cgi.ms.assessment.common.model.Receipe;

import lombok.extern.slf4j.Slf4j;

import static com.cgi.ms.assessment.web.constants.WebConstants.RECEIPE_ENDPOINT;
import static com.cgi.ms.assessment.web.constants.WebConstants.RECEIPES_FILTER_ENDPOINT;;

@RestController
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@Validated
public class ReceipeController {

	private @Autowired ReceipeService receipeService;

	@GetMapping(value = "/receipes")
	public ResponseEntity<Collection<Receipe>> getReceipes() {
		try {
			Collection<Receipe> receipes = receipeService.getReceipes();
			return new ResponseEntity<Collection<Receipe>>(receipes, HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for receipes", ex);
			throw ex;
		}
	}

	@GetMapping(value = RECEIPES_FILTER_ENDPOINT)
	public ResponseEntity<Collection<Receipe>> getReceipesByIngredients(
			@RequestBody @PathVariable List<@Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Receipes can only be String types.") String> ingredients) {
		try {
			log.info(" Ingredients list  {} ", ingredients);
			Collection<Receipe> receipes = receipeService.getReceipesByIngredients(ingredients);
			return new ResponseEntity<Collection<Receipe>>(receipes, HttpStatus.OK);
		} catch (AppProcessingException ex) {
			log.error("Exception while looking up for receipes", ex);
			throw ex;
		}
	}
}
