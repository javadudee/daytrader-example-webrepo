## Run on GKE



### Prerequisites

    1.  Install Java Development Kit (64 bit) 1.8+

    2.  Install Apache Maven 3.3.9+

    3.  Install Docker (https://www.docker.com/get-docker) to build Docker images.
    
    4.  Create DockerHub Account (https://hub.docker.com/) to store Docker images.
        
    5.  Configure Maven so it can push Docker images to your DockerHub account

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
                        
    6.  Create GCP Account (https://cloud.google.com/free/)
    
### Installation
    
    1. Install and Verify the GKE Cluster
    
        a.  Install the Cluster
    
            --  Sign-in to Google Cloud Platform Console (console.cloud.google.com)

            --  Install and initialize the [Google Cloud SDK](//https://cloud.google.com/sdk/docs/quickstarts)

                Notes: When you initialize the Google Cloud SDK, be sure to `Create a new project` for daytrader
        
                $ gcloud container clusters create daytrader-gke-cluster
                
                NAME                   LOCATION       MASTER_VERSION  MASTER_IP     MACHINE_TYPE   NODE_VERSION  NUM_NODES  STATUS
                gke-daytrader-cluster  us-central1-a  1.9.7-gke.11    **.**.**.**   n1-standard-1  1.9.7-gke.11  3          RUNNING
            
            --  Get authentication credentials for the cluster
        
                $ gcloud container clusters get-credentials daytrader-gke-cluster
                
                Notes: This command updates your kubeconfig so that the `kubectl` command can connect to your new cluster.
    
        b.  Verify the Cluster
                
            --  $ kubectl cluster-info
                    
                Kubernetes master is running at https://35.184.21.226
                GLBCDefaultBackend is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/default-http-backend:http/proxy
                Heapster is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/heapster/proxy
                KubeDNS is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
                Metrics-server is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy
                    
            --  $ kubectl get nodes
                
                NAME                                                  STATUS   ROLES    AGE     VERSION
                gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11
                gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11
                gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11


    2.  Install the NGINX Ingress Controller
      
        a.  Create the cluster role binding
        
            Give your GCP user account permission to administer the cluster.
            
            $ kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user $(gcloud config get-value account) 
                        
        b.  Deploy the NGINX Ingress Controller
        
            $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/mandatory.yaml
                
            $ kubectl -n ingress-nginx get pods      ## Wait for the controller start. It may take a few minutes
                    
            NAMESPACE       NAME                                       READY   STATUS    RESTARTS   AGE
            ingress-nginx   nginx-ingress-controller-*********-*****   1/1     Running   0          39s
                    
        c.  Create the NGINX Ingress Service
        
            $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/provider/cloud-generic.yaml
                    
            $ kubectl -n ingress-nginx get services    ## What for the service to start. It may take a few minutes.
                    
            NAME            TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)                      AGE 
            ingress-nginx   LoadBalancer   10.7.252.186   xx.xx.xx.xx   80:31884/TCP,443:31058/TCP   55s
        
        d.  Review the logs to verify the installation. This is also useful for troubleshooting ingress resources.
    
            $ kubectl -n ingress-nginx logs nginx-ingress-controller-*********-*****
    
            Starting NGINX process
            attempting to acquire leader lease  ingress-nginx/ingress-controller-leader-nginx...
            Configuration changes detected, backend reload required.
            Backend successfully reloaded.
            Initial synchronization of the NGINX configuration.
            successfully acquired lease ingress-nginx/ingress-controller-leader-nginx
            new leader elected: nginx-ingress-controller-5c6f8b4ddc-6cv4f
            Dynamic reconfiguration succeeded.
            
        e.  See also https://kubernetes.github.io/ingress-nginx/deploy/
                    
    3.  (N/A) Install the Kubernetes Dashboard for managing and troubleshooting your cluster
        
        The Kubernetes Dashboard is disabled and deprecated in GKE. instead, use the built-in [GKE Dashboard](https://cloud.google.com/kubernetes-engine/docs/concepts/dashboards) through the Console.
        

### Build Automation

    1.  $ cd daytrader-webapp
    
    2.  $ mvn -Pcd clean install   ## builds docker image and pushes to docker hub



### Continuous Delivery

    Change to this directory and review the yaml files
    
    1.  $ cd daytrader-web/env/external/k8s
    
    The following steps shows you how to use them to deploy the application.
    
    Note: the required depoyment steps can be automated via Maven at a later date
            
#### Create Deployment

    1.  $ kubectl apply -f deployment.yaml    ## This creates the PODs that run the application and a ReplicaSet to manage the lifecycle of pods 
    
    2.  (Optional) $ kubectl get pods
    
        NAME                                READY   STATUS    RESTARTS   AGE   
        daytrader-web-*********-*****       1/1     Running   0          6m    
    
        ## Wait until the STATUS is Running and the pods are READY (1/1)
              
    3.  (Optional) Retrieve the pods (application) logs
        
        a.  $ kubectl logs <POD_NAME> -f     ## the -f option tails the logs
        
            Tomcat started on port(s): 5443 (https).

    4.  (Optional) Access the pod through the port forwarder
        
        a.  $ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
            Forwarding from 127.0.0.1:8888 -> 5443
       
        b.  $ curl -k https://127.0.0.1:8888/health

            {"status":"UP"} 
            
        
#### Create Service 

    Note: Service load balances requests across the Pods. It is important to remember that pods are ephemeral. If a pod stops, then 
    Replica Sets will create a replacement, but it will have a different IP. For that reason, clients should not communicate to the
    the applicaiton via the Pod. Instead they should talk to applications via the service. To create the service,
        
    1.  $ kubectl apply -f service.yaml
           
    2.  (Optional) $ kubectl get services
        
        NAME            TYPE        CLUSTER-IP     EXTERNAL-IP   PORT(S)         AGE
        daytrader-web   NodePort    10.7.254.225   <none>        443:30650/TCP   23s
       
        ## Notes
        
        a.  The above command creates a service resource. This resource listens on the CLUSTER IP and PORT. It is backed by endpoints 
            to one or more pods. You can see the dnpoints buy doing kubectl get endpoints. When the service receives an HTTPS request,
            it selects one of the endpoints (pods) and forwards the HTTPS request to the pod. The application running inside the pod 
            handles the request and returns an HTTPS response.
            
        b.  See also https://kubernetes.io/docs/concepts/services-networking/service/   

    3.  (Optional) Test the connection to the service using the API Server. 
                        
        a.  $ kubectl proxy      ## Locates and authenticates to the API Server.
        
            Starting to serve on 127.0.0.1:8001

        b.  $ curl -k http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/health
    
            {"status":"UP"}
    
            ## The curl command sent the HTTP request (without any authentication headers) to the proxy that is running on your local machine.
            ## The proxy, then, sent an HTTPS request to the API server's identity by validating its certificate. Encryption, authentication,
            ## and server verification is handled by the proxy. This is the easiest way to connect to the API server. 
    
        c. See also
    
            --  https://kubernetes.io/docs/tasks/administer-cluster/access-cluster-api/ 
            
            
#### Create Secret
        
    1.  (Optional) Create certificates and private keys
        
        Notes: you can skip this step. We already did it for you. It is incuded so others can expose their own services through Ingress.
          
        a.  To encrypt the connection between the browser and the ingress controller, you need to attach a certificate and a private key to 
            the Ingress resource. To do that, the certificate and private key must be stored in a Kubernetes resource called a secret so it 
            can be referenced by the Ingress resource. 

            --  $ openssl genrsa -out tls.key 2048
                
            --  $ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org
                
            --  $ kubectl create secret tls daytrader-web --cert=tls.crt  --key=tls.key
           
            --  $ kubectl get secrets daytrader-web -o yaml --export > secret.yaml  
                
        b.  Check and verify your certificate and key. This can be useful before applying them to your server. 

            --  $ openssl x509 -in tls.crt -text  ## Check the certificate and its information (signing authority, expiration date, etc.)

            --  $ openssl rsa -in tls.key -check  ## Check the SSL key and verify the consistency
    
        c.  Verify the certificate and key match by comparing their checksums for equality

            --  $ openssl x509 -noout -modulus -in tls.crt | openssl md5
        
            --  $ openssl rsa -noout -modulus -in tls.key | openssl md5   

    2. Create the Secret for TLS/SSL

        a.  $ kubectl apply -f secret.yaml
                
        b.  (Optional) $ kubectl get secrets
                
            NAME            TYPE                                  DATA   AGE
            daytrader-web   kubernetes.io/tls                     2      8s
            

#### Create Ingress

    1.  Create the Ingress resource

        a.  $ kubectl apply -f ingress.yaml
                
        b.  (Optional) $ kubectl get ingress
    
            NAME            HOSTS                               ADDRESS       PORTS     AGE
            daytrader-web   web.daytrader.geronimo.apache.org   xx.xx.xx.xx   80, 443   23h
                    
            ## Wait for the Ingress service to run; then record the HOSTS and ADDRESS

            Notes:
                
            -- The HOSTS were specified in the ingress.yaml and match the CN in the tls.crt
                        
            -- The ADDRESS is the public IP Address to the GCP load balancer to the daytrader-gke-cluster
            
        c.  (Optional) If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-***********
        
            --  $ kubectl -n ingress-nginx get pods
                
                NAME                                       READY   STATUS    RESTARTS   AGE
                nginx-ingress-controller-*********-*****   1/1     Running   0          1h
                
            --  $ kubectl -n ingress-nginx logs nginx-ingress-controller-*********-*****
        
                ## If there are any errors in the logs then they should give you a good indication of what went wrong

#### Configure DNS

    To access you application via ingres, the incoming request must be coming from the custom domain name (HOSTS) that you specified in in the 
    ingress yaml and the CN that you specified in the secrets yaml. Otherwise you will get an invalid hosts exception during SSL/TLS handshake.
    This means that you must configure DNS to resolve the HOSTS (custom domain name) to the ingress' ADDRESS. There are a couple of ways to do
    that,
    
    1.  Use /etc/hosts for your custom domain name during development
                                                                          
        a.  Add an entry to your /etc/hosts in the following format
        
            xx.xx.xx.xx   web.daytrader.geronimo.apache.org                    

    2.  Use CNAME record for your custom domain name during production
        
        a.  (TODO) Create a CNAME record to your DNS custom domain

    3.  (Optional) Test the DNS resolution of the custom domain name
    
        a.  Using the curl command
                    
            --  $ curl -k -v https://web.daytrader.geronimo.apache.org/health

                {"status":"UP"} 

        b.  Using the browser
           
             --  Point you browser to https://web.daytrader.geronimo.apache.org/health
    
                {"status":"UP"} 

    5.  (Optional) If the connection timed out then
        
        a.  Make sure that you have configured the DNS resolution correctly. 
                
        
### Functional Test

    The functional test is done in three stesp 1) pre-functional-test, 2) functional-test, and 3) post-functional-test


#### pre-functional-test
    
    Deploy the microservies. See the README.MD files in the following repos
        
    1.  [daytrader-example-accountserepo](https://github.com/jpmorganchase/daytrader-example-accountsrepo/)

    2.  [daytrader-example-gatewayrepo](https://github.com/jpmorganchase/daytrader-example-gatewayrepo/)

    3.  [daytrader-example-portfoliosrepo](https://github.com/jpmorganchase/daytrader-example-portfoliosrepo/)
        
    4.  [daytrader-example-quotesrepo](https://github.com/jpmorganchase/daytrader-example-quotesrepo/)
        
    5.  [daytrader-example-webrepo](https://github.com/jpmorganchase/daytrader-example-webrepo/)
               
                                
#### functional-test
                                 
    Open your browser and navigate to: https://web.daytrader.geronimo.apache.org
    
    1.  Under The Configuration Tab
    
        a.  Press(Re)-create DayTrader Database Tables and Indexes
        
        b.  Press (Re)-populate DayTrader Database
    
        c.  Press Test Daytrader Scenario
    
    2.  Keep refreshing the browser until you are satisfied DayTrader is working correctly. 

        Note: each time you refresh the browser, it runs another functional test scenario. 

    That's it! You have now tested the daytrader
        
        
#### post-functional-test

    (Optional) $ kubectl delete all --all
    
    
                            
### Swagger UI Documentation

    1.  (Option 1) Access Swagger UI through port forwarding
         
        a.  $ kubectl port-forward <POD_NAME> 8888:5443  ## This command forwards local port 8888 to port 5443 of your pod
       
            Forwarding from 127.0.0.1:8888 -> 5443
            
        b.  Point you browser to https://localhost:8888/swagger-ui.html

    2.  (Option 2) Access Swagger UI through the API Server
    
        a.  $ kubectl proxy    ## Locates and authenticates to the API Server.
        
            Starting to serve on 127.0.0.1:8001 
    
        b.  Point you browser to http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/swagger-ui.html        
        
    3.  (Option 3) Access Swagger UI through an Ingress
        
        a.  Point you browser to https://web.daytrader.geronimo.apache.org/swagger-ui.html
                
