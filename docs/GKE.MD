                
## Setup on GKE

### Prerequisites

    1.  [Create GCP Account](https://cloud.google.com/free/)

### Install the Cluster
    
    1.  Sign-in to Google Cloud Platform Console (console.cloud.google.com)

    2.  Install and initialize the [Google Cloud SDK](//https://cloud.google.com/sdk/docs/quickstarts)

        Notes: When you initialize the Google Cloud SDK, be sure to `Create a new project` for daytrader
        
        `$ gcloud container clusters create daytrader-gke-cluster`
                
        NAME                   LOCATION       MASTER_VERSION  MASTER_IP     MACHINE_TYPE   NODE_VERSION  NUM_NODES  STATUS
        gke-daytrader-cluster  us-central1-a  1.9.7-gke.11    **.**.**.**   n1-standard-1  1.9.7-gke.11  3          RUNNING
            
    3.  Get authentication credentials for the cluster
        
        `$ gcloud container clusters get-credentials daytrader-gke-cluster`
                
        Notes: This command updates your kubeconfig so that the `kubectl` command can connect to your new cluster.
                
    4.  Verify that the cluster is running
    
        `$ kubectl cluster-info`
                    
        Kubernetes master is running at https://35.184.21.226
        GLBCDefaultBackend is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/default-http-backend:http/proxy
        Heapster is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/heapster/proxy
        KubeDNS is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/kube-dns:dns/proxy
        Metrics-server is running at https://35.184.21.226/api/v1/namespaces/kube-system/services/https:metrics-server:/proxy
                    
    5.  Verify that the worker nodes are running
    
        `$ kubectl get nodes`
                
        NAME                                                  STATUS   ROLES    AGE     VERSION
        gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11
        gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11
        gke-daytrader-gke-cluste-default-pool-********-****   Ready    <none>   7m11s   v1.9.7-gke.11


### Install the NGINX Ingress Controller
      
    1.  Create the cluster role binding
        
        Give your GCP user account permission to administer the cluster.
            
        `$ kubectl create clusterrolebinding cluster-admin-binding --clusterrole cluster-admin --user $(gcloud config get-value account)`
                        
    2.  Deploy the NGINX Ingress Controller
        
        `$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/mandatory.yaml`
                
        `$ kubectl -n ingress-nginx get pods`      ## Wait for the controller start. It may take a few minutes
                    
        NAMESPACE       NAME                                       READY   STATUS    RESTARTS   AGE
        ingress-nginx   nginx-ingress-controller-*********-*****   1/1     Running   0          39s
                    
    3.  Create the NGINX Ingress Service
        
        `$ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/provider/cloud-generic.yaml`
                    
        `$ kubectl -n ingress-nginx get services`    ## What for the service to start. It may take a few minutes.
                    
        NAME            TYPE           CLUSTER-IP     EXTERNAL-IP   PORT(S)                      AGE 
        ingress-nginx   LoadBalancer   10.7.252.186   xx.xx.xx.xx   80:31884/TCP,443:31058/TCP   55s
        
    4.  Review the controller's logs. This is also useful for troubleshooting ingress resources.
    
        `$ kubectl -n ingress-nginx logs nginx-ingress-controller-*********-*****`
    
        Starting NGINX process
        attempting to acquire leader lease  ingress-nginx/ingress-controller-leader-nginx...
        Configuration changes detected, backend reload required.
        Backend successfully reloaded.
        Initial synchronization of the NGINX configuration.
        successfully acquired lease ingress-nginx/ingress-controller-leader-nginx
        new leader elected: nginx-ingress-controller-5c6f8b4ddc-6cv4f
        Dynamic reconfiguration succeeded.
            
    See also [NGINX Ingress Controller Installation Guide](https://kubernetes.github.io/ingress-nginx/deploy/)

                    
### (N/A) Install the Kubernetes Dashboard
        
        The Kubernetes Dashboard is disabled and deprecated in GKE. instead, use the built-in [GKE Dashboard](https://cloud.google.com/kubernetes-engine/docs/concepts/dashboards) through the Console.
        

