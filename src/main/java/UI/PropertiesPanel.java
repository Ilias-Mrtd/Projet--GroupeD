package UI;

import UI.panel.*;
import controllers.SelectionSystem;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.VBox;
import model.agents.Agent;
import model.graph.Edge;
import model.graph.Graph;
import model.graph.Node;
import java.util.List;

/**
 * Control dashboard sidebar detailing individual element configurations,
 * simulation performance logs, dynamic KPIs, and automated infrastructure controls.
 */
public class PropertiesPanel extends VBox {

    private final Graph graph;
    private final List<Agent> agents;
    private SelectionSystem selectionSystem;

    // Component Panels References
    private final NodeSettingsPanel nodeSettingsPanel;
    private final EdgeSettingsPanel edgeSettingsPanel;
    private final AgentSettingsPanel agentSettingsPanel;
    private final BatchGenerationPanel batchGenerationPanel;
    private final InspectorPanel inspectorPanel;

    // Event Functional Interfaces Pipelines Callbacks
    private Runnable onAddNode;
    private Runnable onRemoveNode;
    private Runnable onRemoveEdge;
    private Runnable onAddAgent;
    private Runnable onGenerateGraph;
    private Runnable onSpawnAgents;
    private Runnable onRemoveAgent;
    private Runnable onAssignObjective;

    /**
     * Constructs and frames the core application panel manager sidebar wrapper.
     */
    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        // Visual Outer Frame Layout Definitions
        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(280);
        setStyle("-fx-background-color: #252526; -fx-border-color: #3E3E42; -fx-border-width: 0 0 0 1;");

        Label titleLabel = new Label("✏️ Graph manager");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #00E5FF;");

        // Sub-panel Modular System Instantiations passing references
        this.nodeSettingsPanel = new NodeSettingsPanel(graph, selectionSystem, this::refresh, this::redrawCanvas, () -> { if (onAddNode != null) onAddNode.run(); }, () -> { if (onRemoveNode != null) onRemoveNode.run(); });
        this.edgeSettingsPanel = new EdgeSettingsPanel(graph, selectionSystem, this::refresh, this::redrawCanvas, () -> { if (onRemoveEdge != null) onRemoveEdge.run(); });
        this.agentSettingsPanel = new AgentSettingsPanel(agents, selectionSystem, this::refresh, () -> { if (onAddAgent != null) onAddAgent.run(); }, () -> { if (onRemoveAgent != null) onRemoveAgent.run(); }, () -> { if (onAssignObjective != null) onAssignObjective.run(); });
        this.batchGenerationPanel = new BatchGenerationPanel(() -> { if (onGenerateGraph != null) onGenerateGraph.run(); }, () -> { if (onSpawnAgents != null) onSpawnAgents.run(); });
        this.inspectorPanel = new InspectorPanel(graph, agents);

        // Assembly Hierarchy Sequence Pipeline
        getChildren().addAll(
            titleLabel, new Separator(),
            nodeSettingsPanel, new Separator(),
            edgeSettingsPanel, new Separator(),
            agentSettingsPanel, new Separator(),
            batchGenerationPanel, new Separator(),
            inspectorPanel
        );
    }

    /** Binds application interaction frameworks inside processing layers. */
    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
        
        // On transmet la référence reçue aux sous-panneaux
        this.nodeSettingsPanel.setSelectionSystem(ss);
        this.edgeSettingsPanel.setSelectionSystem(ss);
        this.agentSettingsPanel.setSelectionSystem(ss);
        
        ss.setOnEmptyClick((x, y) -> {
            nodeSettingsPanel.getBtnAddNode().setDisable(false);
            nodeSettingsPanel.getBtnAddNode().setText("➕ Place Here (" + (int) x + "," + (int) y + ")");
        });
        agentSettingsPanel.setAddAgentDisable(true);
    }

    /** Triggers graphics model viewport calculations. */
    private void redrawCanvas() {
        if (selectionSystem != null && selectionSystem.getCanvas() != null) {
            selectionSystem.getCanvas().draw();
        }
    }

    /** Evaluates states across child elements based on user input models. */
    public void refresh() {
        Object selected = findSelectedItem();

        // Pass UI state down to the dedicated sub-components
        nodeSettingsPanel.updateUIState(selected);
        edgeSettingsPanel.updateUIState(selected);
        agentSettingsPanel.updateUIState(selected);
        inspectorPanel.updateInspectorContent(selected);
    }

    private Object findSelectedItem() {
        for (Agent a : agents) if (a.isSelected()) return a;
        for (Node n : graph.getNodes()) if (n.isSelected()) return n;
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge e : edges) if (e.isSelected()) return e;
        }
        return null;
    }

    // Contextual references access wrappers preserving structural calls
    public Node getSelectedNode() { return graph.getNodes().stream().filter(Node::isSelected).findFirst().orElse(null); }
    public Agent getSelectedAgent() { return agents.stream().filter(Agent::isSelected).findFirst().orElse(null); }
    public Edge getSelectedEdge() { return graph.getEdges().stream().flatMap(List::stream).filter(Edge::isSelected).findFirst().orElse(null); }
    public Agent.agentBehavior getSelectedAgentBehavior() { return agentSettingsPanel.getSelectedAgentBehavior(); }
    public int getNodeCapacity() { return nodeSettingsPanel.getDefaultNodeCapacity(); }
    public int getGenGridSide() { return batchGenerationPanel.getGenGridSide(); }
    public int getGenAgentCount() { return batchGenerationPanel.getGenAgentCount(); }
    public void objectiveAssignedDone() { agentSettingsPanel.objectiveAssignedDone(); }

    // External layout setup runner assignments
    public void setOnAddNode(Runnable r) { this.onAddNode = r; }
    public void setOnRemoveNode(Runnable r) { this.onRemoveNode = r; }
    public void setOnRemoveEdge(Runnable r) { this.onRemoveEdge = r; }
    public void setOnAddAgent(Runnable r) { this.onAddAgent = r; }
    public void setOnGenerateGraph(Runnable r) { this.onGenerateGraph = r; }
    public void setOnSpawnAgents(Runnable r) { this.onSpawnAgents = r; }
    public void setOnRemoveAgent(Runnable r) { this.onRemoveAgent = r; }
    public void setOnAssignObjective(Runnable r) { this.onAssignObjective = r; }
}