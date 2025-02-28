FROM bitnami/java:17.0.12-10

WORKDIR /app
ADD ./opentheso.jar .
ADD ./application-prod.yaml .

EXPOSE 8080
CMD java -jar opentheso.jar --spring.profiles.active=prod --spring.config.additional-location=./application-prod.yaml
