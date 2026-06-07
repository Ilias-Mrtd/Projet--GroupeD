package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton; 
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

import model.graph.*;
import services.FileService;
import simulationEngine.engine.SimulationEngine;
import model.agents.Agent;
import model.agents.Agent.agentBehavior;
import model.agents.Agent.agentState;
import UI.*;
import UI.renderers.*;
import controllers.SelectionSystem;

public class Main extends Application {

    private final AtomicInteger nextAgentId = new AtomicInteger(1000);
    private final Random random = new Random();
    private Agent.AlgoType globalAlgo = Agent.AlgoType.RANDOM;

    @Override
    public void start(Stage primaryStage) {

        Graph graph = new Graph();
        List<Agent> agents = new ArrayList<>();
        SimulationEngine engine = new SimulationEngine(graph, agents);

        GraphRenderer renderer = new GraphRenderer(new NodeRenderer(), new EdgeRenderer(), new AgentRenderer());
        GraphCanvas graphCanvas = new GraphCanvas(graph, renderer);
        graphCanvas.setAgents(agents);
        PropertiesPanel propertiesPanel = new PropertiesPanel(graph, agents);

        HBox toolbar = new HBox(15);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #E0E0E0; -fx-alignment: center-left;");

        Button btnRestart = new Button("🔄 Relaunch");
        Button btnPlay = new Button("▶️ Play");
        Button btnPause = new Button("⏸️ Pause");
        Button btnStep = new Button("⏭️ Step");
        Button btnClear = new Button("🗑️Clear");
        Button btnSave = new Button("⤵️ Save");
        MenuButton menuLoad = new MenuButton("📂 Load");

        Label lblSpeed = new Label("Vitesse : 1.0x");
        Slider speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setBlockIncrement(0.1);
        
        ToggleButton btnHeatmap = new ToggleButton("🔥 Heatmap");
        btnHeatmap.setStyle("-fx-font-weight: bold; -fx-text-fill: #D84315;");
        btnHeatmap.setOnAction(e -> {
            graphCanvas.setHeatmapMode(btnHeatmap.isSelected());
            graphCanvas.draw();
        });

        ComboBox<String> algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll("Algo : Aléatoire", "Algo : Dijkstra", "Algo : A*");
        algoSelector.setValue("Algo : Aléatoire");
        algoSelector.setOnAction(e -> {
            if (algoSelector.getValue().contains("Dijkstra")) { globalAlgo = Agent.AlgoType.DIJKSTRA; } 
            else if (algoSelector.getValue().contains("A*")) { globalAlgo = Agent.AlgoType.ASTAR; } 
            else { globalAlgo = Agent.AlgoType.RANDOM; }
            for (Agent a : engine.getAgents()) { a.setAlgoType(globalAlgo); }
        });

        toolbar.getChildren().addAll(btnRestart, btnPlay, btnPause, btnStep, lblSpeed, speedSlider, btnHeatmap, algoSelector, btnClear, btnSave, menuLoad);

        BorderPane root = new BorderPane();
        root.setTop(toolbar);
        root.setCenter(graphCanvas);
        ScrollPane panelScroll = new ScrollPane(propertiesPanel);
        panelScroll.setFitToWidth(true);
        panelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panelScroll.setStyle("-fx-background: #FAFAFA; -fx-background-color: #FAFAFA;");
        root.setRight(panelScroll);

        SelectionSystem selectionSystem = new SelectionSystem(graph, agents, graphCanvas);
        graphCanvas.setSelectionSystem(selectionSystem);
        graphCanvas.setOnInteraction(propertiesPanel::refresh);

        engine.setOnTick(() -> {
            graphCanvas.draw();
            propertiesPanel.refresh();
        });

        btnSave.setOnAction(e -> {
            FileService.ensureSaveDirectoryExists();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(FileService.SAVE_DIR));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Simulation Files", "*.sim"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    FileService.saveSimulation(file.getAbsolutePath(), graph, agents);
                    showAlert(AlertType.INFORMATION, "Succès", "Simulation enregistrée avec succès !");
                } catch (Exception ex) {
                    showAlert(AlertType.ERROR, "Erreur", "Impossible de sauvegarder : " + ex.getMessage());
                }
            }
        });

        menuLoad.setOnShowing(e -> {
            menuLoad.getItems().clear(); 
            List<String> files = FileService.getSavedFiles();
            if (files.isEmpty()) {
                MenuItem emptyItem = new MenuItem("Aucune sauvegarde");
                emptyItem.setDisable(true);
                menuLoad.getItems().add(emptyItem);
            } else {
                for (String fileName : files) {
                    MenuItem item = new MenuItem(fileName);
                    item.setOnAction(event -> loadSimulationFile(fileName, graph, agents, graphCanvas));
                    menuLoad.getItems().add(item);
                }
            }
        });

        btnClear.setOnAction(e -> { agents.clear(); graph.clear(); });
        propertiesPanel.setSelectionSystem(selectionSystem);

        propertiesPanel.setOnAddNode(() -> {
            int cap = propertiesPanel.getNodeCapacity();
            if (selectionSystem.hasPendingPosition()) {
                graph.addNode((int) selectionSystem.getPendingNodeX(), (int) selectionSystem.getPendingNodeY(), cap);
                selectionSystem.clearPendingPosition();
            }
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveNode(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;
            engine.evictAgentsFromNode(sel);
            graph.removeNode(sel);
            for (List<Edge> edgeList : graph.getEdges()) { edgeList.removeIf(e -> e.getTarget() == sel || e.getSource() == sel); }
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveEdge(() -> {
            Edge sel = propertiesPanel.getSelectedEdge();
            if (sel == null) return;
            engine.evictAgentsFromEdge(sel);
            for (List<Edge> list : graph.getEdges()) list.removeIf(e -> e.getId() == sel.getId());
            graphCanvas.draw();
        });

        // AJOUT D'AGENT MANUEL 

        propertiesPanel.setOnAddAgent(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null) return;
            
            int newId = nextAgentId.getAndIncrement();
            Agent.agentBehavior chosenBehavior = propertiesPanel.getSelectedAgentBehavior();
            
            
            float speed = (chosenBehavior == agentBehavior.VIP) ? 4.0f : 2.5f;
            
            Agent newAgent = new Agent(newId, speed, agentState.AVAILABLE);
            newAgent.setStartingNode(sel);
            newAgent.setAlgoType(globalAlgo); 
            newAgent.setAgentBehavior(chosenBehavior); 
            
            engine.addAgent(newAgent);
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveAgent(() -> {
            Agent sel = propertiesPanel.getSelectedAgent();
            if (sel == null) return;
            sel.releaseAll(); 
            agents.remove(sel); 
            graphCanvas.draw();
        });

        propertiesPanel.setOnGenerateGraph(() -> {
            int side = propertiesPanel.getGenGridSide();
            generateRandomGraph(graph, agents, engine, graphCanvas, side);
        });

        propertiesPanel.setOnSpawnAgents(() -> {
            int n = propertiesPanel.getGenAgentCount();
            spawnRandomAgents(graph, engine, graphCanvas, n);
        });

        btnRestart.setOnAction(e -> engine.restartSimulation());
        btnPlay.setOnAction(e -> engine.start());
        btnPause.setOnAction(e -> engine.stop());
        btnStep.setOnAction(e -> { engine.stop(); engine.doSingleStep(); });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            engine.setTimeMultiplier(speed);
            lblSpeed.setText("Vitesse : " + speed + "x");
        });

        setupSampleGraph(graph, agents, engine);
        Scene scene = new Scene(root, 1100, 768);
        primaryStage.setTitle("Gestion d'entrepôt — Groupe D");
        primaryStage.setScene(scene);
        primaryStage.show();
        graphCanvas.draw();
        engine.start();
    }

    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type); alert.setTitle(title); alert.setHeaderText(null); alert.setContentText(message); alert.showAndWait();
    }

    private void loadSimulationFile(String fileName, Graph graph, List<Agent> agents, GraphCanvas graphCanvas) {
        try { 
            FileService.SimulationData data = FileService.loadSimulation(FileService.SAVE_DIR + fileName);
            graph.resetNodes(); graph.resetEdges(); agents.clear();
            graph.addAllNodes(data.graph().getNodes()); graph.addAllEdges(data.graph().getEdges()); agents.addAll(data.agents());
            graphCanvas.draw();
        } catch (Exception ex) { showAlert(AlertType.ERROR, "Erreur", "Impossible de charger le fichier : " + ex.getMessage()); }
    }

    private void generateRandomGraph(Graph graph, List<Agent> agents, SimulationEngine engine, GraphCanvas canvas, int side) {
        agents.clear(); graph.setNodes(new ArrayList<>()); graph.setEdges(new ArrayList<>());
        if (side < 2) side = 2;
        int margin = 80; double w = Math.max(canvas.getWidth(), 600); double h = Math.max(canvas.getHeight(), 400);
        double spacingX = (w - 2 * margin) / Math.max(1, side - 1); double spacingY = (h - 2 * margin) / Math.max(1, side - 1); double spacing = Math.min(spacingX, spacingY);
        Node[][] grid = new Node[side][side];
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                int x = (int) (margin + c * spacing); int y = (int) (margin + r * spacing);
                graph.addNode(x, y, 1 + random.nextInt(5));
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                if (c + 1 < side) graph.addEdge(grid[r][c], grid[r][c + 1], 1 + random.nextInt(5), true);
                if (r + 1 < side) graph.addEdge(grid[r][c], grid[r + 1][c], 1 + random.nextInt(5), true);
            }
        }
        canvas.draw();
    }

    private void spawnRandomAgents(Graph graph, SimulationEngine engine, GraphCanvas canvas, int n) {
        List<Node> nodes = graph.getNodes();
        if (nodes.isEmpty()) return;
        for (int i = 0; i < n; i++) {
            Node start = nodes.get(random.nextInt(nodes.size()));
            Agent agent = new Agent(nextAgentId.getAndIncrement(), 2.0f + random.nextFloat() * 2.0f, agentState.AVAILABLE);
            agent.setStartingNode(start);
            agent.setAgentBehavior(random.nextBoolean() ? agentBehavior.PATIENT : agentBehavior.HURRIED);
            agent.setAlgoType(globalAlgo); 
            engine.addAgent(agent);
            agent.addObjective(nodes.get(random.nextInt(nodes.size())));
        }
        canvas.draw();
    }

    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {
        int cols = 5; int rows = 4; int startX = 100; int startY = 100; int spacing = 150;
        Node[][] grid = new Node[rows][cols];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                graph.addNode(startX + c * spacing, startY + r * spacing, c + 1);
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                if (c + 1 < cols) graph.addEdge(grid[r][c], grid[r][c + 1], r + c + 1, true);
                if (r + 1 < rows) graph.addEdge(grid[r][c], grid[r + 1][c], r + c + 1, true);
            }
        }
        Agent a1 = new Agent(007, 2.5f, Agent.agentState.AVAILABLE); a1.setCurrentNode(grid[0][0]); a1.setAgentBehavior(agentBehavior.PATIENT); a1.setAlgoType(globalAlgo); engine.addAgent(a1);
        Agent a2 = new Agent(15, 3.0f, Agent.agentState.AVAILABLE); a2.setCurrentNode(grid[0][1]); a2.setAgentBehavior(agentBehavior.HURRIED); a2.setAlgoType(globalAlgo); engine.addAgent(a2);
        for (Agent agent : engine.agents) { agent.addObjective(grid[3][4]); agent.addObjective(grid[3][0]); }
    }
    public static void main(String[] args) { launch(args); }
}