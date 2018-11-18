

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

1.  If you want to setup a cluster on Minikube click [here](docs/MINIKUBE.MD)

2.  If you want to set up a cluster on Amazon EKS click [here](docs/EKS.MD)

3.  If you want to setup a cluster on Google GKE, then click [here](docs/GKE.MD)

## Create the Container

We have already created the Dockerfile to containerized the application. If you want to know how click [here](docs/CONTAINERS.MD)

## Deploy the Container to Kubernetes

We have already written the YAML files to put the container on Kubernetes. If you want to know how click [here](docs/KUBERNETES.MD)

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

To deploy the container on Kubernetes

`$ mvn -Pcd clean install`

To undeploy the container from Kubernetes

`$ mvn -Pcd clean install`

### Functional Test

1.  Setup the environment for running the functional test

    a.  [Accounts Microservice](https://github.com/jpmorganchase/daytrader-example-accountsrepo/)

        $ cd daytrader-accountsapp\daytrader-accounts
        $ mvn -P clean install

    b.  [Gateway Microservice](https://github.com/jpmorganchase/daytrader-example-gatewayrepo/)

        ```sh
        $ cd daytrader-gatewayapp\daytrader-gateway
        $ mvn -P clean install
        ```

3.  [Portfolios Microservice](https://github.com/jpmorganchase/daytrader-example-portfoliosrepo/)

        $ cd daytrader-portfoliosapp\daytrader-portfolios
        
        $ mvn -P clean install

4.  [Quotes Microservice](https://github.com/jpmorganchase/daytrader-example-quotesrepo/)

        $ cd daytrader-quotesapp\daytrader-quotes
        
        $ mvn -P clean install

5.  [Web Microservice](https://github.com/jpmorganchase/daytrader-example-webrepo/)

        $ cd daytrader-webapp\daytrader-web
        
        $ mvn -P clean install

To run the functional tests
                                
1.  Open your browser and navigate to: `https://web.daytrader.geronimo.apache.org`

2.  Under The Configuration Tab

    a.  Press(Re)-create DayTrader Database Tables and Indexes
    
    b.  Press (Re)-populate DayTrader Database

    c.  Press Test Daytrader Scenario

3.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

    Note: each time you refresh the browser, it runs another functional test scenario. 

That's it! You have now tested the daytrader
 
To teardown the environment
       
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

Point you browser to `https://web.daytrader.geronimo.apache.org/swagger-ui.html`
    




