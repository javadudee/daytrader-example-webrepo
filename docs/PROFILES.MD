  	<profiles>
  
  		<profile>
  			<id>local</id>
  			<activation>
  				<activeByDefault>true</activeByDefault>
			</activation>
  			<properties>
  				<daytraderKeystoreFolder>${basedir}/src/main/resources/external/ssl</daytraderKeystoreFolder>
  			</properties>
  			<build>
				<plugins>
					<!--  Repackage the WAR so it can be executed from the command line using java -jar -->
					<plugin>
						<groupId>org.springframework.boot</groupId>
						<artifactId>spring-boot-maven-plugin</artifactId>				
						<configuration>
							<fork>true</fork>
							<mainClass>org.apache.geronimo.daytrader.javaee6.web.Application</mainClass>
						</configuration>
					</plugin>
				</plugins>
			</build>
		</profile>
		
		<profile>
  			<id>ci</id>
  			<properties>
  				<daytraderKeystoreFolder>${basedir}/src/main/resources/external/ssl</daytraderKeystoreFolder>
  			</properties>
  			
			<build>
				<plugins>
					<!--  Repackage the WAR so it can be executed from the command line using java -jar -->
					<plugin>
						<groupId>org.springframework.boot</groupId>
						<artifactId>spring-boot-maven-plugin</artifactId>				
						<configuration>
							<fork>true</fork>
							<mainClass>org.apache.geronimo.daytrader.javaee6.web.Application</mainClass>
						</configuration>
					</plugin>
					
					<!-- Integration Testing Plugin -->
					<plugin>
						<groupId>org.apache.maven.plugins</groupId>
						<artifactId>maven-failsafe-plugin</artifactId>
						<executions>
							<execution>
								<id>integration-tests</id>
								<goals>
									<goal>integration-test</goal>
									<goal>verify</goal>
								</goals>
								<configuration>
									<includes>
										<include>**/*IntegrationTests.java</include>
									</includes>
								</configuration>
							</execution>
						</executions>
					</plugin>
				</plugins>
			</build>		
		</profile>
		
  		<profile>
  			<id>cd</id>
  			<properties>
  				<daytraderKeystoreFolder>${basedir}/src/main/resources/external/ssl</daytraderKeystoreFolder>
				<docker.image.prefix>${user.name}</docker.image.prefix>
				<dockerfile-maven-version>1.4.0</dockerfile-maven-version>	
  			</properties>
  			<build>
				<plugins>
					<!--  Repackage the WAR so it can be executed from the command line using java -jar -->
					<plugin>
						<groupId>org.springframework.boot</groupId>
						<artifactId>spring-boot-maven-plugin</artifactId>				
						<configuration>
							<fork>true</fork>
							<mainClass>org.apache.geronimo.daytrader.javaee6.web.Application</mainClass>
						</configuration>
					</plugin>
					<!--  Build the docker image and push it to docker hub -->
			        <plugin>
		       			<groupId>com.spotify</groupId>
       					<artifactId>dockerfile-maven-plugin</artifactId>
       					<version>${dockerfile-maven-version}</version>
       					<executions>
         					<execution>
           						<phase>package</phase>
			           			<goals>
            						<goal>build</goal>
             						<goal>push</goal>
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
				</plugins>
			</build>
		</profile>

	</profiles>
