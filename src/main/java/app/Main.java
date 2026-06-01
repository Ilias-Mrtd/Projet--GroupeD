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
import java.util.concurrent.atomic.AtomicInteger;

import model.graph.*;
import model.agents.Agent;
import UI.*;
import UI.renderers.*;
import controllers.SelectionSystem;
import simulationEngine.SimulationEngine;

public class Main extends Application {

    /** Compteur global pour les IDs agents ajoutés depuis l'UI */
    private final AtomicInteger nextAgentId = new AtomicInteger(1000);

    @Override
    public void start(Stage primaryStage) {

        // ==========================================
        // 1. MODÈLE
        // ==========================================
        Graph graph = new Graph();
        List<Agent> agents = new ArrayList<>();

        // ==========================================
        // 2. VUE
        // ==========================================
        GraphRenderer renderer = new GraphRenderer(
                new NodeRenderer(),
                new EdgeRenderer(),
                new AgentRenderer());

        GraphCanvas graphCanvas = new GraphCanvas(graph, renderer);
        graphCanvas.setAgents(agents);

        PropertiesPanel propertiesPanel = new PropertiesPanel(graph, agents);

        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #E0E0E0;");
        Button btnPlay = new Button("RELANCER");
        toolbar.getChildren().add(btnPlay);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(graphCanvas);
        root.setRight(propertiesPanel);
        
        SelectionSystem selectionSystem = new SelectionSystem(graph, agents, graphCanvas);
        graphCanvas.setSelectionSystem(selectionSystem);

        SimulationEngine engine = new SimulationEngine(graph, agents, graphCanvas, propertiesPanel);

        // ==========================================
        // 4. INJECTION DES CALLBACKS D'ÉDITION
        //    (fait ici car besoin de engine + selectionSystem)
        // ==========================================
        propertiesPanel.setSelectionSystem(selectionSystem);

        // "Ajouter un nœud" : crée un nœud au centre visible du canvas
        propertiesPanel.setOnAddNode(() -> {
            double cx = graphCanvas.getWidth()  / 2 + (Math.random() * 60 - 30);
            double cy = graphCanvas.getHeight() / 2 + (Math.random() * 60 - 30);
            graph.addNode((int) cx, (int) cy, 1);
            graphCanvas.draw();
        });

        // "Supprimer le nœud sélectionné"
        propertiesPanel.setOnRemoveNode(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;

            // Vérifier qu'aucun agent n'est dessus pour éviter un état incohérent
            boolean occupied = agents.stream().anyMatch(a -> a.currentNode == sel);
            if (occupied) {
                System.out.println("[Main] Impossible de supprimer le nœud " + sel.id
                        + " : un agent l'occupe actuellement.");
                return;
            }
            graph.removeNode(sel);
            graphCanvas.draw();
        });

        // "Ajouter un agent sur le nœud sélectionné"
        propertiesPanel.setOnAddAgent(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;
            String newId = String.valueOf(nextAgentId.getAndIncrement());
            Agent newAgent = new Agent(newId, 2.5f, "AVAILABLE");
            newAgent.setStartingNode(sel);
            engine.addAgent(newAgent);
            System.out.println("[Main] Nouvel agent " + newId + " ajouté sur le nœud " + sel.id);
            graphCanvas.draw();
        });

        // Bouton relancer
        btnPlay.setOnAction(e -> engine.restartSimulation());

        // ==========================================
        // 5. SCÉNARIO DE TEST
        // ==========================================
        setupSampleGraph(graph, agents, engine);

        // ==========================================
        // 6. LANCEMENT
        // ==========================================
        Scene scene = new Scene(root, 1024, 768);
        primaryStage.setTitle("Gestion d'entrepôt — Groupe D");
        primaryStage.setScene(scene);
        primaryStage.show();

        graphCanvas.draw();
        engine.start();
    }

    // ---------------------------------------------------------------- scénario test (inchangé)
    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {

    int cols = 5;   // nombre de colonnes
    int rows = 4;   // nombre de lignes

    int startX = 100;
    int startY = 100;
    int spacing = 150;

    Node[][] grid = new Node[rows][cols];

    // 1. Création des nodes en grille
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {
            int x = startX + c * spacing;
            int y = startY + r * spacing;

            graph.addNode(x, y, 1);
            grid[r][c] = graph.Nodes.get(graph.Nodes.size() - 1);
        }
    }

    // 2. Création des edges (grille classique)
    for (int r = 0; r < rows; r++) {
        for (int c = 0; c < cols; c++) {

            Node current = grid[r][c];

            // lien vers la droite
            if (c + 1 < cols) {
                graph.addEdge(current, grid[r][c + 1], 1, true);
            }

            // lien vers le bas
            if (r + 1 < rows) {
                graph.addEdge(current, grid[r + 1][c], 1, true);
            }
        }
    }

    // 3. Agents (inchangé)
    Agent a1 = new Agent("007", 2.5f, "AVAILABLE"); a1.setStartingNode(grid[0][0]); engine.addAgent(a1);

    // 4. Objectifs aléatoires
    Random random = new Random();

    for (Agent agent : engine.agents) {
        agent.addObjective(grid[rows - 1][cols - 1]); // coin bas droite
        agent.addObjective(
            grid[random.nextInt(rows)][random.nextInt(cols)]
        );
    }
}

    public static void main(String[] args) {
        launch(args);
    }
}