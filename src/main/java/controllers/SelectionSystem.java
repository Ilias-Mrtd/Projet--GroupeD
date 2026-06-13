package controllers;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import model.graph.*;
import model.agents.Agent;
import controllers.helpers.*;
import java.util.List;
import UI.GraphCanvas;

/**
 * Main application layout coordinator routing live mouse cursor inputs.
 * Distributes event routines based on the active editing mode, updates selection state,
 * and handles node drag and drop operations.
 */
public class SelectionSystem {

    private final Graph graph;
    private final GraphCanvas canvas;
    private final List<Agent> agents;
    private final SelectionContext context = new SelectionContext();

    private boolean draggingNode = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    /** Active editing modes modifying click behavior. */
    public enum Mode { NORMAL, LINKING_EDGE, ASSIGN_OBJECTIVE }
    private Mode mode = Mode.NORMAL;
    private Node linkSource = null;
    private Agent objectiveAgent = null;

    @FunctionalInterface 
    public interface EdgeLinkCallback { 
        /** Triggered when an edge linkage sequence is completed between two endpoints. */
        void onEdgeLink(Node source, Node target); 
    }
    
    @FunctionalInterface 
    public interface EmptyClickCallback { 
        /** Triggered when the cursor clicks on empty canvas space coordinates. */
        void onEmptyClick(double x, double y); 
    }
    
    @FunctionalInterface 
    public interface ObjectiveCallback { 
        /** Triggered when a node target destination is assigned to an agent profile. */
        void onObjectiveAssigned(Agent agent, Node target); 
    }

    private EdgeLinkCallback edgeLinkCallback = null;
    private EmptyClickCallback emptyClickCallback = null;
    private ObjectiveCallback objectiveCallback = null;

    /**
     * Constructs the master click tracking system.
     *
     * @param graph  The topological network matrix layout container.
     * @param agents The array containing simulation tracking entities.
     * @param canvas The JavaFX custom rendering view instance.
     */
    public SelectionSystem(Graph graph, List<Agent> agents, GraphCanvas canvas) {
        this.graph = graph;
        this.agents = agents;
        this.canvas = canvas;
    }

    /**
     * Redraws the visual elements on the canvas view.
     *
     * @param refreshEdges If true, recalculates all edge lengths based on changed node coordinates.
     */
    private void updateView(boolean refreshEdges) {
        if (refreshEdges) graph.refreshEdgeLengths();
        canvas.draw();
    }

    /**
     * Activates path generation linkage parameters.
     *
     * @param cb Callback action executable once a valid path connection finishes.
     */
    public void startEdgeLinking(EdgeLinkCallback cb) { 
        this.mode = Mode.LINKING_EDGE; 
        this.linkSource = null; 
        this.edgeLinkCallback = cb; 
        context.clearAllSelections(); 
        updateView(false); 
    }

    /**
     * Cancels the edge creation process and cleans temporary link references.
     */
    public void cancelEdgeLinking() { 
        if (linkSource != null) linkSource.setSelected(false); 
        mode = Mode.NORMAL; 
        linkSource = null; 
        edgeLinkCallback = null; 
        updateView(false); 
    }

    /**
     * Prepares destination routing targets for a target mobile profile.
     *
     * @param a  The selected agent node.
     * @param cb Callback executable when target designation finishes.
     */
    public void startAssignObjective(Agent a, ObjectiveCallback cb) { 
        if (a == null) return; 
        this.mode = Mode.ASSIGN_OBJECTIVE; 
        this.objectiveAgent = a; 
        this.objectiveCallback = cb; 
        context.selectAgent(a); 
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

    public void setOnEmptyClick(EmptyClickCallback cb) { this.emptyClickCallback = cb; }

    /**
     * Routes hardware mouse click triggers toward context-appropriate handlers.
     *
     * @param event The mouse event containing click coordinates.
     */
    public void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            if (mode == Mode.LINKING_EDGE) cancelEdgeLinking();
            else if (mode == Mode.ASSIGN_OBJECTIVE) cancelAssignObjective();
            return;
        }
        Point2D worldPos = canvas.screenToWorld(event.getX(), event.getY());
        if (mode == Mode.LINKING_EDGE) handleLinkingClick(worldPos.getX(), worldPos.getY());
        else if (mode == Mode.ASSIGN_OBJECTIVE) handleObjectiveClick(worldPos.getX(), worldPos.getY());
        else handleNormalClick(worldPos.getX(), worldPos.getY());
    }

    /**
     * Begins node drag-and-drop mechanics when a click hits a node bounding circle.
     *
     * @param event Mouse press payload metrics.
     */
    public void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || mode != Mode.NORMAL) return;
        Point2D wp = canvas.screenToWorld(event.getX(), event.getY());
        Node clickedNode = SpatialGeometryCalculator.findNodeAt(graph, wp.getX(), wp.getY());
        if (clickedNode == null) return;

        if (clickedNode != context.getLastSelectedNode()) {
            context.selectNode(clickedNode);
            updateView(false);
        }
        draggingNode = true;
        dragOffsetX = wp.getX() - clickedNode.getX();
        dragOffsetY = wp.getY() - clickedNode.getY();
    }

    /**
     * Updates node coordinates during an active drag operation.
     *
     * @param event Live tracking cursor metrics payload.
     */
    public void handleMouseDragged(MouseEvent event) {
        if (!draggingNode || context.getLastSelectedNode() == null || mode != Mode.NORMAL) return;
        Point2D wp = canvas.screenToWorld(event.getX(), event.getY());
        context.getLastSelectedNode().setX((float) (wp.getX() - dragOffsetX));
        context.getLastSelectedNode().setY((float) (wp.getY() - dragOffsetY));
        updateView(true);
    }

    /**
     * Standard drag drop termination cleanup interface routine.
     *
     * @param event Input termination payload.
     */
    public void handleMouseReleased(MouseEvent event) {
        if (!draggingNode) return;
        draggingNode = false;
        updateView(true);
    }

    private void handleLinkingClick(double x, double y) {
        Node clicked = SpatialGeometryCalculator.findNodeAt(graph, x, y);
        if (clicked == null) return;
        if (linkSource == null) {
            linkSource = clicked;
            context.selectNode(clicked);
            updateView(false);
        } else {
            if (clicked == linkSource) return;
            linkSource.setSelected(false);
            mode = Mode.NORMAL;
            edgeLinkCallback.onEdgeLink(linkSource, clicked);
            linkSource = null;
            edgeLinkCallback = null;
            updateView(false);
        }
    }

    private void handleObjectiveClick(double x, double y) {
        Node clicked = SpatialGeometryCalculator.findNodeAt(graph, x, y);
        if (clicked == null) return;
        mode = Mode.NORMAL;
        if (objectiveAgent != null && objectiveCallback != null) {
            objectiveCallback.onObjectiveAssigned(objectiveAgent, clicked);
        }
        objectiveAgent = null;
        objectiveCallback = null;
        updateView(false);
    }

    private void handleNormalClick(double x, double y) {
        context.clearPendingPosition();

        Agent a = SpatialGeometryCalculator.findAgentAt(agents, x, y);
        if (a != null) { context.selectAgent(a); updateView(false); return; }

        Node n = SpatialGeometryCalculator.findNodeAt(graph, x, y);
        if (n != null) { context.selectNode(n); updateView(false); return; }

        Edge e = SpatialGeometryCalculator.findEdgeAt(graph, x, y);
        if (e != null) { context.selectEdge(e); updateView(false); return; }

        context.setPendingPosition(x, y);
        if (emptyClickCallback != null) emptyClickCallback.onEmptyClick(x, y);
        updateView(false);
    }

    public void selectAgent(Agent agent) { context.selectAgent(agent); updateView(false); }
    public Point2D computeAgentPosition(Agent agent) { return SpatialGeometryCalculator.computeAgentPosition(agent); }

    public Mode getMode() { return mode; }
    public Node getLastSelectedNode() { return context.getLastSelectedNode(); }
    public Edge getLastSelectedEdge() { return context.getLastSelectedEdge(); }
    public Agent getLastSelectedAgent() { return context.getLastSelectedAgent(); }
    public double getPendingNodeX() { return context.getPendingNodeX(); }
    public double getPendingNodeY() { return context.getPendingNodeY(); }
    public boolean hasPendingPosition() { return context.hasPendingPosition(); }
    public void clearPendingPosition() { context.clearPendingPosition(); }
    public Graph getGraph() { return graph; }
    public GraphCanvas getCanvas() { return canvas; }
    public List<Agent> getAgents() { return agents; }
}