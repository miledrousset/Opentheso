[Docker install](https://github.com/miledrousset/Opentheso2/tree/master/docker)

[another docker install](https://github.com/viaacode/opentheso2-docker)

# Installation rapide sous linux (Debian ou Ubuntu):
Télécharger la dernière version du WAR exemple (https://github.com/miledrousset/Opentheso2/releases/tag/v23.09.01)

    Pour une mise à jour : 
    	il suffit de mettre le war dans le dossier tomcat (/var/lib/tomcat9/webapps) renommer le war en opentheso.war
    	attendre que le WAR se déploie
    	modifier les fichiers de conf (hikari.properties, hibernate.cfg.xml, preferences.properties)
    	relancer Postgresql et Tomcat
	
    Pour le virtualHost, ajouter ces lignes :
            ProxyPass /opentheso http://localhost:8080/opentheso timeout=3600
            ProxyPassReverse /opentheso http://localhost:8080/opentheso timeout=3600
            <Directory /opentheso>
                Order allow,deny
                Allow from all
            </Directory>
            <IfModule       mod_rewrite.c>
                    RewriteEngine   On
                    RewriteRule     ^/$     /opentheso/$1   [R]
            </IfModule>

    Pour une nouvelle installation :
    	- télécharger Opentheso (WAR)
    	- placer le war dans le dossier tomcat (/var/lib/tomcat9/webapps) renommer le war en opentheso.war 
    	- Postgresql :
    		. créer une BDD (opentheso)
    		. créer un utilisateur (opentheso + pass) avec des droits SuperAdmin (juste le temps du déploiement du script ci-dessous)
    		. appliquer sur la BDD opentheso le script qui est dans le war déployé (/var/lib/tomcat9/webapps/opentheso/WEB-INF/classes/install# opentheso_current.sql)
    		PS: le script de mise à jour s’applique automatiquement à partir de maintenant
    	- modifier les fichiers de conf (hikari.properties, hibernate.cfg.xml, preferences.properties) en fonction de vos paramètres de BDD
    	- relancer Postgresql et Tomcat
    
    Pour le virtualHost, c’est identique 


# Opentheso2
Nouvelle version du logiciel Opentheso avec un nouveau design

lien vers la doc : https://opentheso.hypotheses.org/

Qu'est-ce qu'Opentheso

Opentheso est un gestionnaire de thésaurus multilingue et multi-hiérarchique. Il est conforme aux normes ISO 25964-1 2011 et ISO 25964-2:2012 (Information et documentation. Thésaurus et intéropérabilité avec d’autres vocabulaires)

Il a été développé à l'origine pour la Fédération et ressources sur l'Antiquité (FRANTIQ, Groupement de services du CNRS) sous la direction de Miled Rousset, responsable de la plateforme Têtes de Réseaux Documentaires (TRD, Maison de l'Orient et de la Méditerranée) et directeur informatique de FRANTIQ.

Ses fonctionnalités actuelles :

    Gestion avancée des termes et des branches
    Drag and Drop
    Gestion collaborative avec 4 niveaux d'authentification (Superadmin, Admin, User et Traducteur)
    Interopérable : génération automatique d'identifiants Handle et ARK (ce service est assuré par le PSIR de la MOM)
    WebServices REST
    import : SKOS, Turtle, JsonLD
    export : SKOS, Turtle, JsonLD, CSV et PDF

Son développement a été en partie financé par le GDS FRANTIQ, la MOM et la TGIR Huma-Num via le Consortium (MASA) Mémoire des archéologues et des sites archéologiques.
Qui utilise Opentheso ?

Opentheso est utilisé par les 39 bibliothèques de la Fédération et ressources sur l’Antiquité (FRANTIQ), par la TGIR Huma-Num pour la gestion des vocabulaires de référence du moteur de recherche ISIDORE, par le GDRI ZooMathia pour l’établissement d’un thésaurus sur la faune antique, par la Documentation centrale des Hospices Civils de Lyon (Médecine, 60.000 termes).
Licence

Opentheso est distribué sous licence CeCILL_C, Licence libre de droit français compatible avec la licence GNU GPL.
Contacts

    Directeur informatique : miled.rousset[at]mom[dot]fr
    Donnez-nous votre avis : opentheso.contact@services.cnrs[dot]fr

What is Opentheso

Opentheso is a web-based thesaurus management tool dedicated to the management of vocabularies. It is developed at the CNRS (National Center for Scientific Research - France). It conforms to ISO 25964-1 2011 and ISO 25964-2:2012 standards (Information and documentation. Thesauri and interoperability with other vocabularies).
Who uses OpenTheso?

OpenTheso is used by a network of 39 research libraries (FRANTIQ. Archaeology, 32 000 concepts. Contact : Blandine Nouvel), research units (Zoomatia, archaelogical research project), les Hospices civils de Lyon (medicine, 60000 concepts), l’Institut des Sciences de l’Homme (part of Rameau vocabulary)…
License

OpenTheso is released under the terms of the CeCILL_C license, fully compatible with the GNU GPL.


# Quick installation under Linux (Debian or Ubuntu):
Download the latest version of the WAR example (https://github.com/miledrousset/Opentheso2/releases/tag/v23.09.01)

     For an update:
     just put the war in the tomcat folder (/var/lib/tomcat9/webapps) rename the war to opentheso.war
     wait for WAR to deploy
     modify conf files (hikari.properties, hibernate.cfg.xml, preferences.properties)
     restart Postgresql and Tomcat

     For the virtualHost, add these lines:
             ProxyPass /opentheso http://localhost:8080/opentheso timeout=3600
             ProxyPassReverse /opentheso http://localhost:8080/opentheso timeout=3600
             <Directory /opentheso>
                 Order allow,deny
                 Allow from all
             </Directory>
             <IfModule mod_rewrite.c>
                     RewriteEngine On
                     RewriteRule ^/$ /opentheso/$1 [R]
             </IfModule>

     For a new installation:
     - download Opentheso (WAR)
     - place the war in the tomcat folder (/var/lib/tomcat9/webapps) rename the war to opentheso.war
     - Postgresql:
     . create a database (opentheso)
     . create a user (opentheso + pass) with SuperAdmin rights (just long enough to deploy the script below)
     . apply the script that is in the deployed war to the opentheso database (/var/lib/tomcat9/webapps/opentheso/WEB-INF/classes/install# opentheso_current.sql)
     PS: the update script applies automatically from now on
     - modify the conf files (hikari.properties, hibernate.cfg.xml, preferences.properties) according to your database settings
     - restart Postgresql and Tomcat
    
     For the virtualHost, it is identical

### How to run Opentheso2:
--> Apach Tomcat 9
1. Launch Apache Tomcat 9
2. Move your war in 'webapps' directory

--> Apache Tomcat 10
To run our project in Apache Tomcat 10 server, you need to follow these steps:
1. Create a directory in the root of the server with name 'webapps-javaee'.
2. Launch Apache Tomcat 10
3. Move the war in this new directory
4. There is a thread that will retrieve the war and create a new one in 'webapps' directory suitable for version 10.

Any technical question?

Contact Opentheso R&D director : miled.rousset[at]mom[dot]fr

Please give us your feedback : opentheso.contact@services.cnrs[dot]fr
