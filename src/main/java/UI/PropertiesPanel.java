package UI;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import model.graph.*;
import model.agents.Agent;
import controllers.SelectionSystem;

import java.util.List;

public class PropertiesPanel extends VBox {

    private Graph graph;
    private List<Agent> agents;

    private SelectionSystem selectionSystem;
    private Runnable onAddNode;
    private Runnable onRemoveNode;
    private Runnable onAddAgent;

    private final Label titleLabel;
    private final Label infoLabel;

    private final Button btnAddNode;
    private final Button btnRemoveNode;
    private final Button btnAddEdge;
    private final Button btnAddAgent;

    private boolean linkingActive = false;

    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        setPadding(new Insets(15));
        setSpacing(12);
        setPrefWidth(250);
        setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        titleLabel = new Label("Inspecteur");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        infoLabel = new Label("Cliquez sur un élément\npour voir ses détails.");
        infoLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #555;");
        infoLabel.setWrapText(true);

        Separator sep = new Separator();
        sep.setPadding(new Insets(4, 0, 4, 0));

        Label editTitle = new Label("Édition du graphe");
        editTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #333;");

        btnAddNode = buildButton("➕  Ajouter un nœud", "#2196F3");
        btnRemoveNode = buildButton("🗑  Supprimer le nœud", "#F44336");
        btnAddEdge = buildButton("🔗  Ajouter une arête", "#9C27B0");
        btnAddAgent = buildButton("🤖  Ajouter un agent", "#4CAF50");

        btnRemoveNode.setDisable(true);
        btnAddAgent.setDisable(true);

        btnAddNode.setOnAction(e -> handleAddNode());
        btnRemoveNode.setOnAction(e -> handleRemoveNode());
        btnAddEdge.setOnAction(e -> handleAddEdge());
        btnAddAgent.setOnAction(e -> handleAddAgent());

        getChildren().addAll(
                titleLabel, infoLabel,
                sep,
                editTitle,
                btnAddNode, btnRemoveNode, btnAddEdge, btnAddAgent);
    }

    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
    }

    public void setOnAddNode(Runnable r) {
        this.onAddNode = r;
    }

    public void setOnRemoveNode(Runnable r) {
        this.onRemoveNode = r;
    }

    public void setOnAddAgent(Runnable r) {
        this.onAddAgent = r;
    }

    public void refresh() {
        Object selected = findSelectedItem();

        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            StringBuilder sb = new StringBuilder();
            sb.append("Type    : Agent\n");
            sb.append("ID      : ").append(a.id).append("\n");
            sb.append("État    : ").append(a.state).append("\n");
            sb.append("Vitesse : ").append(a.speed).append(" px/s\n");
            if (a.currentEdge != null && a.destination != null) {
                sb.append("Trajet  : ").append(a.currentNode.id)
                        .append(" ➔ ").append(a.destination.id).append("\n");
                sb.append("Sur arête : ").append(a.currentEdge.id);
            } else if (a.currentNode != null) {
                sb.append("Position : nœud ").append(a.currentNode.id + "\n ");
            }
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
            // On garde l'info de direction de ton coéquipier + les détails complets
            String dir = ed.direction ? " --> " : " <--> ";
            infoLabel.setText(
                    "Type      : Arête\n"
                            + "ID        : " + ed.id + "\n"
                            + "Connexion : " + ed.source.id + dir + ed.target.id + "\n"
                            + "Longueur  : " + (int) ed.length + " px\n"
                            + "Direction : " + (ed.direction ? "unidirect." : "bidirect.") + "\n"
                            + "État      : " + ed.state);

        } else {
            infoLabel.setText("Cliquez sur un élément\npour voir ses détails.\n \n \n \n ");
        }

        boolean nodeSelected = (selected instanceof Node);
        btnRemoveNode.setDisable(!nodeSelected);
        btnAddAgent.setDisable(!nodeSelected);

        if (linkingActive) {
            btnAddEdge.setText("↩  Annuler liaison");
            btnAddEdge.setStyle(buttonStyle("#FF9800"));
        } else {
            btnAddEdge.setText("🔗  Ajouter une arête");
            btnAddEdge.setStyle(buttonStyle("#9C27B0"));
        }
    }

    private void handleAddNode() {
        if (onAddNode != null)
            onAddNode.run();
        else
            graph.addNode(400, 300, 1);
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
            selectionSystem.startEdgeLinking((source, target) -> {
                graph.addEdge(source, target, 1, false);
                linkingActive = false;
                System.out.println("[PropertiesPanel] Arête créée entre " + source.id + " et " + target.id);
            });
        }
    }

    private void handleAddAgent() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null)
            return;
        if (onAddAgent != null)
            onAddAgent.run();
    }

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

    private Button buildButton(String text, String hexColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(buttonStyle(hexColor));
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
    }

    private String buttonStyle(String hexColor) {
        return "-fx-background-color: " + hexColor + ";"
                + "-fx-text-fill: white;"
                + "-fx-font-size: 13px;"
                + "-fx-padding: 8 12 8 12;"
                + "-fx-background-radius: 6;"
                + "-fx-cursor: hand;";
    }
}