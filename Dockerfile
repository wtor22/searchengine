FROM openjdk:17
WORKDIR /app
COPY target/SearchEngine-1.0-SNAPSHOT.jar  app.jar
COPY application-dev.yml /app/application-dev.yml
COPY application-prod.yml /app/application-prod.yml
COPY application.yml /app/application.yml
#EXPOSE 8085 # Раскоментить если  без docker-compose
ENTRYPOINT ["java", "-jar", "app.jar","--spring.profiles.active=prod"]