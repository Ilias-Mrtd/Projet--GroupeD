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
    private Color agentColor = Color.web("#69F0AE");

    /**
     * Renders the agent entity on the graphical canvas, including its selection halo, projected path, and state colors.
     * @param gc The active graphics context of the canvas.
     * @param agent The target tracking agent to draw.
     */
    @Override
    public void drawAgent(GraphicsContext gc, Agent agent) {

        Point2D pos = computeAgentPosition(agent);
        if (pos == null) return;
        
        double x = pos.getX() - (AGENT_RADIUS / 2.0);
        double y = pos.getY() - (AGENT_RADIUS / 2.0);

        if (agent.isSelected()) {
            // Draw selection halo highlight
            gc.setFill(Color.web("#00E5FF"));
            gc.fillOval(x - 4, y - 4, AGENT_RADIUS + 8, AGENT_RADIUS + 8);

            Node auxNode = null;

            // Draw current projected movement path sequence
            if (agent.getCurrentEdge() != null && agent.getCurrentNode() != null) {
                gc.setStroke(Color.web("#00E5FF"));
                gc.setLineWidth(EDGE_WIDTH / 2.0);
                
                // Refactored destination node resolution
                auxNode = (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) 
                        ? agent.getCurrentEdge().getTarget() 
                        : agent.getCurrentEdge().getSource();

                gc.strokeLine(x + (AGENT_RADIUS / 2.0), y + (AGENT_RADIUS / 2.0), auxNode.getX(), auxNode.getY());
            }

            if (auxNode != null) {
                gc.setFill(Color.web("#00E5FF"));
                gc.fillOval(auxNode.getX() - (AGENT_RADIUS / 3.0), auxNode.getY() - (AGENT_RADIUS / 3.0), AGENT_RADIUS / 1.5, AGENT_RADIUS / 1.5);
                
                for (Node node : agent.getPath()) {
                    gc.strokeLine(auxNode.getX(), auxNode.getY(), node.getX(), node.getY());
                    auxNode = node;
                    gc.fillOval(auxNode.getX() - (AGENT_RADIUS / 3.0), auxNode.getY() - (AGENT_RADIUS / 3.0), AGENT_RADIUS / 1.5, AGENT_RADIUS / 1.5);
                }
            }
        }

        // Determine base color mapping based on operational state
        switch (agent.getState()) {
            case AVAILABLE: agentColor = Color.web("#69F0AE"); break; 
            case CALCULATING: agentColor = Color.web("#FF9800"); break; 
            case WAITING: agentColor = Color.web("#FFEB3B"); break; 
            case RUNNING: agentColor = Color.web("#29B6F6"); break; 
            case OUT: agentColor = Color.web("#9E9E9E"); break; 
        }

        // Apply color alterations based on active behavior modifiers
        switch (agent.getAgentBehavior()) {
            case VIP:
                agentColor = Color.web("#E040FB");
                break;
            case HURRIED:
                agentColor = agentColor.interpolate(Color.web("#FF5252"), 0.5); 
                break;
            case PATIENT:
                break;
            case BROKEN:
                agentColor = agentColor.interpolate(Color.web("#212121"), 0.5);
                break;
        }

        // Draw dark background shadow base for depth perception
        gc.setFill(Color.web("#121212"));
        gc.fillOval(x - 1, y - 1, AGENT_RADIUS + 2, AGENT_RADIUS + 2);
        
        gc.setFill(agentColor);
        gc.fillOval(x, y, AGENT_RADIUS, AGENT_RADIUS);

        gc.setFill(Color.web("#FFFFFF"));
        gc.fillText("A" + agent.getId(), x + 5, y + 20);
    }

    /**
     * Interpolates path records to extract live coordinate dimensions mapping tracking vectors.
     * @param agent The simulated tracking agent node entity component.
     * @return A 2D point mapping layout updates on the tracking panel.
     */
    private Point2D computeAgentPosition(Agent agent) {
        if (agent.getCurrentNode() == null) return null;
        if (agent.getCurrentEdge() == null) {
            return new Point2D(agent.getCurrentNode().getX(), agent.getCurrentNode().getY());
        }

        Edge edge = agent.getCurrentEdge();
        double edgeLength = edge.getLength();
        double visualDist = agent.getDistanceTraveledOnEdge();
        
        if (visualDist >= edgeLength) {
            visualDist = Math.max(0, edgeLength - NODE_RADIUS - (AGENT_RADIUS / 2.0));
        }

        double t = (edgeLength > 0) ? Math.min(visualDist / edgeLength, 1.0) : 1.0;
        Node from = (edge.getSource() == agent.getCurrentNode()) ? edge.getSource() : edge.getTarget();
        Node to = (from == edge.getSource()) ? edge.getTarget() : edge.getSource();
        
        return new Point2D(from.getX() + t * (to.getX() - from.getX()), from.getY() + t * (to.getY() - from.getY()));
    }
} 