package UI.panel;

import UI.utils.UIComponents;
import controllers.SelectionSystem;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import model.graph.Edge;
import model.graph.Graph;
import java.util.List;

/**
 * Sub-panel layout container coordinating direction attributes, speed factors,
 * link creation chains, and capacities across connected routing pathways.
 */
public class EdgeSettingsPanel extends VBox {

    private final Graph graph;
    private SelectionSystem selectionSystem;
    private final Runnable refreshCallback;
    private final Runnable redrawCallback;
    private final Runnable onRemoveEdge;

    private final Label lblEdgeCap;
    private final Label lblEdgeSpeed;
    private final Button btnAddEdge;
    private final Button btnRemoveEdge;
    private final Button btnToggleDir;

    private int defaultEdgeCapacity = 1;
    private boolean defaultEdgeDirection = true;
    private boolean linkingActive = false;

    /**
     * Constructs the connection interface sub-section managing network edge mappings.
     */
    public EdgeSettingsPanel(Graph graph, SelectionSystem selectionSystem, Runnable refreshCallback, 
                             Runnable redrawCallback, Runnable onRemoveEdge) {
        this.graph = graph;
        this.selectionSystem = selectionSystem;
        this.refreshCallback = refreshCallback;
        this.redrawCallback = redrawCallback;
        this.onRemoveEdge = onRemoveEdge;

        setSpacing(10);

        Label lblSection = new Label("Edge Settings");
        lblSection.setStyle(UIComponents.SECTION_TITLE_STYLE);

        lblEdgeCap = new Label("Capacity: 1");
        lblEdgeCap.setStyle(UIComponents.BASE_LABEL_STYLE);

        Button btnCapMinus = UIComponents.createSmallButton("−");
        Button btnCapPlus = UIComponents.createSmallButton("+");
        btnCapMinus.setOnAction(e -> handleCapacityAdjustment(-1));
        btnCapPlus.setOnAction(e -> handleCapacityAdjustment(1));

        HBox capBox = new HBox(6, btnCapMinus, lblEdgeCap, btnCapPlus);
        capBox.setStyle("-fx-alignment: center-left;");

        lblEdgeSpeed = new Label("Speed: x1.0");
        lblEdgeSpeed.setStyle(UIComponents.BASE_LABEL_STYLE);

        Button btnSpeedMinus = UIComponents.createSmallButton("−");
        Button btnSpeedPlus = UIComponents.createSmallButton("+");
        btnSpeedMinus.setOnAction(e -> handleSpeedAdjustment(-0.2f));
        btnSpeedPlus.setOnAction(e -> handleSpeedAdjustment(0.2f));

        HBox speedBox = new HBox(6, btnSpeedMinus, lblEdgeSpeed, btnSpeedPlus);
        speedBox.setStyle("-fx-alignment: center-left;");

        btnToggleDir = UIComponents.buildButton("Bidirectional ⇄", "#455A64");
        btnToggleDir.setOnAction(e -> handleDirectionToggle());

        btnAddEdge = UIComponents.buildButton("🔗 Add Edge Connection", "#7B1FA2");
        btnRemoveEdge = UIComponents.buildButton("🗑 Remove Edge Connection", "#C2185B");
        btnRemoveEdge.setDisable(true);

        btnAddEdge.setOnAction(e -> handleAddEdgeAction());
        btnRemoveEdge.setOnAction(e -> handleRemoveEdgeAction());

        getChildren().addAll(lblSection, capBox, speedBox, btnToggleDir, btnAddEdge, btnRemoveEdge);
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
        Edge sel = getSelectedEdge();
        if (sel != null) {
            int currentCap = sel.getCapacity();
            if (delta > 0 || currentCap > 1) {
                sel.setCapacity(currentCap + delta);
            }
        } else if (delta > 0 || defaultEdgeCapacity > 1) {
            defaultEdgeCapacity += delta;
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    private void handleSpeedAdjustment(float delta) {
        Edge sel = getSelectedEdge();
        if (sel != null) {
            float updatedSpeed = sel.getSpeedModifier() + delta;
            if (updatedSpeed >= 0.2f && updatedSpeed <= 5.0f) {
                sel.setSpeedModifier(updatedSpeed);
            }
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    private void handleDirectionToggle() {
        Edge sel = getSelectedEdge();
        if (sel != null) {
            sel.setDirection(!sel.hasDirection());
        } else {
            defaultEdgeDirection = !defaultEdgeDirection;
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    private void handleAddEdgeAction() {
        if (selectionSystem == null) return;
        if (linkingActive) {
            selectionSystem.cancelEdgeLinking();
            linkingActive = false;
        } else {
            linkingActive = true;
            final int cap = defaultEdgeCapacity;
            final boolean dir = defaultEdgeDirection;
            selectionSystem.startEdgeLinking((source, target) -> {
                graph.addEdge(source, target, cap, dir);
                linkingActive = false;
                refreshCallback.run();
                redrawCallback.run();
            });
        }
        refreshCallback.run();
    }

    private void handleRemoveEdgeAction() {
        Edge sel = selectionSystem != null ? selectionSystem.getLastSelectedEdge() : null;
        if (sel == null) return;
        if (onRemoveEdge != null) {
            onRemoveEdge.run();
        } else {
            graph.getEdges().forEach(list -> list.removeIf(e -> e.getId() == sel.getId()));
        }
        refreshCallback.run();
        redrawCallback.run();
    }

    /**
     * Synchronizes application view elements to report updated parameter models.
     */
    public void updateUIState(Object selected) {
        boolean isEdge = (selected instanceof Edge);
        btnRemoveEdge.setDisable(!isEdge);

        if (isEdge) {
            Edge edge = (Edge) selected;
            lblEdgeCap.setText("Capacity: " + edge.getCapacity());
            lblEdgeSpeed.setText(String.format("Speed: x%.1f", edge.getSpeedModifier()));
        } else {
            lblEdgeCap.setText("Capacity: " + defaultEdgeCapacity);
            lblEdgeSpeed.setText("Speed: x1.0");
        }

        btnToggleDir.setText(defaultEdgeDirection ? "Bidirectional ⇄" : "Directed →");

        if (linkingActive) {
            btnAddEdge.setText("↩ Cancel Linking");
            btnAddEdge.setStyle(UIComponents.getButtonStyle("#F57C00"));
        } else {
            btnAddEdge.setText("🔗 Add Edge Connection");
            btnAddEdge.setStyle(UIComponents.getButtonStyle("#7B1FA2"));
        }
    }

    private Edge getSelectedEdge() {
        return graph.getEdges().stream().flatMap(List::stream).filter(Edge::isSelected).findFirst().orElse(null);
    }
}