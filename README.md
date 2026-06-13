## Simulation Logistique d'Entrepôt

Ce projet est une application JavaFX de simulation de flux de robots autonomes au sein d’un réseau logistique. Conçue selon une architecture multicouche stricte (MVC), elle permet d'éditer des réseaux de transport, d'analyser les comportements d'agents mobiles autonomes et d'optimiser les flux de circulation pour répondre aux problématiques industrielles définies lors de notre phase de Design.

## Auteurs

- ARNOUX Antoine
- BEN-HALIMA Adem
- PELLERIN Corentin
- RIVOHERISSON Tsiky
- MOURTADA Ilias

## Structure du projet

```
Projet--GroupeD-main/
  README.md
  application/Main.Java
  services/GraphStorageManager.java
  simulationEngine/engine/SimulationEngine.java
                   algorithm/Dijkstra.java
                             IPathFinder.java
                             ...
  UI/GraphRenderer.java
     GraphCanvas.java
     PropertiesPanel.java
     renderers/...
     panel/...
  controllers/SelectionSystem.java
              helpers/...
  model/agents/Agent.java
               ...
        graph/Graph.java
              Node.java
              ...
```

## Fonctionnalités

1. Édition et Modélisation du Réseau (Graphe)
Création dynamique : Ajout et suppression de nœuds (Node) et d'arêtes (Edge) directionnelles ou bidirectionnelles directement depuis l'interface graphique.

Gestion des capacités : Configuration de la capacité d'accueil maximale de chaque nœud (ex: stations de tri, zones d'emballage) et de chaque arête (allées de circulation).

Destruction sécurisée : Algorithme de nettoyage automatique des liaisons lors de la suppression d'un nœud, incluant un protocole de relocalisation sécurisée (téléportation) des agents impactés pour éviter l'arrêt du moteur.

2. Moteur Temporel et Dynamique des Flux
Cadencement discret : Moteur d'exécution asynchrone (SimulationEngine) rythmé par des unités de temps discrètes (ticks).

Système de file d'attente (Queue system) : Gestion active de la congestion. Lorsqu'une allée ou un nœud atteint sa capacité maximale, les robots s'engagent dans une file d'attente ordonnée.

Cartographie thermique de l'affluence : Rendu visuel dynamique (GraphRenderer) utilisant un gradient de couleurs en temps réel (du vert au rouge) pour identifier instantanément les goulots d'étranglement du réseau.

3. Intelligence Embarquée et Profils d'Agents
Pathfinding Adaptatif Interopérable : Intégration d'une interface PathFinder permettant de basculer dynamiquement entre plusieurs algorithmes :

Dijkstra Standard : Optimisation basée uniquement sur la distance géométrique brute.

Dijkstra Dynamique : Prise en compte en temps réel du niveau d'encombrement des arêtes dans le calcul de la route.

Profils comportementaux :

PATIENT : Respecte scrupuleusement l'ordre d'arrivée dans les files d'attente.

PRESSE (VIP) : Force le passage et modifie l'ordre des priorités pour minimiser ses temps morts.

DEFAILLANT : Simule de manière stochastique une panne mécanique (vitesse nulle), permettant d'analyser la propagation d'une onde de congestion.

## Lancer le projet

- Java SDK 17 (ou supérieur) et de Maven
bash:
- git clone https://github.com/Ilias-Mrtd/Projet--GroupeD.git
- cd Projet--GroupeD
- mvn clean javafx:run

## Manipulation de l'Interface
# A. Initialisation du Réseau
Au premier démarrage, l'application génère automatiquement un graphe témoin représentant un entrepôt logistique.

Pour concevoir votre propre réseau, utilisez la barre d'outils pour ajouter des nœuds (clic gauche sur le canevas) et reliez-les en sélectionnant un nœud source puis un nœud cible.

# B. Contrôle de la Flotte
Injecter un robot : Utilisez le menu d'ajout d'agents, spécifiez son identifiant, sa vitesse nominale, ainsi que son profil comportemental (PATIENT, PRESSE ou DEFAILLANT).

Piloter le temps : Utilisez les boutons d'action du panneau de contrôle pour lancer la simulation, suspendre l'exécution (Pause), ou réinitialiser l'ensemble du système à son état d'origine.

# C. Analyse Comparative et Supervision
Changer de stratégie : Utilisez le menu déroulant algorithmique pour basculer entre le calcul à distance brute ou le calcul adaptatif à la congestion.

Inspecter les éléments : Cliquez directement sur un nœud ou sur un robot en mouvement. Ses statistiques détaillées s'afficheront instantanément dans le panneau latéral (Roster) et son itinéraire calculé par Dijkstra sera mis en surbrillance sur le canevas.

# D. Sauvegarde et Organisation
Pour sauvegarder manuellement votre topologie et l'état actuel des flux, cliquez sur Sauvegarder. Le gestionnaire ouvrira automatiquement le dossier dédié simulations/.

Pour restaurer une configuration préalablement établie, ouvrez le menu déroulant Charger une simulation. La liste dynamique détectera et affichera en temps réel l'ensemble des fichiers .sim disponibles dans votre répertoire local.