package controllers;

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
    private static final double EDGE_TOLERANCE = 5.0;

    private Node lastSelectedNode = null;
    private Edge lastSelectedEdge = null;
    private Agent lastSelectedAgent = null;

    public SelectionSystem(Graph graph, List<Agent> agents, GraphCanvas canvas) {
        this.graph = graph;
        this.agents = agents;
        this.canvas = canvas;
    }

    /**
     * Méthode appelée automatiquement lors d'un clic sur le Canvas
     */
    public void handleMouseClick(MouseEvent event) {

        double clickX = event.getX();
        double clickY = event.getY();

        // On s'assure de decocher l'element' precedement selectionner
        if (lastSelectedNode != null) {
            lastSelectedNode.isSelected = false;
        }
        if (lastSelectedEdge != null) {
            lastSelectedEdge.isSelected = false;
        }
        if (lastSelectedAgent != null) {
            lastSelectedAgent.isSelected = false;
        }

        Agent clickedAgent = findAgentAt(clickX, clickY);

        if (clickedAgent != null) {
            lastSelectedAgent = clickedAgent;
            lastSelectedAgent.isSelected = true;
            System.out.println("Agent " + clickedAgent.id + " sélectionné !");
            lastSelectedAgent = clickedAgent;
        } else {
            Node clickedNode = findNodeAt(clickX, clickY);

            if (clickedNode != null) {
                clickedNode.isSelected = true;
                System.out.println("Nœud sélectionné : " + clickedNode.id);
                lastSelectedNode = clickedNode;
            } else {
                Edge clickedEdge = findEdgeAt(clickX, clickY);

                if (clickedEdge != null) {
                    clickedEdge.isSelected = true;
                    System.out.println("Arête sélectionnée : " + clickedEdge.id);
                    lastSelectedEdge = clickedEdge;
                } else {
                    System.out.println("Clic dans le vide");
                }
            }
        }

        // redessiner pour afficher la sélection
        canvas.draw();
    }

    /**
     * Parcourt les nœuds, agents et aretes pour trouver le click
     */
    private Node findNodeAt(double x, double y) {
        Point2D clickPoint = new Point2D(x, y);

        for (Node node : graph.Nodes) {
            Point2D nodeCenter = new Point2D(node.x, node.y);

            if (clickPoint.distance(nodeCenter) <= NODE_RADIUS) {
                System.out.println("Noeud " + node.id + "cliquer.");
                return node;
            }
        }

        return null;
    }

    // PROBLEME : Agent sur noeud
    private Agent findAgentAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Agent agent : agents) {
            Point2D agentPos = computeAgentPosition(agent); // Utilise la même logique que ton AgentRenderer
            if (agentPos != null && click.distance(agentPos) <= AGENT_RADIUS) {
                return agent;
            }
        }
        return null;
    }

    private Edge findEdgeAt(double x, double y) {
        Point2D click = new Point2D(x, y);

        for (List<Edge> edges : graph.Edges) {
            for (Edge edge : edges) {
                Node n1 = edge.source;
                Node n2 = edge.target;

                // Longueur au carré du segment
                double l2 = Math.pow(n2.x - n1.x, 2) + Math.pow(n2.y - n1.y, 2);

                if (l2 == 0)
                    continue; // L'arête est un point (anomalie)

                // Calcul du facteur de projection t (borné entre 0 et 1)
                double t = Math.max(0, Math.min(1,
                        ((x - n1.x) * (n2.x - n1.x) + (y - n1.y) * (n2.y - n1.y)) / l2));

                // Coordonnées du point projeté sur le segment
                double projX = n1.x + t * (n2.x - n1.x);
                double projY = n1.y + t * (n2.y - n1.y);

                // Si la distance au segment est inférieure à la tolérance, c'est touché !
                if (click.distance(new Point2D(projX, projY)) <= EDGE_TOLERANCE) {
                    return edge;
                }
            }
        }
        return null;
    }

    // trouver la position de l'agent
    private Point2D computeAgentPosition(Agent agent) {
        if (agent.currentNode == null)
            return null;

        // L'agent est sur un nœud, pas sur une arête
        if (agent.currentEdge == null) {
            return new Point2D(agent.currentNode.x, agent.currentNode.y);
        }

        Edge edge = agent.currentEdge;
        double edgeLength = edge.length;

        // t = progression de 0.0 (source) à 1.0 (target)
        double visualDist = agent.distanceTraveledOnEdge;
        if (visualDist >= edgeLength) {
            // On le dessine juste avant le noeud (on soustrait le rayon du noeud et la
            // taille de l'agent)
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;

        // ← FIX : "from" est toujours edge.source, "to" toujours edge.target
        // La direction de marche est gérée par distanceTraveledOnEdge
        // (qui diminue quand isRetreating=true)
        Node from = (edge.source == agent.currentNode) ? edge.source : edge.target;
        Node to = (from == edge.source) ? edge.target : edge.source;

        double px = from.x + t * (to.x - from.x);
        double py = from.y + t * (to.y - from.y);

        return new Point2D(px, py);
    }
}