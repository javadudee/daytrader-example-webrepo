

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

### Installation

#### For building Docker images and pushing them to a Docker registry

		1.	Inatall Java Development Kit (64 bit) 1.8+
		2.	Install Apache Maven 3.3.9+
		3.  Create DockerHub Account (https://hub.docker.com/)
		4.  Install Docker (https://www.docker.com/get-docker)
		
#### For deploying and managing applications on Kubernetes

		1.  Install kubectl (https://kubernetes.io/docs/tasks/tools/install-kubectl/)

#### For running Kubernetes on Minikube 

		1.  Install Minikube (https://kubernetes.io/docs/setup/minikube/)

#### For running Kubernetes on Amazon EKS

		1.  Create AWS Account (https://aws.amazon.com/premiumsupport/knowledge-center/create-and-activate-aws-account/)
		2.  Install AWS IAM Authenticator (https://docs.aws.amazon.com/eks/latest/userguide/configure-kubectl.html)
		2.  Install AWS Command Line Interface (https://docs.aws.amazon.com/cli/latest/userguide/installing.html)
		3.  Install eksctl (https://github.com/weaveworks/eksctl)


### Configuration

#### Maven

        The following settings are required for Maven to push Docker images to you DockerHub account

        1.  Change the following property in daytrader-web/pom.xml to your DockerHub user name:
    
            <docker.image.prefix>${user.name}</docker.image.prefix>
               
        3.  Change the following key-value in daytrader-web/env/external/k8s/web-deployment.yaml to your DockerHub user name
        
            image: YOUR_DOCKERHUB_USERNAME/daytrader-gateway:4.0.18
        
        4.  The following property in daytrader-web/pom.xml is also worth noting. You do not have to change it, but if you do, change the above key-value accordingly.
        
    		<tag>4.0.18</tag>
        
        5.  Add the following settings to your Maven ~/.m2/settings.xml so Maven can push images to your DockerHub account

                <servers>
                  <server>
                        <id>docker.io</id>
                        <username>YOUR_DOCKERHUB_USERNAME</username>
                        <password>YOUR_DOCKERHUB_PASSWORD</password>
                    </server>
                </servers>       
   
        

#### AWS CLI

        1.  Configuring the AWS CLI (https://docs.aws.amazon.com/cli/latest/userguide/cli-chap-getting-started.html)

                
                
#### Kubernetes Cluster

        1.  You must have at least one Kubernetes cluster.

                a.  For Minikube,
        
                    $ minikube --memory 8192 --cpus 2 start
            
                    ## Minikube comes with a cluster. It was running out of memory so increased to 8GB of RAM. You may be able to run with less RAM, but that setting worked well.
            
                b.  For EKS, 
        
                    $ eksctl create cluster --name=user116-eks-cluster --region=us-east-1
            
                    ## This command creates an EKS cluster named user116-eks-cluster in the us-east-1 region. For additional details on parameters, see https://github.com/weaveworks/eksctl
            
        2.  If you installed Minikube and Amazon EKS, then you will have two contexts in your ~/.kube/config file. One of those contexts is for accessing the Minikube
            cluster; the other context is for accessing the EKS cluster. You must set the context to the cluster before running kubectl commands. Set the context once
            then all subsequent kubectl commands will be directed to that cluster. Set it again and the kubectl commands will go to the other cluster. To do that,
                  
                a.  For Minikube, 
                
                    $ kubectl config use-context minikube
                        
                b.  For Amazon EKS, 
                
                    $ kubectl config use-context user116-eks-cluster  ## Use the name of your Amazon EKS cluster

        3.  Now you can verify your cluster by running some kubectl commands
        
                a.  For Minikube
        
                    $ kubectl config use-context minikube
                    
                    Switched to context "minikube".

                    $ kubectl cluster-info
                    
                    Kubernetes master is running at https://192.168.99.100:8443
                    KubeDNS is running at https://192.168.99.100:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
                        
                b.  For Amazon EKS,
        
                    $ kubectl config use-context user116-eks-cluster  ## Use the name of your Amazon EKS cluster
                    
                    Switched to context "user116-eks-cluster".
            
                    $ kubectl kubectl cluster-info
                    
                    Kubernetes master is running at https://8D36DB7CBD2E3394FF0843CEA0C0A266.sk1.us-east-1.eks.amazonaws.com
                

   
## Delivery Pipeline


### Build Automation

		1.	cd daytrader-webapp
		2.	mvn clean install


### Functional Test (Spring Boot)

	The functional test is done in three phases: pre-functional-test functional-test, and post-functional-test

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

		4. 	Start Gateway

			a.	daytrader-gatewayapp\daytrader-gateway
			b.	env\external\bin\start_server.bat

		5.	Start Web

			a.	cd daytrader-webapp\daytrader-web
			b.	env\external\bin\start_server

#### Phase 2: functional-test

		1.	Open: https://localhost:5443

			a.	Under The Configuration Tab

				- Press(Re)-create DayTrader Database Tables and Indexes
				- Press(Re)-populate DayTrader Database
				- Press Test Daytrader Scenario

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



### Continuous Delivery

    Many of these manual steps can be automated by maven at a later date.
    

#### Create the Docker image and push it to DockerHub

        1.	$ cd daytrader-webapp
	    2.	$ mvn -Pcd clean install
		
	
#### Configure kubectl to access the cluster
    
        1.  For Minikube,  
        
            $ minikube --memory 8192 --cpus 2 start
        
            $ kubectl config use-context minikube   
        
        2.  For EKS, 
        
            $ kubectl config use-context eks
            
            
            
#### Create a Deployment to run the application in one or more pods

    Notes: A Deployment also creates the specified number of PODs and a ReplicaSet to manage the lifecycle of the pods

	    1.  $ cd daytrader-quotesapp\daytrader-web\env\external\k8s
    
		2.	$ kubectl apply -f deployment.yaml 
		
		3.  $ kubectl get pods
		
    		NAME                                READY   STATUS    RESTARTS   AGE   
            daytrader-web-87b5658f4-889gp       1/1     Running   0          6m    
		
		    ## Record the NAME of the pod, and wait until the STATUS is Running and all pods are READY (e.g. 1/1, 2/2, or 3/3)
              
        4.  $ kubectl logs <POD_NAME>
        
            ##  You should see : Tomcat started on port(s): 5443 (https). If not see Troubleshooting
            
        
#### Create a Service to load balance requests across the Pods

	Before creating the service it is important to understand the rationale for creating them. Remember that pods are ephemeral. If 
	a pod stops, then Kubernetes will create an identical one, but it will have a different IP. For that reason, clients should not 
	communicate with application's directly. Instead they should talk to applications via service. To create the service,
        
        1.  $ kubectl apply -f service.yaml
           
        2.  $ kubectl get services
        
            NAME           	TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)         AGE
            daytrader-web	ClusterIP   10.100.17.24     <none>        443/TCP         3d
       
            ## Notes
        
            	a.	The above command creates a service resource. This resource listens on the CLUSTER IP and PORT. It is backed by endpoints 
               	    to one or more pods. You can see the dnpoints buy doing kubectl get endpoints. When the service receives an HTTPS request, 
               	    it selects one of the endpoints (pods) and forwards the HTTPS request to the pod. The application running inside the pod 
               	    handles the request and returns an HTTPS response.
            	
              	b.	See also https://kubernetes.io/docs/concepts/services-networking/service/   
        

#### Create an Ingress to direct external requests to the Service

        
        1.  (Optional) Create certificates and private keys
        
            Notes: you can skip this step. We already did it for you. It is incuded so others can expose their own services throug Ingress.
          
            To encrypt the connection between the browser and the ingress controller, you need to attach a certificate and a private key to 
            the Ingress resource. To do that, the certificate and private key need to be stored in a Kubernetes resource called a secret; which is then 
            referenced by the Ingress resource. 

                a.  $ openssl genrsa -out tls.key 2048
                
                b.  $ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org
                
                c.  $ kubectl create secret tls daytrader-web --cert=tls.crt  --key=tls.key
           
                d.  $ kubectl get secrets daytrader-web -o yaml --export > secret.yaml  

        2. Create the Secret for TLS/SSL

                a.  $ kubectl apply -f secret.yaml
                
                b.  $ kubectl get secrets
                
                    NAME            TYPE                                  DATA   AGE
                    daytrader-web   kubernetes.io/tls                     2      8s
    
        3.  Create the Ingress resource

                a.  $ kubectl apply -f ingress.yaml
                
              	b.	$ kubectl get ingress  
    
                    NAME            HOSTS                               ADDRESS       PORTS     AGE
                    daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx   80, 443   23h
                    
                    ## Wait for the Ingress service to run; then record the HOSTS and ADDRESS

                Notes 
                
                    - The HOSTS is the host you specified in your Ingress resource; which match the CN in your certificate
                
                    - For Minikube
                            
                        - The ADDRESS is the private IP Address of the Ingress resource.
                        - Use the "minikube ip" command to get the public IP Address 
              
                    -   EKS Notes 
                        
                            - The ADDRESS is the DNS Name (A Record) to the Load Balancer (ELB)


        4.  (For Developer Testing) Add the public IP Address and HOSTS to /etc/hosts

                a.  Get the ADDRESS and HOST of the Ingress resource
                                                           
                    $ kubectl get ingress  
    
                    NAME            HOSTS                               ADDRESS     PORTS     AGE
                    daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx 80, 443   4

                    ## Wait for the Ingress service to run then record the HOSTS and ADDRESS
                        
                    Notes
                        
                        - For Minikube, the ADDRESS is the *private* IP address of ingress resource
                        - For EKS, the ADDRESS is the DNS Name (A Record) to the Load Balancer (ELB) 
                        - HOSTS is the host in the ingress.yaml; it must match the CN in the tls.crt
                                                                  
                b.  Get the public IP Address of the Ingress resource
                
                    For Minikube
                                            
                        $ minikube ip
                        
                        192.168.99.100
                        
                        ## Record the IP Address. That is the public IP Address for the Ingress service in Minikube
                            
                    For EKS,
                        
                        $ ping <ADDRESS>
                        
                        PING <ADDRESS> (52.206.153.148) 56(84) bytes of data.
                        
                        ## Record the Public IP Address. That is the public IP Address for the Ingress service in EKS
                        
                        Notes:                            
                              
                            - The public IP address associated with the Ingress service (Load Balancer) in EKS can change over time. So you should 
                              only use the IP address for developer testing. Instead, you shold create a CNAME record for the LoadBalancer DNS name, 
                              or use Amazon Route 53 to create a hosted zone. 
                              
                            - See also https://stackoverflow.com/questions/24631714/dynamic-ip-changing-in-amazon-elastic-load-balancer-creating-issue
        
                d.  Add a lines to your /etc/hosts that maps the *Public* IP Address to the HOSTS, e.g.
        
                        <Public IP Address>  web.daytrader.geronimo.apache.org         

                e.  Test the connection
    
                    --  Using the curl command
                    
                        $ curl -k -v https://web.daytrader.geronimo.apache.org/health

                        {"status":"UP"} 
                    
                        ## You should see the above, but if not see Troubleshooting

        	        --- Using the browser
    	        
    	                Point you browser to https://web.daytrader.geronimo.apache.org/health
 	
    	    			{"status":"UP"} 

                        ## You should see the above, but if not see Troubleshooting

        
                            
## Troubleshooting


### Pods
        
        1.	Retrieve the pods (application) logs
   
                a.  $ kubectl get pods
                 
	           	b.	$ kubectl logs <POD_NAME> -f      ## The -f option tails the logs
               

        2.  Access the pod (application) through the port forwarder
        
	            a.	$ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
        	    	Forwarding from 127.0.0.1:8888 -> 5443
       
	            b.	$ curl -k https://127.0.0.1:8888/health

        	    	{"status":"UP"}      

        	    	
### Services
        
        1.	Access the service (application) through the API Server. 
                        
	    		a.	$ kubectl proxy
        
	    			Starting to serve on 127.0.0.1:8001
                
	    			## Locates and authenticates to the API Server.

    			b.	$ curl -k http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/health      
		
	    			{"status":"UP"}      		
				
	    		    ## The curl command sent the HTTP request (without any authentication headers) to the proxy that is running on your local machine.
	    		    ## The proxy, then, sent an HTTPS request to the API server's identity by validating its certificate. Encryption, authentication,
	    		    ## and server verification is handled by the proxy. This is the easiest way to connect to the API server. 
			    
	    		c. See also
						  
	    		   - https://kubernetes.io/docs/tasks/administer-cluster/access-cluster-api/ 

	    		   
### Ingress
			                 
	    1.	Check and verify your certificate and key. This can be useful before applying them to your server. 

		        a.	$ openssl x509 -in tls.crt -text  ## Check the certificate and its information (signing authority, expiration date, etc.)

        		b.	$ openssl rsa -in tls.key -check  ## Check the SSL key and verify the consistency
    
    	2. 	Verify the certificate and key match by comparing their checksums for equality

	        	a.	$ openssl x509 -noout -modulus -in tls.crt | openssl md5
        
        		b.	$ openssl rsa -noout -modulus -in tls.key | openssl md5      
                                            
        3.  If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-***********
        
                a.  $ kubectl -n ingress-nginx get pods
                
                    NAME                                       READY   STATUS    RESTARTS   AGE
                    nginx-ingress-controller-f88c75bc6-qbccj   1/1     Running   0          1h
                
                b.  $ kubectl -n ingress-nginx logs nginx-ingress-controller-f88c75bc6-qbccj
        
                    ## If there are any errors in the logs then they should give you a good indication of what went wrong
                    
        4.  If the connection timed out when accessing the application through an Ingress then
        
                a.  Make sure that you have the correct HOST and Public IP Address in your /etc/hosts file. 
                
                b.  For EKS
                
                    Make sure the "Security group for Kubernetes ELB" allows inbound traffic from your home IP address
                    
                    TYPE    PROTOCOL    PORT RANGE      SOURCE                  DESCRIPTION
                    HTTP    TCP         80              Custom  xx.xx.xx.xx/32  Home IP access
                    HTTPS   TCP         443             Custom  xx.xx.xx.xx/32  Home IP access     
                    


### Dashboard

        1.  Use kubernetes dashboard to manage and troubleshoot your cluster
        
            For Minikube
        
                a.  $ minikube dashboard
        
            For EKS
        
                a.  $ kubectl proxy
                
                b.  $ kubectl -n kube-system get secrets | grep admin
               
                      ## Copy the NAME of the admin-user-token
                
                c.  $ kubectl -n kube-system describe secrets <ADMIN_USER_TOKEN>
                
                    ## Copy the value of the authentication token
                                    
                d.  Open the following link in a browser to access the dashboard
                
                    http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/
                    
                e.  You will see a login screen; choose Token and then enter the <AUTHENTICATION_TOKEN> from step c. 
   
            See also 
    
                a.  https://github.com/AcalephStorage/kubernetes-dashboard/blob/master/docs/user-guide/troubleshooting.md
                b.  https://docs.aws.amazon.com/eks/latest/userguide/dashboard-tutorial.html
       


### Functional Test (Kubernetes)

    The functional test is done in three phases: pre-functional-test functional-test, and post-functional-test


#### Phase #1: Pre-Functional Test
    
        Deploy microservies per their Continuous Delivery instructions. See the README.MD files in the following repos
        
            1.  daytrader-example-accountserepo
            2.  daytrader-example-gatewayrepo
            3.  daytrader-example-portfoliosrepo
            4.  daytrader-example-quotesrepo
            5.  daytrader-example-webrepo
               
                
#### Phase #2: Functional Test

        For Minikube,  
        
            1.  $ minikube --memory 8192 --cpus 2 start
            
            2.  $ kubectl config use-context minikube
            
            2.  $ kubectl cluster-info
                                    
                Kubernetes master is running at https://192.168.99.100:8443
            
                ## Note the IP address of the Kubernetes master (192.168.99.100).
                
            4.  $ kubectl get services daytrader-web
            
                NAME                   TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)         AGE
                daytrader-web          NodePort    10.98.28.179     <none>        443:31198/TCP   9d

                ## The PORT(S) column shows the internal port of the cluter IP (443) and the node port (31198)
        
    		5.	Open: https://192.168.99.100:{NODE_PORT}
    			
            	a.	Under The Configuration Tab

        			- Press(Re)-create DayTrader Database Tables and Indexes
		        	- Press(Re)-populate DayTrader Database
	        		- Press Test Daytrader Scenario

			    b.	Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

				Note: each time you refresh the browser, it runs another functional test scenario. 

			That's it! You have now tested the daytrader
        
        For EKS, 
        
            1.  $ kubectl config use-context eks
            
            2.  $ kubectl cluster-info
                                    
                Kubernetes master is running at https://8D36DB7CBD2E3394FF0843CEA0C0A266.sk1.us-east-1.eks.amazonaws.com
            
                ## Note the IP address of the Kubernetes master.
                
            3.  $ kubectl proxy
            
                Starting to serve on 127.0.0.1:8001 
            
                ## This command starts a reverse proxy for locating the apiserver and authenticating for and on behalf of the browser
                            
    		5.	Open your browser and navigate to: http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/
    			
            	a.	Under The Configuration Tab

        			- Press(Re)-create DayTrader Database Tables and Indexes
		        	- Press(Re)-populate DayTrader Database
	        		- Press Test Daytrader Scenario

			    b.	Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

				Note: each time you refresh the browser, it runs another functional test scenario. 

			That's it! You have now tested the daytrader
        
        
#### Phase #3: Post-Functional Test

        (Optional)
                          
        $ kubectl delete all --all    
    
    



## Swagger UI Documentation



### Access Swagger UI via Spring Boot

   		1. Start the application

     			$ cd daytrader-webapp
       			$ daytrader-web\env\external\bin\start_server

   		2. 	Point you browser to https://localhost:5443/swagger-ui.html

	    3.	Stop the application after reviewing the APIs

      			$ cd daytrader-webapp	
            	$ daytrader-web\env\external\bin\stop_server



### Access Swagger UI via Kubernetes


#### Pod
        
        1.  Make sure the application is accessible via the Pod resource
                
   		2. 	Point you browser to https://localhost:8888/swagger-ui.html

#### Service
        
        1.  Make sure the application is accessible via the Service resource

		2. 	Point you browser to http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html        		
        		
#### Ingress
        
        1.  Make sure the application is accessible via the Ingress resource

        2. 	Point you browser to https://web.daytrader.geronimo.apache.org/swagger-ui.html
         
