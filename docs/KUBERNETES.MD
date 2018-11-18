
## Put the container on Kubernetes

### Ceate the Deployment YAML

The `deployment.yaml` is where you specify the containers that will host your application along with other specs
    
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

1. `$ kubectl apply -f deployment.yaml` 

    This command creates a Deployment resource, which creates the ReplicaSet, which then creates the Pods
    
2.  (Optional) `$ kubectl get pods`
    
    NAME | READY | STATUS | RESTARTS | AGE
    ---- | ----- | ------ | -------- | ---
    daytrader-web-xxxxxxxxx-xxxxx | 1/1 | Running | 0 | 6m
    
    Wait until you see READY `1/1` and STATUS `Running`
              
3.  (Optional) Review the pods (application) logs
        
    `$ kubectl logs <POD_NAME> -f`
    
    You should see `Tomcat started on port(s): 5443 (https)`

4.  (Optional) Test the connection using the port forwarder
        
    `$ kubectl port-forward <POD_NAME> 8888:5443`
    
    This command forwards local port 8888 to port 5443 of your pod
       
    You shouhld see `Forwarding from 127.0.0.1:8888 -> 5443`
       
    `$ curl -k https://127.0.0.1:8888/health`

    You should see `{"status":"UP"}` 
            
        
## Create the Service

The Service load balances requests across the Pods. Remember that the pods are ephemeral. So if one of the stops, the ReplicaSet will 
create a replacement, but it will have a different IP. For that reason, clients should not communicate directly with the application 
in a Pod. Instead clients should talk to applications via the service. 
    
### Create the Service YAML

The `service.yaml` is where you specify the selector for the backend pods along with other specs

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
        
    NAME | TYPE | CLUSTER-IP | EXTERNAL-IP | PORT(S) | AGE
    ---- | ---- | ---------- | ----------- | ------- | ---
    daytrader-web | NodePort | 10.7.254.225| <none> | 443:30650/TCP | 23s
       
    Notes

    a.  The above command creates a service resource. This resource listens on the CLUSTER IP and PORT. It is backed by endpoints 
        to one or more pods. You can see the dnpoints buy doing kubectl get endpoints. When the service receives an HTTPS request,
        it selects one of the endpoints (pods) and forwards the HTTPS request to the pod. The application running inside the pod 
        handles the request and returns an HTTPS response.
            
    b.  See also [Kubernetes Concepts - Services, Load Balancing, and Networking](https://kubernetes.io/docs/concepts/services-networking/service)

3.  (Optional) Test the connection using the Kubernetes API.
                        
    a.  Start the proxy to locates and authenticate to the API Server.
    
        `$ kubectl proxy`      
        
        Starting to serve on 127.0.0.1:8001
            
    b.  If you get a port binding error then find the processId for that server and kill it 

        `$ lsof -P | grep ':8001'`
        `$ kill -9 processId`

    c.  `$ curl -k http://localhost:8001/api/v1/namespaces/default/services/https:daytrader-web:/proxy/health`
    
        {"status":"UP"}
    
        The above curl command sends the HTTP request (without any authentication headers) to the proxy that is running on your local machine.
        The proxy, then, sends an HTTPS request with appropriate authentication headers to the API server. This is the easiest way to connect 
        to the API server. 
    
    d.  See also [Access Clusters Using the Kubernetes API](https://kubernetes.io/docs/tasks/administer-cluster/access-cluster-api/) 
            
            
## Create the Secret
        
### Create Certificates and Private Keys
          
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
            

### Create the Secret YAML

The `secret.yaml` contains certificates and private keys that are used by other resources; such as Ingress. 

// ADD THE COMMANDS TO CREATE THE SECRET //

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
            
2.  `$ kubectl get secrets`
            
    NAME | TYPE | DATA | AGE
    ---- | ---- | ---- | ---
    daytrader-web | kubernetes.io/tls | 2 | 8s
            

## Create the Ingress

### Create the Inress YAML

The `ingress.yaml` defines the custom domain name for accesing the application through the Ingress and the matching secret for incoming TLS/SSL requests

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

    NAME | HOSTS | ADDRESS | PORTS | AGE
    ---- | ----- | ------- | ----- | ---
    daytrader-web | web.daytrader.geronimo.apache.org | xx.xx.xx.xx | 80, 443 | 23h
                
    Wait for the Ingress service to run; then record the HOSTS and ADDRESS

    a.  The HOSTS was specified in the ingress.yaml and match the Secrets (tls.crt CN)
                    
    b.  The ADDRESS is the DNS Name or IP Address to the Ingress resource's load balancer
        
4.  If the Ingress resource failed to start then check the logs on the nginx-ingress-controller-xxxxxxxxx-xxxxx
    
    a.  `$ kubectl -n ingress-nginx get pods`
            
        NAME | READY | STATUS | RESTARTS | AGE
        ---- | ----- | ------ | -------- | ---
        nginx-ingress-controller-xxxxxxxxx-xxxxx | 1/1 | Running - 0 | 1h
        
    b.  `$ kubectl -n ingress-nginx logs nginx-ingress-controller-xxxxxxxxx-xxxxx`
    
        If there are errors in the logs then they should give you a good indication of what went wrong
        
## Setup the DNS

To access you application via the Kubernetes ingress resource, the incoming request must be coming from the custom domain name (HOSTS) that you specified in the ingress.yaml (whcih is also the the CN that you specified for the crt in the secrets.yaml). Otherwise you will get an invalid hosts exception during SSL/TLS handshake. This means that you must setup DNS to resolve the HOSTS (custom domain name) to the ingress' ADDRESS. 
    
    
### Use /etc/hosts

This is a good solution for local development servers

1.  Add an entry to your /etc/hosts in the following format
    
    <ADDRESS>   <web.daytrader.geronimo.apache.org>
        
2.  Notes: <ADDRESS> must be an External IP Address. 
        
    a.  If the ADDRESS is a DNS name, then `ping` <ADDRESS>` to get the External IP Address.
         
    b.  If the ADDRESS is an Internal IP Address, then `kubectl cluster-info` to get the External IP Address

### Use CNAME record

This is a good solution for DEV, TST, and PRD environments
    
1.  (TODO) Create a CNAME record to your DNS custom domain

### Test DNS resolution
    
1.  Using the curl command
                
    `$ curl -k https://web.daytrader.geronimo.apache.org/health`

    {"status":"UP"} 

2.  Using the browser
       
    Point you browser to `https://web.daytrader.geronimo.apache.org/health`

    {"status":"UP"} 

3.  If the connection timed out then double check your DNS configuration
    



