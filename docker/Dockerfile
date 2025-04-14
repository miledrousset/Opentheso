# Étape 1 : base image Java
FROM openjdk:17-jdk-slim

# Dossier de travail dans le container
WORKDIR /app

# Copie des fichiers nécessaires qui sont à télécharger de Github
COPY opentheso.jar .
COPY logback-spring.xml .
COPY application-prod.yaml .

# Port exposé (à adapter selon l'application)
EXPOSE 8080

# Commande de démarrage
CMD ["java", "-jar", "-Dlogging.config=./logback-spring.xml", "opentheso.jar", "--spring.profiles.active=prod", "--spring.config.additional-location=application-prod.yaml"]
