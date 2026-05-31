package controllers;

import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.geometry.Point2D;
import model.graph.*;
import model.agents.Agent;

import java.util.List;

import UI.GraphCanvas;

/**
 * SelectionSystem — gère la sélection d'éléments ET le mode "liaison d'arête".
 *
 * Mode normal   : clic gauche sélectionne un nœud / arête / agent.
 * Mode LINKING  : activé depuis PropertiesPanel via startEdgeLinking().
 *                 Le 1er clic gauche fixe le nœud source (surligné en orange).
 *                 Le 2e  clic gauche fixe le nœud cible → callback onEdgeLink.
 *                 Clic droit ou Échap annule le mode.
 */
public class SelectionSystem {

    // ------------------------------------------------------------------ deps
    private final Graph graph;
    private final GraphCanvas canvas;
    private final List<Agent> agents;

    // ---------------------------------------------------------------- radii
    private static final double NODE_RADIUS  = 30.0;
    private static final double AGENT_RADIUS = 10.0;
    private static final double EDGE_TOL     = 5.0;

    // -------------------------------------------------------- sélection courante
    private Node  lastSelectedNode  = null;
    private Edge  lastSelectedEdge  = null;
    private Agent lastSelectedAgent = null;

    // -------------------------------------------------------- mode liaison arête
    public enum Mode { NORMAL, LINKING_EDGE }
    private Mode mode = Mode.NORMAL;

    /** Premier nœud choisi pendant LINKING_EDGE */
    private Node linkSource = null;

    /** Callback appelé quand les deux nœuds sont choisis. */
    @FunctionalInterface
    public interface EdgeLinkCallback {
        void onEdgeLink(Node source, Node target);
    }
    private EdgeLinkCallback edgeLinkCallback = null;

    // ---------------------------------------------------------------- ctor
    public SelectionSystem(Graph graph, List<Agent> agents, GraphCanvas canvas) {
        this.graph   = graph;
        this.agents  = agents;
        this.canvas  = canvas;
    }

    // ========================================================= API publique

    /** Lance le mode "choisir 2 nœuds pour créer une arête". */
    public void startEdgeLinking(EdgeLinkCallback callback) {
        this.mode             = Mode.LINKING_EDGE;
        this.linkSource       = null;
        this.edgeLinkCallback = callback;
        clearAllSelections();
        canvas.draw();
        System.out.println("[SelectionSystem] Mode LINKING_EDGE activé — cliquez sur le nœud SOURCE.");
    }

    /** Annule le mode liaison et revient en mode normal. */
    public void cancelEdgeLinking() {
        if (linkSource != null) linkSource.isSelected = false;
        mode             = Mode.NORMAL;
        linkSource       = null;
        edgeLinkCallback = null;
        canvas.draw();
        System.out.println("[SelectionSystem] Mode LINKING_EDGE annulé.");
    }

    public Mode getMode()              { return mode; }
    public Node getLastSelectedNode()  { return lastSelectedNode; }
    public Edge getLastSelectedEdge()  { return lastSelectedEdge; }
    public Agent getLastSelectedAgent(){ return lastSelectedAgent; }

    // ========================================================= gestion clics

    /**
     * Point d'entrée unique pour tous les clics sur le Canvas.
     * Appelé par GraphCanvas sur MOUSE_CLICKED (gauche ET droit).
     */
    public void handleMouseClick(MouseEvent event) {

        // Clic droit → annule le mode liaison si actif, sinon rien de spécial ici
        if (event.getButton() == MouseButton.SECONDARY) {
            if (mode == Mode.LINKING_EDGE) {
                cancelEdgeLinking();
            }
            return;
        }

        // --- clic gauche ---
        double x = event.getX();
        double y = event.getY();

        if (mode == Mode.LINKING_EDGE) {
            handleLinkingClick(x, y);
        } else {
            handleNormalClick(x, y);
        }
    }

    // -------------------------------------------------------- mode LINKING_EDGE
    private void handleLinkingClick(double x, double y) {
        Node clicked = findNodeAt(x, y);
        if (clicked == null) return; // on ignore les clics dans le vide

        if (linkSource == null) {
            // 1er clic : nœud source
            linkSource = clicked;
            clearAllSelections();
            linkSource.isSelected = true;
            System.out.println("[SelectionSystem] Source choisie : nœud " + linkSource.id
                    + " — cliquez maintenant sur le nœud CIBLE.");
            canvas.draw();
        } else {
            // 2e clic : nœud cible
            if (clicked == linkSource) {
                System.out.println("[SelectionSystem] Source == cible, ignoré.");
                return;
            }
            Node target = clicked;
            System.out.println("[SelectionSystem] Cible choisie : nœud " + target.id
                    + " — création de l'arête.");

            // On repasse en mode normal AVANT le callback (le callback peut re-dessiner)
            linkSource.isSelected = false;
            Mode prevMode = mode;
            mode             = Mode.NORMAL;
            edgeLinkCallback.onEdgeLink(linkSource, target);
            linkSource       = null;
            edgeLinkCallback = null;
            canvas.draw();
        }
    }

    // -------------------------------------------------------- mode NORMAL
    private void handleNormalClick(double x, double y) {
        clearAllSelections();

        Agent clickedAgent = findAgentAt(x, y);
        if (clickedAgent != null) {
            lastSelectedAgent = clickedAgent;
            clickedAgent.isSelected = true;
            System.out.println("[SelectionSystem] Agent " + clickedAgent.id + " sélectionné.");
            canvas.draw();
            return;
        }

        Node clickedNode = findNodeAt(x, y);
        if (clickedNode != null) {
            lastSelectedNode = clickedNode;
            clickedNode.isSelected = true;
            System.out.println("[SelectionSystem] Nœud " + clickedNode.id + " sélectionné.");
            canvas.draw();
            return;
        }

        Edge clickedEdge = findEdgeAt(x, y);
        if (clickedEdge != null) {
            lastSelectedEdge = clickedEdge;
            clickedEdge.isSelected = true;
            System.out.println("[SelectionSystem] Arête " + clickedEdge.id + " sélectionnée.");
        } else {
            System.out.println("[SelectionSystem] Clic dans le vide.");
        }

        canvas.draw();
    }

    // ========================================================= helpers privés

    private void clearAllSelections() {
        if (lastSelectedNode  != null) { lastSelectedNode.isSelected  = false; lastSelectedNode  = null; }
        if (lastSelectedEdge  != null) { lastSelectedEdge.isSelected  = false; lastSelectedEdge  = null; }
        if (lastSelectedAgent != null) { lastSelectedAgent.isSelected = false; lastSelectedAgent = null; }
    }

    private Node findNodeAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Node node : graph.Nodes) {
<<<<<<< HEAD
            Point2D nodeCenter = new Point2D(node.x, node.y);

            if (clickPoint.distance(nodeCenter) <= NODE_RADIUS) {
=======
            if (click.distance(new Point2D(node.x, node.y)) <= NODE_RADIUS)
>>>>>>> a7ef079 (feat: menu édition graphe - ajout/suppression nœuds, arêtes, agents)
                return node;
        }
        return null;
    }

    private Agent findAgentAt(double x, double y) {
        Point2D click = new Point2D(x, y);
        for (Agent a : agents) {
            Point2D pos = computeAgentPosition(a);
            if (pos != null && click.distance(pos) <= AGENT_RADIUS)
                return a;
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
                        ((x - n1.x) * (n2.x - n1.x) + (y - n1.y) * (n2.y - n1.y)) / l2));
                double projX = n1.x + t * (n2.x - n1.x);
                double projY = n1.y + t * (n2.y - n1.y);
                if (click.distance(new Point2D(projX, projY)) <= EDGE_TOL)
                    return edge;
            }
        }
        return null;
    }

    private Point2D computeAgentPosition(Agent agent) {
        if (agent.currentNode == null) return null;
        if (agent.currentEdge == null)
            return new Point2D(agent.currentNode.x, agent.currentNode.y);

        Edge   edge       = agent.currentEdge;
        double edgeLength = edge.length;
        double visualDist = agent.distanceTraveledOnEdge;
        if (visualDist >= edgeLength)
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));

        double t    = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node   from = (edge.source == agent.currentNode) ? edge.source : edge.target;
        Node   to   = (from == edge.source)              ? edge.target : edge.source;

        return new Point2D(from.x + t * (to.x - from.x),
                from.y + t * (to.y - from.y));
    }
}