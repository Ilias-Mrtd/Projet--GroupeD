package UI;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.graph.*;
import model.agents.Agent;
import controllers.SelectionSystem;

import java.util.List;

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

    private final ComboBox<Agent.agentBehavior> cbAgentBehavior;

    // ---- paramètres arête ----
    private int edgeCapacity = 1;
    private boolean edgeDirection = true;
    private final Label lblEdgeCap;
    private final Label lblEdgeSpeed;

    // ---- paramètres nœud ----
    private int nodeCapacity = 1;
    private final Label lblNodeCap;
    private final CheckBox chkUnderConstruction;

    // ---- paramètres génération de masse ----
    private int genGridSide = 4;
    private int genAgentCount = 10;
    private final Label lblGenNodes;
    private final Label lblGenAgents;

    private boolean linkingActive = false;

    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(260);
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        titleLabel = new Label("Inspecteur");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        TabPane inspectorTabs = new TabPane();
        inspectorTabs.setPrefHeight(300);

        Tab tabDetails = new Tab("Détails");
        tabDetails.setClosable(false);
        infoLabel = new Label("Cliquez sur un élément\npour voir ses détails.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555; -fx-padding: 5px;");
        infoLabel.setWrapText(true);
        ScrollPane detailsScroll = new ScrollPane(infoLabel);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle("-fx-background-color: transparent; -fx-background: #FAFAFA;");
        tabDetails.setContent(detailsScroll);

        Tab tabHistory = new Tab("Historique");
        tabHistory.setClosable(false);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle("-fx-font-size: 11px; -fx-font-family: monospace;");
        logArea.setWrapText(true);
        tabHistory.setContent(logArea);

        Tab tabGlobal = new Tab("Scoreboard");
        tabGlobal.setClosable(false);
        globalStatsArea = new TextArea();
        globalStatsArea.setEditable(false);
        globalStatsArea.setStyle("-fx-font-size: 12px; -fx-font-family: monospace; -fx-control-inner-background: #2b2b2b; -fx-text-fill: #a9b7c6;");
        globalStatsArea.setWrapText(true);
        tabGlobal.setContent(globalStatsArea);

        inspectorTabs.getTabs().addAll(tabDetails, tabHistory, tabGlobal);

        Separator sep1 = new Separator();

        // ---- Section nœud ----
        Label lblNodeSection = new Label("Nœud");
        lblNodeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        lblNodeCap = new Label("Capacité : 1");
        lblNodeCap.setStyle("-fx-font-size: 12px;");
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

        chkUnderConstruction = new CheckBox("En travaux (Fermé)");
        chkUnderConstruction.setDisable(true);
        chkUnderConstruction.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) sel.setUnderConstruction(chkUnderConstruction.isSelected());
            refresh(); redrawCanvas();
        });

        btnAddNode = buildButton("➕  Ajouter un nœud ici", "#2196F3");
        btnAddNode.setDisable(true);
        btnRemoveNode = buildButton("🗑  Supprimer le nœud", "#F44336");
        btnRemoveNode.setDisable(true);
        btnAddNode.setOnAction(e -> handleAddNode());
        btnRemoveNode.setOnAction(e -> handleRemoveNode());

        Separator sep2 = new Separator();

        // ---- Section arête ----
        Label lblEdgeSection = new Label("Arête");
        lblEdgeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        lblEdgeCap = new Label("Capacité : 1");
        lblEdgeCap.setStyle("-fx-font-size: 12px;");
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

        lblEdgeSpeed = new Label("Vitesse : x1.0");
        lblEdgeSpeed.setStyle("-fx-font-size: 12px;");
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

        Button btnToggleDir = buildButton("Bidirected ⇄", "#607D8B");
        btnToggleDir.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { sel.setDirection(!sel.hasDirection()); } else { edgeDirection = !edgeDirection; }
            refresh(); redrawCanvas();
            if (edgeDirection) { btnToggleDir.setText("Bidirected ⇄"); } else { btnToggleDir.setText("Directed →"); }
        });

        btnAddEdge = buildButton("🔗  Ajouter une arête", "#9C27B0");
        btnRemoveEdge = buildButton("🗑  Supprimer l'arête", "#E91E63");
        btnRemoveEdge.setDisable(true);
        btnAddEdge.setOnAction(e -> handleAddEdge());
        btnRemoveEdge.setOnAction(e -> handleRemoveEdge());

        Separator sep3 = new Separator();

        // ---- Section agent ----
        Label lblAgentSection = new Label("Agent");
        lblAgentSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        // LISTE DÉROULANTE VIP / PATIENT / HURRIED
        cbAgentBehavior = new ComboBox<>();
        cbAgentBehavior.getItems().addAll(Agent.agentBehavior.PATIENT, Agent.agentBehavior.HURRIED, Agent.agentBehavior.VIP);
        cbAgentBehavior.setValue(Agent.agentBehavior.PATIENT);
        cbAgentBehavior.setStyle("-fx-font-size: 12px;");
        cbAgentBehavior.setMaxWidth(Double.MAX_VALUE);

        btnAddAgent = buildButton("🤖  Ajouter un agent", "#4CAF50");
        btnAddAgent.setDisable(true);
        btnAddAgent.setOnAction(e -> handleAddAgent());

        btnRemoveAgent = buildButton("🗑  Supprimer l'agent", "#FF5722");
        btnRemoveAgent.setDisable(true);
        btnRemoveAgent.setOnAction(e -> handleRemoveAgent());

        Separator sep4 = new Separator();

        // ---- Section génération de masse ----
        Label lblGenSection = new Label("Génération de masse");
        lblGenSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        lblGenNodes = new Label("Grille : " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " nœuds)");
        lblGenNodes.setStyle("-fx-font-size: 12px;");
        Button btnGenNodesMinus = smallButton("−");
        Button btnGenNodesPlus = smallButton("+");
        btnGenNodesMinus.setOnAction(e -> { if (genGridSide > 2) { genGridSide--; lblGenNodes.setText("Grille : " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " nœuds)"); } });
        btnGenNodesPlus.setOnAction(e -> { if (genGridSide < 12) { genGridSide++; lblGenNodes.setText("Grille : " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " nœuds)"); } });
        HBox genNodesBox = new HBox(6, btnGenNodesMinus, lblGenNodes, btnGenNodesPlus);
        genNodesBox.setStyle("-fx-alignment: center-left;");

        Button btnGenerateGraph = buildButton("🏙  Générer un graphe", "#3F51B5");
        btnGenerateGraph.setOnAction(e -> { if (onGenerateGraph != null) onGenerateGraph.run(); });

        lblGenAgents = new Label("Agents : " + genAgentCount);
        lblGenAgents.setStyle("-fx-font-size: 12px;");
        Button btnGenAgentsMinus = smallButton("−");
        Button btnGenAgentsPlus = smallButton("+");
        btnGenAgentsMinus.setOnAction(e -> { if (genAgentCount > 1) { genAgentCount--; lblGenAgents.setText("Agents : " + genAgentCount); } });
        btnGenAgentsPlus.setOnAction(e -> { if (genAgentCount < 100) { genAgentCount++; lblGenAgents.setText("Agents : " + genAgentCount); } });
        HBox genAgentsBox = new HBox(6, btnGenAgentsMinus, lblGenAgents, btnGenAgentsPlus);
        genAgentsBox.setStyle("-fx-alignment: center-left;");

        Button btnSpawnAgents = buildButton("👥  Faire apparaître les agents", "#009688");
        btnSpawnAgents.setOnAction(e -> { if (onSpawnAgents != null) onSpawnAgents.run(); });

        getChildren().addAll(
                titleLabel, inspectorTabs,
                sep1, lblNodeSection, nodeCapBox, chkUnderConstruction, btnAddNode, btnRemoveNode,
                sep2, lblEdgeSection, edgeCapBox, edgeSpeedBox, btnToggleDir, btnAddEdge, btnRemoveEdge,
                sep3, lblAgentSection, cbAgentBehavior, btnAddAgent, btnRemoveAgent, // AJOUTÉ ICI
                sep4, lblGenSection, genNodesBox, btnGenerateGraph, genAgentsBox, btnSpawnAgents);
    }

    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
        ss.setOnEmptyClick((x, y) -> {
            btnAddNode.setDisable(false);
            btnAddNode.setText("➕  Placer ici (" + (int) x + "," + (int) y + ")");
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
    

    public Agent.agentBehavior getSelectedAgentBehavior() {
        return cbAgentBehavior.getValue();
    }

    public void refresh() {
        Object selected = findSelectedItem();

        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            double avgSpeed = 0.0;
            if (a.getTotalActiveTime() > 0) { avgSpeed = (a.getTotalDistance() / a.getTotalActiveTime()) / 60.0; }
            double efficiency = 100.0;
            if (a.getTotalActiveTime() > 0) { efficiency = ((a.getTotalActiveTime() - a.getTotalWaitTime()) / a.getTotalActiveTime()) * 100.0; }

            StringBuilder sb = new StringBuilder();
            sb.append("Type    : Agent [").append(a.getAgentBehavior()).append("]\n")
                    .append("ID      : ").append(a.getId()).append("\n")
                    .append("Algo    : ").append(a.getAlgoType()).append("\n")
                    .append("État    : ").append(a.getState()).append("\n\n")
                    .append("-- KPI & STATISTIQUES --\n")
                    .append("Obj. atteints    : ").append(a.getObjectivesReached()).append("\n")
                    .append("Obj. abandonnés  : ").append(a.getAbandonedObjectives()).append("\n")
                    .append("Détours forcés   : ").append(a.getDetoursTaken()).append("\n")
                    .append("Temps d'activité : ").append(String.format("%.1fs", a.getTotalActiveTime())).append("\n")
                    .append("Temps d'attente  : ").append(String.format("%.1fs", a.getTotalWaitTime())).append("\n")
                    .append("Efficacité trafic: ").append(String.format("%.1f%%", efficiency)).append("\n")
                    .append("Vitesse théorique: ").append(String.format("%.1f", a.getSpeed())).append(" px/s\n")
                    .append("Vitesse Réelle   : ").append(String.format("%.1f", avgSpeed)).append(" px/s\n\n");

            if (a.getCurrentEdge() != null && a.getDestination() != null)
                sb.append("Sur arête : ").append(a.getCurrentEdge().getId()).append("\n").append("Objectif  : ").append(a.getDestination().getId());
            else if (a.getCurrentNode() != null)
                sb.append("Position : nœud ").append(a.getCurrentNode().getId());

            infoLabel.setText(sb.toString());

            String logText = String.join("\n", a.getHistoryLog());
            if (!logArea.getText().equals(logText)) {
                logArea.setText(logText);
                logArea.setScrollTop(Double.MAX_VALUE);
            }

        } else if (selected instanceof Node) {
            Node n = (Node) selected;
            infoLabel.setText("Type     : Nœud\nID       : " + n.getId() + "\nCoord.   : (" + (int) n.getX() + ", " + (int) n.getY() + ")\nTravaux  : " + (n.isUnderConstruction() ? "OUI (Fermé)" : "Non") + "\nCapacité : " + n.getCapacity() + "\nÉtat     : " + n.getState() + "\nOccupants: " + n.getCurrentOccupants() + "/" + n.getCapacity());
            logArea.setText("Historique non disponible pour les noeuds.");
        } else if (selected instanceof Edge) {
            Edge ed = (Edge) selected;
            String dir = ed.hasDirection() ? " --> " : " <--> ";
            infoLabel.setText("Type      : Arête\nID        : " + ed.getId() + "\nConnexion : " + ed.getSource().getId() + dir + ed.getTarget().getId() + "\nLongueur  : " + String.format("%.1f", ed.getLength()) + "\nVitesse   : x" + String.format("%.1f", ed.getSpeedModifier()) + "\nCapacité  : " + ed.getCapacity() + "\nÉtat      : " + ed.getState());
            logArea.setText("Historique non disponible pour les arêtes.");
        } else {
            infoLabel.setText("Cliquez sur un élément\npour voir ses détails.\n \n \n \n ");
            logArea.setText("");
        }

        boolean nodeSelected = (selected instanceof Node);
        boolean edgeSelected = (selected instanceof Edge);
        boolean agentSelected = (selected instanceof Agent);

        if (nodeSelected) {
            lblNodeCap.setText("Capacité : " + ((Node) selected).getCapacity());
            chkUnderConstruction.setSelected(((Node) selected).isUnderConstruction());
            chkUnderConstruction.setDisable(false);
        } else {
            lblNodeCap.setText("Capacité : " + nodeCapacity);
            chkUnderConstruction.setSelected(false);
            chkUnderConstruction.setDisable(true);
        }

        if (edgeSelected) {
            lblEdgeCap.setText("Capacité : " + ((Edge) selected).getCapacity());
            lblEdgeSpeed.setText(String.format("Vitesse : x%.1f", ((Edge) selected).getSpeedModifier()));
        } else {
            lblEdgeCap.setText("Capacité : " + edgeCapacity);
            lblEdgeSpeed.setText("Vitesse : x1.0");
        }

        btnRemoveNode.setDisable(!nodeSelected);
        btnAddAgent.setDisable(!nodeSelected);
        btnRemoveEdge.setDisable(!edgeSelected);
        btnRemoveAgent.setDisable(!agentSelected);

        if (linkingActive) {
            btnAddEdge.setText("↩  Annuler liaison");
            btnAddEdge.setStyle(buttonStyle("#FF9800"));
        } else {
            btnAddEdge.setText("🔗  Ajouter une arête");
            btnAddEdge.setStyle(buttonStyle("#9C27B0"));
        }

        updateGlobalScoreboard();
    }

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
        sb.append(" SCOREBOARD GLOBAL \n========================\n\n");

        String[] names = { " ÉQUIPE DIJKSTRA", " ÉQUIPE A-STAR", " ÉQUIPE ALÉATOIRE" };
        for (int i = 0; i < 3; i++) {
            if (counts[i] == 0) continue; 
            double avgEff = 100.0;
            if (act[i] > 0) avgEff = ((act[i] - wait[i]) / act[i]) * 100.0;
            sb.append(names[i]).append("\n  Agents actifs : ").append(counts[i]).append("\n  Obj. atteints : ").append(objs[i]).append("\n  Obj. ratés    : ").append(abds[i]).append("\n  Efficacité    : ").append(String.format("%.1f%%", avgEff)).append("\n  Attente (total): ").append(String.format("%.1fs", wait[i])).append("\n\n");
        }
        if (agents.isEmpty()) { sb.append("Aucun agent sur la carte\npour le moment."); }
        String newText = sb.toString();
        if (!globalStatsArea.getText().equals(newText)) { globalStatsArea.setText(newText); }
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
        btnAddNode.setText("➕ Ajouter un nœud ici");
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
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private Button smallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 2 8 2 8; -fx-background-radius: 4; -fx-cursor: hand;");
        return btn;
    }

    private String buttonStyle(String hexColor) {
        return "-fx-background-color: " + hexColor + "; -fx-text-fill: white; -fx-font-size: 12px; -fx-padding: 7 10 7 10; -fx-background-radius: 6; -fx-cursor: hand;";
    }
}