package model.graph;

import java.io.Serializable;
import model.agents.Agent;
import model.graph.Edge.edgeState;

/**
 * Controller subsystem handling traffic execution rules, capacity validation thresholds,
 * intersection queuing priorities, and VIP injection protocols on graph edges.
 * * @author Group D
 */
public class EdgeManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public static boolean isFull(Edge edge) { 
        return edge.getCurrentOccupants() >= edge.getCapacity(); 
    }

    public static boolean canEnter(Edge edge, Agent a) {
        return !isFull(edge) && (edge.getWaitingQueue().isEmpty() || edge.getWaitingQueue().peek() == a);
    }

    public static boolean tryEnter(Edge edge, Agent a) {
        if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
            edge.getWaitingQueue().remove(a);
            edge.setCurrentOccupants(edge.getCurrentOccupants() + 1);
            if (isFull(edge)) { edge.setState(edgeState.FULL); }
            return true;
        }

        if (canEnter(edge, a)) {
            edge.getWaitingQueue().remove(a);
            edge.setCurrentOccupants(edge.getCurrentOccupants() + 1);
            if (isFull(edge)) { edge.setState(edgeState.FULL); }
            return true;
        }
        return false;
    }

    public static void leave(Edge edge) {
        if (edge.getCurrentOccupants() > 0) { 
            edge.setCurrentOccupants(edge.getCurrentOccupants() - 1); 
        }
        if (!isFull(edge)) { 
            edge.setState(edgeState.AVAILABLE); 
        }
    }

    public static void enqueue(Edge edge, Agent a) {
        if (!edge.getWaitingQueue().contains(a)) {
            if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
                int insertIndex = 0;
                for (Agent waiting : edge.getWaitingQueue()) {
                    if (waiting.getAgentBehavior() != Agent.agentBehavior.VIP) break;
                    insertIndex++;
                }
                edge.getWaitingQueue().add(insertIndex, a);
            } else {
                edge.getWaitingQueue().add(a);
            }
        }
    }

    public static void removeQueue(Edge edge, Agent a) { 
        edge.getWaitingQueue().remove(a); 
    }
}