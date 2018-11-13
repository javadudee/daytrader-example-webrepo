

# DayTrader Web

	The Web UI to the DayTrader business functions



## Current State Architecture

DayTrader is multi-tier application built around the paradigm of an online stock trading system. Example business functions include
login, register, view portfolio, lookup stock quotes, and buy or sell stock. DayTrader was originally developed by IBM and donated 
to the Apache Geronimo community in the 2005 timeframe. The DayTrader architecture is representitive of monolithic applications that 
many businesses are still using. For that reason, we selected it to clarify the techniques for refactoring a monolith to microservices. 
For the original monolithic architecture, see http://geronimo.apache.org/GMOxDOC30/daytrader-a-more-complex-application.html. 



## Target State Architecture

The Webmicroservice is a Spring Boot Application responsible for the UI to the DayTrader business functions. Although 
it still contains the business functions, their implementations have been changed to delegate their invocation to the 
Gateway microservice which directs them to the appropriate microservice (Accounts, Portfolios, and Quotes). 

![Target-State-Architecture](images/Target-State-Architecture.JPG)



### Static Viewpoint

![Static-Viewpoint](images/Static-Viewpoint.JPG)



### Dynamic Viewpoint

![Dynamic-Viewpoint](images/Dynamic-Viewpoint.JPG)



### Participant and Responsibilities

![Participant-Responsibilities](images/Participant-Responsibilities.JPG)



## Run on Spring Boot

[Instructions to Run on Spring Boot](docs/SPRING-BOOT.MD)



## Run on Minikube

[Instructions to Run on Minikube](docs/MINIKUBE.MD)



## Run on EKS

[Instructions to Run on Amazon EKS](docs/EKS.MD)


