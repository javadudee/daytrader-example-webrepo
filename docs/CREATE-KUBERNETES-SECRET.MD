
            
## Create the Kubernetes Secret for our Certificates and Private Keys

We are going to use an Ingress for routing incoming requests to our Service but first we need to create a Secret which holds certificates and private keys for securing that connection with HTTPS.
 
### Create the Certificate and Private Key
          
1.  To encrypt the SSL/TLS connection between the browser and the Ingress resource, you need to attach a certificate and a private key 
    to the Ingress resource. To do that, the certificate and private key must be stored in a Kubernetes resource called a secret so it 
    can be referenced by the Ingress resource. 

    a.  `$ openssl genrsa -out tls.key 2048`
    
    b.  `$ openssl req -new -x509 -key tls.key -out tls.crt -days 360 -subj /CN=web.daytrader.geronimo.apache.org`
    
2.  Check and verify your certificate and key. This can be useful before applying them to your server. 

    a.  `$ openssl x509 -in tls.crt -text`
    
        This command check the certificate and its information (signing authority, expiration date, etc.)
    
    b.  `$ openssl rsa -in tls.key -check`
    
        This command checks the SSL key and verifies its consistency

3.  Verify the certificate and key match by comparing their checksums for equality

    a.  `$ openssl x509 -noout -modulus -in tls.crt | openssl md5`
    
    b.  `$ openssl rsa -noout -modulus -in tls.key | openssl md5`
            

### Generate the Secret Specification

The `secret.yaml` contains certificates and private keys that are used by our Ingress resource. 

Instead of creating the `secret.yaml` from scratch, which involved cutting and pasting certificates, run

`$ kubectl create secret tls daytrader-web --key tls.key --cert tls.crt --dry-run > secret.yaml`

It is simply less error prone to use `kubectl` to generate the `secret.yaml` than to create it from scratch.

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
    
