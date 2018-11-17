

# DayTrader Web

	The Web UI to the DayTrader business functions



## Current State Architecture

DayTrader is multi-tier application built around the paradigm of an online stock trading system. Example business functions include
login, register, view portfolio, lookup stock quotes, and buy or sell stock. DayTrader was originally developed by IBM and donated 
to the Apache Geronimo community in the 2005 timeframe. The DayTrader architecture is representitive of monolithic applications that 
many businesses are still using. For that reason, we selected it to clarify the techniques for refactoring a monolith to microservices. 
For the original monolithic architecture, see http://geronimo.apache.org/GMOxDOC30/daytrader-a-more-complex-application.html. 



## Target State Architecture

The Web application is a Spring Boot Application responsible for the UI to the DayTrader business functions. Although it still contains 
all the business operations, their implementations have been changed to call the Gateway microservice; which then directs them onto the 
appropriate microservice (Accounts, Portfolios, and Quotes). It is the microservices that contain the busienss logic so that it can now
be leveraged by other business applications.

![Target-State-Architecture](images/Target-State-Architecture.JPG)



### Static Viewpoint

![Static-Viewpoint](images/Static-Viewpoint.JPG)



### Dynamic Viewpoint

![Dynamic-Viewpoint](images/Dynamic-Viewpoint.JPG)



### Participant and Responsibilities

![Participant-Responsibilities](images/Participant-Responsibilities.JPG)



## Prerequisites

    1.  [Install Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)

    2.  [Install Maven](https://maven.apache.org/download.cgi) (3.3.9+)

    3.  [Install Docker](https://www.docker.com/get-docker)
    
    4.  [Create DockerHub Account](https://hub.docker.com/)


## Configuration

    Maven must be able to authenticate to your DockerHub account so it can push Docker images

    1..  Change the following property in daytrader-web/pom.xml to your DockerHub user name:
    
        ```xml
        <docker.image.prefix>${user.name}</docker.image.prefix>
        ```
               
    2.  Change the following key-value in daytrader-web/env/external/k8s/web-deployment.yaml to your DockerHub user name
        
        image: YOUR_DOCKERHUB_USERNAME/daytrader-gateway:4.0.18
        
    3.  The following property in daytrader-web/pom.xml is also worth noting. You do not have to change it, but if you do, change the above key-value accordingly.
        
        ```xml
        <tag>4.0.18</tag>
        ```
       
    4.  Add the following settings to your Maven ~/.m2/settings.xml so Maven can push images to your DockerHub account

        ```xml
        <servers>
            <server>
                <id>docker.io</id>
                <username>YOUR_DOCKERHUB_USERNAME</username>
                <password>YOUR_DOCKERHUB_PASSWORD</password>
            </server>
        </servers> 


## Setup the Cluster

Kubernetes can run on various platforms and there are various ways to setup a Kubernetes cluster. See [Picking the right Solution](https://kubernetes.io/docs/setup/pick-right-solution/). This section details the steps that we used to install the cluster and to install the NGINX Ingress Controller. If you already have a cluster with the NGINX Ingress Controller then feel free to use it. All other instructions in this README should be portable across platforms.
    

### Setup on Minikube

[Click here for instructions to setup a cluster on Minikube](docs/GKE.MD)


### Setup on Amazon EKS

[Click here for instructions to setup a cluster on Amazon EKS](docs/GKE.MD)


### Setup on Google Kubernetes Engine

[Click here for instructions to setup a cluster on Google GKE](docs/GKE.MD)


### Setup on Azure Kubernetes Service

[Click here for instructions to setup a cluster on Azure AKS](docs/GKE.MD)



## Create the Docker Image
 
### (Optional) Review the Dockerfile
    
    The Dockerfile defines all the commands to setup the environment and start the application in the container.

    ```yaml
    FROM openjdk:8-jdk
    VOLUME /tmp
    MAINTAINER Donald Vines <donald_vines@hotmail.com>
    # Set ssl variables
    ENV DAYTRADER_KEYSTORE_FILENAME=/var/ssl/daytrader/keystore.jks
    ENV DAYTRADER_KEYSTORE_PASSWORD=password
    ENV DAYTRADER_TRUSTSTORE_LOCATION=/var/ssl/daytrader/truststore.jks
    ENV DAYTRADER_TRUSTSTORE_PASSWORD=password
    # Set app variables
    ENV DAYTRADER_APP_VERSION=4.0.0
    ENV DAYTRADER_APP_ARTIFACTID=daytrader-accountsapp
    ENV DAYTRADER_WAR_ARTIFACTID=daytrader-accounts
    # Set database variables
    ENV DAYTRADER_DATABASE_DRIVER=org.apache.derby.jdbc.EmbeddedDriver
    ENV DAYTRADER_DATABASE_URL='jdbc:derby:tradesdb;create=true'
    ENV DAYTRADER_DATABASE_USERNAME=xxx
    ENV DAYTRADER_DATABASE_PASSWORD=xxx
    # Set tomcat variables
    ENV SERVER_PORT=1443
    ENV SERVER_PORT_HTTPS=1443
    # Make port visible
    EXPOSE 1443
    # Set service routes
    ENV DAYTRADER_ACCOUNTS_SERVICE=https://daytrader-accounts
    ENV DAYTRADER_GATEWAY_SERVICE=https://daytrader-gateway
    ENV DAYTRADER_PORTFOLIOS_SERVICE=https://daytrader-portfolios
    ENV DAYTRADER_QUOTES_SERVICE=https://daytrader-quotes
    # Set logging variables
    ENV DAYTRADER_LOG_FILENAME=/var/log/daytrader/$DAYTRADER_APP_ARTIFACTID-$DAYTRADER_APP_VERSION.log
    ENV DAYTRADER_LOG_LEVEL=TRACE
    ENV DAYTRADER_LOG_APPENDER=ConsoleAppender
    # Create the log folder
    RUN mkdir -p -m 0777 /var/log/daytrader
    # Create the log file and set permissions
    RUN touch $DAYTRADER_LOG_FILENAME
    RUN chmod 666 $DAYTRADER_LOG_FILENAME
    # Create the ssl folder
    RUN mkdir -p -m 0777 /var/ssl/daytrader
    # Add the truststore to the container and set permissions
    ARG JKS_FILE=target/$DAYTRADER_WAR_ARTIFACTID-$DAYTRADER_APP_VERSION/WEB-INF/classes/truststore.jks
    ADD ${JKS_FILE} $DAYTRADER_TRUSTSTORE_LOCATION
    RUN chmod 666 $DAYTRADER_TRUSTSTORE_LOCATION
    # Add the application's war to the container
    ARG WAR_FILE=target/$DAYTRADER_WAR_ARTIFACTID-$DAYTRADER_APP_VERSION.war
    ADD ${WAR_FILE} app.war
    ENV JAVA_OPTS="-Djavax.net.ssl.trustStore=/var/ssl/daytrader/truststore.jks -Djavax.net.ssl.trustStorePassword=password"
    ENTRYPOINT exec java $JAVA_OPTS -jar app.war
    ```
    
### (Optional) Review the Dockerfile Maven plugin

    To build and push the Docker image, we elected to use the [Spotify Dockerfile Maven Plugin](https://github.com/spotify/dockerfile-maven). It is one of two major Docker plugins that you still
    see active development. The other major Docker plugin is the [fabric8 Docker plugin](https://github.com/fabric8io/docker-maven-plugin). They are both valid options. For a recent comparison, 
    see [Using Docker from Maven and Maven from Docker](https://codefresh.io/howtos/using-docker-maven-maven-docker/)
    
    1.  `$ cd daytrader-webapp/daytrader-web`
    
    2.  In the `pom.xml`, review the `dockerfile-maven-plugin`
        
        ```xml
        <plugin>
            <groupId>com.spotify</groupId>
            <artifactId>dockerfile-maven-plugin</artifactId>
            <version>${dockerfile-maven-version}</version>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>build</goal>    <!-- Builds the Docker image -->
                        <goal>push</goal>     <!-- Pushes the Docker image -->
                    </goals>
                </execution>
            </executions>
            <configuration>
                <repository>${docker.image.prefix}/${project.artifactId}</repository>
                <tag>4.0.18</tag>
                <buildArgs>
                    <JAR_FILE>target/${project.artifactId}-${project.version}.war</JAR_FILE>
                </buildArgs>
                <useMavenSettingsForAuth>true</useMavenSettingsForAuth>
            </configuration>
        </plugin>
        ```

### Build and Push the Docker image

    1.  `$ cd daytrader-webapp`
    
    2.  `$ mvn -Pcd clean install`   ## This command builds the Docker image and pushes it to the specified DockerHub account
    
    3.  `$ cd daytrader-web/env/external/k8s`   ## This directory contains the application's Kubernetes configuration files written in YAML


## Create the Deployment, ReplicaSet, and Pod(s)

### (Optional) Review the Deployment YAML

    The `deployment.yaml` is where you specify the containers that will host your application along with some other specifications
    
    ```yaml
    apiVersion: extensions/v1beta1
    kind: Deployment
    metadata:
      labels:
        app: daytrader-web
      name: daytrader-web
      namespace: default
    spec:
      replicas: 1
      selector:
        matchLabels:
          app: daytrader-web
      template:
        metadata:
          labels:
            app: daytrader-web
        spec:
          containers:
          - name: daytrader-web
            image: dhvines/daytrader-web:4.0.18
            imagePullPolicy: Always
            ports:
            - containerPort: 5443
              protocol: TCP
            readinessProbe:  
              httpGet:
                path: /health
                scheme: HTTPS
                port: 5443
              initialDelaySeconds: 30
              periodSeconds: 30
              timeoutSeconds: 10
            livenessProbe:  
              httpGet:
                path: /health
                scheme: HTTPS
                port: 5443
              initialDelaySeconds: 30
              periodSeconds: 30
              timeoutSeconds: 10
              failureThreshold: 4
            env:
            - name: ACCOUNTS_SERVICE_ROUTE
              value: https://daytrader-accounts
            - name: GATEWAY_SERVICE_ROUTE
              value: https://daytrader-gateway
            - name: PORTFOLIOS_SERVICE_ROUTE
              value: https://daytrader-portfolios
            - name: QUOTES_SERVICE_ROUTE
              value: https://daytrader-quotes
            - name: GET_HOSTS_FROM
              value: dns
        ```
            
### Create the Deployment Resource

    1.  `$ kubectl apply -f deployment.yaml`    ## This command creates a Deployment resource. The Deployment resource creates a ReplicaSet, which then create the Pods
    
    2.  (Optional) `$ kubectl get pods`
    
        NAME                                READY   STATUS    RESTARTS   AGE   
        daytrader-web-*********-*****       1/1     Running   0          6m    
    
        ## Wait until the STATUS is Running and the pods are READY (1/1)
              
    3.  (Optional) Review the pods (application) logs
        
        `$ kubectl logs <POD_NAME> -f`     ## the -f option tails the logs
        
        Tomcat started on port(s): 5443 (https).

    4.  (Optional) Test the connection using the port forwarder
        
        a.  `$ kubectl port-forward <POD_NAME> 8888:5443`  ## This command forwards local port 8888 to port 5443 of your pod
       
            Forwarding from 127.0.0.1:8888 -> 5443
       
        b.  `$ curl -k https://127.0.0.1:8888/health`

            {"status":"UP"} 
            
        
## Create the Service

    The Service load balances requests across the Pods. Remember that the pods are ephemeral. So if one of the stops, the ReplicaSet will 
    create a replacement, but it will have a different IP. For that reason, clients should not communicate directly with the application 
    in a Pod. Instead clients should talk to applications via the service. 
    
### (Optional) Review the Service YAML

    The `service.yaml` is where you specify the selector for pods along with other specifications

    ```yaml
    apiVersion: v1
    kind: Service
    metadata:
      labels:
        app: daytrader-web
      name: daytrader-web
      namespace: default
    spec:
      ports:
      - port: 443
        protocol: TCP
        targetPort: 5443
      selector:
        app: daytrader-web
      sessionAffinity: None
      type: ClusterIP
    ```

### Create the Service Resource

    1.  `$ kubectl apply -f service.yaml`
           
    2.  (Optional) `$ kubectl get services`
        
        NAME            TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)         AGE
        daytrader-web   NodePort    10.7.254.225   <none>        443:30650/TCP   23s
       
        ## Notes
        
        a.  The above command creates a service resource. This resource listens on the CLUSTER IP and PORT. It is backed by endpoints 
            to one or more pods. You can see the dnpoints buy doing kubectl get endpoints. When the service receives an HTTPS request,
            it selects one of the endpoints (pods) and forwards the HTTPS request to the pod. The application running inside the pod 
            handles the request and returns an HTTPS response.
            
        b.  See also [Kubernetes Concepts - Services, Load Balancing, and Networking](https://kubernetes.io/docs/concepts/services-networking/service)

    3.  (Optional) Test the connection using the Kubernetes API. 
    
                        
        a.  `$ kubectl proxy`      ## Locates and authenticates to the API Server.
        
            Starting to serve on 127.0.0.1:8001
            
            If you get a port binding error then find the processId for that server and kill it 

            --  `$ lsof -P | grep ':8001'`
            --  `$ kill -9 processId`

        b.  `$ curl -k http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/health`
    
            {"status":"UP"}
    
            ## The curl command sent the HTTP request (without any authentication headers) to the proxy that is running on your local machine.
            ## The proxy, then, sent an HTTPS request to the API server's identity by validating its certificate. Encryption, authentication,
            ## and server verification is handled by the proxy. This is the easiest way to connect to the API server. 
    
        c. See also
    
            --  [Access Clusters Using the Kubernetes API](https://kubernetes.io/docs/tasks/administer-cluster/access-cluster-api/) 
            
            
## Create the Secret
        
### (Optional) Create Certificates and Private Keys
        
    Notes: you can skip this step. We already did it for you. It is incuded so others can expose their own services through Ingress.
          
    1.  To encrypt the connection between the browser and the ingress controller, you need to attach a certificate and a private key to 
        the Ingress resource. To do that, the certificate and private key must be stored in a Kubernetes resource called a secret so it 
        can be referenced by the Ingress resource. 

        a.  `$ openssl genrsa -out tls.key 2048`
                
        b.  `$ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org`
                
        c.  `$ kubectl create secret tls daytrader-web --cert=tls.crt  --key=tls.key`
           
        d.  `$ kubectl get secrets daytrader-web -o yaml --export > secret.yaml`
                
    2.  Check and verify your certificate and key. This can be useful before applying them to your server. 

        a.  `$ openssl x509 -in tls.crt -text`  ## Check the certificate and its information (signing authority, expiration date, etc.)

        b.  `$ openssl rsa -in tls.key -check`  ## Check the SSL key and verify the consistency
    
    3.  Verify the certificate and key match by comparing their checksums for equality

        a.  `$ openssl x509 -noout -modulus -in tls.crt | openssl md5`
        
        b.  `$ openssl rsa -noout -modulus -in tls.key | openssl md5`
            

### (Optional) Review the Secret YAML

    The `secret.yaml` contains certificates and private keys that are used by other resources; such as Ingress
    
    ```yaml
    apiVersion: v1
    data:
      tls.crt: ""
      tls.key: ""
    kind: Secret
    metadata:
      creationTimestamp: null
      name: daytrader-web
      selfLink: /api/v1/namespaces/default/secrets/daytrader-web
    type: kubernetes.io/tls
    ```

### Create the Secret Resource

    1.  `$ kubectl apply -f secret.yaml`
                
    2.  (Optional) `$ kubectl get secrets`
                
        NAME            TYPE                                  DATA   AGE
        daytrader-web   kubernetes.io/tls                     2      8s
            

## Create the Ingress

### (Optional) Review the Inress YAML

    The `inress.yaml` defines the custom domain name for accesing the applicaiton through the ingress and the matching secret to use for incoming TLS/SSL requests
    
    ```yaml
    apiVersion: extensions/v1beta1
    kind: Ingress
    metadata:
      name: daytrader-web
      annotations:
        kubernetes.io/ingress.allow-http: "false"
        nginx.ingress.kubernetes.io/ssl-passthrough: "true"
    spec:
      tls:
      - hosts:
        - web.daytrader.geronimo.apache.org
        secretName: daytrader-web
      rules:
        - host: web.daytrader.geronimo.apache.org
          http: 
            paths:
            - path: /
              backend:
                serviceName: daytrader-web
                servicePort: 443 
    ```

### Create the Ingress Resource

    1..  `$ kubectl apply -f ingress.yaml`
                
    2.  (Optional) `$ kubectl get ingress`
    
        NAME            HOSTS                               ADDRESS       PORTS     AGE
        daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx   80, 443   23h
                    
        ## Wait for the Ingress service to run; then record the HOSTS and ADDRESS

    3.  Notes:
                
        a.  The HOSTS was specified in the ingress.yaml and match the Secrets (tls.crt CN)
                        
        b.  The ADDRESS is the DNS Name or IP Address to the Ingress resource's load balancer
            
    4.  (Optional) If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-***********
        
        a.  `$ kubectl -n ingress-nginx get pods`
                
            NAME                                       READY   STATUS    RESTARTS   AGE
            nginx-ingress-controller-*********-*****   1/1     Running   0          1h
            
        b.  `$ kubectl -n ingress-nginx logs nginx-ingress-controller-*********-*****`
        
            ## If there are errors in the logs then they should give you a good indication of what went wrong

## Setup the DNS

    To access you application via ingress, the incoming request must be coming from the custom domain name (HOSTS) that you specified in the 
    ingress.yaml (whcih is also the the CN that you specified for the crt in the secrets.yaml). Otherwise you will get an invalid hosts exception during 
    SSL/TLS handshake. This means that you must setup DNS to resolve the HOSTS (custom domain name) to the ingress' ADDRESS. There are a couple of ways to do
    that,
    
### Use /etc/hosts for your custom domain

    This is a good solution for local development servers
    
    1.  Add an entry to your /etc/hosts in the following format
        
        <ADDRESS>   <web.daytrader.geronimo.apache.org>
            
    2.  Notes: <ADDRESS> must be an External IP Address. 
            
        a.  If the ADDRESS is a DNS name, then `ping` <ADDRESS>` to get the External IP Address.
             
        b.  If the ADDRESS is an Internal IP Address, then `kubectl cluster-info` to get the External IP Address

### Use CNAME record for your custom domain

    This is a good solution for DEV, TST, and PRD environments
        
    1.  (TODO) Create a CNAME record to your DNS custom domain

### Test the DNS resolution for your custom domain name
    
    1.  Using the curl command
                    
        `$ curl -k https://web.daytrader.geronimo.apache.org/health`

        {"status":"UP"} 

    2.  Using the browser
           
        Point you browser to `https://web.daytrader.geronimo.apache.org/health`
    
        {"status":"UP"} 

    3.  If the connection timed out then double check your DNS configuration
    
        
## Perform the functional test


### Provision the environment
    
    Deploy the microservices to the cluster. This means you need to build and push the docker images, and to create the deployment, service, secret, and ingress resources. To do that, follow the

    1.  [Accounts README](https://github.com/jpmorganchase/daytrader-example-accountsrepo/) instructions to build and push the docker image, and to create the Kubernetes resources 

    2.  [Gateway README](https://github.com/jpmorganchase/daytrader-example-accountsrepo/) instructions to build and push the docker image, and to create the Kubernetes resources 

    3.  [Portfolios README](https://github.com/jpmorganchase/daytrader-example-accountsrepo/) instructions to build and push the docker image, and to create the Kubernetes resources 

    4.  [Quotes README](https://github.com/jpmorganchase/daytrader-example-accountsrepo/) instructions to build and push the docker image, and to create the Kubernetes resources 
    
    5.  [Web README](https://github.com/jpmorganchase/daytrader-example-accountsrepo/) instructions to build and push the docker image, and to create the Kubernetes resources 
         
                                
### Run the tests
                                 
    Open your browser and navigate to: `https://web.daytrader.geronimo.apache.org`
    
    1.  Under The Configuration Tab
    
        a.  Press(Re)-create DayTrader Database Tables and Indexes
        
        b.  Press (Re)-populate DayTrader Database
    
        c.  Press Test Daytrader Scenario
    
    2.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

        Note: each time you refresh the browser, it runs another functional test scenario. 

    That's it! You have now tested the daytrader
        
        
### Decommission the environment

    (Optional) `$ kubectl delete all --all`
    
    Notes: these steps to teardown the environement can be automated via Maven at a later date
    
                            
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




