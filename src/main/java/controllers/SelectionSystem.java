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

    private static final double NODE_RADIUS  = 30.0;
    private static final double AGENT_RADIUS = 10.0;
    private static final double EDGE_TOL     = 5.0;

    private Node  lastSelectedNode  = null;
    private Edge  lastSelectedEdge  = null;
    private Agent lastSelectedAgent = null;

    // Position mémorisée lors d'un clic dans le vide
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

    // Callback notifié quand l'utilisateur clique dans le vide
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
        if (linkSource != null) linkSource.isSelected = false;
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

    private void handleLinkingClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked == null) return;

        if (linkSource == null) {
            linkSource = clicked;
            clearAllSelections();
            linkSource.isSelected = true;
            canvas.draw();
        } else {
            if (clicked == linkSource) return;
            linkSource.isSelected = false;
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
            clickedAgent.isSelected = true;
            canvas.draw();
            return;
        }

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode != null) {
            lastSelectedNode = clickedNode;
            clickedNode.isSelected = true;
            canvas.draw();
            return;
        }

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            lastSelectedEdge = clickedEdge;
            clickedEdge.isSelected = true;
            canvas.draw();
            return;
        }

        // Clic dans le vide → mémorise la position pour placement de nœud
        pendingNodeX       = x;
        pendingNodeY       = y;
        hasPendingPosition = true;
        if (emptyClickCallback != null) emptyClickCallback.onEmptyClick(x, y);
        canvas.draw();
    }

    // ================================================================= helpers

    private void clearAllSelections() {
        if (lastSelectedNode  != null) { lastSelectedNode.isSelected  = false; lastSelectedNode  = null; }
        if (lastSelectedEdge  != null) { lastSelectedEdge.isSelected  = false; lastSelectedEdge  = null; }
        if (lastSelectedAgent != null) { lastSelectedAgent.isSelected = false; lastSelectedAgent = null; }
    }

    private Node findNodeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Node node : graph.Nodes)
            if (click.distance(new Point2D(node.x, node.y)) <= NODE_RADIUS)
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

    private Edge findEdgeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (List<Edge> edges : graph.Edges) {
            for (Edge edge : edges) {
                Node n1 = edge.source, n2 = edge.target;
                double l2 = Math.pow(n2.x - n1.x, 2) + Math.pow(n2.y - n1.y, 2);
                if (l2 == 0) continue;
                double t = Math.max(0, Math.min(1,
                        ((x-n1.x)*(n2.x-n1.x) + (y-n1.y)*(n2.y-n1.y)) / l2));
                if (click.distance(new Point2D(n1.x + t*(n2.x-n1.x), n1.y + t*(n2.y-n1.y))) <= EDGE_TOL)
                    return edge;
            }
        }
        return null;
    }

    private Point2D computeAgentPosition(Agent agent) {
        if (agent.currentNode == null) return null;
        if (agent.currentEdge == null) return new Point2D(agent.currentNode.x, agent.currentNode.y);

        Edge   edge       = agent.currentEdge;
        double edgeLength = edge.length;
        double visualDist = agent.distanceTraveledOnEdge;
        if (visualDist >= edgeLength)
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));

        double t    = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node   from = (edge.source == agent.currentNode) ? edge.source : edge.target;
        Node   to   = (from == edge.source)              ? edge.target : edge.source;
        return new Point2D(from.x + t*(to.x-from.x), from.y + t*(to.y-from.y));
    }
}