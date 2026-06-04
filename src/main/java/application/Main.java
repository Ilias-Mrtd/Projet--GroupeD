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
import java.util.Random;
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
    private final Random random = new Random();


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
        propertiesPanel.setSelectionSystem(selectionSystem);

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

        propertiesPanel.setOnRemoveNode(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;
            boolean occupied = agents.stream().anyMatch(a -> a.getCurrentNode() == sel);
            if (occupied) {
                System.out.println("[Main] Nœud " + sel.getId() + " occupé, suppression impossible.");
                return;
            }
            graph.removeNode(sel);
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveEdge(() -> {
            Edge sel = propertiesPanel.getSelectedEdge();
            if (sel == null) return;
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

        propertiesPanel.setOnAddAgent(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;
            int newId = nextAgentId.getAndIncrement();
            Agent newAgent = new Agent(newId, 2.5f, agentState.AVAILABLE);
            newAgent.setStartingNode(sel);
            engine.addAgent(newAgent);
            System.out.println("[Main] Agent " + newId + " ajouté sur nœud " + sel.getId());
            graphCanvas.draw();
        });

        // Supprimer l'agent sélectionné (libère toutes ses ressources)
        propertiesPanel.setOnRemoveAgent(() -> {
            Agent sel = propertiesPanel.getSelectedAgent();
            if (sel == null) return;
            sel.releaseAll();          // libère réservations + occupants + files
            agents.remove(sel);        // retire de la liste du moteur
            System.out.println("[Main] Agent " + sel.getId() + " supprimé de la simulation.");
            graphCanvas.draw();
        });

        // ====================================================== GÉNÉRATION DE MASSE

        // "Générer un graphe" : crée un réseau aléatoire connexe
        propertiesPanel.setOnGenerateGraph(() -> {
            int side = propertiesPanel.getGenGridSide();
            generateRandomGraph(graph, agents, engine, graphCanvas, side);
        });

        // "Faire apparaître les agents" : N agents répartis aléatoirement
        propertiesPanel.setOnSpawnAgents(() -> {
            int n = propertiesPanel.getGenAgentCount();
            spawnRandomAgents(graph, engine, graphCanvas, n);
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

    // ============================================================== GRAPHE ALÉATOIRE

    /**
     * Génère un graphe en GRILLE CARRÉE de côté "side" (side x side nœuds).
     *
     * Principe simple (facile à présenter) :
     *  - "side" vient directement du panneau (+/-), donc chaque clic change la grille.
     *  - L'espacement entre nœuds s'adapte à la taille du canvas → la grille rentre toujours.
     *  - Chaque nœud est relié à son voisin de DROITE et du BAS (grille classique, connexe).
     *  - Capacités des nœuds et des arêtes aléatoires entre 1 et 5.
     */
    private void generateRandomGraph(Graph graph, List<Agent> agents,
                                     SimulationEngine engine, GraphCanvas canvas, int side) {

        // 1. On vide le graphe et les agents existants
        agents.clear();
        graph.setNodes(new ArrayList<>());
        graph.setEdges(new ArrayList<>());

        if (side < 2) side = 2;

        // 2. Espacement adaptatif : on répartit la grille dans le canvas avec une marge
        int margin = 80;
        double w = Math.max(canvas.getWidth(), 600);
        double h = Math.max(canvas.getHeight(), 400);

        // (side - 1) intervalles entre les nœuds sur chaque axe
        double spacingX = (w - 2 * margin) / Math.max(1, side - 1);
        double spacingY = (h - 2 * margin) / Math.max(1, side - 1);
        double spacing  = Math.min(spacingX, spacingY); // on garde des carrés réguliers

        Node[][] grid = new Node[side][side];

        // 3. Création des nœuds (capacité aléatoire 1 à 5)
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                int x = (int) (margin + c * spacing);
                int y = (int) (margin + r * spacing);
                int cap = 1 + random.nextInt(5); // 1 à 5
                graph.addNode(x, y, cap);
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }

        // 4. Arêtes : voisin de droite + voisin du bas (capacité aléatoire 1 à 5)
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                if (c + 1 < side)
                    graph.addEdge(grid[r][c], grid[r][c + 1], 1 + random.nextInt(5), true);
                if (r + 1 < side)
                    graph.addEdge(grid[r][c], grid[r + 1][c], 1 + random.nextInt(5), true);
            }
        }

        System.out.println("[Main] Grille " + side + "x" + side + " générée ("
                + graph.getNodes().size() + " nœuds).");
        canvas.draw();
    }

    // ============================================================== AGENTS EN MASSE

    /**
     * Fait apparaître n agents sur des nœuds choisis au hasard,
     * avec un objectif aléatoire chacun.
     */
    private void spawnRandomAgents(Graph graph, SimulationEngine engine,
                                   GraphCanvas canvas, int n) {
        List<Node> nodes = graph.getNodes();
        if (nodes.isEmpty()) {
            System.out.println("[Main] Aucun nœud : générez d'abord un graphe.");
            return;
        }

        for (int i = 0; i < n; i++) {
            Node start = nodes.get(random.nextInt(nodes.size()));
            int id = nextAgentId.getAndIncrement();
            float speed = 2.0f + random.nextFloat() * 2.0f; // vitesse 2.0 à 4.0
            Agent agent = new Agent(id, speed, agentState.AVAILABLE);
            agent.setStartingNode(start);
            // comportement aléatoire
            agent.setAgentBehavior(random.nextBoolean() ? agentBehavior.PATIENT : agentBehavior.HURRIED);
            engine.addAgent(agent);
            // un objectif aléatoire différent du départ si possible
            Node objective = nodes.get(random.nextInt(nodes.size()));
            agent.addObjective(objective);
        }

        System.out.println("[Main] " + n + " agents générés.");
        canvas.draw();
    }

    // ============================================================== SCÉNARIO TEST

    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {
        int cols = 5;
        int rows = 4;
        int startX = 100;
        int startY = 100;
        int spacing = 150;

        Node[][] grid = new Node[rows][cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                graph.addNode(startX + c * spacing, startY + r * spacing, 1);
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }

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