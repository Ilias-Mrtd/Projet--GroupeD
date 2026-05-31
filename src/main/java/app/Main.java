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

        // ==========================================
        // 3. CONTRÔLEURS & MOTEUR
        // ==========================================
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
        graph.addNode(150, 300, 1);
        graph.addNode(400, 150, 1);
        graph.addNode(650, 300, 1);
        graph.addNode(50,  50,  1);
        graph.addNode(200, 150, 1);
        graph.addNode(650, 100, 1);
        graph.addNode(500, 400, 1);
        graph.addNode(400, 50,  1);
        graph.addNode(350, 300, 1);
        graph.addNode(50,  400, 1);
        graph.addNode(200, 500, 1);
        graph.addNode(650, 500, 1);

        Node n0  = graph.Nodes.get(0);
        Node n1  = graph.Nodes.get(1);
        Node n2  = graph.Nodes.get(2);
        Node n3  = graph.Nodes.get(3);
        Node n4  = graph.Nodes.get(4);
        Node n5  = graph.Nodes.get(5);
        Node n6  = graph.Nodes.get(6);
        Node n7  = graph.Nodes.get(7);
        Node n8  = graph.Nodes.get(8);
        Node n9  = graph.Nodes.get(9);
        Node n10 = graph.Nodes.get(10);
        Node n11 = graph.Nodes.get(11);

        graph.addEdge(n0,  n1,  1, true);
        graph.addEdge(n1,  n2,  1, true);
        graph.addEdge(n2,  n11, 1, true);
        graph.addEdge(n1,  n5,  1, true);
        graph.addEdge(n0,  n4,  1, false);
        graph.addEdge(n5,  n7,  1, true);
        graph.addEdge(n7,  n3,  1, true);
        graph.addEdge(n3,  n0,  1, true);
        graph.addEdge(n4,  n7,  1, true);
        graph.addEdge(n9,  n0,  1, true);
        graph.addEdge(n10, n9,  1, false);
        graph.addEdge(n10, n8,  1, true);
        graph.addEdge(n8,  n2,  1, true);
        graph.addEdge(n8,  n6,  1, true);
        graph.addEdge(n6,  n11, 1, true);
        graph.addEdge(n11, n10, 1, true);

        Agent a1 = new Agent("007", 2.5f, "AVAILABLE"); a1.setStartingNode(n0); engine.addAgent(a1);
        Agent a2 = new Agent("018", 2.5f, "AVAILABLE"); a2.setStartingNode(n0); engine.addAgent(a2);
        Agent a3 = new Agent("057", 2.5f, "AVAILABLE"); a3.setStartingNode(n0); engine.addAgent(a3);
        Agent a4 = new Agent("063", 2.5f, "AVAILABLE"); a4.setStartingNode(n0); engine.addAgent(a4);
        Agent a5 = new Agent("023", 2.5f, "AVAILABLE"); a5.setStartingNode(n0); engine.addAgent(a5);

        Random random = new Random();
        for (Agent agent : engine.agents) {
            agent.addObjective(engine.graph.Nodes.get(11));
            agent.addObjective(engine.graph.Nodes.get(random.nextInt(engine.graph.Nodes.size())));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}