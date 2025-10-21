FROM bitnamilegacy/java:17.0.12-10

WORKDIR /app
ADD ./opentheso.jar .
ADD ./application-prod.yaml .
ADD ./logback-spring.xml .

EXPOSE 8080
CMD java -jar -Dlogging.config=./logback-spring.xml opentheso.jar --spring.profiles.active=prod --spring.config.additional-location=./application-prod.yaml