
## Run on Spring Boot

### Prerequisites

	1.	Install Java Development Kit (64 bit) 1.8+

	2.	Install Apache Maven 3.3.9+

### Build Automation

	1.	cd daytrader-webapp

	2.	mvn clean install    ## builds spring boot executable jar
		
### Functional Test

	The functional test is done in three phases: pre-functional-test functional-test, and post-functional-test

#### Phase 1: pre-functional-test

	1.	Start Accounts

		a.	cd daytrader-accountsapp\daytrader-accounts
		
		b.	env\external\bin\start_server.bat

	2.	Start Portfolios

		a.	cd daytrader-portfoliosapp\daytrader-portfolios
		
		b.	env\external\bin\start_server.bat

		3.	Start Quotes

            a.  cd daytrader-quotesapp\daytrader-quotes
            
            b.  env\external\bin\start_server.bat

		4. 	Start Gateway

            a.  daytrader-gatewayapp\daytrader-gateway
            
            b.  env\external\bin\start_server.bat

		5.	Start Web

            a.  cd daytrader-webapp\daytrader-web
            
            b.  env\external\bin\start_server

#### Phase 2: functional-test

    1.	Open: https://localhost:5443

		a.	Under The Configuration Tab

			a.  Press (Re)-create DayTrader Database Tables and Indexes
			
			b.  Press (Re)-populate DayTrader Database
			
			c.  Press Test Daytrader Scenario

		b.	Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

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
    			

### Swagger UI Documentation

	1. Start the application

		a.	$ cd daytrader-webapp

		b.	$ daytrader-web\env\external\bin\start_server

	2. 	Point you browser to https://localhost:5443/swagger-ui.html

    3.  Stop the application after reviewing the APIs

		a.  $ cd daytrader-webapp	

        b.  $ daytrader-web\env\external\bin\stop_server
         
	
