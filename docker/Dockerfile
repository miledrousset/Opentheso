FROM tomcat:9.0
MAINTAINER Evolved Binary

COPY opentheso2-23.09.01.war /usr/local/tomcat/webapps/opentheso2.war
RUN apt-get update && apt-get install -y unzip
RUN mkdir /usr/local/tomcat/webapps/opentheso2 && unzip -d /usr/local/tomcat/webapps/opentheso2/ /usr/local/tomcat/webapps/opentheso2.war && mkdir -p /var/www/images/resize


# Modify the config for Opentheso
COPY preferences.properties /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
COPY hikari.properties /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
COPY hibernate.cfg.xml /usr/local/tomcat/webapps/opentheso2/WEB-INF/classes/
