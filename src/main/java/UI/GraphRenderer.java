package UI;

import javafx.scene.canvas.GraphicsContext;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Edge;
import model.agents.Agent;
import UI.renderers.NodeRendering;
import UI.renderers.EdgeRendering;
import UI.renderers.AgentRendering;
import java.util.List;

/**
 * Orchestrates the global multi-layered drawing pipeline on the canvas graphics viewport,
 * ensuring correct Z-index ordering across network components.
 */
public class GraphRenderer {
    private final NodeRendering nodeRenderer;
    private final EdgeRendering edgeRenderer;
    private final AgentRendering agentRenderer;

    public GraphRenderer(NodeRendering nr, EdgeRendering er, AgentRendering ar) {
        this.nodeRenderer = nr;
        this.edgeRenderer = er;
        this.agentRenderer = ar;
    }

    /**
     * Executes a full rendering loop iteration, layering infrastructure elements and active simulation entities.
     * @param gc The active graphics context of the canvas.
     * @param graph The target structural network layout to draw.
     * @param agents The collection of active agents navigating the spatial environment.
     */
    public void draw(GraphicsContext gc, Graph graph, List<Agent> agents) {
        // Guard clause to prevent canvas drawing instabilities during initial loading phases
        if (graph == null || agents == null) {
            return;
        }

        // Layer 1: Draw underlying routing links (Edges). 
        // Directional arrow geometry calculations are delegated internally to edgeRenderer.
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                edgeRenderer.drawEdge(gc, edge);
            }
        }

        // Layer 2: Draw intersection intersections (Nodes) overlapping edge terminations.
        for (Node node : graph.getNodes()) {
            nodeRenderer.drawNode(gc, node);
        }

        // Layer 3: Draw mobile entities (Agents) on the topmost visible Z-index layer.
        // Spatial coordinate interpolations are resolved internally within agentRenderer.
        for (Agent agent : agents) {
            agentRenderer.drawAgent(gc, agent);
        }
    }

    public NodeRendering getNodeRenderer() { return nodeRenderer; }
    public EdgeRendering getEdgeRenderer() { return edgeRenderer; }
    public AgentRendering getAgentRenderer() { return agentRenderer; }
}