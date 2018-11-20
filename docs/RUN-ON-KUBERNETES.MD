
## Run on Kubernetes


### Build the Web UI

1.  `$ cd daytrader-webapp`

2.  `$ mvn -Pcd clean install`

    This command builds the Docker Image and pushes it to the DockerHub registry

### Start the Web UI

1.  `$ cd daytrader-web\env\external\k8s`

2.  `$ kubectl apply -f deployment.yaml`

    This command pulls the image from DockerHub and deploys it to the Kubernetes Cluster

3.  `$ kubectl get pods -watch`
    
    NAME | READY | STATUS | RESTARTS | AGE
    ---- | ----- | ------ | -------- | ---
    daytrader-web-xxxxxxxxx-xxxxx | 1/1 | Running | 0 | 6m
    
    Watch until you see READY `1/1` and STATUS `Running` then Ctrl-c to stop watching.
    
4.  `$ kubectl apply -f service.yaml`

Currently, these commands are run manually, but they can easliy be added to Maven
    
### Test the Web UI

To run the functional test,

1.  Start the Accounts, Gateway, Portfolios, and Quotes
                                   
2.  `kubectl port-forward daytrader-web-xxxxxxxxx-xxxxx`

    You should see `Forwarding from 127.0.0.1:8888 -> 5443`
    
    This command forwards local port 8888 to port 5443 of your Pod
    
3.  Open your browser and navigate to: `https://localhost:5443`
    
4.  Under The Configuration Tab

    a.  Press(Re)-create DayTrader Database Tables and Indexes
    
    b.  Press (Re)-populate DayTrader Database

    c.  Press Test Daytrader Scenario

5.  Keep refreshing the browser until you are satisfied that DayTrader is working.

    Note that each time you refresh the browser, it runs a random functional test. 
   
    That's it! You have now tested the DayTrader
    
6.  Stop the Accounts, Gateway, Portfolios, and Quotes
    
### Stop the Web UI

1.  `$ cd daytrader-web\env\external\k8s`

1.  `$ kubectl delete deployments daytrader-web`
    
2.  `$ kubectl delete services daytrader-web`

Currently, these commands are run manually, but they can easily be added to Maven

### Access the Swagger UI

1.  Start the Gateway

2.  `kubectl port-forward daytrader-web-xxxxxxxxx-xxxxx`

    You should see `Forwarding from 127.0.0.1:8888 -> 5443`
    
    This command forwards local port 8888 to port 5443 of your Pod

3.  Point you browser to https://localhost:5443/swagger-ui.html

4.  Stop the Gateway after reviewing (or testing) the APIs

