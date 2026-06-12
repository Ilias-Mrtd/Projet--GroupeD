package UI.panel;

import UI.utils.UIComponents;
import controllers.SelectionSystem;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.graph.Graph;
import model.graph.Node;

/**
 * Sub-panel layout container coordinating configuration metrics, status modifications,
 * and capacity allocations for graph network Node entities.
 */
public class NodeSettingsPanel extends VBox {

    private final Graph graph;
    private SelectionSystem selectionSystem;
    private final Runnable refreshCallback;
    private final Runnable redrawCallback;
    private final Runnable onAddNode;
    private final Runnable onRemoveNode;

    private final Label lblNodeCap;
    private final CheckBox chkUnderConstruction;
    private final Button btnAddNode;
    private final Button btnRemoveNode;
    private int defaultNodeCapacity = 1;

    /**
     * Constructs the node controller layout sub-section mapping functional listeners.
     */
    public NodeSettingsPanel(Graph graph, SelectionSystem selectionSystem, Runnable refreshCallback, 
                             Runnable redrawCallback, Runnable onAddNode, Runnable onRemoveNode) {
        this.graph = graph;
        this.selectionSystem = selectionSystem;
        this.refreshCallback = refreshCallback;
        this.redrawCallback = redrawCallback;
        this.onAddNode = onAddNode;
        this.onRemoveNode = onRemoveNode;

        setSpacing(10);

        Label lblSection = new Label("Node Settings");
        lblSection.setStyle(UIComponents.SECTION_TITLE_STYLE);

        lblNodeCap = new Label("Capacity: 1");
        lblNodeCap.setStyle(UIComponents.BASE_LABEL_STYLE);

        Button btnMinus = UIComponents.createSmallButton("−");
        Button btnPlus = UIComponents.createSmallButton("+");

        btnMinus.setOnAction(e -> handleCapacityAdjustment(-1));
        btnPlus.setOnAction(e -> handleCapacityAdjustment(1));

        HBox capBox = new HBox(6, btnMinus, lblNodeCap, btnPlus);
        capBox.setStyle("-fx-alignment: center-left;");

        chkUnderConstruction = new CheckBox("Under Construction (Closed)");
        chkUnderConstruction.setStyle(UIComponents.CHKBOX_STYLE);
        chkUnderConstruction.setDisable(true);
        chkUnderConstruction.setOnAction(e -> {
            Node sel = getSelectedNode();
            if (sel != null) {
                sel.setUnderConstruction(chkUnderConstruction.isSelected());
            }
            refreshCallback.run();
            redrawCallback.run();
        });

        btnAddNode = UIComponents.buildButton("➕ Add Node Here", "#0288D1");
        btnAddNode.setDisable(true);
        btnRemoveNode = UIComponents.buildButton("🗑 Remove Node", "#D32F2F");
        btnRemoveNode.setDisable(true);

        btnAddNode.setOnAction(e -> handleAddNodeAction());
        btnRemoveNode.setOnAction(e -> handleRemoveNodeAction());

        getChildren().addAll(lblSection, capBox, chkUnderConstruction, btnAddNode, btnRemoveNode);
    }

    /**
     * Sets the delayed contextual selection system reference token.
     *
     * @param selectionSystem the central interactive selector instance
     */
    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;
    }

    private void handleCapacityAdjustment(int delta) {
        Node sel = getSelectedNode();
        if (sel != null) {
            int currentCap = sel.getCapacity();
            if (delta > 0 || currentCap > 1) {
                sel.setCapacity(currentCap + delta);
            }
        } else if (delta > 0 || defaultNodeCapacity > 1) {
            defaultNodeCapacity += delta;
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    private void handleAddNodeAction() {
        if (onAddNode != null) {
            onAddNode.run();
        } else {
            if (selectionSystem != null && selectionSystem.hasPendingPosition()) {
                graph.addNode((int) selectionSystem.getPendingNodeX(), (int) selectionSystem.getPendingNodeY(), defaultNodeCapacity);
            } else {
                graph.addNode(400, 300, defaultNodeCapacity);
            }
        }
        btnAddNode.setText("➕ Add Node Here");
        btnAddNode.setDisable(true);
        if (selectionSystem != null) {
            selectionSystem.clearPendingPosition();
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    private void handleRemoveNodeAction() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null) return;
        if (onRemoveNode != null) {
            onRemoveNode.run();
        } else {
            graph.removeNode(sel);
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    /**
     * Synchronizes local display widgets with live attribute maps of selected targets.
     */
    public void updateUIState(Object selected) {
        boolean isNode = (selected instanceof Node);
        btnRemoveNode.setDisable(!isNode);

        if (isNode) {
            Node node = (Node) selected;
            lblNodeCap.setText("Capacity: " + node.getCapacity());
            chkUnderConstruction.setSelected(node.isUnderConstruction());
            chkUnderConstruction.setDisable(false);
        } else {
            lblNodeCap.setText("Capacity: " + defaultNodeCapacity);
            chkUnderConstruction.setSelected(false);
            chkUnderConstruction.setDisable(true);
        }
    }

    public int getDefaultNodeCapacity() { return defaultNodeCapacity; }
    public Button getBtnAddNode() { return btnAddNode; }
    private Node getSelectedNode() { return graph.getNodes().stream().filter(Node::isSelected).findFirst().orElse(null); }
}