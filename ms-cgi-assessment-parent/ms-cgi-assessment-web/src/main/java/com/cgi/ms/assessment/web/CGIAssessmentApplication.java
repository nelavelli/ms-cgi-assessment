package com.cgi.ms.assessment.web;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.cgi.ms.assessment.business.CGIAssessmentBuiness;

@SpringBootApplication(scanBasePackageClasses = {CGIAssessmentApplication.class, CGIAssessmentBuiness.class})
public class CGIAssessmentApplication {

	public static void main(String[] args) {
		SpringApplication.run(CGIAssessmentApplication.class, args);
	}

	@Bean
	public WebMvcConfigurer corsConfigurer() {
		return new WebMvcConfigurer() {
			@Override
			public void addCorsMappings(CorsRegistry registry) {
				registry.addMapping("/**")
                .allowedMethods(HttpMethod.GET.name()).allowedOrigins("http://localhost:3000");
			}
		};
	}
}
