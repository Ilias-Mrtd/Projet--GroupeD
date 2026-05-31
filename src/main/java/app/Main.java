package app;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import model.graph.*;
import model.agents.Agent;
import UI.*;
import UI.renderers.*;
import controllers.SelectionSystem;
import simulationEngine.SimulationEngine;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // ==========================================
        // 1. INITIALISATION DU MODÈLE (Les Données)
        // ==========================================
        Graph graph = new Graph();
        List<Agent> agents = new ArrayList<>();

        // ==========================================
        // 2. INITIALISATION DE LA VUE (L'UI)
        // ==========================================

        // Création du chef d'orchestre des dessins
        GraphRenderer renderer = new GraphRenderer(
                new NodeRenderer(),
                new EdgeRenderer(),
                new AgentRenderer());

        // Création du Canvas (La zone de dessin au centre)
        GraphCanvas graphCanvas = new GraphCanvas(graph, renderer);
        graphCanvas.setStyle(STYLESHEET_CASPIAN);
        graphCanvas.setAgents(agents);

        // Création du panneau latéral pour les propriétés
        PropertiesPanel propertiesPanel = new PropertiesPanel(graph, agents);

        // (Optionnel) Une petite barre d'outils en haut
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #E0E0E0;");
        Button btnPlay = new Button("RELANCER");
        toolbar.getChildren().add(btnPlay);

        // Assemblage dans le conteneur principal
        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(graphCanvas);
        root.setRight(propertiesPanel);

        // ==========================================
        // 3. INITIALISATION DES CONTRÔLEURS & MOTEUR
        // ==========================================

        // Le système de clics
        SelectionSystem selectionSystem = new SelectionSystem(graph, agents, graphCanvas);
        graphCanvas.setSelectionSystem(selectionSystem);

        // Le moteur physique
        SimulationEngine engine = new SimulationEngine(graph, agents, graphCanvas, propertiesPanel);

        // Bouton Relancer
        btnPlay.setOnAction(e -> {
            engine.restartSimulation();
        });

        // Création du graphe de test
        setupSampleGraph(graph, agents, engine);

        // ==========================================
        // 4. LANCEMENT DE LA FENÊTRE
        // ==========================================
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Deplacement d'agents sur un graph - PGL - Groupe D");
        primaryStage.setScene(scene);
        primaryStage.show();

        // On force un premier rendu APRÈS que la scène soit affichée
        graphCanvas.draw();

        // On démarre le moteur !
        engine.start();
    }

    /**
     * Méthode utilitaire pour créer un scénario de test
     */
    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {
        // --- Création des Nœuds ---

        graph.addNode(150, 300, 1); // n0 — gauche
        graph.addNode(400, 150, 1); // n1 — centre-haut
        graph.addNode(650, 300, 1); // n2 — droite
        graph.addNode(50, 50, 1); // n3
        graph.addNode(200, 150, 1); // n4
        graph.addNode(650, 100, 1); // n5
        graph.addNode(500, 400, 1); // n6
        graph.addNode(400, 50, 1); // n7
        graph.addNode(350, 300, 1); // n8
        graph.addNode(50, 400, 1); // n9
        graph.addNode(200, 500, 1); // n10
        graph.addNode(650, 500, 1); // n11

        Node n0 = graph.Nodes.get(0);
        Node n1 = graph.Nodes.get(1);
        Node n2 = graph.Nodes.get(2);
        Node n3 = graph.Nodes.get(3);
        Node n4 = graph.Nodes.get(4);
        Node n5 = graph.Nodes.get(5);
        Node n6 = graph.Nodes.get(6);
        Node n7 = graph.Nodes.get(7);
        Node n8 = graph.Nodes.get(8);
        Node n9 = graph.Nodes.get(9);
        Node n10 = graph.Nodes.get(10);
        Node n11 = graph.Nodes.get(11);

        // --- Création des Arêtes ---

        graph.addEdge(n0, n1, 1, true); // Jai orienter les noeuds pour teste
        graph.addEdge(n1, n2, 1, true);
        graph.addEdge(n2, n11, 1, true);
        graph.addEdge(n1, n5, 1, true);
        graph.addEdge(n0, n4, 1, false);
        graph.addEdge(n5, n7, 1, true);
        graph.addEdge(n7, n3, 1, true);
        graph.addEdge(n3, n0, 1, true);
        graph.addEdge(n4, n7, 1, true);
        graph.addEdge(n9, n0, 1, true);
        graph.addEdge(n10, n9, 1, false);
        graph.addEdge(n10, n8, 1, true);
        graph.addEdge(n8, n2, 1, true);
        graph.addEdge(n8, n6, 1, true);
        graph.addEdge(n6, n11, 1, true);
        graph.addEdge(n11, n10, 1, true);

        // Ajout des agents
        Agent monAgent1 = new Agent("007", 2.5f, "AVAILABLE");
        monAgent1.setStartingNode(n0);
        engine.addAgent(monAgent1);
        Agent monAgent2 = new Agent("018", 2.5f, "AVAILABLE");
        monAgent2.setStartingNode(n0);
        engine.addAgent(monAgent2);
        Agent monAgent3 = new Agent("057", 2.5f, "AVAILABLE");
        monAgent3.setStartingNode(n0);
        engine.addAgent(monAgent3);
        Agent monAgent4 = new Agent("063", 2.5f, "AVAILABLE");
        monAgent4.setStartingNode(n0);
        engine.addAgent(monAgent4);
        Agent monAgent5 = new Agent("023", 2.5f, "AVAILABLE");
        monAgent5.setStartingNode(n0);
        engine.addAgent(monAgent5);

        Random random = new Random();

        for (Agent agent : engine.agents) {
            System.out.println("Agent ID : " + agent.id);
            System.out.println("État     : " + agent.state);

            // ── 4. Objectifs ─────────────────────────────────────────────────
            agent.addObjective(engine.graph.Nodes.get(11));
            agent.addObjective(engine.graph.Nodes.get(random.nextInt(engine.graph.Nodes.size())));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}