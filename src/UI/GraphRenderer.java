package UI;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.List;

import controllers.SelectionSystem;
import model.agents.Agent;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Edge;

public class GraphRenderer {

    /** Rayon en pixels d'un nœud (NodeRenderer.TAILLE = 30 → rayon = 15). */
    private static final int NODE_RADIUS = 15;

    /** Taille de l'agent en pixels (AgentRenderer.TAILLE = 10). */
    private static final int AGENT_SIZE = 10;

    private Graph graph;
    private List<Agent> Agents;

    private final NodeRenderer nodeRenderer = new NodeRenderer();
    private final EdgeRenderer edgeRenderer = new EdgeRenderer();
    private final AgentRenderer agentRenderer = new AgentRenderer();

    private final SelectionSystem selectionSystem;

    public GraphRenderer(Graph graph, List<Agent> agents, SelectionSystem selectionSystem) {
        this.graph = graph;
        this.Agents = agents;
        this.selectionSystem = selectionSystem;
    }

    public void render(Graph graph, List<Agent> agents, Graphics2D g2) {
        this.graph = graph;
        this.Agents = agents;

        // Anticrénelage
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        renderEdges(g2);
        renderNodes(g2);
        renderAgents(g2);
    }

    private void renderEdges(Graphics2D g2) {
        for (List<Edge> edgeList : graph.Edges) {
            for (Edge edge : edgeList) {
                boolean isSelected = (edge == selectionSystem.getSelectedEdge());

                // Surbrillance de sélection : trait plus épais + couleur orange
                if (isSelected) {
                    g2.setStroke(new BasicStroke(3.5f));
                    g2.setColor(new Color(255, 160, 0));
                    g2.drawLine(
                            (int) edge.source.x, (int) edge.source.y,
                            (int) edge.target.x, (int) edge.target.y);
                    g2.setStroke(new BasicStroke(1f));
                }

                // Délégation au EdgeRenderer
                edgeRenderer.drawEdge(
                        g2,
                        (int) edge.source.x, (int) edge.source.y,
                        (int) edge.target.x, (int) edge.target.y,
                        edge.id,
                        edge.state);

                // Indicateur de sens pour les arêtes unidirectionnelles
                if (!edge.direction) {
                    renderDirectionArrow(g2, edge);
                }
            }
        }
    }

    private void renderDirectionArrow(Graphics2D g2, Edge edge) {
        int mx = (int) ((edge.source.x + edge.target.x) / 2);
        int my = (int) ((edge.source.y + edge.target.y) / 2);

        double angle = Math.atan2(edge.target.y - edge.source.y,
                edge.target.x - edge.source.x);
        double headAng = Math.PI / 6;
        int len = 8;
        int head = 7;

        int x2 = mx + (int) (len * Math.cos(angle));
        int y2 = my + (int) (len * Math.sin(angle));
        int x1 = mx - (int) (len * Math.cos(angle));
        int y1 = my - (int) (len * Math.sin(angle));

        g2.setColor(Color.DARK_GRAY);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawLine(x1, y1, x2, y2);
        g2.drawLine(x2, y2,
                x2 - (int) (head * Math.cos(angle - headAng)),
                y2 - (int) (head * Math.sin(angle - headAng)));
        g2.drawLine(x2, y2,
                x2 - (int) (head * Math.cos(angle + headAng)),
                y2 - (int) (head * Math.sin(angle + headAng)));
        g2.setStroke(new BasicStroke(1f));
    }

    private void renderNodes(Graphics2D g2) {
        for (Node node : graph.Nodes) {
            boolean isSelected = (node == selectionSystem.getSelectedNode());

            // Halo de sélection dessiné AVANT le nœud
            if (isSelected) {
                int margin = 6;
                g2.setColor(new Color(255, 165, 0, 90));
                g2.fillOval(
                        (int) node.x - NODE_RADIUS - margin,
                        (int) node.y - NODE_RADIUS - margin,
                        (NODE_RADIUS + margin) * 2,
                        (NODE_RADIUS + margin) * 2);
            }

            // NodeRenderer.drawNode() attend le coin supérieur gauche de l'oval.
            // node.x / node.y représentent le centre → on décale de -NODE_RADIUS.
            nodeRenderer.drawNode(
                    g2,
                    (int) node.x - NODE_RADIUS,
                    (int) node.y - NODE_RADIUS,
                    node.id,
                    node.state);
        }
    }

    private void renderAgents(Graphics2D g2) {
        for (Agent agent : Agents) {
            Point2D.Float pos = computeAgentPosition(agent);
            if (pos == null)
                continue;

            boolean isSelected = (agent == selectionSystem.getSelectedAgent());

            // Halo si l'agent est sélectionné
            if (isSelected) {
                int halo = 8;
                g2.setColor(new Color(0, 180, 255, 100));
                g2.fillOval(
                        (int) pos.x - halo,
                        (int) pos.y - halo,
                        halo * 2,
                        halo * 2);
            }

            // AgentRenderer.drawAgent() attend le coin sup. gauche → centrage
            agentRenderer.drawAgent(
                    g2,
                    (int) pos.x - AGENT_SIZE / 2,
                    (int) pos.y - AGENT_SIZE / 2,
                    agent.id,
                    agent.state);
        }
    }

    private Point2D.Float computeAgentPosition(Agent agent) {
        if (agent.currentNode == null)
            return null;

        if (agent.currentEdge == null) {
            return new Point2D.Float(agent.currentNode.x, agent.currentNode.y);
        }

        Edge edge = agent.currentEdge;
        
        // --- NOUVEAU : Ajustement visuel pour les bouchons ---
        double visualDist = agent.distanceTraveledOnEdge;
        
        // Si l'agent est arrivé au bout de l'arête et attend la permission d'entrer
        if (visualDist >= edge.length) {
            // On le dessine juste avant le noeud (on soustrait le rayon du noeud et la taille de l'agent)
            visualDist = Math.max(0, edge.length - NODE_RADIUS - (AGENT_SIZE / 2));
        }
        
        double t = (edge.length > 0)
                ? Math.min(visualDist / edge.length, 1.0)
                : 1.0;
        // -----------------------------------------------------

        Node from = (edge.source == agent.currentNode) ? edge.source : edge.target;
        Node to = (from == edge.source) ? edge.target : edge.source;

        float px = (float) (from.x + t * (to.x - from.x));
        float py = (float) (from.y + t * (to.y - from.y));
        return new Point2D.Float(px, py);
    }
}