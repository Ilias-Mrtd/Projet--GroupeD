package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import model.graph.*;
import model.agents.Agent;
import model.agents.Agent.agentBehavior;
import model.agents.Agent.agentState;
import UI.*;
import UI.renderers.*;
import controllers.SelectionSystem;
import simulationEngine.SimulationEngine;

public class Main extends Application {

    private final AtomicInteger nextAgentId = new AtomicInteger(1000);

    @Override
    public void start(Stage primaryStage) {

        // 1. MODÈLE
        Graph graph = new Graph();
        List<Agent> agents = new ArrayList<>();

        // 2. VUE
        GraphRenderer renderer = new GraphRenderer(
                new NodeRenderer(), new EdgeRenderer(), new AgentRenderer());

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

        // 3. CONTRÔLEURS & MOTEUR
        SelectionSystem selectionSystem = new SelectionSystem(graph, agents, graphCanvas);
        graphCanvas.setSelectionSystem(selectionSystem);

        SimulationEngine engine = new SimulationEngine(graph, agents, graphCanvas, propertiesPanel);

        // 4. CALLBACKS D'ÉDITION
        // Important : on ne touche JAMAIS à engine.stop()/start() ici
        propertiesPanel.setSelectionSystem(selectionSystem);

        // Ajouter un nœud à la position du clic dans le vide
        propertiesPanel.setOnAddNode(() -> {
            int cap = propertiesPanel.getNodeCapacity();
            if (selectionSystem.hasPendingPosition()) {
                graph.addNode((int) selectionSystem.getPendingNodeX(),
                        (int) selectionSystem.getPendingNodeY(), cap);
                selectionSystem.clearPendingPosition();
            } else {
                System.out.println("You tried to add a Node but did not selected a place.");
            }
            graphCanvas.draw();
        });

        // Supprimer le nœud sélectionné (seulement si aucun agent dessus)
        propertiesPanel.setOnRemoveNode(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null)
                return;
            boolean occupied = agents.stream().anyMatch(a -> a.getCurrentNode() == sel);
            if (occupied) {
                System.out.println("[Main] Nœud " + sel.getId() + " occupé, suppression impossible.");
                return;
            }
            graph.removeNode(sel);
            graphCanvas.draw();
        });

        // Supprimer l'arête sélectionnée
        propertiesPanel.setOnRemoveEdge(() -> {
            Edge sel = propertiesPanel.getSelectedEdge();
            if (sel == null)
                return;
            // Vérifier qu'aucun agent n'est dessus
            boolean occupied = agents.stream().anyMatch(a -> a.getCurrentEdge() == sel);
            if (occupied) {
                System.out.println("[Main] Arête " + sel.getId() + " occupée, suppression impossible.");
                return;
            }
            for (List<Edge> list : graph.getEdges())
                list.removeIf(e -> e.getId() == sel.getId());
            System.out.println("[Main] Arête " + sel.getId() + " supprimée.");
            graphCanvas.draw();
        });

        // Ajouter un agent sur le nœud sélectionné
        propertiesPanel.setOnAddAgent(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null)
                return;
            int newId = nextAgentId.getAndIncrement();
            Agent newAgent = new Agent(newId, 2.5f, agentState.AVAILABLE);
            newAgent.setStartingNode(sel);
            engine.addAgent(newAgent);
            System.out.println("[Main] Agent " + newId + " ajouté sur nœud " + sel.getId());
            graphCanvas.draw();
        });

        btnPlay.setOnAction(e -> engine.restartSimulation());

        // 5. SCÉNARIO DE TEST
        setupSampleGraph(graph, agents, engine);

        // 6. LANCEMENT
        Scene scene = new Scene(root, 1100, 768);
        primaryStage.setTitle("Gestion d'entrepôt — Groupe D");
        primaryStage.setScene(scene);
        primaryStage.show();

        graphCanvas.draw();
        engine.start();
    }

    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {
        int cols = 5;
        int rows = 4;
        int startX = 100;
        int startY = 100;
        int spacing = 150;

        Node[][] grid = new Node[rows][cols];

        // Test de degrader de couleurs node
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                graph.addNode(startX + c * spacing, startY + r * spacing, c + 1);
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }

        // Test de degrader de couleurs edge
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c + 1 < cols)
                    graph.addEdge(grid[r][c], grid[r][c + 1], r + c + 1, true);
                if (r + 1 < rows)
                    graph.addEdge(grid[r][c], grid[r + 1][c], r + c + 1, true);
            }
        }

        Agent a1 = new Agent(007, 2.5f, Agent.agentState.AVAILABLE);
        a1.setCurrentNode(grid[0][0]);
        a1.setAgentBehavior(agentBehavior.PATIENT);
        a1.setPriority(1);
        engine.addAgent(a1);
        Agent a2 = new Agent(15, 3.0f, Agent.agentState.AVAILABLE);
        a2.setCurrentNode(grid[0][1]);
        a2.setAgentBehavior(agentBehavior.HURRIED);
        engine.addAgent(a2);

        for (Agent agent : engine.agents) {
            agent.addObjective(grid[3][4]);
            agent.addObjective(grid[3][0]);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}