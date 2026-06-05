package UI;

import javafx.scene.canvas.GraphicsContext;
import model.graph.*;
import model.agents.Agent;
import UI.renderers.*;
import java.util.List;

public class GraphRenderer {
    private final NodeRendering nodeRenderer;
    private final EdgeRendering edgeRenderer;
    private final AgentRendering agentRenderer;

    public GraphRenderer(NodeRendering nr, EdgeRendering er, AgentRendering ar) {
        this.nodeRenderer = nr;
        this.edgeRenderer = er;
        this.agentRenderer = ar;
    }

    public void draw(GraphicsContext gc, Graph graph, List<Agent> agents) {

        // On dessine d'abord les arêtes qui seront recouvertent par les noeuds
        // javatenant le dessin de fleche directionnelle dans edgeRenderer
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                edgeRenderer.drawEdge(gc, edge);
            }
        }

        // On dessine les nœuds par-dessus
        for (Node node : graph.getNodes()) {
            nodeRenderer.drawNode(gc, node);
        }

        // On dessine les agents tout devant
        // javatenant avec les calculs dans agentRenderer
        for (Agent agent : agents) {
            agentRenderer.drawAgent(gc, agent);
        }
    }

    public NodeRendering getNodeRenderer() { return nodeRenderer; }
    public EdgeRendering getEdgeRenderer() { return edgeRenderer; }
    public AgentRendering getAgentRenderer() { return agentRenderer; }
}