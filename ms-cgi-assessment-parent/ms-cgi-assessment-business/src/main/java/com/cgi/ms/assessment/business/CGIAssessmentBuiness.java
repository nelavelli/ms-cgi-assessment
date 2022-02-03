package com.cgi.ms.assessment.business;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.cgi.ms.assessment.data.CGIAssessmentData;

@Configuration
@ComponentScan(basePackageClasses = CGIAssessmentData.class)
public class CGIAssessmentBuiness {

}
