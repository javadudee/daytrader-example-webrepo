
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
        
            a.  $ kubectl config use-context minikube
        
            b.  $ helm upgrade --install CHART_NAME chartmuseum/CHART_NAME -f chartmuseum/CHART_NAME/values.yaml
            
            Where CHART_NAME is daytrader-accounts, daytrader-gateway, daytrader-portfolios, daytrader-quotes, or daytrader-web

    Troubleshooting

        1.  Verify that your chart follows best practices
        
            $ helm lint CHART_NAME
            
        2.  Render your chart to see the manifest
        
            $ helm install CHART_NAME --dry-run --debug 
    
        3.  Get the manifest from an installed chart
        
            $ helm get manifest CHART_NAME
            
        4.  List releases of charts
        
            $ helm list
            

