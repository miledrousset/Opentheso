Docker install : https://github.com/viaacode/opentheso2-docker
https://github.com/miledrousset/Opentheso2/tree/master/docker

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
    Donnez-nous votre avis : opentheso[at]mom[dot]fr

What is Opentheso

Opentheso is a web-based thesaurus management tool dedicated to the management of vocabularies. It is developed at the CNRS (National Center for Scientific Research - France). It conforms to ISO 25964-1 2011 and ISO 25964-2:2012 standards (Information and documentation. Thesauri and interoperability with other vocabularies).
Who uses OpenTheso?

OpenTheso is used by a network of 39 research libraries (FRANTIQ. Archaeology, 32 000 concepts. Contact : Blandine Nouvel), research units (Zoomatia, archaelogical research project), les Hospices civils de Lyon (medicine, 60000 concepts), l’Institut des Sciences de l’Homme (part of Rameau vocabulary)…
License

OpenTheso is released under the terms of the CeCILL_C license, fully compatible with the GNU GPL.

### How to run Opentheso2:
--> Apach Tomcat < 10
1. Launch Apache Tomcat 10
2. Move your war in 'webapps' directory

--> Apache Tomcat 10
To run our project in Apache Tomcat 10 server, you need to follow these steps:
1. Create a directory in the root of the server with name 'webapps-javaee'.
2. Launch Apache Tomcat 10
3. Move the war in this new directory
4. There is a thread that will retrieve the war and create a new one in 'webapps' directory suitable for version 10.

Any technical question?

Contact Opentheso R&D director : miled.rousset[at]mom[dot]fr

Please give us your feedback : opentheso[at]mom[dot]fr
