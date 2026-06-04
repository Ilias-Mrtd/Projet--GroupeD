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

    private final Label titleLabel;
    private final Label infoLabel;

    private final Button btnAddNode;
    private final Button btnRemoveNode;
    private final Button btnAddEdge;
    private final Button btnRemoveEdge;
    private final Button btnAddAgent;

    // ---- paramètres arête ----
    private int edgeCapacity = 1;
    private boolean edgeDirection = true;
    private final Label lblEdgeCap;
    private final Label lblEdgeDir;
    private final Label lblEdgeSpeed; // NOUVEAU

    // ---- paramètres nœud ----
    private int nodeCapacity = 1;
    private final Label lblNodeCap;
    private final CheckBox chkUnderConstruction; // NOUVEAU

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

        infoLabel = new Label("Cliquez sur un élément\npour voir ses détails.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        infoLabel.setWrapText(true);

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
            if (sel != null) { if (sel.getCapacity() > 1) sel.setCapacity(sel.getCapacity() - 1); } 
            else if (nodeCapacity > 1) { nodeCapacity--; }
            refresh(); redrawCanvas();
        });
        btnNodeCapPlus.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) { sel.setCapacity(sel.getCapacity() + 1); } else { nodeCapacity++; }
            refresh(); redrawCanvas();
        });
        HBox nodeCapBox = new HBox(6, btnNodeCapMinus, lblNodeCap, btnNodeCapPlus);
        nodeCapBox.setStyle("-fx-alignment: center-left;");

        // NOUVEAU : Checkbox Travaux
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
            if (sel != null) { if (sel.getCapacity() > 1) sel.setCapacity(sel.getCapacity() - 1); } 
            else if (edgeCapacity > 1) { edgeCapacity--; }
            refresh(); redrawCanvas();
        });
        btnEdgeCapPlus.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { sel.setCapacity(sel.getCapacity() + 1); } else { edgeCapacity++; }
            refresh(); redrawCanvas();
        });
        HBox edgeCapBox = new HBox(6, btnEdgeCapMinus, lblEdgeCap, btnEdgeCapPlus);
        edgeCapBox.setStyle("-fx-alignment: center-left;");

        // NOUVEAU : Modificateur Vitesse Arete
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

        lblEdgeDir = new Label("Direction : Bidirect.");
        lblEdgeDir.setStyle("-fx-font-size: 12px;");
        Button btnToggleDir = buildButton("⇄  Changer direction", "#607D8B");
        btnToggleDir.setOnAction(e -> {
            Edge sel = getSelectedEdge();
            if (sel != null) { sel.setDirection(!sel.isDirection()); } else { edgeDirection = !edgeDirection; }
            refresh(); redrawCanvas();
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

        btnAddAgent = buildButton("🤖  Ajouter un agent", "#4CAF50");
        btnAddAgent.setDisable(true);
        btnAddAgent.setOnAction(e -> handleAddAgent());

        getChildren().addAll(
                titleLabel, infoLabel,
                sep1,
                lblNodeSection, nodeCapBox, chkUnderConstruction, btnAddNode, btnRemoveNode,
                sep2,
                lblEdgeSection, edgeCapBox, edgeSpeedBox, lblEdgeDir, btnToggleDir, btnAddEdge, btnRemoveEdge,
                sep3,
                lblAgentSection, btnAddAgent);
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

    public void refresh() {
        Object selected = findSelectedItem();

        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            StringBuilder sb = new StringBuilder();
            sb.append("Type    : Agent\n")
                    .append("ID      : ").append(a.getId()).append("\n")
                    .append("État    : ").append(a.getState()).append("\n")
                    .append("Vitesse : ").append(a.getSpeed()).append(" px/s\n");
            if (a.getCurrentEdge() != null && a.getDestination() != null)
                sb.append("Trajet  : ").append(a.getCurrentNode().getId()).append(" ➔ ")
                        .append(a.getDestination().getId()).append("\n")
                        .append("Sur arête : ").append(a.getCurrentEdge().getId());
            else if (a.getCurrentNode() != null)
                sb.append("Position : nœud ").append(a.getCurrentNode().getId() + "\nBehavior : " + a.getAgentBehavior());
            infoLabel.setText(sb.toString());

        } else if (selected instanceof Node) {
            Node n = (Node) selected;
            infoLabel.setText(
                    "Type     : Nœud\n"
                            + "ID       : " + n.getId() + "\n"
                            + "Travaux  : " + (n.isUnderConstruction() ? "OUI (Fermé)" : "Non") + "\n"
                            + "Capacité : " + n.getCapacity() + "\n"
                            + "État     : " + n.getState() + "\n"
                            + "Occupants: " + n.getCurrentOccupants() + "/" + n.getCapacity());

        } else if (selected instanceof Edge) {
            Edge ed = (Edge) selected;
            String dir = ed.isDirection() ? " --> " : " <--> ";
            infoLabel.setText(
                    "Type      : Arête\n"
                            + "ID        : " + ed.getId() + "\n"
                            + "Connexion : " + ed.getSource().getId() + dir + ed.getTarget().getId() + "\n"
                            + "Vitesse   : x" + String.format("%.1f", ed.getSpeedModifier()) + "\n"
                            + "Capacité  : " + ed.getCapacity() + "\n"
                            + "État      : " + ed.getState());
        } else {
            infoLabel.setText("Cliquez sur un élément\npour voir ses détails.\n \n \n \n ");
        }

        boolean nodeSelected = (selected instanceof Node);
        boolean edgeSelected = (selected instanceof Edge);

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
            lblEdgeDir.setText("Direction : " + (((Edge) selected).isDirection() ? "Unidirect. →" : "Bidirect. ⇄"));
            lblEdgeSpeed.setText(String.format("Vitesse : x%.1f", ((Edge) selected).getSpeedModifier()));
        } else {
            lblEdgeCap.setText("Capacité : " + edgeCapacity);
            lblEdgeDir.setText("Direction : " + (edgeDirection ? "Unidirect. →" : "Bidirect. ⇄"));
            lblEdgeSpeed.setText("Vitesse : x1.0");
        }

        btnRemoveNode.setDisable(!nodeSelected);
        btnAddAgent.setDisable(!nodeSelected);
        btnRemoveEdge.setDisable(!edgeSelected);

        if (linkingActive) {
            btnAddEdge.setText("↩  Annuler liaison");
            btnAddEdge.setStyle(buttonStyle("#FF9800"));
        } else {
            btnAddEdge.setText("🔗  Ajouter une arête");
            btnAddEdge.setStyle(buttonStyle("#9C27B0"));
        }
    }

    private void redrawCanvas() {
        if (selectionSystem != null && selectionSystem.getCanvas() != null) {
            selectionSystem.getCanvas().draw();
        }
    }

    private void handleAddNode() {
        if (onAddNode != null)
            onAddNode.run();
        else {
            if (selectionSystem != null && selectionSystem.hasPendingPosition()) {
                graph.addNode((int) selectionSystem.getPendingNodeX(),
                        (int) selectionSystem.getPendingNodeY(), nodeCapacity);
            } else {
                graph.addNode(400, 300, nodeCapacity);
            }
        }
        btnAddNode.setText("➕ Ajouter un nœud ici");
        btnAddNode.setDisable(true);
        if (selectionSystem != null)
            selectionSystem.clearPendingPosition();
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
        for (List<Edge> list : graph.getEdges()) {
            list.removeIf(e -> e.getId() == edge.getId());
        }
    }

    private void handleAddAgent() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null) return;
        if (onAddAgent != null) onAddAgent.run();
    }

    private Object findSelectedItem() {
        for (Agent a : agents) if (a.isSelected()) return a;
        for (Node n : graph.getNodes()) if (n.isSelected()) return n;
        for (List<Edge> edges : graph.getEdges())
            for (Edge e : edges) if (e.isSelected()) return e;
        return null;
    }

    public Node getSelectedNode() {
        for (Node n : graph.getNodes()) if (n.isSelected()) return n;
        return null;
    }

    public Edge getSelectedEdge() {
        for (List<Edge> edges : graph.getEdges())
            for (Edge e : edges) if (e.isSelected()) return e;
        return null;
    }

    public int getNodeCapacity() {
        return nodeCapacity;
    }

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
        return "-fx-background-color: " + hexColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 12px;"
                + "-fx-padding: 7 10 7 10;"
                + "-fx-background-radius: 6;"
                + "-fx-cursor: hand;";
    }
}