package UI;

import UI.panel.*;
import UI.utils.UIComponents;
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
 * Master controller UI orchestrator sidebar framing decoupled settings blocks,
 * managing core synchronization callbacks, and routing layout selections.
 */
public class PropertiesPanel extends VBox {

    private final Graph graph;
    private final List<Agent> agents;
    private SelectionSystem selectionSystem;

    // Sub-components mappings references
    private final NodeSettingsPanel nodeSettingsPanel;
    private final EdgeSettingsPanel edgeSettingsPanel;
    private final AgentSettingsPanel agentSettingsPanel;
    private final BatchGenerationPanel batchGenerationPanel;
    private final InspectorPanel inspectorPanel;

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

        setPadding(new Insets(15));
        setSpacing(10);
        setPrefWidth(280);
        setStyle(UIComponents.PANEL_BACKGROUND);

        Label titleLabel = new Label("✏️ Graph manager");
        titleLabel.setStyle(UIComponents.TITLE_LABEL_STYLE);

        // Sub-panel modular initializations passing references pipelines 
        this.nodeSettingsPanel = new NodeSettingsPanel(graph, selectionSystem, this::refresh, this::redrawCanvas, () -> { if (onAddNode != null) onAddNode.run(); }, () -> { if (onRemoveNode != null) onRemoveNode.run(); });
        this.edgeSettingsPanel = new EdgeSettingsPanel(graph, selectionSystem, this::refresh, this::redrawCanvas, () -> { if (onRemoveEdge != null) onRemoveEdge.run(); });
        this.agentSettingsPanel = new AgentSettingsPanel(agents, selectionSystem, this::refresh, () -> { if (onAddAgent != null) onAddAgent.run(); }, () -> { if (onRemoveAgent != null) onRemoveAgent.run(); }, () -> { if (onAssignObjective != null) onAssignObjective.run(); });
        this.batchGenerationPanel = new BatchGenerationPanel(() -> { if (onGenerateGraph != null) onGenerateGraph.run(); }, () -> { if (onSpawnAgents != null) onSpawnAgents.run(); });
        this.inspectorPanel = new InspectorPanel(graph, agents);

        // UI tree structuring execution layout
        getChildren().addAll(
            titleLabel, new Separator(),
            nodeSettingsPanel, new Separator(),
            edgeSettingsPanel, new Separator(),
            agentSettingsPanel, new Separator(),
            batchGenerationPanel, new Separator(),
            inspectorPanel
        );
    }

    /** Binds runtime selection context instances down through reactive panels pipeline loops. */
    public void setSelectionSystem(SelectionSystem ss) {
        this.selectionSystem = ss;
        
        // Distribute initialization token references safely downwards to sub-sections
        this.nodeSettingsPanel.setSelectionSystem(ss);
        this.edgeSettingsPanel.setSelectionSystem(ss);
        this.agentSettingsPanel.setSelectionSystem(ss);
        
        ss.setOnEmptyClick((x, y) -> {
            nodeSettingsPanel.getBtnAddNode().setDisable(false);
            nodeSettingsPanel.getBtnAddNode().setText("➕ Place Here (" + (int) x + "," + (int) y + ")");
        });
        agentSettingsPanel.setAddAgentDisable(true);
    }

    private void redrawCanvas() {
        if (selectionSystem != null && selectionSystem.getCanvas() != null) {
            selectionSystem.getCanvas().draw();
        }
    }

    /** Evaluates states across child elements based on user input models. */
    public void refresh() {
        Object selected = findSelectedItem();

        nodeSettingsPanel.updateUIState(selected);
        edgeSettingsPanel.updateUIState(selected);
        agentSettingsPanel.updateUIState(selected);
        inspectorPanel.updateInspectorContent(selected);
    }

    /**
     * Streamlined lookup parsing targeted tracking choice items via high efficiency functional Java Streams.
     */
    private Object findSelectedItem() {
        return agents.stream().filter(Agent::isSelected).findFirst()
            .map(Object.class::cast)
            .orElseGet(() -> graph.getNodes().stream().filter(Node::isSelected).findFirst()
            .map(Object.class::cast)
            .orElseGet(() -> graph.getEdges().stream().flatMap(List::stream).filter(Edge::isSelected).findFirst()
            .map(Object.class::cast)
            .orElse(null)));
    }

    // Contextual references wrappers maintaining architectural operations
    public Node getSelectedNode() { return graph.getNodes().stream().filter(Node::isSelected).findFirst().orElse(null); }
    public Agent getSelectedAgent() { return agents.stream().filter(Agent::isSelected).findFirst().orElse(null); }
    public Edge getSelectedEdge() { return graph.getEdges().stream().flatMap(List::stream).filter(Edge::isSelected).findFirst().orElse(null); }
    public Agent.agentBehavior getSelectedAgentBehavior() { return agentSettingsPanel.getSelectedAgentBehavior(); }
    public int getNodeCapacity() { return nodeSettingsPanel.getDefaultNodeCapacity(); }
    public int getGenGridSide() { return batchGenerationPanel.getGenGridSide(); }
    public int getGenAgentCount() { return batchGenerationPanel.getGenAgentCount(); }
    public void objectiveAssignedDone() { agentSettingsPanel.objectiveAssignedDone(); }

    // Callback assignments mapping
    public void setOnAddNode(Runnable r) { this.onAddNode = r; }
    public void setOnRemoveNode(Runnable r) { this.onRemoveNode = r; }
    public void setOnRemoveEdge(Runnable r) { this.onRemoveEdge = r; }
    public void setOnAddAgent(Runnable r) { this.onAddAgent = r; }
    public void setOnGenerateGraph(Runnable r) { this.onGenerateGraph = r; }
    public void setOnSpawnAgents(Runnable r) { this.onSpawnAgents = r; }
    public void setOnRemoveAgent(Runnable r) { this.onRemoveAgent = r; }
    public void setOnAssignObjective(Runnable r) { this.onAssignObjective = r; }
}