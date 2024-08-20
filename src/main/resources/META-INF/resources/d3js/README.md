# Thésaurus Graph Visualizer with D3.js

> ### ⚠️ Attention
>
> Ce projet est conçu pour fonctionner avec Openthéso et la fonctionnalité d'affichage des thésaurus sous forme de graphe. Ce README n'a pas pour but d'expliquer comment obtenir les données et le format nécéssaire mais uniquement comment se servir du code pour afficher les concepts sous forme de graphe. Pour plus de détails, il faudra se rediriger vers la documentation officielle d'Openthéso une fois que celle-ci sera mise en ligne. (Mis à jour le 5 juin 2024)

## Installation

Cloner le repository:

```bash
git clone https://github.com/jboureux/express-json-api.git
```

Installer les dépendances:

```bash
npm install
```

Lancer le serveur:

```bash
npm run dev
```

## Mode d'emploi

La lecture des fichiers JSON se fait grâce à l'API fetch de Javascript. De ce fait, il faudra au préalable servir les données via une API REST (si aucune solution utilisez ce répo: https://github.com/jboureux/express-json-api)

Une fois les données prêtes et l'application lancée, écrire l'URL à fetch dans l'Input en haut à droite afin de récuperer les données. Une erreur sera rejetée si aucune URL n'est spécifiée ou si l'URL n'est pas valide.

## TODO

-   [ ] Gestion des erreurs sur le format des données
-   [ ] Gestion du resize de la fenêtre pour déclencher un re-render du SVG
-   [ ] Création de paramètres supplémentaires
