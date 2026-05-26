package controllers;

import java.awt.geom.Point2D;
import java.util.List;

import model.agents.Agent;
import model.graph.*;

public class SelectionSystem {

    private static final double NODE_RADIUS = 15;
    private static final double EDGE_RADIUS = 6;
    private static final double AGENT_RADIUS = 8;

    private Object selectedObject;

    public Object selectAct(Point2D.Float clickPosition,
            Graph graph,
            List<Agent> agents) {

        float x = clickPosition.x;
        float y = clickPosition.y;

        // agents
        for (Agent agent : agents) {

            Point2D.Float pos = getAgentPosition(agent);

            if (pos == null) {
                continue;
            }

            if (distance(x, y, pos.x, pos.y) <= AGENT_RADIUS) {
                selectedObject = agent;
                return agent;
            }
        }

        // nodes
        for (Node node : graph.Nodes) {

            if (distance(x, y, node.x, node.y) <= NODE_RADIUS) {
                selectedObject = node;
                return node;
            }
        }

        // edges
        for (List<Edge> edges : graph.Edges) {

            for (Edge edge : edges) {

                if (distanceToEdge(x, y, edge) <= EDGE_RADIUS) {
                    selectedObject = edge;
                    return edge;
                }
            }
        }

        selectedObject = null;
        return null;
    }

    public Object getSelectedObject() {
        return selectedObject;
    }

    public Node getSelectedNode() {

        if (selectedObject instanceof Node) {
            return (Node) selectedObject;
        }

        return null;
    }

    public Edge getSelectedEdge() {

        if (selectedObject instanceof Edge) {
            return (Edge) selectedObject;
        }

        return null;
    }

    public Agent getSelectedAgent() {

        if (selectedObject instanceof Agent) {
            return (Agent) selectedObject;
        }

        return null;
    }

    private double distance(double x1, double y1,
            double x2, double y2) {

        return Math.hypot(x2 - x1, y2 - y1);
    }

    private double distanceToEdge(float px, float py, Edge edge) {

        double x1 = edge.source.x;
        double y1 = edge.source.y;

        double x2 = edge.target.x;
        double y2 = edge.target.y;

        double dx = x2 - x1;
        double dy = y2 - y1;

        double t = ((px - x1) * dx +
                (py - y1) * dy) / (dx * dx + dy * dy);

        t = Math.max(0, Math.min(1, t));

        double closestX = x1 + t * dx;
        double closestY = y1 + t * dy;

        return distance(px, py, closestX, closestY);
    }

    private Point2D.Float getAgentPosition(Agent agent) {

        if (agent.currentNode == null) {
            return null;
        }

        if (agent.currentEdge == null) {

            return new Point2D.Float(
                    agent.currentNode.x,
                    agent.currentNode.y);
        }

        Edge edge = agent.currentEdge;

        double t = agent.distanceTraveledOnEdge / edge.length;

        if (t > 1) {
            t = 1;
        }

        float x = (float) (edge.source.x +
                t * (edge.target.x - edge.source.x));

        float y = (float) (edge.source.y +
                t * (edge.target.y - edge.source.y));

        return new Point2D.Float(x, y);
    }
}