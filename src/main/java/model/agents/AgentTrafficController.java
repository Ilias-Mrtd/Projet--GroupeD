package model.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.agents.Agent.*;
import model.graph.*;
import simulationEngine.algorithm.*;

/**
 * Intelligent subsystem handling pathfinding algorithms, map reservations,
 * detour procedures, and deadlock-prevention logic for warehouse agents.
 * * @author Group D
 */
public class AgentTrafficController implements Serializable {
    private static final long serialVersionUID = 1L;

    public static void startNextObjective(Agent agent) {
        if (!agent.getObjectives().isEmpty()) {
            agent.setDestination(agent.getObjectives().remove(0));
            agent.setState(agentState.CALCULATING);
            if (agent.getGraph() != null && agent.getCurrentNode() != null) {
                AbstractAlgorithm calculator = getCalculator(agent);
                if (calculator.getPath().isEmpty() && agent.getCurrentNode().getId() != agent.getDestination().getId()) {
                    agent.logMsg("❌ NO PATH to node " + agent.getDestination().getId() + "! Objective abandoned.");
                    agent.incrementAbandonedObjectives(); 
                    startNextObjective(agent); 
                    return;
                }
                agent.setPath(calculator.getPath()); 
                makeReservations(agent);
            }
            agent.setState(agentState.RUNNING); 
            agent.setCurrentPatience(agent.getMaxPatience());
            agent.logMsg(">>> En route to objective: Node " + agent.getDestination().getId());
        } else {
            handleEndBehavior(agent);
        }
    }

    public static void handleEndBehavior(Agent agent) {
        agent.logMsg("Completed all objectives. Final behavior: " + agent.getEndBehavior());
        clearReservations(agent);
        switch (agent.getEndBehavior()) {
            case STOP: 
                agent.setState(agentState.AVAILABLE); 
                break;
            case REMOVE: 
                agent.setState(agentState.OUT); 
                if (agent.getCurrentNode() != null) agent.getCurrentNode().leave(); 
                agent.logMsg("Left the simulation."); 
                break;
            case RANDOM_WANDER: 
                agent.setState(agentState.AVAILABLE); 
                if (agent.getCurrentNode() != null) agent.getCurrentNode().leave(); 
                break;
        }
    }

    public static void handlePatienceDecrement(Agent agent, double deltaTime) {
        agent.addWaitTime(deltaTime);
        int decrease = switch (agent.getAgentBehavior()) {
            case VIP -> 5;
            case HURRIED -> 3;
            case PATIENT, BROKEN -> 1;
        };
        if (agent.getCurrentNode() == agent.getStartingNode() && agent.getCurrentEdge() == null) {
            if (Math.random() < 0.5) agent.setCurrentPatience(agent.getCurrentPatience() - decrease);
        } else {
            agent.setCurrentPatience(agent.getCurrentPatience() - decrease * 2);
        }

        if (agent.getCurrentPatience() <= 0) {
            if (agent.isRetreating()) {
                agent.getCurrentNode().removeQueue(agent);
            } else if (agent.getCurrentEdge() == null && !agent.getPath().isEmpty()) {
                Edge nextEdge = findEdgeBetween(agent, agent.getCurrentNode(), agent.getPath().get(0));
                if (nextEdge != null) nextEdge.removeQueue(agent);
            } else if (agent.getCurrentEdge() != null) {
                Node targetNode = (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) ? agent.getCurrentEdge().getTarget() : agent.getCurrentEdge().getSource();
                targetNode.removeQueue(agent);
            }

            if (agent.getCurrentEdge() != null) {
                agent.logMsg("!!! Lost patience, REVERSING on the edge !!!");
                agent.setRetreating(true); 
                agent.setState(agentState.RUNNING);
            } else {
                applyDetour(agent);
            }
        }
    }

    public static void makeReservations(Agent agent) {
        if (agent.getAgentBehavior() == agentBehavior.BROKEN) return;
        clearReservations(agent);
        Node prev = agent.getCurrentNode();
        for (Node n : agent.getPath()) {
            n.setExpectedOccupants(n.getExpectedOccupants() + 1);
            agent.getReservedNodes().add(n);
            Edge e = findEdgeBetween(agent, prev, n);
            if (e != null) { 
                e.setExpectedOccupants(e.getExpectedOccupants() + 1); 
                agent.getReservedEdges().add(e); 
            }
            prev = n;
        }
    }

    public static void clearReservations(Agent agent) {
        for (Node n : agent.getReservedNodes()) { 
            if (n.getExpectedOccupants() > 0) n.setExpectedOccupants(n.getExpectedOccupants() - 1); 
        }
        agent.getReservedNodes().clear();
        for (Edge e : agent.getReservedEdges()) { 
            if (e.getExpectedOccupants() > 0) e.setExpectedOccupants(e.getExpectedOccupants() - 1); 
        }
        agent.getReservedEdges().clear();
    }

    public static Edge findEdgeBetween(Agent agent, Node s, Node t) {
        if (agent.getGraph() == null) return null;
        int index = agent.getGraph().getNodes().indexOf(s);
        if (index != -1) {
            for (Edge e : agent.getGraph().getEdges().get(index)) {
                if (e.getSource() == t || e.getTarget() == t) return e;
            }
        }
        return null;
    }

    public static AbstractAlgorithm getCalculator(Agent agent) {
        if (agent.getAlgoType() == AlgoType.DIJKSTRA) {
            agent.logMsg("Calculating path (Forced: Dijkstra)");
            return new Dijkstra(agent.getGraph(), agent.getCurrentNode(), agent.getDestination());
        } else if (agent.getAlgoType() == AlgoType.ASTAR) {
            agent.logMsg("Calculating path (Forced: A*)");
            return new AStar(agent.getGraph(), agent.getCurrentNode(), agent.getDestination());
        } else {
            if (Math.random() < 0.5) {
                agent.logMsg("Calculating path (Random -> Dijkstra)");
                return new Dijkstra(agent.getGraph(), agent.getCurrentNode(), agent.getDestination());
            } else {
                agent.logMsg("Calculating path (Random -> A*)");
                return new AStar(agent.getGraph(), agent.getCurrentNode(), agent.getDestination());
            }
        }
    }

    public static boolean isPathClearAhead(Agent agent, int depth) {
        if (agent.getAgentBehavior() == agentBehavior.HURRIED || agent.getAgentBehavior() == agentBehavior.VIP) return true;
        if (agent.getPath().size() < 2) return true;

        Node prev = agent.getPath().get(0);
        int limit = Math.min(depth, agent.getPath().size());

        for (int i = 1; i < limit; i++) {
            Node nextNode = agent.getPath().get(i);
            Edge nextEdge = findEdgeBetween(agent, prev, nextNode);
            if (nextEdge != null && nextEdge.getCurrentOccupants() >= nextEdge.getCapacity()) return false;
            if (nextNode.getCurrentOccupants() + nextNode.getIncomingOccupants() >= nextNode.getCapacity()) return false;
            prev = nextNode;
        }
        return true;
    }

    public static void applyDetour(Agent agent) {
        agent.incrementDetoursTaken();
        agent.logMsg("!!! Looking for a detour to unblock the situation !!!");
        List<Node> validDetours = new ArrayList<>();
        List<Node> fallbackDetours = new ArrayList<>();

        int index = agent.getGraph().getNodes().indexOf(agent.getCurrentNode());
        if (index != -1) {
            for (Edge e : agent.getGraph().getEdges().get(index)) {
                Node neighbor = null;
                if (!e.hasDirection()) { 
                    if (e.getSource() == agent.getCurrentNode()) { neighbor = e.getTarget(); } else continue; 
                } else { 
                    neighbor = (e.getSource() == agent.getCurrentNode()) ? e.getTarget() : e.getSource(); 
                }

                if (neighbor == null) continue;
                if (!agent.getPath().isEmpty() && neighbor.getId() == agent.getPath().get(0).getId()) continue;

                if (neighbor.getState() != Node.nodeState.FULL && !neighbor.isUnderConstruction()) {
                    if (agent.getPreviousNode() != null && neighbor.getId() == agent.getPreviousNode().getId()) { 
                        fallbackDetours.add(neighbor); 
                    } else { 
                        validDetours.add(neighbor); 
                    }
                }
            }
        }

        Node detour = null;
        if (!validDetours.isEmpty()) { detour = validDetours.get((int) (Math.random() * validDetours.size())); } 
        else if (!fallbackDetours.isEmpty()) { detour = fallbackDetours.get((int) (Math.random() * fallbackDetours.size())); }

        if (detour != null) {
            agent.logMsg("↪️ Taking a random detour to Node " + detour.getId());
            agent.getPath().clear(); 
            agent.getPath().add(detour); 
            makeReservations(agent);
            agent.setState(agentState.RUNNING); 
            agent.setCurrentPatience(agent.getMaxPatience());
        } else {
            agent.logMsg("No street to detour, recalculating route...");
            AbstractAlgorithm calculator = getCalculator(agent);
            if (calculator.getPath().isEmpty() && agent.getCurrentNode().getId() != agent.getDestination().getId()) {
                agent.logMsg("❌ Completely blocked towards node " + agent.getDestination().getId() + ". Objective abandoned!");
                agent.incrementAbandonedObjectives(); 
                startNextObjective(agent); 
                return;
            }
            agent.setPath(calculator.getPath()); 
            makeReservations(agent);
            agent.setState(agentState.RUNNING); 
            agent.setCurrentPatience(agent.getMaxPatience());
        }
    }
}