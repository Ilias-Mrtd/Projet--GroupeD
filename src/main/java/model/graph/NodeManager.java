package model.graph;

import java.io.Serializable;
import model.agents.Agent;
import model.graph.Node.nodeState;

/**
 * Controller subsystem managing intersection bottlenecks, occupancy enforcement thresholds,
 * priority queue sorting algorithms, and construction-driven state modifications for Graph Nodes.
 * * @author Group D
 * @since 2026
 */
public class NodeManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public NodeManager() {}

    /**
     * Modifies the node's construction status. Enabling construction preserves the initial
     * capacity parameters before dropping operational limits down to zero and forcing a FULL status flag.
     * Disabling construction gracefully restores the baseline configuration criteria.
     * * @param node The target Node instance to update.
     * @param underConstruction true to isolate this node for maintenance; false to re-enable it.
     */
    public static void setUnderConstruction(Node node, boolean underConstruction) {
        node.setRawUnderConstruction(underConstruction);
        if (underConstruction) {
            node.setSavedCapacity(node.getCapacity());
            node.setCapacity(0);
            node.setState(nodeState.FULL);
        } else {
            node.setCapacity(node.getSavedCapacity());
            if (node.getCurrentOccupants() < node.getCapacity()) {
                node.setState(nodeState.AVAILABLE);
            }
        }
    }

    /**
     * Evaluates if the total count of active occupants has reached or bypassed the node's maximum capacity.
     * * @param node The target Node instance to check.
     * @return true if the node structural limits are fully saturated; false otherwise.
     */
    public static boolean isFull(Node node) {
        return node.getCurrentOccupants() >= node.getCapacity();
    }

    /**
     * Validates if a given agent is permitted to enter the node based on current capacity
     * availability and priority placement at the front of the waiting line.
     * * @param node The target Node instance to evaluate.
     * @param a The tracking Agent component requesting access validation.
     * @return true if space is clear and the agent holds entry priority; false if blocked.
     */
    public static boolean canEnter(Node node, Agent a) {
        return !isFull(node) && (node.getWaitingQueue().isEmpty() || node.getWaitingQueue().peek() == a);
    }

    /**
     * Processes registration lookup interactions for an agent attempting to merge onto this vertex.
     * Higher-order behavioral profiles like VIP bypass typical structural capacity thresholds, 
     * forcing registration values forward instantly.
     * * @param node The target Node instance to enter.
     * @param a The tracking Agent object requesting physical access clearance.
     * @return true if structural parameters updated and entry was cleared; false if turned away.
     */
    public static boolean tryEnter(Node node, Agent a) {
        if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
            node.getWaitingQueue().remove(a);
            node.setCurrentOccupants(node.getCurrentOccupants() + 1);
            if (isFull(node)) {
                node.setState(nodeState.FULL);
            }
            return true;
        }

        if (canEnter(node, a)) {
            node.getWaitingQueue().remove(a);
            node.setCurrentOccupants(node.getCurrentOccupants() + 1);
            if (isFull(node)) {
                node.setState(nodeState.FULL);
            }
            return true;
        }
        return false;
    }

    /**
     * Forcefully increments the internal occupant counters bypassing any conditional 
     * validation workflows, matching safety status triggers as required.
     * * @param node The target Node instance to update.
     */
    public static void forceEnter(Node node) {
        node.setCurrentOccupants(node.getCurrentOccupants() + 1);
        if (isFull(node)) {
            node.setState(nodeState.FULL);
        }
    }

    /**
     * Decrements the active entity density registry whenever an occupant leaves the node vertex,
     * resetting state descriptors to AVAILABLE when constraints clear up.
     * * @param node The target Node instance being vacated.
     */
    public static void leave(Node node) {
        if (node.getCurrentOccupants() > 0) {
            node.setCurrentOccupants(node.getCurrentOccupants() - 1);
        }
        if (!isFull(node)) {
            node.setState(nodeState.AVAILABLE);
        }
    }

    /**
     * Appends an agent into the structural waiting registration list. Priority rules 
     * ensure that incoming VIP agents jump the queue, positioning themselves ahead of 
     * standard agents while keeping behind already queued VIP elements.
     * * @param node The target Node instance hosting the waiting line.
     * @param a The tracking Agent component requesting queue insertion registration.
     */
    public static void enqueue(Node node, Agent a) {
        if (!node.getWaitingQueue().contains(a)) {
            if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
                int insertIndex = 0;
                for (Agent waiting : node.getWaitingQueue()) {
                    if (waiting.getAgentBehavior() != Agent.agentBehavior.VIP) break;
                    insertIndex++;
                }
                node.getWaitingQueue().add(insertIndex, a);
            } else {
                node.getWaitingQueue().add(a);
            }
        }
    }

    /**
     * Removes an agent immediately from the structural waiting lineup array, canceling pending requests.
     * * @param node The target Node instance hosting the waiting line.
     * @param a The target Agent instance to dismiss from the line.
     */
    public static void removeQueue(Node node, Agent a) {
        node.getWaitingQueue().remove(a);
    }
}