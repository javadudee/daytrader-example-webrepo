
    Notes:
    
        1.  These instructions are for minikube ony.
        
        2.  Will add instructions for eks, aks, and gks

    Prerequisites
    
        1.  $ helm init
    
        2.  $ minikube start --cpus 2 --memory 8192
        
        3.  $ minikube addons enable heapster

    Instructions
    
        1.  Start the local repo
        
            $ helm serve
            
        2.  Add charts to the repo
        
            $ helm repo add chartmuseum http://127.0.0.1:8879/charts
                
        3.  Install the charts
        
            a.  $ kubectl config use-context CLUSTER_NAME
        
            b.  $ helm upgrade --values chartmuseum/CHART_NAME/values.yaml --install --force CHART_NAME chartmuseum/CHART_NAME
            
            Where CHART_NAME is daytrader-accounts, daytrader-gateway, daytrader-portfolios, daytrader-quotes, or daytrader-web

    Troubleshooting

        1.  Verify that your chart follows best practices
        
            $ helm lint CHART_NAME
            
        2.  Render your chart to see the manifest
        
            $ helm install CHART_NAME --dry-run --debug 
            
        3.  List the installed charts
        
            $ helm list
            
        4.  Delete failed deployments, if any
            
            $ helm delete --purge CHART_NAME
            
        5.  Get the manifest from an installed chart
        
            $ helm get manifest CHART_NAME
