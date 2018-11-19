

## Run on Kubernetes


### Build the Web UI

1.  `$ cd daytrader-webapp`

2.  `$ mvn -Plocal clean install`

### Start the Web UI

1.  `$ cd daytrader-web`
2.  `$ ./env/ext/bin/start_server.sh`
    
### Test the Web UI

**Pre-Condition:** Start Accounts, Gateway, Portfolios, and Quotes
                                   
1.  Open your browser and navigate to: `https://web.daytrader.geronimo.apache.org`

    **Note** you can also access Swagger UI at `https://web.daytrader.geronimo.apache.org/swagger-ui.html`

2.  Under The Configuration Tab

    a.  Press(Re)-create DayTrader Database Tables and Indexes
    
    b.  Press (Re)-populate DayTrader Database

    c.  Press Test Daytrader Scenario

3.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

    **Note** Each time you refresh the browser, it runs another functional test. 
   
4.  That's it! You have now tested the DayTrader
    
**Post-Condition:** Stop Accounts, Gateway, Portfolios, and Quotes
    
### Stop the Web UI

    `$ ./env/ext/bin/stop_server.sh`
    
