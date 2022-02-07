package com.cgi.ms.assessment.business;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cgi.ms.assessment.data.CGIAssessmentDataModule;

@Configuration
@ComponentScan(basePackageClasses = CGIAssessmentDataModule.class)
public class CGIAssessmentBuinessModule {

}
