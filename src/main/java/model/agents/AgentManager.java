package model.agents;

import java.io.Serializable;
import model.agents.Agent.*;
import model.graph.*;
import simulationEngine.algorithm.*;

/**
 * Execution engine managing kinetics, coordinate updates, 
 * and core movement frame loops for simulation agents.
 * * @author Group D
 */
public class AgentManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public AgentManager() {}

    public void addObjective(Agent agent, Node dest) {
        agent.getObjectives().add(dest);
        agent.logMsg("New objective received: Node " + dest.getId());
        if (agent.getState() == agentState.AVAILABLE) {
            AgentTrafficController.startNextObjective(agent);
        }
    }

    public void releaseAll(Agent agent) {
        AgentTrafficController.clearReservations(agent);
        if (agent.getCurrentEdge() != null) {
            Node targetNode = (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) ? agent.getCurrentEdge().getTarget() : agent.getCurrentEdge().getSource();
            if (targetNode != null && targetNode.getIncomingOccupants() > 0) { 
                targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1); 
            }
            agent.getCurrentEdge().leave(); 
            agent.getCurrentEdge().removeQueue(agent); 
            agent.setCurrentEdge(null);
        }
        if (agent.getCurrentNode() != null) { 
            agent.getCurrentNode().leave(); 
            agent.getCurrentNode().removeQueue(agent); 
        }
        agent.getPath().clear(); 
        agent.getObjectives().clear(); 
        agent.setState(agentState.OUT);
    }

    public void update(Agent agent, double deltaTime) {
        if (agent.isYieldingToVIP()) {
            if (agent.getState() != agentState.WAITING) {
                agent.setState(agentState.WAITING);
                agent.logMsg("🚓 Sirens heard! Pulling over for VIP...");
            }
            agent.addWaitTime(deltaTime);
            return;
        }

        if (agent.getState() == agentState.RUNNING || agent.getState() == agentState.WAITING || agent.getState() == agentState.CALCULATING) {
            agent.addActiveTime(deltaTime);
        }

        // Délégation de la gestion de la patience au contrôleur de trafic
        if (agent.getState() == agentState.WAITING) {
            AgentTrafficController.handlePatienceDecrement(agent, deltaTime);
            if (agent.getState() == agentState.RUNNING) return; // Reparti suite à un détour
        }

        if (agent.getState() == agentState.RUNNING || agent.getState() == agentState.WAITING) {
            // Check blocage statique sur un nœud
            if (agent.getState() == agentState.RUNNING && agent.getCurrentNode() != null && agent.getCurrentEdge() == null) {
                if (agent.getAuxNode() != null && agent.getCurrentNode() == agent.getAuxNode()) {
                    agent.setBlockedSince(agent.isBlockedSince() + 1);
                    if (agent.isBlockedSince() > 3) {
                        agent.setBlockedSince(0);
                        agent.logMsg("Step 3: Routine recalculation.");
                        agent.getPath().clear(); 
                        agent.getObjectives().add(0, agent.getDestination()); 
                        AgentTrafficController.startNextObjective(agent);
                    }
                } else {
                    agent.setAuxNode(agent.getCurrentNode());
                }
            }

            // Gestion de l'avancement / Entrée sur arête
            if (agent.getCurrentEdge() == null) {
                if (agent.getCurrentNode() != null && agent.getCurrentNode().getCurrentOccupants() > agent.getCurrentNode().getCapacity()) {
                    if (agent.getAgentBehavior() != agentBehavior.VIP) {
                        if (agent.getCongestionTimer() < 2.0) { 
                            agent.setCongestionTimer(agent.getCongestionTimer() + deltaTime);
                            if (agent.getState() != agentState.WAITING) {
                                agent.setState(agentState.WAITING);
                                agent.logMsg("⚠️ Heavy Node Congestion! 2s penalty applied...");
                            }
                            return; 
                        }
                    }
                }
                agent.setCongestionTimer(0.0); 

                if (!agent.getPath().isEmpty()) {
                    if (!AgentTrafficController.isPathClearAhead(agent, 2)) {
                        if (agent.getState() != agentState.WAITING) {
                            agent.setState(agentState.WAITING);
                            agent.logMsg("🧠 Anticipating a traffic jam, doing SMART WAITING.");
                        }
                        return;
                    }

                    Node nextNode = agent.getPath().get(0);
                    Edge nextEdge = AgentTrafficController.findEdgeBetween(agent, agent.getCurrentNode(), nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter(agent)) {
                            if (agent.getReservedEdges().contains(nextEdge)) {
                                if (nextEdge.getExpectedOccupants() > 0) nextEdge.setExpectedOccupants(nextEdge.getExpectedOccupants() - 1);
                                agent.getReservedEdges().remove(nextEdge);
                            }
                            if (agent.getCurrentNode() != null) agent.getCurrentNode().leave();
                            agent.setCurrentEdge(nextEdge); 
                            agent.getPath().remove(0); 
                            agent.setDistanceTraveledOnEdge(0.0f);
                            agent.setState(agentState.RUNNING); 
                            agent.setCurrentPatience(agent.getMaxPatience());
                            nextNode.setIncomingOccupants(nextNode.getIncomingOccupants() + 1);
                            agent.logMsg("🟢 ENTERING towards node " + nextNode.getId());
                        } else {
                            nextEdge.enqueue(agent);
                            if (agent.getState() != agentState.WAITING) agent.setState(agentState.WAITING);
                            return;
                        }
                    }
                } else {
                    if (agent.getCurrentNode() != null && agent.getDestination() != null && agent.getCurrentNode().getId() == agent.getDestination().getId()) {
                        agent.logMsg("✅ Objective Node " + agent.getDestination().getId() + " REACHED!");
                        agent.incrementObjectivesReached();
                    }
                    if (agent.getObjectives().isEmpty()) { AgentTrafficController.handleEndBehavior(agent); } else { AgentTrafficController.startNextObjective(agent); }
                }
            }

            // Physique de déplacement sur l'arête
            if (agent.getCurrentEdge() != null) {
                processEdgeMovement(agent, deltaTime);
            }
        }
    }

    private void processEdgeMovement(Agent agent, double deltaTime) {
        float distMoved = agent.getSpeed() * agent.getCurrentEdge().getSpeedModifier() * (float) deltaTime * 60f;

        if (agent.isRetreating()) {
            agent.setDistanceTraveledOnEdge(agent.getDistanceTraveledOnEdge() - distMoved);
            agent.addDistance(distMoved);

            if (agent.getDistanceTraveledOnEdge() <= 0.0f) {
                agent.setDistanceTraveledOnEdge(0.0f);
                if (agent.getCurrentNode().tryEnter(agent)) {
                    Node targetNode = (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) ? agent.getCurrentEdge().getTarget() : agent.getCurrentEdge().getSource();
                    if (targetNode.getIncomingOccupants() > 0) targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);
                    agent.getCurrentEdge().leave(); 
                    agent.setCurrentEdge(null); 
                    agent.setRetreating(false);
                    agent.logMsg("Returned to node and freed the edge."); 
                    AgentTrafficController.applyDetour(agent);
                } else {
                    agent.getCurrentNode().enqueue(agent);
                    if (agent.getState() != agentState.WAITING) agent.setState(agentState.WAITING);
                }
            }
        } else {
            if (agent.getDistanceTraveledOnEdge() + distMoved <= agent.getCurrentEdge().getLength()) { 
                agent.addDistance(distMoved); 
            } else { 
                agent.addDistance(agent.getCurrentEdge().getLength() - agent.getDistanceTraveledOnEdge()); 
            }

            if (agent.getDistanceTraveledOnEdge() < agent.getCurrentEdge().getLength()) {
                agent.setDistanceTraveledOnEdge(agent.getDistanceTraveledOnEdge() + distMoved);
            }

            if (agent.getDistanceTraveledOnEdge() >= agent.getCurrentEdge().getLength()) {
                agent.setDistanceTraveledOnEdge((float) agent.getCurrentEdge().getLength());
                Node targetNode = (agent.getCurrentEdge().getSource() == agent.getCurrentNode()) ? agent.getCurrentEdge().getTarget() : agent.getCurrentEdge().getSource();

                if (targetNode.tryEnter(agent)) {
                    if (targetNode.getIncomingOccupants() > 0) targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);
                    if (agent.getReservedNodes().contains(targetNode)) {
                        if (targetNode.getExpectedOccupants() > 0) targetNode.setExpectedOccupants(targetNode.getExpectedOccupants() - 1);
                        agent.getReservedNodes().remove(targetNode);
                    }
                    agent.setPreviousNode(agent.getCurrentNode()); 
                    agent.setCurrentNode(targetNode); 
                    agent.getCurrentEdge().leave();
                    agent.setCurrentEdge(null); 
                    agent.setState(agentState.RUNNING); 
                    agent.setCurrentPatience(agent.getMaxPatience());
                    agent.logMsg("📍 ARRIVED at node " + agent.getCurrentNode().getId());

                    if (agent.getPath().isEmpty()) {
                        if (agent.getCurrentNode().getId() == agent.getDestination().getId()) {
                            agent.logMsg("✅ Final Objective Node " + agent.getDestination().getId() + " REACHED!");
                            agent.incrementObjectivesReached();
                            if (!agent.getObjectives().isEmpty()) { AgentTrafficController.startNextObjective(agent); } else { AgentTrafficController.handleEndBehavior(agent); }
                        } else {
                            agent.logMsg("🔄 Finished detouring. Recalculating path to objective " + agent.getDestination().getId());
                            AbstractAlgorithm calculator = AgentTrafficController.getCalculator(agent);
                            if (calculator.getPath().isEmpty()) {
                                agent.logMsg("❌ Route destroyed to node " + agent.getDestination().getId() + ". Objective abandoned!");
                                agent.incrementAbandonedObjectives(); 
                                AgentTrafficController.startNextObjective(agent);
                            } else {
                                agent.setPath(calculator.getPath()); 
                                AgentTrafficController.makeReservations(agent);
                            }
                        }
                    }
                } else {
                    targetNode.enqueue(agent);
                    if (agent.getState() != agentState.WAITING) agent.setState(agentState.WAITING);
                }
            }
        }
    }
}