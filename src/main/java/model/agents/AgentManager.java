package model.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import model.agents.Agent.*;
import model.graph.*;
import simulationEngine.algorithm.AStar;
import simulationEngine.algorithm.AbstractAlgorithm;
import simulationEngine.algorithm.Dijkstra;

/**
 * Centered backend simulation processing engine executing transactional logic for Agents.
 * Houses look-aheads, spatial pathfinding generation, network reservations, 
 * and kinematic coordinate updates.
 * * @author Group D
 * @since 2026
 */
public class AgentManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public AgentManager() {
        // Zero-args constructor for simple delegation setup
    }

    public void addObjective(Agent agent, Node dest) {
        agent.getObjectives().add(dest);
        agent.logMsg("New objective received: Node " + dest.getId());
        if (agent.getState() == agentState.AVAILABLE) {
            startNextObjective(agent);
        }
    }

    public void releaseAll(Agent agent) {
        clearReservations(agent);
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

    private void startNextObjective(Agent agent) {
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

    private void handleEndBehavior(Agent agent) {
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

    private void makeReservations(Agent agent) {
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

    private void clearReservations(Agent agent) {
        for (Node n : agent.getReservedNodes()) { 
            if (n.getExpectedOccupants() > 0) n.setExpectedOccupants(n.getExpectedOccupants() - 1); 
        }
        agent.getReservedNodes().clear();
        for (Edge e : agent.getReservedEdges()) { 
            if (e.getExpectedOccupants() > 0) e.setExpectedOccupants(e.getExpectedOccupants() - 1); 
        }
        agent.getReservedEdges().clear();
    }

    private Edge findEdgeBetween(Agent agent, Node s, Node t) {
        if (agent.getGraph() == null) return null;
        int index = agent.getGraph().getNodes().indexOf(s);
        if (index != -1) {
            for (Edge e : agent.getGraph().getEdges().get(index)) {
                if (e.getSource() == t || e.getTarget() == t) return e;
            }
        }
        return null;
    }

    private AbstractAlgorithm getCalculator(Agent agent) {
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

    private boolean isPathClearAhead(Agent agent, int depth) {
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

    private void applyDetour(Agent agent) {
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
        }
        agent.setState(agentState.RUNNING); 
        agent.setCurrentPatience(agent.getMaxPatience());
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

        if (agent.getState() == agentState.WAITING) {
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
                return;
            }
        }

        if (agent.getState() == agentState.RUNNING || agent.getState() == agentState.WAITING) {
            if (agent.getState() == agentState.RUNNING && agent.getCurrentNode() != null && agent.getCurrentEdge() == null) {
                if (agent.getAuxNode() != null && agent.getCurrentNode() == agent.getAuxNode()) {
                    agent.setBlockedSince(agent.isBlockedSince() + 1);
                    if (agent.isBlockedSince() > 3) {
                        agent.setBlockedSince(0);
                        agent.logMsg("Step 3: Routine recalculation.");
                        agent.getPath().clear(); 
                        agent.getObjectives().add(0, agent.getDestination()); 
                        startNextObjective(agent);
                    }
                } else {
                    agent.setAuxNode(agent.getCurrentNode());
                }
            }

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
                    if (!isPathClearAhead(agent, 2)) {
                        if (agent.getState() != agentState.WAITING) {
                            agent.setState(agentState.WAITING);
                            agent.logMsg("🧠 Anticipating a traffic jam, doing SMART WAITING.");
                        }
                        return;
                    }

                    Node nextNode = agent.getPath().get(0);
                    Edge nextEdge = findEdgeBetween(agent, agent.getCurrentNode(), nextNode);

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
                    if (agent.getObjectives().isEmpty()) { handleEndBehavior(agent); } else { startNextObjective(agent); }
                }
            }

            if (agent.getCurrentEdge() != null) {
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
                            applyDetour(agent);
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
                                    if (!agent.getObjectives().isEmpty()) { startNextObjective(agent); } else { handleEndBehavior(agent); }
                                } else {
                                    agent.logMsg("🔄 Finished detouring. Recalculating path to objective " + agent.getDestination().getId());
                                    AbstractAlgorithm calculator = getCalculator(agent);
                                    if (calculator.getPath().isEmpty()) {
                                        agent.logMsg("❌ Route destroyed to node " + agent.getDestination().getId() + ". Objective abandoned!");
                                        agent.incrementAbandonedObjectives(); 
                                        startNextObjective(agent);
                                    } else {
                                        agent.setPath(calculator.getPath()); 
                                        makeReservations(agent);
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
    }
}