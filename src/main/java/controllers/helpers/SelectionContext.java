package controllers.helpers;

import model.agents.Agent;
import model.graph.Edge;
import model.graph.Node;

/**
 * Isolated structural state container maintaining historical runtime targets.
 * Holds active tracking selections and handles state transition operations
 * for elements inside the grid map context.
 */
public class SelectionContext {
    private Node lastSelectedNode = null;
    private Edge lastSelectedEdge = null;
    private Agent lastSelectedAgent = null;

    private double pendingNodeX = -1;
    private double pendingNodeY = -1;
    private boolean hasPendingPosition = false;

    /**
     * Resets visual highlight parameters across all elements
     * and clears cache tracking registries.
     */
    public void clearAllSelections() {
        if (lastSelectedNode != null) { lastSelectedNode.setSelected(false); lastSelectedNode = null; }
        if (lastSelectedEdge != null) { lastSelectedEdge.setSelected(false); lastSelectedEdge = null; }
        if (lastSelectedAgent != null) { lastSelectedAgent.setSelected(false); lastSelectedAgent = null; }
    }

    /**
     * Sets a single Node target as selected and resets other categories.
     *
     * @param node The node vertex infrastructure to focus.
     */
    public void selectNode(Node node) { 
        clearAllSelections(); 
        this.lastSelectedNode = node; 
        if (node != null) node.setSelected(true); 
    }

    /**
     * Sets a single Agent target as selected and resets other categories.
     *
     * @param agent The autonomous entity component to focus.
     */
    public void selectAgent(Agent agent) { 
        clearAllSelections(); 
        this.lastSelectedAgent = agent; 
        if (agent != null) agent.setSelected(true); 
    }

    /**
     * Sets a single Edge target as selected and resets other categories.
     *
     * @param edge The infrastructure segment to focus.
     */
    public void selectEdge(Edge edge) { 
        clearAllSelections(); 
        this.lastSelectedEdge = edge; 
        if (edge != null) edge.setSelected(true); 
    }

    /**
     * Caches empty workspace target mouse clicks to create a pending location
     * for node generation tools.
     *
     * @param x Horizontal positioning point.
     * @param y Vertical positioning point.
     */
    public void setPendingPosition(double x, double y) { 
        this.pendingNodeX = x; 
        this.pendingNodeY = y; 
        this.hasPendingPosition = true; 
    }

    /**
     * Resets spatial placeholder buffer metrics back to factory default values.
     */
    public void clearPendingPosition() { 
        hasPendingPosition = false; 
        pendingNodeX = -1; 
        pendingNodeY = -1; 
    }

    public Node getLastSelectedNode() { return lastSelectedNode; }
    public Edge getLastSelectedEdge() { return lastSelectedEdge; }
    public Agent getLastSelectedAgent() { return lastSelectedAgent; }
    public double getPendingNodeX() { return pendingNodeX; }
    public double getPendingNodeY() { return pendingNodeY; }
    public boolean hasPendingPosition() { return hasPendingPosition; }
}