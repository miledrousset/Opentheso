FROM tomcat:9.0
MAINTAINER Evolved Binary

COPY opentheso2-23.09.01.war /usr/local/tomcat/webapps/opentheso2.war
RUN apt-get update
RUN apt-get install -y unzip
RUN mkdir -p /usr/local/tomcat/webapps/opentheso2
# RUN ls
RUN unzip -v -d /usr/local/tomcat/webapps/opentheso2/ /usr/local/tomcat/webapps/opentheso2.war
RUN mkdir -p /var/www/images/resize


# Modify the config for Opentheso
COPY preferences.properties /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
COPY hikari.properties /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
COPY hibernate.cfg.xml /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
