package UI;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.graph.*;
import model.agents.Agent;
import controllers.SelectionSystem;

import java.util.List;

/**
 * PropertiesPanel — panneau latéral droit.
 *
 * Améliorations v2 :
 * - Ajouter un nœud : placement à l'endroit du clic dans le vide + champ
 * capacité
 * - Ajouter une arête : choix capacité (+/-) et direction avant liaison
 * - Supprimer une arête : bouton actif quand une arête est sélectionnée
 * - Bug timer corrigé : les callbacks ne touchent JAMAIS au moteur (stop/start)
 */
public class PropertiesPanel extends VBox {

    private final Graph graph;
    private final List<Agent> agents;

    private SelectionSystem selectionSystem;
    private Runnable onAddNode;
    private Runnable onRemoveNode;
    private Runnable onRemoveEdge;
    private Runnable onAddAgent;

    // ---- inspecteur ----
    private final Label titleLabel;
    private final Label infoLabel;

    // ---- boutons principaux ----
    private final Button btnAddNode;
    private final Button btnRemoveNode;
    private final Button btnAddEdge;
    private final Button btnRemoveEdge;
    private final Button btnAddAgent;

    // ---- paramètres arête ----
    private int edgeCapacity = 1;
    private boolean edgeDirection = true; // false = unidirectionnel
    private final Label lblEdgeCap;
    private final Label lblEdgeDir;

    // ---- paramètres nœud ----
    private int nodeCapacity = 1;
    private final Label lblNodeCap;

    private boolean linkingActive = false;

    // ================================================================= ctor

    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(260);
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        // ---- Inspecteur ----
        titleLabel = new Label("Inspecteur");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        infoLabel = new Label("Cliquez sur un élément\npour voir ses détails.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        infoLabel.setWrapText(true);

        // ---- Séparateur ----
        Separator sep1 = new Separator();

        // ---- Section nœud ----
        Label lblNodeSection = new Label("Nœud");
        lblNodeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        // Capacité nœud
        lblNodeCap = new Label("Capacité : 1");
        lblNodeCap.setStyle("-fx-font-size: 12px;");
        Button btnNodeCapMinus = smallButton("−");
        Button btnNodeCapPlus = smallButton("+");
        btnNodeCapMinus.setOnAction(e -> {
            if (nodeCapacity > 1) {
                nodeCapacity--;
                lblNodeCap.setText("Capacité : " + nodeCapacity);
            }
        });
        btnNodeCapPlus.setOnAction(e -> {
            nodeCapacity++;
            lblNodeCap.setText("Capacité : " + nodeCapacity);
        });
        HBox nodeCapBox = new HBox(6, btnNodeCapMinus, lblNodeCap, btnNodeCapPlus);
        nodeCapBox.setStyle("-fx-alignment: center-left;");

        btnAddNode = buildButton("➕  Ajouter un nœud ici", "#2196F3");
        btnAddNode.setDisable(true); // actif seulement après clic dans le vide
        btnRemoveNode = buildButton("🗑  Supprimer le nœud", "#F44336");
        btnRemoveNode.setDisable(true);

        btnAddNode.setOnAction(e -> handleAddNode());
        btnRemoveNode.setOnAction(e -> handleRemoveNode());

        // ---- Séparateur ----
        Separator sep2 = new Separator();

        // ---- Section arête ----
        Label lblEdgeSection = new Label("Arête");
        lblEdgeSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        // Capacité arête
        lblEdgeCap = new Label("Capacité : 1");
        lblEdgeCap.setStyle("-fx-font-size: 12px;");
        Button btnEdgeCapMinus = smallButton("−");
        Button btnEdgeCapPlus = smallButton("+");
        btnEdgeCapMinus.setOnAction(e -> {
            if (edgeCapacity > 1) {
                edgeCapacity--;
                lblEdgeCap.setText("Capacité : " + edgeCapacity);
            }
        });
        btnEdgeCapPlus.setOnAction(e -> {
            edgeCapacity++;
            lblEdgeCap.setText("Capacité : " + edgeCapacity);
        });
        HBox edgeCapBox = new HBox(6, btnEdgeCapMinus, lblEdgeCap, btnEdgeCapPlus);
        edgeCapBox.setStyle("-fx-alignment: center-left;");

        // Direction arête
        lblEdgeDir = new Label("Direction : Bidirect.");
        lblEdgeDir.setStyle("-fx-font-size: 12px;");
        Button btnToggleDir = buildButton("⇄  Changer direction", "#607D8B");
        btnToggleDir.setOnAction(e -> {
            edgeDirection = !edgeDirection;
            lblEdgeDir.setText("Direction : " + (edgeDirection ? "Unidirect. →" : "Bidirect. ⇄"));
        });

        btnAddEdge = buildButton("🔗  Ajouter une arête", "#9C27B0");
        btnRemoveEdge = buildButton("🗑  Supprimer l'arête", "#E91E63");
        btnRemoveEdge.setDisable(true);

        btnAddEdge.setOnAction(e -> handleAddEdge());
        btnRemoveEdge.setOnAction(e -> handleRemoveEdge());

        // ---- Séparateur ----
        Separator sep3 = new Separator();

        // ---- Section agent ----
        Label lblAgentSection = new Label("Agent");
        lblAgentSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #333;");

        btnAddAgent = buildButton("🤖  Ajouter un agent", "#4CAF50");
        btnAddAgent.setDisable(true);
        btnAddAgent.setOnAction(e -> handleAddAgent());

        // ---- Assemblage ----
        getChildren().addAll(
                titleLabel, infoLabel,
                sep1,
                lblNodeSection, nodeCapBox, btnAddNode, btnRemoveNode,
                sep2,
                lblEdgeSection, edgeCapBox, lblEdgeDir, btnToggleDir, btnAddEdge, btnRemoveEdge,
                sep3,
                lblAgentSection, btnAddAgent);
    }

    // ================================================================= setters

    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
        // Quand l'utilisateur clique dans le vide → activer btnAddNode
        ss.setOnEmptyClick((x, y) -> {
            btnAddNode.setDisable(false);
            btnAddNode.setText("➕  Placer ici (" + (int) x + "," + (int) y + ")");
        });
        // Desactiver le bouton addAgent
        btnAddAgent.setDisable(true);
    }

    public void setOnAddNode(Runnable r) {
        this.onAddNode = r;
    }

    public void setOnRemoveNode(Runnable r) {
        this.onRemoveNode = r;
    }

    public void setOnRemoveEdge(Runnable r) {
        this.onRemoveEdge = r;
    }

    public void setOnAddAgent(Runnable r) {
        this.onAddAgent = r;
    }

    // ================================================================= refresh
    // (60×/s)

    public void refresh() {
        Object selected = findSelectedItem();

        // ---- inspecteur ----
        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            StringBuilder sb = new StringBuilder();
            sb.append("Type    : Agent\n")
                    .append("ID      : ").append(a.id).append("\n")
                    .append("État    : ").append(a.state).append("\n")
                    .append("Vitesse : ").append(a.speed).append(" px/s\n");
            if (a.currentEdge != null && a.destination != null)
                sb.append("Trajet  : ").append(a.currentNode.id).append(" ➔ ").append(a.destination.id).append("\n")
                        .append("Sur arête : ").append(a.currentEdge.id);
            else if (a.currentNode != null)
                sb.append("Position : nœud ").append(a.currentNode.id + "\n ");
            infoLabel.setText(sb.toString());

        } else if (selected instanceof Node) {
            Node n = (Node) selected;
            infoLabel.setText(
                    "Type     : Nœud\n"
                            + "ID       : " + n.id + "\n"
                            + "Position : (" + (int) n.x + ", " + (int) n.y + ")\n"
                            + "Capacité : " + n.capacity + "\n"
                            + "État     : " + n.state + "\n"
                            + "Occupants: " + n.currentOccupants + "/" + n.capacity);

        } else if (selected instanceof Edge) {
            Edge ed = (Edge) selected;
            String dir = ed.direction ? " --> " : " <--> ";
            infoLabel.setText(
                    "Type      : Arête\n"
                            + "ID        : " + ed.id + "\n"
                            + "Connexion : " + ed.source.id + dir + ed.target.id + "\n"
                            + "Capacité  : " + ed.capacity + "\n"
                            + "Direction : " + (ed.direction ? "unidirect." : "bidirect.") + "\n"
                            + "État      : " + ed.state);

        } else {
            infoLabel.setText("Cliquez sur un élément\npour voir ses détails.\n \n \n \n ");
        }

        // ---- état des boutons ----
        boolean nodeSelected = (selected instanceof Node);
        boolean edgeSelected = (selected instanceof Edge);

        btnRemoveNode.setDisable(!nodeSelected);
        btnAddAgent.setDisable(!nodeSelected);
        btnRemoveEdge.setDisable(!edgeSelected);

        // bouton liaison arête
        if (linkingActive) {
            btnAddEdge.setText("↩  Annuler liaison");
            btnAddEdge.setStyle(buttonStyle("#FF9800"));
        } else {
            btnAddEdge.setText("🔗  Ajouter une arête");
            btnAddEdge.setStyle(buttonStyle("#9C27B0"));
        }
    }

    // ================================================================= handlers

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
        // Reset bouton après utilisation
        btnAddNode.setText("➕ Ajouter un nœud ici");
        btnAddNode.setDisable(true);
        if (selectionSystem != null)
            selectionSystem.clearPendingPosition();
    }

    private void handleRemoveNode() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null)
            return;
        if (onRemoveNode != null)
            onRemoveNode.run();
        else
            graph.removeNode(sel);
    }

    private void handleAddEdge() {
        if (selectionSystem == null)
            return;
        if (linkingActive) {
            selectionSystem.cancelEdgeLinking();
            linkingActive = false;
        } else {
            linkingActive = true;
            // Capture des paramètres au moment du clic (pas au moment de la création)
            final int cap = edgeCapacity;
            final boolean dir = edgeDirection;
            selectionSystem.startEdgeLinking((source, target) -> {
                graph.addEdge(source, target, cap, dir);
                linkingActive = false;
                System.out.println("[PropertiesPanel] Arête créée : " + source.id
                        + (dir ? " → " : " ⇄ ") + target.id + " cap=" + cap);
            });
        }
    }

    private void handleRemoveEdge() {
        Edge sel = selectionSystem != null ? selectionSystem.getLastSelectedEdge() : null;
        if (sel == null)
            return;
        if (onRemoveEdge != null)
            onRemoveEdge.run();
        else
            removeEdgeFromGraph(sel);
    }

    private void removeEdgeFromGraph(Edge edge) {
        for (List<Edge> list : graph.Edges) {
            list.removeIf(e -> e.id == edge.id);
        }
        System.out.println("[PropertiesPanel] Arête " + edge.id + " supprimée.");
    }

    private void handleAddAgent() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null)
            return;
        if (onAddAgent != null)
            onAddAgent.run();
    }

    // ================================================================= helpers

    private Object findSelectedItem() {
        for (Agent a : agents)
            if (a.isSelected)
                return a;
        for (Node n : graph.Nodes)
            if (n.isSelected)
                return n;
        for (List<Edge> edges : graph.Edges)
            for (Edge e : edges)
                if (e.isSelected)
                    return e;
        return null;
    }

    public Node getSelectedNode() {
        for (Node n : graph.Nodes)
            if (n.isSelected)
                return n;
        return null;
    }

    public Edge getSelectedEdge() {
        for (List<Edge> edges : graph.Edges)
            for (Edge e : edges)
                if (e.isSelected)
                    return e;
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