package controllers;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import model.graph.*;
import model.agents.Agent;
import java.util.List;
import UI.GraphCanvas;

public class SelectionSystem {

    private final Graph graph;
    private final GraphCanvas canvas;
    private final List<Agent> agents;

    private static final double NODE_RADIUS = 30.0;
    private static final double AGENT_RADIUS = 10.0;
    private static final double EDGE_TOL = 5.0;

    private Node lastSelectedNode = null;
    private Edge lastSelectedEdge = null;
    private Agent lastSelectedAgent = null;

    private boolean draggingNode = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private double pendingNodeX = -1;
    private double pendingNodeY = -1;
    private boolean hasPendingPosition = false;

    public enum Mode { NORMAL, LINKING_EDGE, ASSIGN_OBJECTIVE }
    private Mode mode = Mode.NORMAL;
    private Node linkSource = null;
    private Agent objectiveAgent = null;

    @FunctionalInterface
    public interface EdgeLinkCallback { void onEdgeLink(Node source, Node target); }
    private EdgeLinkCallback edgeLinkCallback = null;

    @FunctionalInterface
    public interface EmptyClickCallback { void onEmptyClick(double x, double y); }
    private EmptyClickCallback emptyClickCallback = null;

    @FunctionalInterface
    public interface ObjectiveCallback { void onObjectiveAssigned(Agent agent, Node target); }
    private ObjectiveCallback objectiveCallback = null;

    public SelectionSystem(Graph graph, List<Agent> agents, GraphCanvas canvas) {
        this.graph = graph;
        this.agents = agents;
        this.canvas = canvas;
    }

    /**
     * Helper method to centralize graph logic updates and canvas redrawing.
     * @param refreshEdges True if the edge physical lengths need recalculation.
     */
    private void updateView(boolean refreshEdges) {
        if (refreshEdges) {
            graph.refreshEdgeLengths();
        }
        canvas.draw();
    }

    /**
     * Initializes the edge creation mode between two nodes.
     * @param callback The handler triggered when the link is complete.
     */
    public void startEdgeLinking(EdgeLinkCallback callback) {
        this.mode = Mode.LINKING_EDGE;
        this.linkSource = null;
        this.edgeLinkCallback = callback;
        clearAllSelections();
        updateView(false);
    }

    /**
     * Cancels the edge creation process and clears temporary link references.
     */
    public void cancelEdgeLinking() {
        if (linkSource != null) {
            linkSource.setSelected(false);
        }
        mode = Mode.NORMAL;
        linkSource = null;
        edgeLinkCallback = null;
        updateView(false);
    }

    /**
     * Activates the path target assignment mode for a specific mobile entity.
     * @param agent The target agent receiving the new destination objective.
     * @param callback The handler executed once the destination node is picked.
     */
    public void startAssignObjective(Agent agent, ObjectiveCallback callback) {
        if (agent == null) return;
        this.mode = Mode.ASSIGN_OBJECTIVE;
        this.objectiveAgent = agent;
        this.objectiveCallback = callback;
        
        clearAllSelections();
        objectiveAgent = agent;
        agent.setSelected(true);
        lastSelectedAgent = agent;
        updateView(false);
    }

    /**
     * Cancels the current target selection mode and releases the agent context.
     */
    public void cancelAssignObjective() {
        mode = Mode.NORMAL;
        objectiveAgent = null;
        objectiveCallback = null;
        updateView(false);
    }

    public void setOnEmptyClick(EmptyClickCallback cb) { 
        this.emptyClickCallback = cb; 
    }

    public Mode getMode() { return mode; }
    public Node getLastSelectedNode() { return lastSelectedNode; }
    public Edge getLastSelectedEdge() { return lastSelectedEdge; }
    public Agent getLastSelectedAgent() { return lastSelectedAgent; }
    public double getPendingNodeX() { return pendingNodeX; }
    public double getPendingNodeY() { return pendingNodeY; }
    public boolean hasPendingPosition() { return hasPendingPosition; }
    
    public void clearPendingPosition() { 
        hasPendingPosition = false; 
        pendingNodeX = -1; 
        pendingNodeY = -1; 
    }

    /**
     * Directs raw mouse click triggers towards active context routine logic.
     * @param event The mouse event containing click source metrics.
     */
    public void handleMouseClick(MouseEvent event) {
        // Right click cancels current active editing modes
        if (event.getButton() == MouseButton.SECONDARY) {
            if (mode == Mode.LINKING_EDGE) cancelEdgeLinking();
            else if (mode == Mode.ASSIGN_OBJECTIVE) cancelAssignObjective();
            return;
        }
        
        // Convert screen pixels coordinates to world map position
        Point2D worldPos = canvas.screenToWorld(event.getX(), event.getY());
        double x = worldPos.getX();
        double y = worldPos.getY();

        if (mode == Mode.LINKING_EDGE) handleLinkingClick(x, y);
        else if (mode == Mode.ASSIGN_OBJECTIVE) handleObjectiveClick(x, y);
        else handleNormalClick(x, y);
    }

    /**
     * Evaluates cursor placement to initialize real-time vertex drag translations.
     * @param event The mouse pressed triggers registry data.
     */
    public void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || mode != Mode.NORMAL) return;
        
        Point2D worldPos = canvas.screenToWorld(event.getX(), event.getY());
        double x = worldPos.getX();
        double y = worldPos.getY();

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode == null) return;

        if (clickedNode != lastSelectedNode) {
            clearAllSelections();
            lastSelectedNode = clickedNode;
            clickedNode.setSelected(true);
            updateView(false);
        }

        // Initialize drag offsets
        draggingNode = true;
        dragOffsetX = x - clickedNode.getX();
        dragOffsetY = y - clickedNode.getY();
    }

    /**
     * Translates coordinates for selected nodes when a drag transaction is registered.
     * @param event The live mouse drag movement variables.
     */
    public void handleMouseDragged(MouseEvent event) {
        if (!draggingNode || lastSelectedNode == null || mode != Mode.NORMAL) return;

        Point2D worldPos = canvas.screenToWorld(event.getX(), event.getY());
        double x = worldPos.getX() - dragOffsetX;
        double y = worldPos.getY() - dragOffsetY;

        lastSelectedNode.setX((float) x);
        lastSelectedNode.setY((float) y);
        updateView(true);
    }

    /**
     * Releases structural movement tracking flags when cursor selection drops.
     * @param event The input termination release metadata.
     */
    public void handleMouseReleased(MouseEvent event) {
        if (!draggingNode) return;
        draggingNode = false;
        updateView(true);
    }

    private void handleLinkingClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked == null) return;

        if (linkSource == null) {
            // First click selects origin
            linkSource = clicked;
            clearAllSelections();
            linkSource.setSelected(true);
            updateView(false);
        } else {
            // Second click establishes edge connection if valid
            if (clicked == linkSource) return;
            linkSource.setSelected(false);
            Node target = clicked;
            mode = Mode.NORMAL;
            edgeLinkCallback.onEdgeLink(linkSource, target);
            linkSource = null;
            edgeLinkCallback = null;
            updateView(false);
        }
    }

    private void handleObjectiveClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked == null) return;

        Agent agent = objectiveAgent;
        mode = Mode.NORMAL;
        if (agent != null && objectiveCallback != null) {
            objectiveCallback.onObjectiveAssigned(agent, clicked);
        }
        objectiveAgent = null;
        objectiveCallback = null;
        updateView(false);
    }

    private void handleNormalClick(double x, double y) {
        clearAllSelections();
        hasPendingPosition = false;

        // Selection hierarchy routing priority: Agent > Node > Edge
        Agent clickedAgent = findAgentAt(x, y);
        if (clickedAgent != null) {
            lastSelectedAgent = clickedAgent;
            clickedAgent.setSelected(true);
            updateView(false);
            return;
        }

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode != null) {
            lastSelectedNode = clickedNode;
            clickedNode.setSelected(true);
            updateView(false);
            return;
        }

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            lastSelectedEdge = clickedEdge;
            clickedEdge.setSelected(true);
            updateView(false);
            return;
        }

        // Click on empty space registers a pending placement position
        pendingNodeX = x;
        pendingNodeY = y;
        hasPendingPosition = true;
        if (emptyClickCallback != null) {
            emptyClickCallback.onEmptyClick(x, y);
        }
        updateView(false);
    }

    private void clearAllSelections() {
        if (lastSelectedNode != null) { 
            lastSelectedNode.setSelected(false); 
            lastSelectedNode = null; 
        }
        if (lastSelectedEdge != null) { 
            lastSelectedEdge.setSelected(false); 
            lastSelectedEdge = null; 
        }
        if (lastSelectedAgent != null) { 
            lastSelectedAgent.setSelected(false); 
            lastSelectedAgent = null; 
        }
    }

    /**
     * Programmatically targets and forces the visual selection state of an agent.
     * @param agent The target tracking instance to isolate.
     */
    public void selectAgent(Agent agent) {
        clearAllSelections();
        if (agent != null) {
            lastSelectedAgent = agent;
            agent.setSelected(true);
        }
        updateView(false);
    }

    private Node findNodeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Node node : graph.getNodes()) {
            if (click.distance(new Point2D(node.getX(), node.getY())) <= NODE_RADIUS) {
                return node;
            }
        }
        return null;
    }

    private Agent findAgentAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Agent a : agents) {
            Point2D pos = computeAgentPosition(a);
            if (pos != null && click.distance(pos) <= AGENT_RADIUS) {
                return a;
            }
        }
        return null;
    }

    private Edge findEdgeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                Node n1 = edge.getSource();
                Node n2 = edge.getTarget();
                double l2 = Math.pow(n2.getX() - n1.getX(), 2) + Math.pow(n2.getY() - n1.getY(), 2);
                if (l2 == 0) continue;
                
                // Vector projection to find closest point on segment
                double t = Math.max(0, Math.min(1, ((x - n1.getX()) * (n2.getX() - n1.getX()) + (y - n1.getY()) * (n2.getY() - n1.getY())) / l2));
                Point2D projection = new Point2D(n1.getX() + t * (n2.getX() - n1.getX()), n1.getY() + t * (n2.getY() - n1.getY()));
                
                if (click.distance(projection) <= EDGE_TOL) {
                    return edge;
                }
            }
        }
        return null;
    }

    /**
     * Interpolates paths records to extract live coordinate dimensions mapping tracking vectors.
     * @param agent The simulated tracking agent node entity component.
     * @return A 2D point mapping layout updates on the tracking panel.
     */
    public Point2D computeAgentPosition(Agent agent) {
        if (agent.getCurrentNode() == null) return null;
        if (agent.getCurrentEdge() == null) {
            return new Point2D(agent.getCurrentNode().getX(), agent.getCurrentNode().getY());
        }

        Edge edge = agent.getCurrentEdge();
        double edgeLength = edge.getLength();
        double visualDist = agent.getDistanceTraveledOnEdge();
        
        // Clamp visual position to prevent overlap with target node radius bounds
        if (visualDist >= edgeLength) {
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node from = (edge.getSource() == agent.getCurrentNode()) ? edge.getSource() : edge.getTarget();
        Node to = (from == edge.getSource()) ? edge.getTarget() : edge.getSource();
        
        return new Point2D(from.getX() + t * (to.getX() - from.getX()), from.getY() + t * (to.getY() - from.getY()));
    }

    public Graph getGraph() { return graph; }
    public GraphCanvas getCanvas() { return canvas; }
    public List<Agent> getAgents() { return agents; }
}