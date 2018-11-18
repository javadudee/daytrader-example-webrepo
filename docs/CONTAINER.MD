
## Containerize the application
 
### Create the Dockerfile
    
The Dockerfile defines all the commands to setup the environment and start the application in the container.

```yaml
FROM openjdk:8-jdk
VOLUME /tmp
MAINTAINER Donald Vines <donald_vines@hotmail.com>
# Set ssl variables
ENV DAYTRADER_KEYSTORE_FILENAME=/var/ssl/daytrader/keystore.jks
ENV DAYTRADER_KEYSTORE_PASSWORD=password
ENV DAYTRADER_TRUSTSTORE_LOCATION=/var/ssl/daytrader/truststore.jks
ENV DAYTRADER_TRUSTSTORE_PASSWORD=password
# Set app variables
ENV DAYTRADER_APP_VERSION=4.0.0
ENV DAYTRADER_APP_ARTIFACTID=daytrader-accountsapp
ENV DAYTRADER_WAR_ARTIFACTID=daytrader-accounts
# Set database variables
ENV DAYTRADER_DATABASE_DRIVER=org.apache.derby.jdbc.EmbeddedDriver
ENV DAYTRADER_DATABASE_URL='jdbc:derby:tradesdb;create=true'
ENV DAYTRADER_DATABASE_USERNAME=xxx
ENV DAYTRADER_DATABASE_PASSWORD=xxx
# Set tomcat variables
ENV SERVER_PORT=1443
ENV SERVER_PORT_HTTPS=1443
# Make port visible
EXPOSE 1443
# Set service routes
ENV DAYTRADER_ACCOUNTS_SERVICE=https://daytrader-accounts
ENV DAYTRADER_GATEWAY_SERVICE=https://daytrader-gateway
ENV DAYTRADER_PORTFOLIOS_SERVICE=https://daytrader-portfolios
ENV DAYTRADER_QUOTES_SERVICE=https://daytrader-quotes
# Set logging variables
ENV DAYTRADER_LOG_FILENAME=/var/log/daytrader/$DAYTRADER_APP_ARTIFACTID-$DAYTRADER_APP_VERSION.log
ENV DAYTRADER_LOG_LEVEL=TRACE
ENV DAYTRADER_LOG_APPENDER=ConsoleAppender
# Create the log folder
RUN mkdir -p -m 0777 /var/log/daytrader
# Create the log file and set permissions
RUN touch $DAYTRADER_LOG_FILENAME
RUN chmod 666 $DAYTRADER_LOG_FILENAME
# Create the ssl folder
RUN mkdir -p -m 0777 /var/ssl/daytrader
# Add the truststore to the container and set permissions
ARG JKS_FILE=target/$DAYTRADER_WAR_ARTIFACTID-$DAYTRADER_APP_VERSION/WEB-INF/classes/truststore.jks
ADD ${JKS_FILE} $DAYTRADER_TRUSTSTORE_LOCATION
RUN chmod 666 $DAYTRADER_TRUSTSTORE_LOCATION
# Add the application's war to the container
ARG WAR_FILE=target/$DAYTRADER_WAR_ARTIFACTID-$DAYTRADER_APP_VERSION.war
ADD ${WAR_FILE} app.war
ENV JAVA_OPTS="-Djavax.net.ssl.trustStore=/var/ssl/daytrader/truststore.jks -Djavax.net.ssl.trustStorePassword=password"
ENTRYPOINT exec java $JAVA_OPTS -jar app.war
```

### Add the Dockerfile Maven plugin

To build and push the Docker image, we elected to use the [Spotify Dockerfile Maven Plugin](https://github.com/spotify/dockerfile-maven). It is one of two major Docker plugins that you still
see active development. The other major Docker plugin is the [fabric8 Docker plugin](https://github.com/fabric8io/docker-maven-plugin). For a recent comparison, 
see [Using Docker from Maven and Maven from Docker](https://codefresh.io/howtos/using-docker-maven-maven-docker/)
    
1.  `$ cd daytrader-webapp/daytrader-web`
    
2.  In `pom.xml`, review the `dockerfile-maven-plugin`
        
    ```xml
    <plugin>
        <groupId>com.spotify</groupId>
        <artifactId>dockerfile-maven-plugin</artifactId>
        <version>${dockerfile-maven-version}</version>
        <executions>
            <execution>
                <phase>package</phase>
                <goals>
                    <goal>build</goal>    <!-- Builds the Docker image -->
                    <goal>push</goal>     <!-- Pushes the Docker image -->
                </goals>
            </execution>
        </executions>
        <configuration>
            <repository>${docker.image.prefix}/${project.artifactId}</repository>
            <tag>4.0.18</tag>
            <buildArgs>
                <JAR_FILE>target/${project.artifactId}-${project.version}.war</JAR_FILE>
            </buildArgs>
            <useMavenSettingsForAuth>true</useMavenSettingsForAuth>
        </configuration>
    </plugin>
    ```

### Build and Push the Docker image

1.  `$ cd daytrader-webapp`
    
2.  `$ mvn -Pcd clean install`

    This command builds the Docker image and pushes it to the specified DockerHub account
    

