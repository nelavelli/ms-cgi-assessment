# ms-cgi-assessment

This project has implementation for 2 modules that caters receipes and log analyser requirments.

source code is implemented on java and provides REST Api endpoints with spring-boot support.

# Getting Started with ms-cgi-assessment.

#Running project in local.

git clone https://github.com/nelavelli/ms-cgi-assessment.git

cd to ms-cgi-assessment-parent directory where you checked out the project

we can run this application from command line but since it has dependency with maven to install in local.

# just import the application in your favorite IDE - i use eclise.

from run configurations - configure select the project parent directory and click on run.

the build will show success once all the test cases are passed.

now just navigate to CGIAssessmentApplication.java file

right click and run as java application.

use "CGIAssessment.postman_collection.json" file avaiable as siblig to this file.

import into postman and start using the application.


# In case Maven installed or you want to run in your local by configuring maven.

use the below link to install maven in your local and verify mvn commands working.

https://maven.apache.org/install.html

cd to /ms-cgi-assessment-parent directory.

mvn clean install #runs the all the test cases and build app jar in everything is ok.

cd /ms-cgi-assessment-web/target # app jar is located in this folder.

java -jar ms-cgi-assessment-web-0.0.1-SNAPSHOT.jar #brings the app up.


#Solution implementation and assumptions.

There were 2 requiements asked to implement.

1. Recipe problem - should provide 2 end points
 - 1.1 - api/recipes - resulting in showing the all the recipes avaiable in the system with all details. 
 - 1.2 - api/recipes/{ingredients} - filters out the recipes based selected ingredient(s).

Both of these apis returns the results alphabetically sorted by receipe title.

validation
----------
api/recipes/{ingredients} - will Only accept ingredients in alpahbet format(whitespace is allowed along with words).

2. Log Analyser - should provide an end point
 - 2.1 - api//loganalyser/{logType}&pageSize={number} -pageSize is an optional input field.

- returns all the selected log typ logs avaiable in the system, showing how many times each log message has repeated, top occured will be shown first. 
- in case pageSize is passed as part of the api, it also shows only those number of records 

in both the cases respose shows how many total log records avaiable for that particular log type.

### assumption is that initially user may want to see all the logtypes but when he/she wants to see only few they can make use of pageSize param exposed as part of same API.

validation
----------
{logType} param only accepts - TRACE, DEBUG, INFO, WARN, ERROR or FATAL - strings.

#Non Functional requiements.

###Perfomance

Both the requirments uses files as source of data and since IO operations are expensive.
-- it uses the caching feature and it refreshes for evey 1 min, 
-- implemented using ehcache and for certain limit of records it uses the heap memory for improved performance. 

###Easy to enhnace for I18N

-- it uses the spring's message labeling feature and can support internationalizaiton without any code change.

###Unit Testing

-- all the unit test cases are implemented covering both functional and non-functional requirments.
-- although code coverage is not mesured for this component - as per calc it must have crossed 80.

###Functional test cases

--end to end functional test cases are implemented in CGIAssessmentApplicationTest.java file.

###Scalling up

-- can support number of uses by easily scalling up on diff pods with minimal docker configuration.

###Availability

-- we can easily achive 99.999% by deploying the app on muliple pods and keeping infra stable.
