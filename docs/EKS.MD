                
## Setup on EKS

### Prerequisites

1.  Create AWS Account (https://aws.amazon.com/premiumsupport/knowledge-center/create-and-activate-aws-account/)

### Install the Cluster

1.  Follow the instructions in the [EKS getting started guide](https://docs.aws.amazon.com/eks/latest/userguide/getting-started.html)
        
    Start from "Amazon EKS Prerequisites" up to "Step 4: Launch a Guest Book Application" exclusive

2.  Verify the EKS Cluster
     
    a.  $ kubectl config use-context user116-eks-cluster  ## Use the name of your Amazon EKS cluster
                    
        Switched to context "user116-eks-cluster".
                
    b.  $ kubectl kubectl cluster-info
                    
        Kubernetes master is running at https://8D36DB7CBD2E3394FF0843CEA0C0A266.sk1.us-east-1.eks.amazonaws.com
                    
    c.  $ kubectl get nodes
                
        NAME                            STATUS   ROLES    AGE   VERSION
        ip-172-31-19-232.ec2.internal   Ready    <none>   3h    v1.10.3
        ip-172-31-2-133.ec2.internal    Ready    <none>   3h    v1.10.3
        ip-172-31-30-11.ec2.internal    Ready    <none>   3h    v1.10.3

### Install the Ingres Controller

1.  Deploy the NGINX Ingress Controller
                
    a.  $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/mandatory.yaml
    
    b.  $ kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/provider/cloud-generic.yam
                    
2.  Make sure the controller is running and in the ready state. Note that it may take a few minutes to start
                
    $ kubectl -n ingress-nginx get pods 
                    
    NAMESPACE       NAME                                       READY   STATUS    RESTARTS   AGE
    ingress-nginx   nginx-ingress-controller-f88c75bc6-nc4bt   1/1     Running   0          39s
                    
3.  Review the logs to verify the installation. This is also useful for troubleshooting ingress resources.
                
    $ kubectl -n ingress-nginx logs <POD_NAME>
                   
    You should see "successfully acquired lease ingress-nginx/ingress-controller-leader-nginx". 
                    
### Install the Kubernetes Dashboard

The Kubernetes is a great resource for managing and troubleshooting the cluster. To install it,
                
1.  Start the proxy to authenticate with the API Server

    $ kubectl proxy
    
2.  Get the token to authenticate with the dashboard

    a.  $ kubectl -n kube-system get secrets | grep admin
       
        Record the NAME of the admin-user-token
        
    b.  $ kubectl -n kube-system describe secrets <ADMIN_USER_TOKEN>
        
        Copy the value of the authentication token
                            
3.  Open the following link to access the dashboard through the API Server
        
    http://localhost:8001/api/v1/namespaces/kube-system/services/https:kubernetes-dashboard:/proxy/
            
4.  You will see a login screen; choose Token and then enter the <AUTHENTICATION_TOKEN> from step 2. 

## Resources 

1.  See also https://kubernetes.github.io/ingress-nginx/deploy/

2.  https://github.com/AcalephStorage/kubernetes-dashboard/blob/master/docs/user-guide/troubleshooting.md
    
3.  https://docs.aws.amazon.com/eks/latest/userguide/dashboard-tutorial.html


