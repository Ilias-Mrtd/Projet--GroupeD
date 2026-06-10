package UI;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.graph.*;
import model.agents.Agent;
import controllers.SelectionSystem;
import java.util.List;

/**
 * Control dashboard sidebar detailing individual element configurations, 
 * simulation performance logs, dynamic KPIs, and automated infrastructure controls.
 */
public class PropertiesPanel extends VBox {

    private final Graph graph;
    private final List<Agent> agents;

    private SelectionSystem selectionSystem;
    private Runnable onAddNode;
    private Runnable onRemoveNode;
    private Runnable onRemoveEdge;
    private Runnable onAddAgent;
    private Runnable onGenerateGraph;
    private Runnable onSpawnAgents;
    private Runnable onRemoveAgent;
    private Runnable onAssignObjective;

    private final Label titleLabel;
    private final Label infoLabel;
    private final TextArea logArea;
    private final TextArea globalStatsArea;

    private final Button btnAddNode;
    private final Button btnRemoveNode;
    private final Button btnAddEdge;
    private final Button btnRemoveEdge;
    private final Button btnAddAgent;
    private final Button btnRemoveAgent;
    private final Button btnAssignObjective;

    private final ComboBox<Agent.agentBehavior> cbAgentBehavior;

    private int edgeCapacity = 1;
    private boolean edgeDirection = true;
    private final Label lblEdgeCap;
    private final Label lblEdgeSpeed;

    private int nodeCapacity = 1;
    private final Label lblNodeCap;
    private final CheckBox chkUnderConstruction;

    private int genGridSide = 4;
    private int genAgentCount = 10;
    private final Label lblGenNodes;
    private final Label lblGenAgents;

    private boolean linkingActive = false;
    private boolean assigningObjective = false;

    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(280);
        setStyle("-fx-background-color: #252526; -fx-border-color: #3E3E42; -fx-border-width: 0 0 0 1;");

        titleLabel = new Label("🔍 Inspector");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #00E5FF;");

        TabPane inspectorTabs = new TabPane();
        inspectorTabs.setPrefHeight(300);

        // Details Tab Layout
        Tab tabDetails = new Tab("Details");
        tabDetails.setClosable(false);
        infoLabel = new Label("Click on an entity\nto view its details.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #CCCCCC; -fx-padding: 5px;");
        infoLabel.setWrapText(true);
        ScrollPane detailsScroll = new ScrollPane(infoLabel);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background: #1E1E1E;");
        tabDetails.setContent(detailsScroll);

        // History Log Tab Layout
        Tab tabHistory = new Tab("History");
        tabHistory.setClosable(false);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");
        logArea.setWrapText(true);
        tabHistory.setContent(logArea);

        // Scoreboard Tab Layout
        Tab tabGlobal = new Tab("Scoreboard");
        tabGlobal.setClosable(false);
        globalStatsArea = new TextArea();
        globalStatsArea.setEditable(false);
        globalStatsArea.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #00E5FF;");
        globalStatsArea.setWrapText(true);
        tabGlobal.setContent(globalStatsArea);

        inspectorTabs.getTabs().addAll(tabDetails, tabHistory, tabGlobal);

        Separator sep1 = new Separator();

        // ---- Node Control Properties Section ----
        Label lblNodeSection = new Label("Node Settings");
        lblNodeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        lblNodeCap = new Label("Capacity: 1");
        lblNodeCap.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
        Button btnNodeCapMinus = smallButton("−");
        Button btnNodeCapPlus = smallButton("+");
        btnNodeCapMinus.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) { if (sel.getCapacity() > 1) sel.setCapacity(sel.getCapacity() - 1); } else if (nodeCapacity > 1) { nodeCapacity--; }
            refresh(); redrawCanvas();
        });
        btnNodeCapPlus.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) { sel.setCapacity(sel.getCapacity() + 1); } else { nodeCapacity++; }
            refresh(); redrawCanvas();
        });
        HBox nodeCapBox = new HBox(6, btnNodeCapMinus, lblNodeCap, btnNodeCapPlus);
        nodeCapBox.setStyle("-fx-alignment: center-left;");

        chkUnderConstruction = new CheckBox("Under Construction (Closed)");
        chkUnderConstruction.setStyle("-fx-text-fill: #CCCCCC;");
        chkUnderConstruction.setDisable(true);
        chkUnderConstruction.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) sel.setUnderConstruction(chkUnderConstruction.isSelected());
            refresh(); redrawCanvas();
        });

        btnAddNode = buildButton("➕ Add Node Here", "#0288D1");
        btnAddNode.setDisable(true);
        btnRemoveNode = buildButton("🗑 Remove Node", "#D32F2F");
        btnRemoveNode.setDisable(true);
        btnAddNode.setOnAction(e -> handleAddNode());
        btnRemoveNode.setOnAction(e -> handleRemoveNode());

        Separator sep2 = new Separator();

        // ---- Edge Control Properties Section ----
        Label lblEdgeSection = new Label("Edge Settings");
        lblEdgeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        lblEdgeCap = new Label("Capacity: 1");
        lblEdgeCap.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
        Button btnEdgeCapMinus = smallButton("−");
        Button btnEdgeCapPlus = smallButton("+");
        btnEdgeCapMinus.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { if (sel.getCapacity() > 1) sel.setCapacity(sel.getCapacity() - 1); } else if (edgeCapacity > 1) { edgeCapacity--; }
            refresh(); redrawCanvas();
        });
        btnEdgeCapPlus.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { sel.setCapacity(sel.getCapacity() + 1); } else { edgeCapacity++; }
            refresh(); redrawCanvas();
        });
        HBox edgeCapBox = new HBox(6, btnEdgeCapMinus, lblEdgeCap, btnEdgeCapPlus);
        edgeCapBox.setStyle("-fx-alignment: center-left;");

        lblEdgeSpeed = new Label("Speed: x1.0");
        lblEdgeSpeed.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
        Button btnEdgeSpeedMinus = smallButton("−");
        Button btnEdgeSpeedPlus = smallButton("+");
        btnEdgeSpeedMinus.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null && sel.getSpeedModifier() > 0.2f) sel.setSpeedModifier(sel.getSpeedModifier() - 0.2f);
            refresh(); redrawCanvas();
        });
        btnEdgeSpeedPlus.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null && sel.getSpeedModifier() < 5.0f) sel.setSpeedModifier(sel.getSpeedModifier() + 0.2f);
            refresh(); redrawCanvas();
        });
        HBox edgeSpeedBox = new HBox(6, btnEdgeSpeedMinus, lblEdgeSpeed, btnEdgeSpeedPlus);
        edgeSpeedBox.setStyle("-fx-alignment: center-left;");

        Button btnToggleDir = buildButton("Bidirectional ⇄", "#455A64");
        btnToggleDir.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { sel.setDirection(!sel.hasDirection()); } else { edgeDirection = !edgeDirection; }
            refresh(); redrawCanvas();
            btnToggleDir.setText(edgeDirection ? "Bidirectional ⇄" : "Directed →");
        });

        btnAddEdge = buildButton("🔗 Add Edge Connection", "#7B1FA2");
        btnRemoveEdge = buildButton("🗑 Remove Edge Connection", "#C2185B");
        btnRemoveEdge.setDisable(true);
        btnAddEdge.setOnAction(e -> handleAddEdge());
        btnRemoveEdge.setOnAction(e -> handleRemoveEdge());

        Separator sep3 = new Separator();

        // ---- Agent Configuration Section ----
        Label lblAgentSection = new Label("Agent Settings");
        lblAgentSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        cbAgentBehavior = new ComboBox<>();
        cbAgentBehavior.getItems().addAll(Agent.agentBehavior.PATIENT, Agent.agentBehavior.HURRIED, Agent.agentBehavior.VIP);
        cbAgentBehavior.setValue(Agent.agentBehavior.PATIENT);
        cbAgentBehavior.setStyle("-fx-font-size: 12px;");
        cbAgentBehavior.setMaxWidth(Double.MAX_VALUE);

        btnAddAgent = buildButton("🤖 Add Autonomous Agent", "#388E3C");
        btnAddAgent.setDisable(true);
        btnAddAgent.setOnAction(e -> handleAddAgent());

        btnRemoveAgent = buildButton("🗑 Remove Agent Profile", "#E64A19");
        btnRemoveAgent.setDisable(true);
        btnRemoveAgent.setOnAction(e -> handleRemoveAgent());

        btnAssignObjective = buildButton("🎯 Assign Navigation Target", "#FBC02D");
        btnAssignObjective.setDisable(true);
        btnAssignObjective.setOnAction(e -> handleAssignObjective());

        Separator sep4 = new Separator();

        // ---- Structural Generation Controls ----
        Label lblGenSection = new Label("Batch Matrix Generation");
        lblGenSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        lblGenNodes = new Label("Grid Map: " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " vertices)");
        lblGenNodes.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
        Button btnGenNodesMinus = smallButton("−");
        Button btnGenNodesPlus = smallButton("+");
        btnGenNodesMinus.setOnAction(e -> { if (genGridSide > 2) { genGridSide--; lblGenNodes.setText("Grid Map: " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " vertices)"); } });
        btnGenNodesPlus.setOnAction(e -> { if (genGridSide < 12) { genGridSide++; lblGenNodes.setText("Grid Map: " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " vertices)"); } });
        HBox genNodesBox = new HBox(6, btnGenNodesMinus, lblGenNodes, btnGenNodesPlus);
        genNodesBox.setStyle("-fx-alignment: center-left;");

        Button btnGenGraphAction = buildButton("🏙 Generate Synthesized Graph", "#303F9F");
        btnGenGraphAction.setOnAction(e -> { if (onGenerateGraph != null) onGenerateGraph.run(); });

        lblGenAgents = new Label("Entity Profiles: " + genAgentCount);
        lblGenAgents.setStyle("-fx-font-size: 12px; -fx-text-fill: #CCCCCC;");
        Button btnGenAgentsMinus = smallButton("−");
        Button btnGenAgentsPlus = smallButton("+");
        btnGenAgentsMinus.setOnAction(e -> { if (genAgentCount > 1) { genAgentCount--; lblGenAgents.setText("Entity Profiles: " + genAgentCount); } });
        btnGenAgentsPlus.setOnAction(e -> { if (genAgentCount < 100) { genAgentCount++; lblGenAgents.setText("Entity Profiles: " + genAgentCount); } });
        HBox genAgentsBox = new HBox(6, btnGenAgentsMinus, lblGenAgents, btnGenAgentsPlus);
        genAgentsBox.setStyle("-fx-alignment: center-left;");

        Button btnSpawnAgentsAction = buildButton("👥 Spawn Agent Clusters", "#00796B");
        btnSpawnAgentsAction.setOnAction(e -> { if (onSpawnAgents != null) onSpawnAgents.run(); });

        getChildren().addAll(
                titleLabel, inspectorTabs,
                sep1, lblNodeSection, nodeCapBox, chkUnderConstruction, btnAddNode, btnRemoveNode,
                sep2, lblEdgeSection, edgeCapBox, edgeSpeedBox, btnToggleDir, btnAddEdge, btnRemoveEdge,
                sep3, lblAgentSection, cbAgentBehavior, btnAddAgent, btnRemoveAgent, btnAssignObjective,
                sep4, lblGenSection, genNodesBox, btnGenGraphAction, genAgentsBox, btnSpawnAgentsAction);
    }

    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
        ss.setOnEmptyClick((x, y) -> {
            btnAddNode.setDisable(false);
            btnAddNode.setText("➕ Place Here (" + (int) x + "," + (int) y + ")");
        });
        btnAddAgent.setDisable(true);
    }

    public void setOnAddNode(Runnable r) { this.onAddNode = r; }
    public void setOnRemoveNode(Runnable r) { this.onRemoveNode = r; }
    public void setOnRemoveEdge(Runnable r) { this.onRemoveEdge = r; }
    public void setOnAddAgent(Runnable r) { this.onAddAgent = r; }
    public void setOnGenerateGraph(Runnable r) { this.onGenerateGraph = r; }
    public void setOnSpawnAgents(Runnable r) { this.onSpawnAgents = r; }
    public void setOnRemoveAgent(Runnable r) { this.onRemoveAgent = r; }
    public void setOnAssignObjective(Runnable r) { this.onAssignObjective = r; }

    public Agent.agentBehavior getSelectedAgentBehavior() { return cbAgentBehavior.getValue(); }

    /**
     * Refreshes content panels based on currently tracked entity selection models.
     */
    public void refresh() {
        Object selected = findSelectedItem();

        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            double avgSpeed = (a.getTotalActiveTime() > 0) ? (a.getTotalDistance() / a.getTotalActiveTime()) / 60.0 : 0.0;
            double efficiency = (a.getTotalActiveTime() > 0) ? ((a.getTotalActiveTime() - a.getTotalWaitTime()) / a.getTotalActiveTime()) * 100.0 : 100.0;

            StringBuilder sb = new StringBuilder();
            sb.append("Type     : Agent [").append(a.getAgentBehavior()).append("]\n")
                    .append("ID       : ").append(a.getId()).append("\n")
                    .append("Routine  : ").append(a.getAlgoType()).append("\n")
                    .append("Status   : ").append(a.getState()).append("\n\n")
                    .append("-- KPI & PERFORMANCE METRICS --\n")
                    .append("Objectives Reached : ").append(a.getObjectivesReached()).append("\n")
                    .append("Objectives Aborted : ").append(a.getAbandonedObjectives()).append("\n")
                    .append("Forced Detours     : ").append(a.getDetoursTaken()).append("\n")
                    .append("Total Active Time  : ").append(String.format("%.1fs", a.getTotalActiveTime())).append("\n")
                    .append("Total Delay (Wait) : ").append(String.format("%.1fs", a.getTotalWaitTime())).append("\n")
                    .append("Traffic Efficiency : ").append(String.format("%.1f%%", efficiency)).append("\n")
                    .append("Target Speed Limit : ").append(String.format("%.1f", a.getSpeed())).append(" px/s\n")
                    .append("Measured Net Velocity: ").append(String.format("%.1f", avgSpeed)).append(" px/s\n\n");

            if (a.getCurrentEdge() != null && a.getDestination() != null) {
                sb.append("On Connection Link : ").append(a.getCurrentEdge().getId()).append("\n")
                  .append("Target Destination : ").append(a.getDestination().getId());
            } else if (a.getCurrentNode() != null) {
                sb.append("Current Position   : Node ").append(a.getCurrentNode().getId());
            }

            infoLabel.setText(sb.toString());

            String logText = String.join("\n", a.getHistoryLog());
            if (!logArea.getText().equals(logText)) {
                logArea.setText(logText);
                logArea.setScrollTop(Double.MAX_VALUE);
            }

        } else if (selected instanceof Node) {
            Node n = (Node) selected;
            infoLabel.setText("Type     : Node Vertex\nID       : " + n.getId() + "\nCoordinates: (" + (int) n.getX() + ", " + (int) n.getY() + ")\nIn Works : " + (n.isUnderConstruction() ? "YES (Closed)" : "No") + "\nCapacity : " + n.getCapacity() + "\nStatus   : " + n.getState() + "\nOccupancy: " + n.getCurrentOccupants() + "/" + n.getCapacity());
            logArea.setText("History logs are unavailable for infrastructure nodes.");
        } else if (selected instanceof Edge) {
            Edge ed = (Edge) selected;
            String directionSymbol = ed.hasDirection() ? " --> " : " <--> ";
            infoLabel.setText("Type      : Edge Connection\nID        : " + ed.getId() + "\nConnection: " + ed.getSource().getId() + directionSymbol + ed.getTarget().getId() + "\nLength    : " + String.format("%.1f", ed.getLength()) + "\nSpeed Mult: x" + String.format("%.1f", ed.getSpeedModifier()) + "\nCapacity  : " + ed.getCapacity() + "\nStatus    : " + ed.getState());
            logArea.setText("History logs are unavailable for routing edges.");
        } else {
            infoLabel.setText("Click on an entity\nto view its details.\n \n \n \n ");
            logArea.setText("");
        }

        boolean nodeSelected = (selected instanceof Node);
        boolean edgeSelected = (selected instanceof Edge);
        boolean agentSelected = (selected instanceof Agent);

        if (nodeSelected) {
            lblNodeCap.setText("Capacity: " + ((Node) selected).getCapacity());
            chkUnderConstruction.setSelected(((Node) selected).isUnderConstruction());
            chkUnderConstruction.setDisable(false);
        } else {
            lblNodeCap.setText("Capacity: " + nodeCapacity);
            chkUnderConstruction.setSelected(false);
            chkUnderConstruction.setDisable(true);
        }

        if (edgeSelected) {
            lblEdgeCap.setText("Capacity: " + ((Edge) selected).getCapacity());
            lblEdgeSpeed.setText(String.format("Speed: x%.1f", ((Edge) selected).getSpeedModifier()));
        } else {
            lblEdgeCap.setText("Capacity: " + edgeCapacity);
            lblEdgeSpeed.setText("Speed: x1.0");
        }

        btnRemoveNode.setDisable(!nodeSelected);
        btnAddAgent.setDisable(!nodeSelected);
        btnRemoveEdge.setDisable(!edgeSelected);
        btnRemoveAgent.setDisable(!agentSelected);

        if (!assigningObjective) {
            btnAssignObjective.setDisable(!agentSelected);
        }

        if (linkingActive) {
            btnAddEdge.setText("↩ Cancel Linking");
            btnAddEdge.setStyle(buttonStyle("#F57C00"));
        } else {
            btnAddEdge.setText("🔗 Add Edge Connection");
            btnAddEdge.setStyle(buttonStyle("#7B1FA2"));
        }

        if (assigningObjective) {
            btnAssignObjective.setText("↩ Cancel Objective");
            btnAssignObjective.setStyle(buttonStyle("#F57C00"));
        } else {
            btnAssignObjective.setText("🎯 Assign Navigation Target");
            btnAssignObjective.setStyle(buttonStyle("#FBC02D"));
        }

        updateGlobalScoreboard();
    }

    /**
     * Aggregates cluster runtime data metrics grouped by analytical routing algorithms.
     */
    private void updateGlobalScoreboard() {
        int[] counts = new int[3];
        int[] objs = new int[3];
        int[] abds = new int[3];
        double[] act = new double[3];
        double[] wait = new double[3];

        for (Agent ag : agents) {
            int idx = 2;
            if (ag.getAlgoType() == Agent.AlgoType.DIJKSTRA) idx = 0;
            else if (ag.getAlgoType() == Agent.AlgoType.ASTAR) idx = 1;

            counts[idx]++;
            objs[idx] += ag.getObjectivesReached();
            abds[idx] += ag.getAbandonedObjectives();
            act[idx] += ag.getTotalActiveTime();
            wait[idx] += ag.getTotalWaitTime();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" GLOBAL SCOREBOARD \n========================\n\n");

        String[] teamNames = { " TEAM DIJKSTRA ROUTING", " TEAM A-STAR OPTIMIZED", " TEAM RANDOM NAVIGATOR" };
        for (int i = 0; i < 3; i++) {
            if (counts[i] == 0) continue;
            double avgEff = (act[i] > 0) ? ((act[i] - wait[i]) / act[i]) * 100.0 : 100.0;
            sb.append(teamNames[i]).append("\n  Active Agents: ").append(counts[i])
              .append("\n  Goals Reached: ").append(objs[i])
              .append("\n  Goals Missed : ").append(abds[i])
              .append("\n  Net Efficiency: ").append(String.format("%.1f%%", avgEff))
              .append("\n  Total Delay  : ").append(String.format("%.1fs", wait[i])).append("\n\n");
        }
        if (agents.isEmpty()) { 
            sb.append("No active agents tracked\non spatial grid maps."); 
        }
        
        String newText = sb.toString();
        if (!globalStatsArea.getText().equals(newText)) { 
            globalStatsArea.setText(newText); 
        }
    }

    private void redrawCanvas() {
        if (selectionSystem != null && selectionSystem.getCanvas() != null) { selectionSystem.getCanvas().draw(); }
    }

    private void handleAddNode() {
        if (onAddNode != null) onAddNode.run();
        else {
            if (selectionSystem != null && selectionSystem.hasPendingPosition()) {
                graph.addNode((int) selectionSystem.getPendingNodeX(), (int) selectionSystem.getPendingNodeY(), nodeCapacity);
            } else { graph.addNode(400, 300, nodeCapacity); }
        }
        btnAddNode.setText("➕ Add Node Here");
        btnAddNode.setDisable(true);
        if (selectionSystem != null) selectionSystem.clearPendingPosition();
    }

    private void handleRemoveNode() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null) return;
        if (onRemoveNode != null) onRemoveNode.run();
        else graph.removeNode(sel);
    }

    private void handleAddEdge() {
        if (selectionSystem == null) return;
        if (linkingActive) {
            selectionSystem.cancelEdgeLinking();
            linkingActive = false;
        } else {
            linkingActive = true;
            final int cap = edgeCapacity;
            final boolean dir = edgeDirection;
            selectionSystem.startEdgeLinking((source, target) -> {
                graph.addEdge(source, target, cap, dir);
                linkingActive = false;
            });
        }
    }

    private void handleRemoveEdge() {
        Edge sel = selectionSystem != null ? selectionSystem.getLastSelectedEdge() : null;
        if (sel == null) return;
        if (onRemoveEdge != null) onRemoveEdge.run();
        else removeEdgeFromGraph(sel);
    }

    private void removeEdgeFromGraph(Edge edge) {
        for (List<Edge> list : graph.getEdges()) list.removeIf(e -> e.getId() == edge.getId());
    }

    private void handleAddAgent() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null) return;
        if (onAddAgent != null) onAddAgent.run();
    }

    private void handleRemoveAgent() {
        Agent sel = selectionSystem != null ? selectionSystem.getLastSelectedAgent() : null;
        if (sel == null) return;
        if (onRemoveAgent != null) onRemoveAgent.run();
    }

    private void handleAssignObjective() {
        if (selectionSystem == null) return;
        if (assigningObjective) {
            selectionSystem.cancelAssignObjective();
            assigningObjective = false;
            return;
        }
        Agent sel = selectionSystem.getLastSelectedAgent();
        if (sel == null) return;
        assigningObjective = true;
        if (onAssignObjective != null) onAssignObjective.run();
    }

    public void objectiveAssignedDone() { assigningObjective = false; }

    private Object findSelectedItem() {
        for (Agent a : agents) if (a.isSelected()) return a;
        for (Node n : graph.getNodes()) if (n.isSelected()) return n;
        for (List<Edge> edges : graph.getEdges()) for (Edge e : edges) if (e.isSelected()) return e;
        return null;
    }

    public Node getSelectedNode() { for (Node n : graph.getNodes()) if (n.isSelected()) return n; return null; }
    public Agent getSelectedAgent() { for (Agent a : agents) if (a.isSelected()) return a; return null; }
    public Edge getSelectedEdge() { for (List<Edge> edges : graph.getEdges()) for (Edge e : edges) if (e.isSelected()) return e; return null; }
    public int getNodeCapacity() { return nodeCapacity; }
    public int getGenGridSide() { return genGridSide; }
    public int getGenAgentCount() { return genAgentCount; }

    private Button buildButton(String text, String hexColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(buttonStyle(hexColor));
        return btn;
    }

    private Button smallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 2 8 2 8; -fx-background-color: #3E3E42; -fx-text-fill: #E0E0E0; -fx-background-radius: 4; -fx-cursor: hand;");
        return btn;
    }

    private String buttonStyle(String hexColor) {
        return "-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 7 10 7 10; -fx-background-radius: 4; -fx-cursor: hand;";
    }
}