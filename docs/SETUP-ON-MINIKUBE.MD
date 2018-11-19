## Setup on Minikube



### Prerequisites

    1.  Install Java Development Kit (64 bit) 1.8+

    2.  Install Apache Maven 3.3.9+
        
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
                
            --  $ kubectl config use-context minikube
                    
                Switched to context "minikube".

            --  $ kubectl cluster-info
                    
                Kubernetes master is running at https://192.168.99.100:8443
                KubeDNS is running at https://192.168.99.100:8443/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
                    
            --  $ kubectl get nodes
                    
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

    1.  $ cd daytrader-webapp
    
	2.  $ mvn -Pcd clean install   ## builds docker image and pushes to docker hub



### Continuous Delivery

    Note: many of these manual steps can be automated via Maven at a later date
            
            
#### Create Deployment

    Notes: Deployment creates the PODs that run the application and a ReplicaSet to manage the lifecycle of pods

    1.  $ cd daytrader-quotesapp\daytrader-web\env\external\k8s
    
    2.  $ kubectl apply -f deployment.yaml 
    
    3.  $ kubectl get pods
    
        NAME                                READY   STATUS    RESTARTS   AGE   
        daytrader-web-87b5658f4-889gp       1/1     Running   0          6m    
    
        ## Record the NAME of the pod, and wait until the STATUS is Running and all pods are READY (e.g. 1/1, 2/2, or 3/3)
              
    4.  Retrieve the pods (application) logs
        
        a.  $ kubectl logs <POD_NAME> -f     ## the -f option tails the logs
        
            Tomcat started on port(s): 5443 (https).              

    5.  Access the pod (application) through the port forwarder
        
        a.  $ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
            Forwarding from 127.0.0.1:8888 -> 5443
       
        b.  $ curl -k https://127.0.0.1:8888/health

            {"status":"UP"} 
            
        
#### Create Service 

    Note: Service load balances requests across the Pods. It is important to remember that pods are ephemeral. If a pod stops, then 
    Replica Sets will create a replacement, but it will have a different IP. For that reason, clients should not communicate to the
    the applicaiton via the Pod. Instead they should talk to applications via the service. To create the service,
        
    1.  $ kubectl apply -f service.yaml
           
    2.  $ kubectl get services
        
        NAME            TYPE        CLUSTER-IP       EXTERNAL-IP   PORT(S)         AGE
        daytrader-web   ClusterIP   10.100.17.24     <none>        443/TCP         3d
       
        ##  Notes
        
        a.  The above command creates a service resource. This resource listens on the CLUSTER IP and PORT. It is backed by endpoints 
            to one or more pods. You can see the dnpoints buy doing kubectl get endpoints. When the service receives an HTTPS request, 
            it selects one of the endpoints (pods) and forwards the HTTPS request to the pod. The application running inside the pod 
            handles the request and returns an HTTPS response.
           
        b.  See also https://kubernetes.io/docs/concepts/services-networking/service/   
              
    3.  Test the connection to the service through the API Server. 
                        
        a.  $ kubectl proxy
        
            Starting to serve on 127.0.0.1:8001
                
            ## Locates and authenticates to the API Server.

        b.  $ curl -k http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/health      
    
            {"status":"UP"}      
    
            ## The curl command sent the HTTP request (without any authentication headers) to the proxy that is running on your local machine.
            ## The proxy, then, sent an HTTPS request to the API server's identity by validating its certificate. Encryption, authentication,
            ## and server verification is handled by the proxy. This is the easiest way to connect to the API server. 
    
        c.  See also
    
            -- https://kubernetes.io/docs/tasks/administer-cluster/access-cluster-api/ 


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

            --  $ openssl x509 -in tls.crt -text  ## Check the certificate and its information (signing authority, expiration date, etc.)

            --	$ openssl rsa -in tls.key -check  ## Check the SSL key and verify the consistency
    
        c.  Verify the certificate and key match by comparing their checksums for equality

            --	$ openssl x509 -noout -modulus -in tls.crt | openssl md5
        
            --	$ openssl rsa -noout -modulus -in tls.key | openssl md5   

    2.  Create the Secret for TLS/SSL

        a.  $ kubectl apply -f secret.yaml
                
        b.  $ kubectl get secrets
                
            NAME            TYPE                                  DATA   AGE
            daytrader-web   kubernetes.io/tls                     2      8s
            

#### Create Ingress

    1.  Create the Ingress resource

        a.  $ kubectl apply -f ingress.yaml
                
        b.  $ kubectl get ingress  
    
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
                
            --  The HOSTS is the host you specified in your Ingress resource; it must match the CN in the tls.crt
                        
            --  The ADDRESS is the Internal IP Address in Minikube; you'll get the Public IP Address in the next step

    2.  Use an /etc/hosts file for your custom domain (HOSTS) during development
                                                                                         
        a.  Get the Public IP Address of the Ingress resource
                                                            
            --  $ minikube ip
                        
                192.168.99.100
                        
        b.  Record the Minikube IP; that is the Public IP Address for the Ingress resource
                                    
        c.  Add an entry to your /etc/hosts in the following format
        
            192.168.99.100  web.daytrader.geronimo.apache.org                

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
        
    1.  daytrader-example-accountserepo (https://github.com/jpmorganchase/daytrader-example-accountsrepo/)

    2.  daytrader-example-gatewayrepo (https://github.com/jpmorganchase/daytrader-example-gatewayrepo/)

    3.  daytrader-example-portfoliosrepo (https://github.com/jpmorganchase/daytrader-example-portfoliosrepo/)
        
    4.  daytrader-example-quotesrepo (https://github.com/jpmorganchase/daytrader-example-quotesrepo/)
        
    5.  daytrader-example-webrepo (https://github.com/jpmorganchase/daytrader-example-webrepo/)
               
                                
#### Phase #2: functional-test
                                 
    Open your browser and navigate to: https://web.daytrader.geronimo.apache.org
    
    1.  Under The Configuration Tab

        a.  Press (Re)-create DayTrader Database Tables and Indexes

        b.  Press (Re)-populate DayTrader Database

        c.  Press Test Daytrader Scenario

    2.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

        Note: each time you refresh the browser, it runs another functional test scenario. 

    That's it! You have now tested the daytrader
        
        
#### Phase #3: post-functional-test

    (Optional) 
    
    $ kubectl delete all --all
  
                            
                            
### Swagger UI Documentation

    1.  (Option 1) Access Swagger UI through port forwarding
         
        a.  $ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
            Forwarding from 127.0.0.1:8888 -> 5443
            
        b.  Point you browser to https://localhost:8888/swagger-ui.html

    2.  (Option 2) Access Swagger UI through the API Server
    
        a.  $ kubectl proxy
        
            Starting to serve on 127.0.0.1:8001
                
            ## Locates and authenticates to the API Server. 
    
        b.  Point you browser to http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html        		
        
    3.  (Option 3) Access Swagger UI through an Ingress
        
        a.  Point you browser to https://web.daytrader.geronimo.apache.org/swagger-ui.html
         


