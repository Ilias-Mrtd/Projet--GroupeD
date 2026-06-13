package application;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.cell.ComboBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.converter.FloatStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.stage.Stage;
import javafx.stage.FileChooser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.io.File;

import model.graph.*;
import services.GraphStorageManager;
import simulationEngine.engine.SimulationEngine;
import model.agents.Agent;
import model.agents.Agent.agentBehavior;
import model.agents.Agent.agentState;
import UI.*;
import UI.renderers.*;
import controllers.SelectionSystem;

/**
 * Main application class responsible for initializing and managing the
 * JavaFX Graphical User Interface of the Warehouse Management Simulation.
 * It coordinates the simulation engine, graph models, entity rendering,
 * layout configuration, and real-time user interactions.
 * * @author Group D
 *
 */
public class Main extends Application {

    private final AtomicInteger nextAgentId = new AtomicInteger(1000);
    private final Random random = new Random();
    private Agent.AlgoType globalAlgo = Agent.AlgoType.RANDOM;

    /**
     * Initializes and builds the primary stage and root scene graph for the JavaFX
     * application.
     * Sets up UI panels, menu bars, canvas renderers, event-driven listeners,
     * and triggers the startup sequence of the core simulation engine.
     * * @param primaryStage The primary stage for this application, onto which
     * the application scene can be set.
     */
    @Override
    public void start(Stage primaryStage) {

        Graph graph = new Graph();
        List<Agent> agents = new ArrayList<>();
        SimulationEngine engine = new SimulationEngine(graph, agents);

        GraphRenderer renderer = new GraphRenderer(new NodeRenderer(), new EdgeRenderer(), new AgentRenderer());
        GraphCanvas graphCanvas = new GraphCanvas(graph, renderer);
        graphCanvas.setAgents(agents);
        PropertiesPanel propertiesPanel = new PropertiesPanel(graph, agents);

        // Top main navigation layout
        VBox topContainer = new VBox(5);
        topContainer.setStyle("-fx-background-color: #2D2D30; -fx-border-color: #3E3E42; -fx-border-width: 0 0 1 0;");
        topContainer.setPadding(new Insets(10));

        // Simulation engine control panel
        HBox toolbar1 = new HBox(15);
        toolbar1.setStyle("-fx-alignment: center-left;");
        Button btnRestart = new Button("🔄 Relaunch");
        Button btnPlay = new Button("▶️ Play");
        Button btnPause = new Button("⏸️ Pause");
        Button btnStep = new Button("⏭️ Step");
        Button btnClear = new Button("🗑️ Clear");
        Button btnSave = new Button("⤵️ Save");
        MenuButton menuLoad = new MenuButton("📂 Load");

        // Algorithm selection configuration
        ComboBox<String> algoSelector = new ComboBox<>();
        algoSelector.getItems().addAll("AbstractAlgorithm: Random", "AbstractAlgorithm: Dijkstra",
                "AbstractAlgorithm: A*");
        algoSelector.setValue("AbstractAlgorithm: Random");
        algoSelector.setOnAction(e -> {
            if (algoSelector.getValue().contains("Dijkstra")) {
                globalAlgo = Agent.AlgoType.DIJKSTRA;
            } else if (algoSelector.getValue().contains("A*")) {
                globalAlgo = Agent.AlgoType.ASTAR;
            } else {
                globalAlgo = Agent.AlgoType.RANDOM;
            }

            // Dynamic routing runtime updates
            for (Agent a : engine.getAgents()) {
                a.setAlgoType(globalAlgo);
            }
        });

        // Heatmap renderer option toggle
        ToggleButton btnHeatmap = new ToggleButton("🔥 Heatmap");
        btnHeatmap.setStyle("-fx-font-weight: bold; -fx-text-fill: #FF5722;");
        btnHeatmap.setOnAction(e -> {
            graphCanvas.setHeatmapMode(btnHeatmap.isSelected());
            graphCanvas.draw();
        });

        toolbar1.getChildren().addAll(btnRestart, btnPlay, btnPause, btnStep, btnClear, btnSave, menuLoad,
                new Separator(Orientation.VERTICAL), btnHeatmap, algoSelector);

        // Secondary rendering & view customization bar
        HBox toolbar2 = new HBox(15);
        toolbar2.setStyle("-fx-alignment: center-left;");

        Label lblSpeed = new Label("Speed: 1.0x");
        Slider speedSlider = new Slider(0.1, 5.0, 1.0);
        speedSlider.setShowTickMarks(true);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(1.0);
        speedSlider.setBlockIncrement(0.1);

        Label lblZoom = new Label("Zoom : 100%");
        Slider zoomSlider = new Slider(0.2, 2.0, 1.0);
        zoomSlider.setShowTickMarks(true);
        zoomSlider.setShowTickLabels(true);
        zoomSlider.setMajorTickUnit(0.5);
        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            lblZoom.setText("Zoom: " + Math.round(newVal.doubleValue() * 100) + "%");
            graphCanvas.setZoomLevel(newVal.doubleValue());
        });

        // Toggle tracking camera bound to a selected target entity
        ToggleButton btnFollowCamera = new ToggleButton("Follow Camera");
        btnFollowCamera.setStyle("-fx-font-weight: bold; -fx-text-fill: #00E5FF;");
        btnFollowCamera.setOnAction(e -> {
            graphCanvas.setFollowAgentMode(btnFollowCamera.isSelected());
            graphCanvas.draw();
        });

        ToggleButton btnToggleRoster = new ToggleButton("Show List");
        btnToggleRoster.setSelected(true);
        ToggleButton btnToggleInspector = new ToggleButton("Show Inspector");
        btnToggleInspector.setSelected(true);

        toolbar2.getChildren().addAll(lblSpeed, speedSlider, new Separator(Orientation.VERTICAL), lblZoom, zoomSlider,
                new Separator(Orientation.VERTICAL), btnFollowCamera, btnToggleRoster, btnToggleInspector);
        topContainer.getChildren().addAll(toolbar1, toolbar2);

        // Sidebar: Real-time agent status tracker
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #252526;");

        Label leftTitle = new Label("📋 Agent Roster");
        leftTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 15px; -fx-text-fill: #00E5FF;");

        TableView<Agent> agentTable = new TableView<>();
        agentTable.setEditable(true);
        agentTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

        TableColumn<Agent, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getId()));
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<Agent, Agent.agentState> stateCol = new TableColumn<>("State");
        stateCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getState()));

        // Editable behavior configurations mapped to entity properties updates
        TableColumn<Agent, Agent.agentBehavior> behaviorCol = new TableColumn<>("Behavior");
        behaviorCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAgentBehavior()));
        behaviorCol.setCellFactory(ComboBoxTableCell.forTableColumn(Agent.agentBehavior.values()));
        behaviorCol.setOnEditCommit(e -> {
            e.getRowValue().setAgentBehavior(e.getNewValue());
            if (e.getNewValue() == agentBehavior.VIP)
                e.getRowValue().setSpeed(4.0f);
            else
                e.getRowValue().setSpeed(2.5f);
        });

        TableColumn<Agent, Float> speedCol = new TableColumn<>("Speed");
        speedCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getSpeed()));
        speedCol.setCellFactory(TextFieldTableCell.forTableColumn(new FloatStringConverter()));
        speedCol.setOnEditCommit(e -> {
            if (e.getNewValue() != null && e.getNewValue() > 0)
                e.getRowValue().setSpeed(e.getNewValue());
        });

        TableColumn<Agent, Integer> patienceCol = new TableColumn<>("Max Patience");
        patienceCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getMaxPatience()));
        patienceCol.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        patienceCol.setOnEditCommit(e -> {
            if (e.getNewValue() != null && e.getNewValue() > 0)
                e.getRowValue().setMaxPatience(e.getNewValue());
        });

        TableColumn<Agent, Agent.AlgoType> algoCol = new TableColumn<>("Algorithm");
        algoCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getAlgoType()));
        algoCol.setCellFactory(ComboBoxTableCell.forTableColumn(Agent.AlgoType.values()));
        algoCol.setOnEditCommit(e -> {
            e.getRowValue().setAlgoType(e.getNewValue());
        });

        TableColumn<Agent, Agent.EndBehavior> endBehaviorCol = new TableColumn<>("Final Action");
        endBehaviorCol.setCellValueFactory(data -> new ReadOnlyObjectWrapper<>(data.getValue().getEndBehavior()));
        endBehaviorCol.setCellFactory(ComboBoxTableCell.forTableColumn(Agent.EndBehavior.values()));
        endBehaviorCol.setOnEditCommit(e -> {
            e.getRowValue().setEndBehavior(e.getNewValue());
        });

        agentTable.getColumns().addAll(List.of(idCol, stateCol, behaviorCol, speedCol, patienceCol, algoCol, endBehaviorCol));
        leftPanel.getChildren().addAll(leftTitle, agentTable);
        VBox.setVgrow(agentTable, Priority.ALWAYS);

        Runnable updateAgentTable = () -> {
            agentTable.getItems().setAll(agents);
        };

        // Flexible Canvas & Layout architecture setup
        Pane canvasContainer = new Pane(graphCanvas);
        graphCanvas.widthProperty().bind(canvasContainer.widthProperty());
        graphCanvas.heightProperty().bind(canvasContainer.heightProperty());

        ScrollPane panelScroll = new ScrollPane(propertiesPanel);
        panelScroll.setFitToWidth(true);
        panelScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        panelScroll.setStyle("-fx-background: #252526; -fx-background-color: #252526;");

        SplitPane splitPane = new SplitPane();
        splitPane.getItems().addAll(leftPanel, canvasContainer, panelScroll);
        splitPane.setDividerPositions(0.30, 0.75);

        // Dynamic workspace component visibility toggling logic
        Runnable updateLayout = () -> {
            splitPane.getItems().clear();
            if (btnToggleRoster.isSelected())
                splitPane.getItems().add(leftPanel);
            splitPane.getItems().add(canvasContainer);
            if (btnToggleInspector.isSelected())
                splitPane.getItems().add(panelScroll);

            if (btnToggleRoster.isSelected() && btnToggleInspector.isSelected()) {
                splitPane.setDividerPositions(0.30, 0.75);
            } else if (btnToggleRoster.isSelected() || btnToggleInspector.isSelected()) {
                splitPane.setDividerPositions(btnToggleRoster.isSelected() ? 0.35 : 0.65);
            }
        };
        btnToggleRoster.setOnAction(e -> updateLayout.run());
        btnToggleInspector.setOnAction(e -> updateLayout.run());

        // Master UI Container Skin Definition
        BorderPane root = new BorderPane();
        root.setStyle(
                "-fx-base: #1E1E1E; -fx-control-inner-background: #252526; -fx-background: #1E1E1E; -fx-text-base-color: #E0E0E0; -fx-accent: #00E5FF; -fx-font-family: 'Segoe UI', sans-serif;");

        root.setTop(topContainer);
        root.setCenter(splitPane);

        // Graphics user interaction hook
        SelectionSystem selectionSystem = new SelectionSystem(graph, agents, graphCanvas);
        graphCanvas.setSelectionSystem(selectionSystem);
        graphCanvas.setOnInteraction(propertiesPanel::refresh);

        // Connect data model table context selections to renderer systems
        agentTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                selectionSystem.selectAgent(newVal);
                propertiesPanel.refresh();
            }
        });

        // Frame update throttling logic (updates table rows every 15 simulation
        // iterations)
        int[] tickCount = { 0 };
        engine.setOnTick(() -> {
            graphCanvas.draw();
            propertiesPanel.refresh();

            tickCount[0]++;
            if (tickCount[0] % 15 == 0) {
                if (agentTable.getEditingCell() == null) {
                    agentTable.refresh();
                }
            }
        });

        // I/O File System Persistence Handlers
        btnSave.setOnAction(e -> {
            GraphStorageManager.ensureSaveDirectoryExists();
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialDirectory(new File(GraphStorageManager.SAVE_DIR));
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Simulation Files", "*.sim"));
            File file = fileChooser.showSaveDialog(primaryStage);
            if (file != null) {
                try {
                    GraphStorageManager.saveSimulation(file.getAbsolutePath(), graph, agents);
                    showAlert(AlertType.INFORMATION, "Success", "Simulation saved successfully!");
                    System.out.println("Success : Simulation saved successfully!");
                } catch (Exception ex) {
                    showAlert(AlertType.ERROR, "Error", "Unable to save file: " + ex.getMessage());
                    System.out.println("Error : Unable to save file: " + ex.getMessage());
                }
            }
        });

        menuLoad.setOnShowing(e -> {
            menuLoad.getItems().clear();
            List<String> files = GraphStorageManager.getSavedFiles();
            if (files.isEmpty()) {
                MenuItem emptyItem = new MenuItem("No saves found");
                emptyItem.setDisable(true);
                menuLoad.getItems().add(emptyItem);
            } else {
                for (String fileName : files) {
                    MenuItem item = new MenuItem(fileName);
                    item.setOnAction(event -> {
                        loadSimulationFile(fileName, graph, agents, graphCanvas);
                        updateAgentTable.run();
                    });
                    menuLoad.getItems().add(item);
                }
            }
        });

        btnClear.setOnAction(e -> {
            agents.clear();
            graph.clear();
            updateAgentTable.run();
        });
        propertiesPanel.setSelectionSystem(selectionSystem);

        // Graphical components interaction listeners
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
            if (sel == null)
                return;
            engine.evictAgentsFromNode(sel);
            graph.removeNode(sel);
            // Evict orphan dependencies links safety loop
            for (List<Edge> edgeList : graph.getEdges()) {
                edgeList.removeIf(e -> e.getTarget() == sel || e.getSource() == sel);
            }
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveEdge(() -> {
            Edge sel = propertiesPanel.getSelectedEdge();
            if (sel == null)
                return;
            engine.evictAgentsFromEdge(sel);
            for (List<Edge> list : graph.getEdges())
                list.removeIf(e -> e.getId() == sel.getId());
            graphCanvas.draw();
        });

        propertiesPanel.setOnAddAgent(() -> {
            Node sel = propertiesPanel.getSelectedNode();
            if (sel == null || sel.isFull())
                return;
            Agent.agentBehavior chosenBehavior = propertiesPanel.getSelectedAgentBehavior();
            float speed = (chosenBehavior == agentBehavior.VIP) ? 4.0f : 2.5f;
            Agent newAgent = new Agent(nextAgentId.getAndIncrement(), speed, agentState.AVAILABLE);
            newAgent.setStartingNode(sel);
            newAgent.setAlgoType(globalAlgo);
            newAgent.setAgentBehavior(chosenBehavior);
            engine.addAgent(newAgent);
            updateAgentTable.run();
            graphCanvas.draw();
        });

        propertiesPanel.setOnRemoveAgent(() -> {
            Agent sel = propertiesPanel.getSelectedAgent();
            if (sel == null)
                return;
            sel.releaseAll();
            agents.remove(sel);
            updateAgentTable.run();
            graphCanvas.draw();
        });

        // Assigner un objectif en direct à l'agent sélectionné
        propertiesPanel.setOnAssignObjective(() -> {
            Agent sel = propertiesPanel.getSelectedAgent();
            if (sel == null)
                return;
            selectionSystem.startAssignObjective(sel, (agent, targetNode) -> {
                agent.addObjective(targetNode);
                propertiesPanel.objectiveAssignedDone();
                System.out.println("[Main] Objectif noeud " + targetNode.getId()
                        + " assigne a l'agent " + agent.getId());
                graphCanvas.draw();
            });
        });

        propertiesPanel.setOnGenerateGraph(() -> {
            int side = propertiesPanel.getGenGridSide();
            generateRandomGraph(graph, agents, engine, graphCanvas, side);
            updateAgentTable.run();
        });
        propertiesPanel.setOnSpawnAgents(() -> {
            int n = propertiesPanel.getGenAgentCount();
            spawnRandomAgents(graph, engine, graphCanvas, n);
            updateAgentTable.run();
        });

        btnRestart.setOnAction(e -> {
            engine.restartSimulation();
            updateAgentTable.run();
        });
        btnPlay.setOnAction(e -> engine.start());
        btnPause.setOnAction(e -> engine.stop());
        btnStep.setOnAction(e -> {
            engine.stop();
            engine.doSingleStep();
        });

        speedSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            double speed = Math.round(newVal.doubleValue() * 10.0) / 10.0;
            engine.setTimeMultiplier(speed);
            lblSpeed.setText("Speed : " + speed + "x");
        });

        // Initialize default app data structures
        initializeSimulation(graph, agents, engine);
        updateAgentTable.run();

        Scene scene = new Scene(root, 1400, 800);
        primaryStage.setTitle("Warehouse Management System — Group D");
        primaryStage.setScene(scene);

        /**
         * Try a quicksave before the application close
         */
        primaryStage.setOnCloseRequest(event -> {
            try {
                services.GraphStorageManager.quickSave(graph, agents);
                showAlert(AlertType.INFORMATION, "[Autosave]",
                        "Simulation saved successfully!\n The application will close. \n THANK YOU !");
                System.out.println(
                        "[Autosave] : Simulation saved successfully! \n The application will close. \n THANK YOU !");
            } catch (Exception ex) {
                showAlert(AlertType.ERROR, "[Autosave]", "Unable to save file: " + ex.getMessage());
                System.out.println("[Autosave] : Unable to save file: " + ex.getMessage());
                ex.printStackTrace();
            }
        });

        primaryStage.show();

        graphCanvas.draw();
        engine.start();

    }

    /**
     * Helper method to instantiate and synchronously display a modal alert feedback
     * window.
     * * @param type The JavaFX AlertType configuration representing the window
     * style icon.
     * 
     * @param title   The textual content mapped to the header title string bar.
     * @param message The main body description contextual text shown inside the
     *                alert canvas.
     */
    private void showAlert(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Requests data retrieval parsing from the disk storage subsystem to override
     * runtime variables.
     * Resets the active nodes, paths, and agents collections inside the current
     * application stack.
     * * @param fileName The relative file descriptor or text name string
     * representing the file.
     * 
     * @param graph       The shared domain runtime Graph instance data model to
     *                    flush and override.
     * @param agents      The memory register tracking current entities arrays to
     *                    refresh.
     * @param graphCanvas The visual viewport area canvas forced to request a full
     *                    interface draw.
     */
    private void loadSimulationFile(String fileName, Graph graph, List<Agent> agents, GraphCanvas graphCanvas) {
        try {
            GraphStorageManager.SimulationData data = GraphStorageManager
                    .loadSimulation(GraphStorageManager.SAVE_DIR + fileName);
            graph.resetNodes();
            graph.resetEdges();
            agents.clear();
            graph.addAllNodes(data.graph().getNodes());
            graph.addAllEdges(data.graph().getEdges());
            agents.addAll(data.agents());
            graphCanvas.draw();
        } catch (Exception ex) {
            showAlert(AlertType.ERROR, "Erreur", "Impossible de charger le fichier : " + ex.getMessage());
        }
    }

    private void generateRandomGraph(Graph graph, List<Agent> agents, SimulationEngine engine, GraphCanvas canvas,
            int side) {
        agents.clear();
        graph.setNodes(new ArrayList<>());
        graph.setEdges(new ArrayList<>());
        if (side < 2)
            side = 2;
        int margin = 80;
        double w = Math.max(canvas.getWidth(), 600);
        double h = Math.max(canvas.getHeight(), 400);
        double spacingX = (w - 2 * margin) / Math.max(1, side - 1);
        double spacingY = (h - 2 * margin) / Math.max(1, side - 1);
        double spacing = Math.min(spacingX, spacingY);
        Node[][] grid = new Node[side][side];

        // Compute matrix locations nodes coordinates
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                int x = (int) (margin + c * spacing);
                int y = (int) (margin + r * spacing);
                graph.addNode(x, y, 1 + random.nextInt(5));
                grid[r][c] = graph.getNodes().get(graph.getNodes().size() - 1);
            }
        }
        // Link neighbors nodes linearly to structure orthogonal pathways paths
        // configurations
        for (int r = 0; r < side; r++) {
            for (int c = 0; c < side; c++) {
                if (c + 1 < side)
                    graph.addEdge(grid[r][c], grid[r][c + 1], 1 + random.nextInt(5), true);
                if (r + 1 < side)
                    graph.addEdge(grid[r][c], grid[r + 1][c], 1 + random.nextInt(5), true);
            }
        }
        canvas.draw();
    }

    /**
     * Spawns a parameterized quantity of simulation entities randomly across the
     * existing node network.
     * Allocates localized parameters, including variable speeds, behaviors, and
     * initial target destinations.
     * * @param graph The active network reference model holding locations available
     * for entity seeding.
     * 
     * @param engine The processing system engine receiving the newly generated
     *               items registry.
     * @param canvas The visual viewport requested to update changes.
     * @param n      The amount of unique agent objects to generate and insert.
     */
    private void spawnRandomAgents(Graph graph, SimulationEngine engine, GraphCanvas canvas, int n) {
        List<Node> nodes = graph.getNodes();
        if (nodes.isEmpty())
            return;
        for (int i = 0; i < n; i++) {
            Node start = nodes.get(random.nextInt(nodes.size()));
            Agent agent = new Agent(nextAgentId.getAndIncrement(), 2.0f + random.nextFloat() * 2.0f,
                    agentState.AVAILABLE);
            agent.setStartingNode(start);
            agent.setAgentBehavior(random.nextBoolean() ? agentBehavior.PATIENT : agentBehavior.HURRIED);
            agent.setAlgoType(globalAlgo);
            engine.addAgent(agent);
            agent.addObjective(nodes.get(random.nextInt(nodes.size())));
        }
        canvas.draw();
    }

    /**
     * Bootstraps standard demonstration infrastructure tracking workspace network
     * environment.
     * Implements the quicksave or initialise samplegraph.
     * * @param graph The target model component to construct the initial node
     * layout inside.
     * 
     * @param agents The global entities registry array receiving preliminary items.
     * @param engine The core task pipeline engine mapping runtime updates hooks.
     */
    private void initializeSimulation(Graph graph, List<Agent> agents, SimulationEngine engine) {
        java.io.File lastSession = new java.io.File(services.GraphStorageManager.SAVE_DIR + "autosave.sim");
        if (lastSession.exists()) {
            try {
                services.GraphStorageManager.SimulationData data = services.GraphStorageManager
                        .loadSimulation(lastSession.getAbsolutePath());
                graph.getNodes().addAll(data.graph().getNodes());
                graph.getEdges().addAll(data.graph().getEdges());
                for (Agent savedAgent : data.agents()) {
                    engine.addAgent(savedAgent);
                }
                System.out.println("[Autosave] succesfully loaded.");
            } catch (Exception e) {
                System.err.println("[Autosave] could not be loaded. \n Loading default graph");
                setupSampleGraph(graph, agents, engine);
            }
        } else {
            System.out.println("[Autosave] could not be found. \n Loading default graph");
            setupSampleGraph(graph, agents, engine);
        }
    }

    private void setupSampleGraph(Graph graph, List<Agent> agents, SimulationEngine engine) {
        for (int i = 100; i < 501; i += 200) {
            graph.addNode(50, i, 1);
        }
        for (int i = 100; i < 501; i += 200) {
            graph.addNode(150, i, 1);
        }
        for (int i = 100; i < 501; i += 200) {
            graph.addNode(450, i, 1);
        }
        for (int i = 100; i < 501; i += 200) {
            graph.addNode(550, i, 1);
        }
        graph.addNode(300, 300, 1);
        for (int i = 0; i < 7; i += 6) {
            graph.addEdge(graph.getNodes().get(0 + i), graph.getNodes().get(1 + i), 1, true);
            graph.addEdge(graph.getNodes().get(0 + i), graph.getNodes().get(3 + i), 1, true);
            graph.addEdge(graph.getNodes().get(1 + i), graph.getNodes().get(2 + i), 1, true);
            graph.addEdge(graph.getNodes().get(1 + i), graph.getNodes().get(4 + i), 1, true);
            graph.addEdge(graph.getNodes().get(2 + i), graph.getNodes().get(5 + i), 1, true);
            graph.addEdge(graph.getNodes().get(3 + i), graph.getNodes().get(4 + i), 1, true);
            graph.addEdge(graph.getNodes().get(4 + i), graph.getNodes().get(5 + i), 1, true);
        }
        for (int i = 3; i < 8; i += 2) {
            graph.addEdge(graph.getNodes().get(i), graph.getNodes().get(12), 1, false);
            graph.addEdge(graph.getNodes().get(12), graph.getNodes().get(i + 1), 1, false);
        }
        Agent a1 = new Agent(1, 2.5f, agentState.AVAILABLE);
        a1.setAgentBehavior(agentBehavior.VIP);
        Agent a2 = new Agent(2, 2.5f, agentState.AVAILABLE);
        a2.setAgentBehavior(agentBehavior.HURRIED);
        Agent a3 = new Agent(3, 2, agentState.AVAILABLE);
        a3.setAgentBehavior(agentBehavior.PATIENT);
        Agent a4 = new Agent(4, 2, agentState.AVAILABLE);
        a4.setAgentBehavior(agentBehavior.PATIENT);
        a1.setStartingNode(graph.getNodes().get(2));
        a2.setStartingNode(graph.getNodes().get(2));
        a3.setStartingNode(graph.getNodes().get(2));
        a4.setStartingNode(graph.getNodes().get(2));
        engine.addAgent(a1);
        engine.addAgent(a2);
        engine.addAgent(a3);
        engine.addAgent(a4);
        a1.addObjective(graph.getNodes().get(9));
        a2.addObjective(graph.getNodes().get(9));
        a3.addObjective(graph.getNodes().get(9));
        a4.addObjective(graph.getNodes().get(9));
    }

    /**
     * Main runtime application entry point loop initialization sequence.
     * * @param args Command-line execution argument matrix array parameters.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
