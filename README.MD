

# DayTrader Web

	The Web UI to the DayTrader business functions



## Current State Architecture

DayTrader is multi-tier application built around the paradigm of an online stock trading system. Example business functions include
login, register, view portfolio, lookup stock quotes, and buy or sell stock. DayTrader was originally developed by IBM and donated 
to the Apache Geronimo community in the 2005 timeframe. The DayTrader architecture is representitive of monolithic applications that 
many businesses are still using. For that reason, we selected it to clarify the techniques for refactoring a monolith to microservices. 
For the original monolithic architecture, see http://geronimo.apache.org/GMOxDOC30/daytrader-a-more-complex-application.html. 



## Target State Architecture

![Target-State-Architecture](images/Target-State-Architecture.JPG)



### Web Application

The Web Application (highlighted in the green box) is a Spring Boot Application responsible for the UI to the DayTrader business functions. Although 
it still contains the business functions, their implementations have been changed to delegate their invocation to the appropriate microservice (API).




#### Static Viewpoint

![Static-Viewpoint](images/Static-Viewpoint.JPG)



#### Dynamic Viewpoint

![Dynamic-Viewpoint](images/Dynamic-Viewpoint.JPG)



#### Participant and Responsibilities

![Participant-Responsibilities](images/Participant-Responsibilities.JPG)



## Prerequisites

		1.	Java Development Kit (64 bit) 1.8+
		2.	Apache Maven 3.3.9+


## Delivery Pipeline

### Build Automation

		1.	cd daytrader-webapp
		2.	mvn clean install


### Functional Test

	Running the functional test is done in three phases: pre-functional-test functional-test, and post-functional-test

#### Phase 1: pre-functional-test

		1.	Start Accounts

			a.	cd daytrader-accountsapp\daytrader-accounts
			b.	env\external\bin\start_server.bat

		2.	Start Portfolios

			a.	cd daytrader-portfoliosapp\daytrader-portfolios
			b.	env\external\bin\start_server.bat

		3.	Start Quotes

			a.	cd daytrader-quotesapp\daytrader-quotes
			b.	env\external\bin\start_server.bat

		4. 	tart Gateway

			a.	daytrader-gatewayapp\daytrader-gateway
			b.	env\external\bin\start_server.bat

		5.	Start Web

			a.	cd daytrader-webapp\daytrader-web
			b.	env\external\bin\start_server

#### Phase 2: functional-test

		1.	Open: http://localhost:5443

			a.	Under The Configuration Tab

				- Press(Re)-create DayTrader Database Tables and Indexes
				- Press(Re)-populate DayTrader Database
				- Press Test Daytrader Scenario

			b.	Keep refreshing the borwser until you are satisfied DayTrader is working correctly. 

				Note: each time you refresh the browser, it runs another functional test scenario. 

			That's it! You have now tested the daytrader

#### Phase 3: post-functional-test

		1.	Stop Accounts

			a.	cd daytrader-accountsapp\daytrader-accounts
			b.	env\external\bin\stop_server.bat

		2.	Stop Portfolios

			a.	cd daytrader-portfoliosapp\daytrader-portfolios
			b.	env\external\bin\stop_server.bat

		3.	Stop Quotes

			a.	cd daytrader-quotesapp\daytrader-quotes
			b.	env\external\bin\stop_server.bat

		4.	Stop Gateway

			a.	daytrader-gatewayapp\daytrader-gateway
			b.	env\external\bin\stop_server.bat

		5.	Stop Web

			a.	cd daytrader-webapp\daytrader-web
			b.	env\external\bin\stop_server



