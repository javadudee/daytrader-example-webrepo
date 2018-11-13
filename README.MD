

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


## Run on Spring Boot

![SPRING-BOOT](docs/SPRING-BOOT.MD)

## Run on Minikube

### Prerequisites

		1.	Install Java Development Kit (64 bit) 1.8+

		2.	Install Apache Maven 3.3.9+
        
   		3.  Create DockerHub Account (https://hub.docker.com/)

		4.  Install Docker (https://www.docker.com/get-docker)
        
        5.  Configure Maven to push Docker images to you DockerHub account

                a.  Change the following property in daytrader-web/pom.xml to your DockerHub user name:
    
                        <docker.image.prefix>${user.name}</docker.image.prefix>
               
                b.  Change the following key-value in daytrader-web/env/external/k8s/web-deployment.yaml to your DockerHub user name
        
                        image: YOUR_DOCKERHUB_USERNAME/daytrader-gateway:4.0.18
        
                c.  The following property in daytrader-web/pom.xml is also worth noting. You do not have to change it, but if you do, change the above key-value accordingly.
        
        	        	<tag>4.0.18</tag>
        
                d.  Add the following settings to your Maven ~/.m2/settings.xml so Maven can push images to your DockerHub account

                        <servers>
                            <server>
                                <id>docker.io</id>
                                <username>YOUR_DOCKERHUB_USERNAME</username>
                                <password>YOUR_DOCKERHUB_PASSWORD</password>
                            </server>
                        </servers> 

		6.  Install kubectl (https://kubernetes.io/docs/tasks/tools/install-kubectl/)
		
		7.  Install, Start, and Verify the Minikube Cluster
		
                a.  Install the latest release of Minikube (https://github.com/kubernetes/minikube/releases)
		        
                b.  Start the Minikube cluster
                
                        --  $ minikube start --cpus 2 --memory 8192 
                        
                            Starting local Kubernetes v1.10.0 cluster...
                            Starting VM...
                            Getting VM IP address...
                            Moving files into cluster...
                            Setting up certs...
                            Connecting to cluster...
                            Setting up kubeconfig...
                            Starting cluster components...
                            Kubectl is now configured to use the cluster.
                            Loading cached images from config file.           
                
                c.  Verify the Minikube cluster
                
                        -  $ kubectl config use-context minikube
                    
                            Switched to context "minikube".

                        -  $ kubectl cluster-info
                    
                           Kubernetes master is running at https://192.168.99.100:8443
                           KubeDNS is running at https://192.168.99.100:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
                    
                        -  $ kubectl get nodes
                    
                            NAME       STATUS   ROLES    AGE   VERSION
                            minikube   Ready    master   24d   v1.10.0

        8.  Install the NGINX Ingress Controller
        
                a.  Create these configmaps for tcp-services and udp-services; otherwise the controller will throw an unexpected error
                
                        --  $ cd daytrader-webapp/daytrader-web/env/external/k8s
                    
                        --  $ kubectl apply -f tcp-services-configmap.yaml
                    
                        --  $ kubectl apply -f udp-services-configmap.yaml
                    
                    ## See also: https://github.com/kubernetes/minikube/blob/master/deploy/addons/ingress/ingress-configmap.yaml 
                
                b.  Enable the ingress add-on
               
                       --  $ minikube addons enable ingress
                    
                           ingress was successfully enabled
                    
                c.  Make sure the controller is running and in the ready state. It may take a few minutes to start
                
                       --  $ kubectl -n kube-system get pods 
                    
                           NAMESPACE     NAME                                        READY   STATUS    RESTARTS   AGE
                           kube-system   nginx-ingress-controller-67956bf89d-j7d6c   1/1     Running   0          39s
                    
                d.  Review the logs to verify the installation. This is also useful for troubleshooting ingress resources.
                
                       --  $ kubectl -n kube-system logs <POD_NAME>
                    
                           ## You should see "successfully acquired lease kube-system/ingress-controller-leader-nginx". 

                e.  See also https://kubernetes.github.io/ingress-nginx/deploy/


        9.  (Optional) Install the Kubernetes Dashboard
        
            Notes: this dashboard can be useful for managing and troubleshooting your cluster
               
                a.  $ minikube dashboard
        

### Build Automation

        1.	$ cd daytrader-webapp     
	    2.	$ mvn -Pcd clean install   ## builds docker image and pushes to docker hub


### Continuous Delivery

        Note: many of these manual steps can be automated via Maven at a later date
		            
            
#### Create Deployment

    Notes: Deployment creates the PODs that run the application and a ReplicaSet to manage the lifecycle of pods

	    1.  $ cd daytrader-quotesapp\daytrader-web\env\external\k8s
    
		2.	$ kubectl apply -f deployment.yaml 
		
		3.  $ kubectl get pods
		
    		NAME                                READY   STATUS    RESTARTS   AGE   
            daytrader-web-87b5658f4-889gp       1/1     Running   0          6m    
		
		    ## Record the NAME of the pod, and wait until the STATUS is Running and all pods are READY (e.g. 1/1, 2/2, or 3/3)
              
        4.  Retrieve the pods (application) logs
        
                a.  $ kubectl logs <POD_NAME> -f     ## the -f option tails the logs
        
                    Tomcat started on port(s): 5443 (https).              

        5.  Access the pod (application) through the port forwarder
        
	            a.	$ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
        	    	Forwarding from 127.0.0.1:8888 -> 5443
       
	            b.	$ curl -k https://127.0.0.1:8888/health

        	    	{"status":"UP"} 
            
        
#### Create Service 

    Note: Service load balances requests across the Pods. It is important to remember that pods are ephemeral. If a pod stops, then 
    Replica Sets will create a replacement, but it will have a different IP. For that reason, clients should not communicate to the
    the applicaiton via the Pod. Instead they should talk to applications via the service. To create the service,
        
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
              	
         3.  Test the connection to the service through the API Server. 
                        
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

#### Create Secret
        
        1.  (Optional) Create certificates and private keys
        
            Notes: you can skip this step. We already did it for you. It is incuded so others can expose their own services throug Ingress.
          
                a.  To encrypt the connection between the browser and the ingress controller, you need to attach a certificate and a private key to 
                    the Ingress resource. To do that, the certificate and private key need to be stored in a Kubernetes resource called a secret; which is then 
                    referenced by the Ingress resource. 

                        --  $ openssl genrsa -out tls.key 2048
                
                        --  $ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org
                
                        --  $ kubectl create secret tls daytrader-web --cert=tls.crt  --key=tls.key
           
                        --  $ kubectl get secrets daytrader-web -o yaml --export > secret.yaml  
                
                b.  Check and verify your certificate and key. This can be useful before applying them to your server. 

        		        -- 	$ openssl x509 -in tls.crt -text  ## Check the certificate and its information (signing authority, expiration date, etc.)

                		--	$ openssl rsa -in tls.key -check  ## Check the SSL key and verify the consistency
    
            	c.  Verify the certificate and key match by comparing their checksums for equality

        	        	--	$ openssl x509 -noout -modulus -in tls.crt | openssl md5
        
                		--	$ openssl rsa -noout -modulus -in tls.key | openssl md5   

        2. Create the Secret for TLS/SSL

                a.  $ kubectl apply -f secret.yaml
                
                b.  $ kubectl get secrets
                
                    NAME            TYPE                                  DATA   AGE
                    daytrader-web   kubernetes.io/tls                     2      8s
            



#### Create Ingress

        1.  Create the Ingress resource

                a.  $ kubectl apply -f ingress.yaml
                
              	b.	$ kubectl get ingress  
    
                    NAME            HOSTS                               ADDRESS       PORTS     AGE
                    daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx   80, 443   23h
                    
                    ## Wait for the Ingress service to run; then record the HOSTS and ADDRESS
                            
                c.  If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-***********
        
                        --  $ kubectl -n ingress-nginx get pods
                
                            NAME                                       READY   STATUS    RESTARTS   AGE
                            nginx-ingress-controller-f88c75bc6-qbccj   1/1     Running   0          1h
                
                        --  $ kubectl -n ingress-nginx logs nginx-ingress-controller-f88c75bc6-qbccj
        
                        ## If there are any errors in the logs then they should give you a good indication of what went wrong
                        
                d.  Record the HOSTS and ADDRESS 
                
                        -- The HOSTS is the host you specified in your Ingress resource; it must match the CN in the tls.crt
                        
                        -- The ADDRESS is the Internal IP Address in Minikube; you'll get the Public IP Address in the next step

        2.  Use an /etc/hosts file for your custom domain (HOSTS) during development
                                                                                         
                a.  Get the Public IP Address of the Ingress resource
                                                            
                        --  $ minikube ip
                        
                            192.168.99.100
                        
                b.  Record the Minikube IP; that is the Public IP Address for the Ingress resource
                                    
                c.  Add an entry to your /etc/hosts in the following format
        
                        52.206.153.148  web.daytrader.geronimo.apache.org                

        3.  Use a CNAME record for your custom domain (HOST) during test and production
        
                a.  (TODO) Create a CNAME record for your custom domain (HOST) to point to the ADDRESS

        4.  Test the connection
    
                a.  (Option 1) Using the curl command
                    
                        $ curl -k -v https://web.daytrader.geronimo.apache.org/health

                        {"status":"UP"} 

            	b.  (Option 2) Using the browser
    	        
        	            Point you browser to https://web.daytrader.geronimo.apache.org/health
 	
        	    	    {"status":"UP"} 

        5.  If the connection timed out then
        
                a.  Make sure that you have the correct HOST and Public IP Address in your /etc/hosts file.     
                    

        
### Functional Test

    The functional test is done in three phases: pre-functional-test functional-test, and post-functional-test
    
    



#### Phase #1: pre-functional-test
    
        Deploy the microservies. See the README.MD files in the following repos
        
            1.  daytrader-example-accountserepo
            2.  daytrader-example-gatewayrepo
            3.  daytrader-example-portfoliosrepo
            4.  daytrader-example-quotesrepo
            5.  daytrader-example-webrepo
               
                                
#### Phase #2: functional-test
                                 
    	Open your browser and navigate to: https://web.daytrader.geronimo.apache.org
    			
            1.	Under The Configuration Tab

        			- Press(Re)-create DayTrader Database Tables and Indexes
		        	- Press(Re)-populate DayTrader Database
	        		- Press Test Daytrader Scenario

			2.	Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

				    Note: each time you refresh the browser, it runs another functional test scenario. 

			That's it! You have now tested the daytrader
        
        
#### Phase #3: post-functional-test

        (Optional)
                          
        $ kubectl delete all --all    
    

                            
### Swagger UI Documentation

        1.  (Option 1) Access Swagger UI through port forwarding
         
                a.	$ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
        	    	Forwarding from 127.0.0.1:8888 -> 5443
            
                b.  Point you browser to https://localhost:8888/swagger-ui.html

		2.  (Option 2) Access Swagger UI through the API Server
		
			    a.	$ kubectl proxy
        
	    			Starting to serve on 127.0.0.1:8001
                
	    			## Locates and authenticates to the API Server. 
		
		        b.  Point you browser to http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html        		
        		
        3.  (Option 3) Access Swagger UI through an Ingress
        
                a.  Point you browser to https://web.daytrader.geronimo.apache.org/swagger-ui.html
         

		                
## Run on EKS

### Prerequisites

   		1.	Install Java Development Kit (64 bit) 1.8+

		2.	Install Apache Maven 3.3.9+
        
		3.  Create DockerHub Account (https://hub.docker.com/)

		4.  Install Docker (https://www.docker.com/get-docker)
        
        5.  Configure Maven to push Docker images to your DockerHub account

                a.  Change the following property in daytrader-web/pom.xml to your DockerHub user name:
    
                        <docker.image.prefix>${user.name}</docker.image.prefix>
               
                b.  Change the following key-value in daytrader-web/env/external/k8s/web-deployment.yaml to your DockerHub user name
        
                        image: YOUR_DOCKERHUB_USERNAME/daytrader-gateway:4.0.18
        
                c.  The following property in daytrader-web/pom.xml is also worth noting. You do not have to change it, but if you do, change the above key-value accordingly.
        
        	        	<tag>4.0.18</tag>
        
                d.  Add the following settings to your Maven ~/.m2/settings.xml so Maven can push images to your DockerHub account

                        <servers>
                            <server>
                                <id>docker.io</id>
                                <username>YOUR_DOCKERHUB_USERNAME</username>
                                <password>YOUR_DOCKERHUB_PASSWORD</password>
                            </server>
                        </servers> 
                        
		6.  Create AWS Account (https://aws.amazon.com/premiumsupport/knowledge-center/create-and-activate-aws-account/)
			
		7.  Install and Verify the EKS Cluster
		
		        a.  Install the EKS Cluster (https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html)
		        
                        --  Start with "Amazon EKS Prerequisites" up to, but not including, "Step 4: Launch a Guest Book Application"

                b.  Verify the EKS Cluster
 
                    --  $ kubectl config use-context user116-eks-cluster  ## Use the name of your Amazon EKS cluster
                    
                        Switched to context "user116-eks-cluster".
            
                    --  $ kubectl kubectl cluster-info
                    
                        Kubernetes master is running at https://8D36DB7CBD2E3394FF0843CEA0C0A266.sk1.us-east-1.eks.amazonaws.com
                    
                    --  $ kubectl get nodes
                
                        NAME                            STATUS   ROLES    AGE   VERSION
                        ip-172-31-19-232.ec2.internal   Ready    <none>   3h    v1.10.3
                        ip-172-31-2-133.ec2.internal    Ready    <none>   3h    v1.10.3
                        ip-172-31-30-11.ec2.internal    Ready    <none>   3h    v1.10.3

        8.  Install the NGINX Ingress Controller
      
                a.  Deploy NGINX Ingress
                
                    --  $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/mandatory.yaml

                    --  $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/provider/cloud-generic.yam
                    
                b.  Make sure the controller is running and in the ready state. Note that it may take a few minutes to start
                
                    --  $ kubectl -n ingress-nginx get pods 
                    
                        NAMESPACE       NAME                                       READY   STATUS    RESTARTS   AGE
                        ingress-nginx   nginx-ingress-controller-f88c75bc6-nc4bt   1/1     Running   0          39s
                    
                c.  Review the logs to verify the installation. This is also useful for troubleshooting ingress resources.
                
                    --  $ kubectl -n ingress-nginx logs <POD_NAME>
                    
                        ## You should see "successfully acquired lease ingress-nginx/ingress-controller-leader-nginx". 
                    
                d.  See also https://kubernetes.github.io/ingress-nginx/deploy/
                    
        9.  (Optional) Install the Kubernetes Dashboard
        
            Notes: this dashboard can be useful for managing and troubleshooting your cluster
                
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


### Build Automation

        1.	$ cd daytrader-webapp     
	    2.	$ mvn -Pcd clean install   ## builds docker image and pushes to docker hub


### Continuous Delivery

        Note: many of these manual steps can be automated via Maven at a later date
		            
            
#### Create Deployment

    Notes: Deployment creates the PODs that run the application and a ReplicaSet to manage the lifecycle of pods

	    1.  $ cd daytrader-quotesapp\daytrader-web\env\external\k8s
    
		2.	$ kubectl apply -f deployment.yaml 
		
		3.  $ kubectl get pods
		
    		NAME                                READY   STATUS    RESTARTS   AGE   
            daytrader-web-87b5658f4-889gp       1/1     Running   0          6m    
		
		    ## Record the NAME of the pod, and wait until the STATUS is Running and all pods are READY (e.g. 1/1, 2/2, or 3/3)
              
        4.  Retrieve the pods (application) logs
        
                a.  $ kubectl logs <POD_NAME> -f     ## the -f option tails the logs
        
                    Tomcat started on port(s): 5443 (https).              

        5.  Access the pod (application) through the port forwarder
        
	            a.	$ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
        	    	Forwarding from 127.0.0.1:8888 -> 5443
       
	            b.	$ curl -k https://127.0.0.1:8888/health

        	    	{"status":"UP"} 
            
        
#### Create Service 

    Note: Service load balances requests across the Pods. It is important to remember that pods are ephemeral. If a pod stops, then 
    Replica Sets will create a replacement, but it will have a different IP. For that reason, clients should not communicate to the
    the applicaiton via the Pod. Instead they should talk to applications via the service. To create the service,
        
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
              	
         3.  Test the connection to the service using the API Server. 
                        
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
		            
            
#### Create Secret
        
        1.  (Optional) Create certificates and private keys
        
            Notes: you can skip this step. We already did it for you. It is incuded so others can expose their own services throug Ingress.
          
                a.  To encrypt the connection between the browser and the ingress controller, you need to attach a certificate and a private key to 
                    the Ingress resource. To do that, the certificate and private key need to be stored in a Kubernetes resource called a secret; which is then 
                    referenced by the Ingress resource. 

                        --  $ openssl genrsa -out tls.key 2048
                
                        --  $ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org
                
                        --  $ kubectl create secret tls daytrader-web --cert=tls.crt  --key=tls.key
           
                        --  $ kubectl get secrets daytrader-web -o yaml --export > secret.yaml  
                
                b.  Check and verify your certificate and key. This can be useful before applying them to your server. 

        		        -- 	$ openssl x509 -in tls.crt -text  ## Check the certificate and its information (signing authority, expiration date, etc.)

                		--	$ openssl rsa -in tls.key -check  ## Check the SSL key and verify the consistency
    
            	c.  Verify the certificate and key match by comparing their checksums for equality

        	        	--	$ openssl x509 -noout -modulus -in tls.crt | openssl md5
        
                		--	$ openssl rsa -noout -modulus -in tls.key | openssl md5   

        2. Create the Secret for TLS/SSL

                a.  $ kubectl apply -f secret.yaml
                
                b.  $ kubectl get secrets
                
                    NAME            TYPE                                  DATA   AGE
                    daytrader-web   kubernetes.io/tls                     2      8s
            

#### Create Ingress

        1.  Create the Ingress resource

                a.  $ kubectl apply -f ingress.yaml
                
              	b.	$ kubectl get ingress  
    
                    NAME            HOSTS                               ADDRESS       PORTS     AGE
                    daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx   80, 443   23h
                    
                    ## Wait for the Ingress service to run; then record the HOSTS and ADDRESS
                            
                c.  If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-***********
        
                        --  $ kubectl -n ingress-nginx get pods
                
                            NAME                                       READY   STATUS    RESTARTS   AGE
                            nginx-ingress-controller-f88c75bc6-qbccj   1/1     Running   0          1h
                
                        --  $ kubectl -n ingress-nginx logs nginx-ingress-controller-f88c75bc6-qbccj
        
                        ## If there are any errors in the logs then they should give you a good indication of what went wrong
                                                                                                
                d.  Record the HOSTS and ADDRESS 
                
                        -- The HOSTS is the host you specified in your Ingress resource; it must match the CN in the tls.crt
                        
                        -- The ADDRESS is the DNS Name (A Record) to the Amazon Elastic Load Balancer (ELB)
                                                                                                
        2.  Use an /etc/hosts file for your custom domain (HOSTS) during development
                                                                  
                a.  Get the Public IP Address of the Ingress resource
                                      
                        --  $ ping <ADDRESS>
                        
                            PING <ADDRESS> (52.206.153.148) 56(84) bytes of data.
                            
                            ## In this example, 52.206.153.148 is the Public IP Address of the ingress resource
                        
                b.  Record the Public IP Address                            
                        
                        --  The public IP address associated with the Ingress service (Load Balancer) in EKS can change over time. So you should 
                            only use the IP address for developer testing. Instead, you shold create a CNAME record for the LoadBalancer DNS name, 
                            or use Amazon Route 53 to create a hosted zone. 
                              
                        --  See also https://stackoverflow.com/questions/24631714/dynamic-ip-changing-in-amazon-elastic-load-balancer-creating-issue
        
                c.  Add an entry to your /etc/hosts in the following format
        
                        52.206.153.148  web.daytrader.geronimo.apache.org                    

        3.  Use a CNAME record for your custom domain (HOST) during test and production
        
                a.  (TODO) Create a CNAME record for your custom domain (HOST) to point to the ADDRESS

        4.  Test the connection
    
                a.  (Option 1) Using the curl command
                    
                        --  $ curl -k -v https://web.daytrader.geronimo.apache.org/health

                            {"status":"UP"} 

            	b.  (Option 2) Using the browser
    	        
        	            --  Point you browser to https://web.daytrader.geronimo.apache.org/health
 	
            	    	    {"status":"UP"} 

        5.  If the connection timed out then
        
                a.  Make sure that you have the correct HOST and Public IP Address in your /etc/hosts file. 
                
                b.  Make sure the "Security group for Kubernetes ELB" allows inbound traffic from your home IP address
                    
                    TYPE    PROTOCOL    PORT RANGE      SOURCE                  DESCRIPTION
                    HTTP    TCP         80              Custom  xx.xx.xx.xx/32  Home IP access
                    HTTPS   TCP         443             Custom  xx.xx.xx.xx/32  Home IP access     
                    
        
        
### Functional Test

    The functional test is done in three phases: pre-functional-test functional-test, and post-functional-test


#### Phase #1: pre-functional-test
    
        Deploy the microservies. See the README.MD files in the following repos
        
            1.  daytrader-example-accountserepo
            2.  daytrader-example-gatewayrepo
            3.  daytrader-example-portfoliosrepo
            4.  daytrader-example-quotesrepo
            5.  daytrader-example-webrepo
               
                                
#### Phase #2: functional-test
                                 
    	Open your browser and navigate to: https://web.daytrader.geronimo.apache.org
    			
            1.	Under The Configuration Tab

        			- Press(Re)-create DayTrader Database Tables and Indexes
		        	- Press(Re)-populate DayTrader Database
	        		- Press Test Daytrader Scenario

			2.	Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

				    Note: each time you refresh the browser, it runs another functional test scenario. 

			That's it! You have now tested the daytrader
        
        
#### Phase #3: post-functional-test

        (Optional)
                          
        $ kubectl delete all --all    
    

                            
### Swagger UI Documentation

        1.  (Option 1) Access Swagger UI through port forwarding
         
                a.	$ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
        	    	Forwarding from 127.0.0.1:8888 -> 5443
            
                b.  Point you browser to https://localhost:8888/swagger-ui.html

		2.  (Option 2) Access Swagger UI through the API Server
		
			    a.	$ kubectl proxy
        
	    			Starting to serve on 127.0.0.1:8001
                
	    			## Locates and authenticates to the API Server. 
		
		        b.  Point you browser to http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html        		
        		
        3.  (Option 3) Access Swagger UI through an Ingress
        
                a.  Point you browser to https://web.daytrader.geronimo.apache.org/swagger-ui.html
                
