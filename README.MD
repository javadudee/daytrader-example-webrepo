

# DayTrader Web

    The Web UI to the DayTrader business functions

## Current State Architecture
DayTrader is multi-tier application built around the paradigm of an online stock trading system. Example business functions include
login, register, view portfolio, lookup stock quotes, and buy or sell stock. DayTrader was originally developed by IBM and donated 
to the Apache Geronimo community in the 2005 timeframe. The DayTrader architecture is representitive of monolithic applications that 
many businesses are still using. For that reason, we selected it to clarify the techniques for refactoring a monolith to microservices. 
For the original monolithic architecture, see [daytrader - a more complex application](http://geronimo.apache.org/GMOxDOC30/daytrader-a-more-complex-application.html). 

## Target State Architecture

The Web application is a Spring Boot Application responsible for the UI to the DayTrader business functions. Although it still contains 
all the business operations, their implementations have been changed to call the Gateway microservice; which then directs them onto the 
appropriate microservice (Accounts, Portfolios, and Quotes). It is the microservices that contain the busienss logic so that it can now
be leveraged by other business applications.

![Target-State-Architecture](images/Target-State-Architecture.JPG)

## Prerequisites

1.  [Install Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

2.  [Install Maven](https://maven.apache.org/download.cgi) (3.3.9+)

3.  [Install Docker](https://www.docker.com/get-docker)
    
4.  [Create DockerHub Account](https://hub.docker.com/)

## Configuration

Maven must be able to authenticate to your DockerHub account so it can push Docker images

1.  In `daytrader-web/pom.xml` change the ${user.name} to your DockerHub user name:

    ```xml
    <docker.image.prefix>${user.name}</docker.image.prefix>
    ``` 

2.  In `daytrader-web/env/external/k8s/web-deployment.yaml`, change the image to your DockerHub username

    ```yaml
    image: YOUR_DOCKERHUB_USERNAME/daytrader-gateway:4.0.18
    ```

3.  In `daytrader-web/pom.xml`, you don't have to change the <tag>, but if you do, change the above version too.
        
    ```xml
    <tag>4.0.18</tag>
    ```

4.  In `~/.m2/settings.xml`, add the following so Maven can push images to your DockerHub account

    ```xml
    <servers>
        <server>
            <id>docker.io</id>
            <username>YOUR_DOCKERHUB_USERNAME</username>
            <password>YOUR_DOCKERHUB_PASSWORD</password>
        </server>
    </servers> 
    ```

## Setup the Cluster

You will need a cluster to run the application. If you already have a cluster with the NGINX Ingress Controller then feel free to use it. If not, we have provided the instructions that we used to setup a cluster on Minikube, Amaon EKS, Google Kubernetes Engine, and Azure Kubernetes Service. We have also tested the application on those platforms. For additional information see [Picking the right Solution](https://kubernetes.io/docs/setup/pick-right-solution/). The following sections detail the steps that we used to install the cluster and the NGINX Ingress Controller. 
    

### Setup on Minikube

If you want to know how we setup the cluster on Minikube click [here](docs/MINIKUBE.MD)


### Setup on Amazon EKS

If you want to know how we set up the cluster on Amazon EKS click [here](docs/EKS.MD)


### Setup on Google Kubernetes Engine

If you want to know how to setup the cluster on Google GKE, then click [here](docs/GKE.MD)



## Containerize the Application

Note: we have already created the Dockerfile for this application so you can skip this step.

But if you want to know how we containerized the application then click [here](docs/CONTAINERS.MD)


## Put Containers on Kubernetes

Note: we have already written the YAML files for this application so you can skip this step.

But if you want to know how we put the container on Kubernetes then click [here](docs/KUBERNETES.MD)



## Delivery Pipeline

In `pom.xml` we defined profiles (local, ci, and cd) to support the delivery pipeline. This makes it easy not only for you as a developer to work with Kubernetes, but it also makes it easy for those in DevOps to create the delivery piplelines in Jenkins or some other such tool. If you want to see those profiles click [here](docs/PROFILES.MD). Otherwise you can just use Maven (as indicated below) to build your container and deploy it to the Kuberenetes cluster as indicated in this section. To do so, make sure you are in the `daytrader-accountsapp` directory.

`$ cd daytrader-accountsapp`

### Build Automation

To build the application

`$ mvn -Plocal clean install`


### Continuous Integration

To run the integration tests

`$ mvn -Pci clean install`

### Continuous Delivery

#### Provision

To provision the environment

`$ mvn -Pcd clean install`

#### Decommission

To decommision the environment

`$ mvn -Pcd clean install`

## Functional Test

### Provision the testing environment
    
1.  [Accounts Microservice](https://github.com/jpmorganchase/daytrader-example-accountsrepo/)

    a.	cd daytrader-accountsapp\daytrader-accounts

    b.	mvn -P clean install

2.  [Gateway Microservice](https://github.com/jpmorganchase/daytrader-example-gatewayrepo/)

    a.	cd daytrader-gatewayapp\daytrader-gateway

    b.	mvn -P clean install

3.  [Portfolios Microservice](https://github.com/jpmorganchase/daytrader-example-portfoliosrepo/)

    a.  cd daytrader-portfoliosapp\daytrader-portfolios
        
    b.	mvn -P clean install

4.  [Quotes Microservice](https://github.com/jpmorganchase/daytrader-example-quotesrepo/)

    a.  cd daytrader-quotesapp\daytrader-quotes
        
    b.	mvn -P clean install

5.  [Web Microservice](https://github.com/jpmorganchase/daytrader-example-webrepo/)

    a.  cd daytrader-webapp\daytrader-web
        
    b.	mvn -P clean install

### Run the functional test
                                 
Open your browser and navigate to: `https://web.daytrader.geronimo.apache.org`

1.  Under The Configuration Tab

    a.  Press(Re)-create DayTrader Database Tables and Indexes
    
    b.  Press (Re)-populate DayTrader Database

    c.  Press Test Daytrader Scenario

2.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

    Note: each time you refresh the browser, it runs another functional test scenario. 

That's it! You have now tested the daytrader
        
### Decommission the testing environment

1.  [Accounts Microservice](https://github.com/jpmorganchase/daytrader-example-accountsrepo/)

    a.	cd daytrader-accountsapp\daytrader-accounts

    b.	mvn -P clean install

2.  [Gateway Microservice](https://github.com/jpmorganchase/daytrader-example-gatewayrepo/)

    a.	cd daytrader-gatewayapp\daytrader-gateway

    b.	mvn -P clean install

3.  [Portfolios Microservice](https://github.com/jpmorganchase/daytrader-example-portfoliosrepo/)

    a.  cd daytrader-portfoliosapp\daytrader-portfolios
        
    b.	mvn -P clean install

4.  [Quotes Microservice](https://github.com/jpmorganchase/daytrader-example-quotesrepo/)

    a.  cd daytrader-quotesapp\daytrader-quotes
        
    b.	mvn -P clean install

5.  [Web Microservice](https://github.com/jpmorganchase/daytrader-example-webrepo/)

    a.  cd daytrader-webapp\daytrader-web
        
    b.	mvn -P clean install
    
                            
## Swagger UI Documentation

### Access Swagger UI through port forwarding
         
1.  `$ kubectl port-forward <POD_NAME> 8888:5443`  ## This command forwards local port 8888 to port 5443 of your pod
   
    Forwarding from 127.0.0.1:8888 -> 5443
        
2.  Point you browser to `https://localhost:8888/swagger-ui.html`

### Access Swagger UI through the API Server
    
1.  `$ kubectl proxy`    ## This proxy locates and authenticates to the API Server.
    
    Starting to serve on 127.0.0.1:8001 

2.  Point you browser to `http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html`
        
        
### Access Swagger UI through an Ingress
        
1.  Point you browser to `https://web.daytrader.geronimo.apache.org/swagger-ui.html`
    




