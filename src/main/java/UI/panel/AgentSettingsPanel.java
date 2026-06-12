package UI.panel;

import UI.utils.UIComponents;
import controllers.SelectionSystem;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import model.agents.Agent;
import model.graph.Node;
import java.util.List;

/**
 * Sub-panel layout container coordinating autonomous navigation profiles, behavior configurations,
 * object tracking deletions, and tactical navigation destination rules.
 */
public class AgentSettingsPanel extends VBox {

    private final List<Agent> agents;
    private final Runnable refreshCallback;
    private final Runnable onAddAgent;
    private final Runnable onRemoveAgent;
    private final Runnable onAssignObjective;
    private SelectionSystem selectionSystem;

    private final ComboBox<Agent.agentBehavior> cbAgentBehavior;
    private final Button btnAddAgent;
    private final Button btnRemoveAgent;
    private final Button btnAssignObjective;
    private boolean assigningObjective = false;

    /**
     * Constructs the agent controller tracking interface block element.
     */
    public AgentSettingsPanel(List<Agent> agents, SelectionSystem selectionSystem, Runnable refreshCallback,
                              Runnable onAddAgent, Runnable onRemoveAgent, Runnable onAssignObjective) {
        this.agents = agents;
        this.selectionSystem = selectionSystem;
        this.refreshCallback = refreshCallback;
        this.onAddAgent = onAddAgent;
        this.onRemoveAgent = onRemoveAgent;
        this.onAssignObjective = onAssignObjective;

        setSpacing(10);

        Label lblSection = new Label("Agent Settings");
        lblSection.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;");

        cbAgentBehavior = new ComboBox<>();
        cbAgentBehavior.getItems().addAll(Agent.agentBehavior.PATIENT, Agent.agentBehavior.HURRIED, Agent.agentBehavior.VIP);
        cbAgentBehavior.setValue(Agent.agentBehavior.PATIENT);
        cbAgentBehavior.setStyle("-fx-font-size: 12px;");
        cbAgentBehavior.setMaxWidth(Double.MAX_VALUE);

        btnAddAgent = UIComponents.buildButton("🤖 Add Autonomous Agent", "#388E3C");
        btnAddAgent.setDisable(true);
        btnRemoveAgent = UIComponents.buildButton("🗑 Remove Agent Profile", "#E64A19");
        btnRemoveAgent.setDisable(true);
        btnAssignObjective = UIComponents.buildButton("🎯 Assign Navigation Target", "#FBC02D");
        btnAssignObjective.setDisable(true);

        btnAddAgent.setOnAction(e -> handleAddAgentAction());
        btnRemoveAgent.setOnAction(e -> handleRemoveAgentAction());
        btnAssignObjective.setOnAction(e -> handleAssignObjectiveAction());

        getChildren().addAll(lblSection, cbAgentBehavior, btnAddAgent, btnRemoveAgent, btnAssignObjective);
    }

    private void handleAddAgentAction() {
        Node sel = selectionSystem != null ? selectionSystem.getLastSelectedNode() : null;
        if (sel == null) return;
        if (onAddAgent != null) onAddAgent.run();
        refreshCallback.run();
    }

    private void handleRemoveAgentAction() {
        Agent sel = selectionSystem != null ? selectionSystem.getLastSelectedAgent() : null;
        if (sel == null) return;
        if (onRemoveAgent != null) onRemoveAgent.run();
        refreshCallback.run();
    }

    private void handleAssignObjectiveAction() {
        if (selectionSystem == null) return;
        if (assigningObjective) {
            selectionSystem.cancelAssignObjective();
            assigningObjective = false;
        } else {
            Agent sel = selectionSystem.getLastSelectedAgent();
            if (sel == null) return;
            assigningObjective = true;
            if (onAssignObjective != null) onAssignObjective.run();
        }
        refreshCallback.run();
    }

    /**
     * Notifies container that external asset allocation routines have concluded.
     */
    public void objectiveAssignedDone() {
        this.assigningObjective = false;
    }

    /**
     * Resolves currently active choices tracking user navigation modifiers.
     */
    public void updateUIState(Object selected) {
        boolean isNode = (selected instanceof Node);
        boolean isAgent = (selected instanceof Agent);

        btnAddAgent.setDisable(!isNode);
        btnRemoveAgent.setDisable(!isAgent);

        if (!assigningObjective) {
            btnAssignObjective.setDisable(!isAgent);
        }

        if (assigningObjective) {
            btnAssignObjective.setText("↩ Cancel Objective");
            btnAssignObjective.setStyle(UIComponents.getButtonStyle("#F57C00"));
        } else {
            btnAssignObjective.setText("🎯 Assign Navigation Target");
            btnAssignObjective.setStyle(UIComponents.getButtonStyle("#FBC02D"));
        }
    }

    /**
     * Returns the selected enum routing parameter behavior currently evaluated.
     *
     * @return the selected strategy configuration model item
     */
    public Agent.agentBehavior getSelectedAgentBehavior() {
        return cbAgentBehavior.getValue();
    }

    /**
     * Sets external access tracking tags managing button overrides.
     *
     * @param state binary execution configuration parameter flag
     */
    public void setAddAgentDisable(boolean state) {
        btnAddAgent.setDisable(state);
    }

    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;
    }
}