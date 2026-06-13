package controllers.helpers;

import javafx.geometry.Point2D;
import model.agents.Agent;
import model.graph.*;
import java.util.List;

/**
 * Pure mathematical spatial coordinate processing engine.
 * Centralizes collision vector calculations, line segment projections,
 * boundary threshold checking, and live kinematic node translations
 * without persisting mutable selection states.
 */
public final class SpatialGeometryCalculator {

    private static final double NODE_RADIUS = 30.0;
    private static final double AGENT_RADIUS = 10.0;
    private static final double EDGE_TOL = 5.0;

    /**
     * Private constructor to enforce static utility class design patterns.
     */
    private SpatialGeometryCalculator() {}

    /**
     * Inspects the graph's nodes list to find a node at the click coordinates.
     * Uses a radial boundary box match threshold check.
     *
     * @param graph The topological spatial grid map system containing vertices.
     * @param x     The targeting click horizontal coordinate.
     * @param y     The targeting click vertical coordinate.
     * @return The matched Node instance inside boundary limits, or null if empty space.
     */
    public static Node findNodeAt(Graph graph, double x, double y) {
        Point2D click = new Point2D(x, y);
        return graph.getNodes().stream()
                .filter(node -> click.distance(new Point2D(node.getX(), node.getY())) <= NODE_RADIUS)
                .findFirst()
                .orElse(null);
    }

    /**
     * Inspects the active entities list to find an agent at the click coordinates.
     * Calculates current visual interpolation coordinates before doing a distance check.
     *
     * @param agents The list of active mobile agents on the grid map.
     * @param x      The targeting click horizontal coordinate.
     * @param y      The targeting click vertical coordinate.
     * @return The matched Agent instance inside boundary limits, or null if none hit.
     */
    public static Agent findAgentAt(List<Agent> agents, double x, double y) {
        Point2D click = new Point2D(x, y);
        return agents.stream()
                .filter(a -> {
                    Point2D pos = computeAgentPosition(a);
                    return pos != null && click.distance(pos) <= AGENT_RADIUS;
                })
                .findFirst()
                .orElse(null);
    }

    /**
     * Inspects all active infrastructure connections to find an edge at the click coordinates.
     * Flattens multi-layered arrays to check linear segments.
     *
     * @param graph The topological spatial grid map system containing paths.
     * @param x     The targeting click horizontal coordinate.
     * @param y     The targeting click vertical coordinate.
     * @return The matched routing Edge path segment, or null if none hit.
     */
    public static Edge findEdgeAt(Graph graph, double x, double y) {
        Point2D click = new Point2D(x, y);
        return graph.getEdges().stream()
                .flatMap(List::stream)
                .filter(edge -> isClickCloseToEdge(click, edge))
                .findFirst()
                .orElse(null);
    }

    /**
     * Applies vector dot-product calculations to find the closest perpendicular 
     * projection point on a bounded linear path segment.
     *
     * @param click The 2D coordinate point representing the user mouse click.
     * @param edge  The routing path connection element to project against.
     * @return True if the perpendicular distance falls within allowed margin tolerances.
     */
    private static boolean isClickCloseToEdge(Point2D click, Edge edge) {
        Node n1 = edge.getSource();
        Node n2 = edge.getTarget();
        double x1 = n1.getX();
        double y1 = n1.getY();
        double x2 = n2.getX();
        double y2 = n2.getY();

        double l2 = Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2);
        if (l2 == 0.0) return false;

        double t = ((click.getX() - x1) * (x2 - x1) + (click.getY() - y1) * (y2 - y1)) / l2;
        t = Math.max(0.0, Math.min(1.0, t));

        Point2D projection = new Point2D(x1 + t * (x2 - x1), y1 + t * (y2 - y1));
        return click.distance(projection) <= EDGE_TOL;
    }

    /**
     * Linearly interpolates historical path records to evaluate live multi-dimensional 
     * coordinate matrices for mobile entities traversing routes.
     * Clamps visual bounds dynamically near terminal nodes to avoid visual clipping.
     *
     * @param agent The target simulated entity tracking instance.
     * @return A 2D spatial point mapping actual positioning metrics on the panel canvas.
     */
    public static Point2D computeAgentPosition(Agent agent) {
        if (agent.getCurrentNode() == null) return null;
        if (agent.getCurrentEdge() == null) {
            return new Point2D(agent.getCurrentNode().getX(), agent.getCurrentNode().getY());
        }

        Edge edge = agent.getCurrentEdge();
        double edgeLength = edge.getLength();
        double visualDist = agent.getDistanceTraveledOnEdge();
        
        if (visualDist >= edgeLength) {
            visualDist = Math.max(0.0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0.0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node from = (edge.getSource() == agent.getCurrentNode()) ? edge.getSource() : edge.getTarget();
        Node to = (from == edge.getSource()) ? edge.getTarget() : edge.getSource();
        
        return new Point2D(from.getX() + t * (to.getX() - from.getX()), from.getY() + t * (to.getY() - from.getY()));
    }
}