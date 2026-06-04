package UI.renderers;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import model.agents.Agent;
import model.graph.Edge;
import model.graph.Node;

public class AgentRenderer implements AgentRendering {
    private final int NODE_RADIUS = 15;
    private final int AGENT_RADIUS = 10;
    private final float EDGE_WIDTH = 8.0f;

    @Override
    public void drawAgent(GraphicsContext gc, Agent agent) {

        Point2D pos = computeAgentPosition(agent);
        if (pos == null)
            return;
        double x = pos.getX() - (AGENT_RADIUS / 2);
        double y = pos.getY() - (AGENT_RADIUS / 2);

        if (agent.isSelected()) {
            // Halo de selection
            gc.setFill(Color.YELLOWGREEN);
            gc.fillOval(x - 3, y - 3, AGENT_RADIUS + 6, AGENT_RADIUS + 6);

            Node auxNode = null;

            // Affichage du chemin de l'agent sur l'arete
            if (agent.getCurrentEdge() != null && agent.getCurrentNode() != null) {
                gc.setStroke(Color.BLUE);
                gc.setLineWidth(EDGE_WIDTH / 3);
                if (agent.getCurrentEdge().source == agent.getCurrentNode()) {
                    gc.strokeLine(x + AGENT_RADIUS / 2, y + AGENT_RADIUS / 2, agent.getCurrentEdge().target.getX(),
                            agent.getCurrentEdge().target.getY());
                    auxNode = agent.getCurrentEdge().target;
                } else {
                    gc.strokeLine(x + AGENT_RADIUS / 2, y + AGENT_RADIUS / 2, agent.getCurrentEdge().source.getX(),
                            agent.getCurrentEdge().source.getY());
                    auxNode = agent.getCurrentEdge().source;
                }
            }

            // Affichage du chemin sur le reste des aretes
            if (auxNode != null) {
                gc.setFill(Color.BLUE);
                gc.fillOval(auxNode.getX() - AGENT_RADIUS / 3, auxNode.getY() - AGENT_RADIUS / 3, AGENT_RADIUS / 1.5,
                        AGENT_RADIUS / 1.5);
                for (Node node : agent.getPath()) {
                    gc.strokeLine(auxNode.getX(), auxNode.getY(), node.getX(),
                            node.getY());
                    auxNode = node;
                    gc.setFill(Color.BLUE);
                    gc.fillOval(auxNode.getX() - AGENT_RADIUS / 3, auxNode.getY() - AGENT_RADIUS / 3, AGENT_RADIUS / 1.5,
                            AGENT_RADIUS / 1.5);
                }
            }
        }

        switch (agent.getState()) {
            case AVAILABLE:
                gc.setFill(Color.GREEN);
                break;
            case CALCULATING:
                gc.setFill(Color.RED);
                break;
            case WAITING:
                gc.setFill(Color.YELLOW);
                break;
            case RUNNING:
                gc.setFill(Color.BLUE);
                break;
            case OUT:
                return;
        }

        gc.fillOval(x, y, AGENT_RADIUS, AGENT_RADIUS);

        // etickette
        gc.setFill(Color.BLACK);
        gc.fillText("id: " + agent.getId(), x + 5, y + 10);
    }

    private Point2D computeAgentPosition(Agent agent) {
        if (agent.getCurrentNode() == null)
            return null;

        // L'agent est sur un nœud, pas sur une arête
        if (agent.getCurrentEdge() == null) {
            return new Point2D(agent.getCurrentNode().getX(), agent.getCurrentNode().getY());
        }

        Edge edge = agent.getCurrentEdge();
        double edgeLength = edge.length;

        // t = progression de 0.0 (source) à 1.0 (target)
        double visualDist = agent.getDistanceTraveledOnEdge();
        if (visualDist >= edgeLength) {
            // On le dessine juste avant le noeud (on soustrait le rayon du noeud et la
            // taille de l'agent)
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;

        // ← FIX : "from" est toujours edge.source, "to" toujours edge.target
        // La direction de marche est gérée par distanceTraveledOnEdge
        // (qui diminue quand isRetreating=true)
        Node from = (edge.source == agent.getCurrentNode()) ? edge.source : edge.target;
        Node to = (from == edge.source) ? edge.target : edge.source;

        double px = from.getX() + t * (to.getX() - from.getX());
        double py = from.getY() + t * (to.getY() - from.getY());

        return new Point2D(px, py);
    }
}
