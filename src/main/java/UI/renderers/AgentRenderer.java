package UI.renderers;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import model.agents.Agent;
import model.graph.Edge;
import model.graph.Node;

public class AgentRenderer implements AgentRendering {
    private final double NODE_RADIUS = Node.RADIUS;
    private final int AGENT_RADIUS = 10;
    private final float EDGE_WIDTH = 8.0f;
    private Color agentColor = Color.GREEN;

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
                if (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) {
                    gc.strokeLine(x + AGENT_RADIUS / 2, y + AGENT_RADIUS / 2, agent.getCurrentEdge().getTarget().getX(),
                            agent.getCurrentEdge().getTarget().getY());
                    auxNode = agent.getCurrentEdge().getTarget();
                } else {
                    gc.strokeLine(x + AGENT_RADIUS / 2, y + AGENT_RADIUS / 2, agent.getCurrentEdge().getSource().getX(),
                            agent.getCurrentEdge().getSource().getY());
                    auxNode = agent.getCurrentEdge().getSource();
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
                    gc.fillOval(auxNode.getX() - AGENT_RADIUS / 3, auxNode.getY() - AGENT_RADIUS / 3,
                            AGENT_RADIUS / 1.5,
                            AGENT_RADIUS / 1.5);
                }
            }
        }

        switch (agent.getState()) {
            case AVAILABLE:
                agentColor = Color.GREEN;
                break;
            case CALCULATING:
                agentColor = Color.RED;
                break;
            case WAITING:
                agentColor = Color.YELLOW;
                break;
            case RUNNING:
                agentColor = Color.BLUE;
                break;
            case OUT:
                agentColor = Color.BLACK;
                break;
        }

        switch (agent.getAgentBehavior()) {
            case HURRIED:
                agentColor = agentColor.interpolate(Color.RED, 0.5);
                break;
            case PATIENT:
                break;
            case BROKEN:
                agentColor = agentColor.interpolate(Color.BLACK, 0.5);
                break;
        }

        gc.setFill(agentColor);

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
        double edgeLength = edge.getLength();

        // t = progression de 0.0 (getSource()) à 1.0 (getTarget())
        double visualDist = agent.getDistanceTraveledOnEdge();
        if (visualDist >= edgeLength) {
            // On le dessine juste avant le noeud (on soustrait le rayon du noeud et la
            // taille de l'agent)
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;

        // ← FIX : "from" est toujours edge.getSource(), "to" toujours edge.getTarget()
        // La direction de marche est gérée par distanceTraveledOnEdge
        // (qui diminue quand isRetreating=true)
        Node from = (edge.getSource() == agent.getCurrentNode()) ? edge.getSource() : edge.getTarget();
        Node to = (from == edge.getSource()) ? edge.getTarget() : edge.getSource();

        double px = from.getX() + t * (to.getX() - from.getX());
        double py = from.getY() + t * (to.getY() - from.getY());

        return new Point2D(px, py);
    }
}
