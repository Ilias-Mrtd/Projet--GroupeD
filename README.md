# Projet MVP — Groupe D

Ce dépôt contient un prototype fonctionnel (MVP) d'un moteur de simulation d'agents dans un graphe avec visualisation interactive en Java.

## Objectif

Ce projet montre comment :
- construire dynamiquement un graphe de nœuds et d’arêtes,
- afficher une simulation graphique avec Swing,
- animer des agents se déplaçant sur le graphe,
- sélectionner des éléments du graphe (nœuds, arêtes, agents).

## Auteur·e·s

- ARNOUX Antoine
- BEN-HALIMA Adem
- PELLERIN Corentin
- RIVOHERISSON Tsiky
- MOURTADA Ilias

## Structure du projet

```
Projet--GroupeD-main/
  README.md
  src/
    AppProjet.Java
    SimulationEngine/
      GraphicApp.java
      SimulationEngine.java
      GraphRenderer/
        GraphRenderer.java
        SelectionSystem.java
        agents/
          Agent.java
        graph/
          Graph.java
          Node.java
          Edge.java
```

## Fonctionnalités du MVP

- Graphe manuel défini dans `src/AppProjet.Java`
- Affichage Swing du graphe et des agents
- Boucle de simulation temps réel avec `javax.swing.Timer`
- Sélection des nœuds, arêtes et agents avec la souris
- Ajout de nœuds, arêtes et agents possible via le code

## Lancer le projet

1. Ouvrir un terminal dans le dossier `Projet--GroupeD`.
2. Compiler les sources :

```bash
javac -d bin $(find src -name "*.java")
```

3. Lancer l’application :

```bash
java -cp bin AppProjet
```

## Améliorations possibles

- ajouter un menu contextuel pour créer/supprimer des nœuds et des arêtes,
- ajouter un système de pathfinding plus robuste,
- gérer plusieurs agents avec objectifs dynamiques,
- améliorer l’interface utilisateur et l’expérience de sélection.
