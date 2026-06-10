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

    private static final double AGENT_RADIUS = 10.0;
    private static final double EDGE_TOL     = 5.0;

    private Node  lastSelectedNode  = null;
    private Edge  lastSelectedEdge  = null;
    private Agent lastSelectedAgent = null;

    private boolean draggingNode = false;
    private double dragOffsetX = 0;
    private double dragOffsetY = 0;

    private double pendingNodeX = -1;
    private double pendingNodeY = -1;
    private boolean hasPendingPosition = false;

    public enum Mode { NORMAL, LINKING_EDGE }
    private Mode mode = Mode.NORMAL;
    private Node linkSource = null;

    @FunctionalInterface
    public interface EdgeLinkCallback {
        void onEdgeLink(Node source, Node target);
    }
    private EdgeLinkCallback edgeLinkCallback = null;

    @FunctionalInterface
    public interface EmptyClickCallback {
        void onEmptyClick(double x, double y);
    }
    private EmptyClickCallback emptyClickCallback = null;

    public SelectionSystem(Graph graph, List<Agent> agents, GraphCanvas canvas) {
        this.graph  = graph;
        this.agents = agents;
        this.canvas = canvas;
    }

    // ================================================================= API

    public void startEdgeLinking(EdgeLinkCallback callback) {
        this.mode             = Mode.LINKING_EDGE;
        this.linkSource       = null;
        this.edgeLinkCallback = callback;
        clearAllSelections();
        canvas.draw();
    }

    public void cancelEdgeLinking() {
        if (linkSource != null) linkSource.setSelected(false);
        mode             = Mode.NORMAL;
        linkSource       = null;
        edgeLinkCallback = null;
        canvas.draw();
    }

    public void setOnEmptyClick(EmptyClickCallback cb) { this.emptyClickCallback = cb; }

    public Mode   getMode()               { return mode; }
    public Node   getLastSelectedNode()   { return lastSelectedNode; }
    public Edge   getLastSelectedEdge()   { return lastSelectedEdge; }
    public Agent  getLastSelectedAgent()  { return lastSelectedAgent; }
    public double getPendingNodeX()       { return pendingNodeX; }
    public double getPendingNodeY()       { return pendingNodeY; }
    public boolean hasPendingPosition()   { return hasPendingPosition; }
    public void clearPendingPosition()    { hasPendingPosition = false; pendingNodeX = -1; pendingNodeY = -1; }

    // ================================================================= clics

    public void handleMouseClick(MouseEvent event) {
        if (event.getButton() == MouseButton.SECONDARY) {
            if (mode == Mode.LINKING_EDGE) cancelEdgeLinking();
            return;
        }
        double x = event.getX();
        double y = event.getY();
        if (mode == Mode.LINKING_EDGE) handleLinkingClick(x, y);
        else                           handleNormalClick(x, y);
    }

    public void handleMousePressed(MouseEvent event) {
        if (event.getButton() != MouseButton.PRIMARY || mode != Mode.NORMAL) return;
        double x = event.getX();
        double y = event.getY();

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode == null) return;

        if (clickedNode != lastSelectedNode) {
            clearAllSelections();
            lastSelectedNode = clickedNode;
            clickedNode.setSelected(true);
            canvas.draw();
        }

        draggingNode = true;
        dragOffsetX = x - clickedNode.getX();
        dragOffsetY = y - clickedNode.getY();
    }

    public void handleMouseDragged(MouseEvent event) {
        if (!draggingNode || lastSelectedNode == null || mode != Mode.NORMAL) return;

        double desiredX = event.getX() - dragOffsetX;
        double desiredY = event.getY() - dragOffsetY;

        if (graph.isNodePositionAvailable(desiredX, desiredY, Node.RADIUS, lastSelectedNode)) {
            lastSelectedNode.setX((float) desiredX);
            lastSelectedNode.setY((float) desiredY);
        } else {
            Point2D current = new Point2D(lastSelectedNode.getX(), lastSelectedNode.getY());
            Point2D target = new Point2D(desiredX, desiredY);
            Point2D validTarget = computeMaxValidPosition(current, target);

            if (!validTarget.equals(current)) {
                lastSelectedNode.setX((float) validTarget.getX());
                lastSelectedNode.setY((float) validTarget.getY());
            }
        }

        graph.refreshEdgeLengths();
        canvas.draw();
    }

    public void handleMouseReleased(MouseEvent event) {
        if (!draggingNode) return;
        draggingNode = false;
        graph.refreshEdgeLengths();
        canvas.draw();
    }

    private void handleLinkingClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked == null) return;

        if (linkSource == null) {
            linkSource = clicked;
            clearAllSelections();
            linkSource.setSelected(true);
            canvas.draw();
        } else {
            if (clicked == linkSource) return;
            linkSource.setSelected(false);
            Node target = clicked;
            mode = Mode.NORMAL;
            edgeLinkCallback.onEdgeLink(linkSource, target);
            linkSource       = null;
            edgeLinkCallback = null;
            canvas.draw();
        }
    }

    private void handleNormalClick(double x, double y) {
        clearAllSelections();
        hasPendingPosition = false;

        Agent clickedAgent = findAgentAt(x, y);
        if (clickedAgent != null) {
            lastSelectedAgent = clickedAgent;
            clickedAgent.setSelected(true);
            canvas.draw();
            return;
        }

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode != null) {
            lastSelectedNode = clickedNode;
            clickedNode.setSelected(true);
            canvas.draw();
            return;
        }

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            lastSelectedEdge = clickedEdge;
            clickedEdge.setSelected(true);
            canvas.draw();
            return;
        }

        if (!graph.isNodePositionAvailable(x, y, Node.RADIUS, null)) {
            System.out.println("Position invalide pour ajouter un nœud : trop proche d'un autre élément.");
            canvas.draw();
            return;
        }

        pendingNodeX       = x;
        pendingNodeY       = y;
        hasPendingPosition = true;
        if (emptyClickCallback != null) emptyClickCallback.onEmptyClick(x, y);
        canvas.draw();
    }

    // ================================================================= helpers

    private void clearAllSelections() {
        if (getLastSelectedNode()  != null) { getLastSelectedNode().setSelected(false); setLastSelectedNode(null); }
        if (getLastSelectedEdge()  != null) { getLastSelectedEdge().setSelected(false); setLastSelectedEdge(null); }
        if (getLastSelectedAgent() != null) { getLastSelectedAgent().setSelected(false); setLastSelectedAgent(null); }
    }

    private Node findNodeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Node node : graph.getNodes())
            if (click.distance(new Point2D(node.getX(), node.getY())) <= Node.RADIUS)
                return node;
        return null;
    }

    private Agent findAgentAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Agent a : agents) {
            Point2D pos = computeAgentPosition(a);
            if (pos != null && click.distance(pos) <= AGENT_RADIUS) return a;
        }
        return null;
    }

    private Point2D computeMaxValidPosition(Point2D current, Point2D target) {
        double low = 0.0;
        double high = 1.0;
        Point2D best = current;

        for (int i = 0; i < 12; i++) {
            double mid = (low + high) / 2.0;
            double testX = current.getX() + (target.getX() - current.getX()) * mid;
            double testY = current.getY() + (target.getY() - current.getY()) * mid;

            if (graph.isNodePositionAvailable(testX, testY, Node.RADIUS, lastSelectedNode)) {
                best = new Point2D(testX, testY);
                low = mid;
            } else {
                high = mid;
            }
        }

        return best;
    }

    private Edge findEdgeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                Node n1 = edge.getSource(), n2 = edge.getTarget();
                double l2 = Math.pow(n2.getX() - n1.getX(), 2) + Math.pow(n2.getY() - n1.getY(), 2);
                if (l2 == 0) continue;
                double t = Math.max(0, Math.min(1,
                        ((x-n1.getX())*(n2.getX()-n1.getX()) + (y-n1.getY())*(n2.getY()-n1.getY())) / l2));
                if (click.distance(new Point2D(n1.getX() + t*(n2.getX()-n1.getX()), n1.getY() + t*(n2.getY()-n1.getY()))) <= EDGE_TOL)
                    return edge;
            }
        }
        return null;
    }

    private Point2D computeAgentPosition(Agent agent) {
        if (agent.getCurrentNode() == null) return null;
        if (agent.getCurrentEdge() == null) return new Point2D(agent.getCurrentNode().getX(), agent.getCurrentNode().getY());

        Edge   edge       = agent.getCurrentEdge();
        double edgeLength = edge.getLength();
        double visualDist = agent.getDistanceTraveledOnEdge();
        if (visualDist >= edgeLength)
            visualDist = Math.max(0, edgeLength - Node.RADIUS - (AGENT_RADIUS / 2.0));

        double t    = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node   from = (edge.getSource() == agent.getCurrentNode()) ? edge.getSource() : edge.getTarget();
        Node   to   = (from == edge.getSource())              ? edge.getTarget() : edge.getSource();
        return new Point2D(from.getX() + t*(to.getX()-from.getX()), from.getY() + t*(to.getY()-from.getY()));
    }

    public Graph getGraph() {
        return graph;
    }

    public GraphCanvas getCanvas() {
        return canvas;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public static double getNodeRadius() {
        return Node.RADIUS;
    }

    public static double getAgentRadius() {
        return AGENT_RADIUS;
    }

    public static double getEdgeTol() {
        return EDGE_TOL;
    }

    public void setLastSelectedNode(Node lastSelectedNode) {
        this.lastSelectedNode = lastSelectedNode;
    }

    public void setLastSelectedEdge(Edge lastSelectedEdge) {
        this.lastSelectedEdge = lastSelectedEdge;
    }

    public void setLastSelectedAgent(Agent lastSelectedAgent) {
        this.lastSelectedAgent = lastSelectedAgent;
    }

    public void setPendingNodeX(double pendingNodeX) {
        this.pendingNodeX = pendingNodeX;
    }

    public void setPendingNodeY(double pendingNodeY) {
        this.pendingNodeY = pendingNodeY;
    }

    public boolean isHasPendingPosition() {
        return hasPendingPosition;
    }

    public void setHasPendingPosition(boolean hasPendingPosition) {
        this.hasPendingPosition = hasPendingPosition;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public Node getLinkSource() {
        return linkSource;
    }

    public void setLinkSource(Node linkSource) {
        this.linkSource = linkSource;
    }

    public EdgeLinkCallback getEdgeLinkCallback() {
        return edgeLinkCallback;
    }

    public void setEdgeLinkCallback(EdgeLinkCallback edgeLinkCallback) {
        this.edgeLinkCallback = edgeLinkCallback;
    }

    public EmptyClickCallback getEmptyClickCallback() {
        return emptyClickCallback;
    }

    public void setEmptyClickCallback(EmptyClickCallback emptyClickCallback) {
        this.emptyClickCallback = emptyClickCallback;
    }
}
