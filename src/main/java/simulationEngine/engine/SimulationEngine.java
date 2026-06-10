package simulationEngine.engine;

import javafx.animation.AnimationTimer;
import java.util.Random;
import java.util.Comparator;
import java.util.List;
import model.graph.*;
import model.graph.Node.nodeState;
import model.agents.Agent;

public class SimulationEngine extends AnimationTimer {
    public final Graph graph;
    public final List<Agent> agents;

    private Runnable onTick;
    private long lastUpdate = 0;
    private double timeMultiplier = 1.0;

    public SimulationEngine(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;
    }

    /**
     * Registers an agent inside the execution pipeline and sorts the collection by priority context.
     * @param agent The target tracking agent entity to embed.
     */
    public void addAgent(Agent agent) {
        agent.setGraph(getGraph());
        this.agents.add(agent);
        agents.sort(Comparator.comparingInt((Agent a) -> a.getCurrentPriority())
                .thenComparingInt(a -> a.getAgentBehavior().getPriority()));

        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().forceEnter();
            agent.setStartingNode(agent.getCurrentNode());
        }
        System.out.println("Agent " + agent.getId() + " successfully registered into the simulation engine.");
    }

    /**
     * Forces immediate evacuation routines for all agents registered on a structural node or its edges.
     * @param node The context framework node being obstructed or destroyed.
     */
    public void evictAgentsFromNode(Node node) {
        for (Agent a : agents) {
            if (a.getCurrentEdge() != null && (a.getCurrentEdge().getSource() == node || a.getCurrentEdge().getTarget() == node)) {
                evictAgentsFromEdge(a.getCurrentEdge());
            }
        }

        for (Agent a : agents) {
            if (a.getCurrentNode() == node && a.getCurrentEdge() == null) {
                Node neighbor = null;
                int index = graph.getNodes().indexOf(node);
                if (index != -1 && !graph.getEdges().get(index).isEmpty()) {
                    for (Edge edge : graph.getEdges().get(index)) {
                        Node candidate = (edge.getSource() == node) ? edge.getTarget() : edge.getSource();
                        if (candidate != node) {
                            neighbor = candidate;
                            break;
                        }
                    }
                }
                
                if (neighbor != null) {
                    node.leave();
                    a.setCurrentNode(neighbor);
                    neighbor.forceEnter();
                    a.getPath().clear();
                    a.logMsg("🚨 Emergency relocation triggered due to heavy infrastructure congestion!");
                } else {
                    a.logMsg("🚨 Node destroyed with no remaining valid neighbors. Agent discarded.");
                    a.releaseAll();
                }
            }
        }
    }

    /**
     * Forces immediate evacuation routines for all agents traveling along a specific edge.
     * @param edge The context connection line being obstructed or destroyed.
     */
    public void evictAgentsFromEdge(Edge edge) {
        for (Agent a : agents) {
            if (a.getCurrentEdge() == edge) {
                edge.leave();
                a.setCurrentEdge(null);
                a.setDistanceTraveledOnEdge(0.0f);
                Node source = a.getCurrentNode();
                if (source != null) {
                    source.forceEnter();
                    a.getPath().clear();
                    a.logMsg("🚨 Edge destroyed! Forced fallback to the previous valid structural vertex.");
                } else {
                    a.releaseAll();
                }
            }
        }
    }

    @Override
    public void start() {
        this.lastUpdate = 0;
        super.start();
    }

    /**
     * Captures system tick signals to distribute computed dynamic steps across active entities.
     * @param now The current system timestamp in nanoseconds.
     */
    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }
        double deltaTime = ((now - lastUpdate) / 1_000_000_000.0) * timeMultiplier;
        lastUpdate = now;
        performStep(deltaTime);
    }

    /**
     * Evaluates spatial behavioral restrictions and triggers localized physics and logic entity ticks.
     * @param deltaTime The elapsed execution window scale.
     */
    public void performStep(double deltaTime) {
        for (Agent a : agents) {
            a.setYieldingToVIP(false);
        }

        for (Agent vip : agents) {
            if (vip.getAgentBehavior() == Agent.agentBehavior.VIP && vip.getState() != Agent.agentState.OUT) {
                boolean onRoad = vip.getCurrentEdge() != null;
                boolean atIntersection = vip.getCurrentNode() != null && vip.getCurrentEdge() == null;

                for (Agent other : agents) {
                    if (other == vip) continue;

                    if (onRoad && other.getCurrentEdge() == vip.getCurrentEdge()) {
                        other.setYieldingToVIP(true);
                    } else if (atIntersection && other.getCurrentNode() == vip.getCurrentNode() && other.getCurrentEdge() == null) {
                        other.setYieldingToVIP(true);
                    }
                }
            }
        }

        for (Agent agent : agents) {
            agent.update(deltaTime);
        }

        if (onTick != null) {
            onTick.run();
        }
    }

    /**
     * Executes a single evaluation loop using a default standard 60 FPS delta value.
     */
    public void doSingleStep() {
        performStep((1.0 / 60.0) * timeMultiplier);
    }

    /**
     * Safely interrupts the execution thread, wipes structural states, and regenerates random tracking targets.
     */
    public void restartSimulation() {
        this.stop();
        
        for (Agent a : agents) {
            if (a.getCurrentEdge() != null) a.getCurrentEdge().leave();
            if (a.getCurrentNode() != null) a.getCurrentNode().leave();
            a.setDistanceTraveledOnEdge(0.0f);
            a.setCurrentEdge(null);
            a.setState(Agent.agentState.AVAILABLE);
            a.getObjectives().clear();
            a.getPath().clear();
            a.setRetreating(false);
            a.resetStats();
            a.setStartingNode(a.getStartingNode());
            if (a.getCurrentNode() != null) {
                a.getCurrentNode().forceEnter();
            }
        }

        for (Node node : this.graph.getNodes()) {
            node.setCurrentOccupants(0);
            node.setExpectedOccupants(0);
            node.setIncomingOccupants(0);
            if (node.isUnderConstruction()) {
                node.setState(nodeState.FULL);
            } else {
                node.setState(nodeState.AVAILABLE);
            }
        }

        Random random = new Random();
        int nodeSize = this.graph.getNodes().size();
        if (nodeSize > 0) {
            for (Agent agent : this.agents) {
                agent.addObjective(this.graph.getNodes().get(random.nextInt(nodeSize)));
                agent.addObjective(this.graph.getNodes().get(random.nextInt(nodeSize)));
            }
        }

        lastUpdate = 0;
        if (onTick != null) onTick.run();
        this.start();
    }

    public double getTimeMultiplier() { return timeMultiplier; }
    public void setTimeMultiplier(double multiplier) { this.timeMultiplier = multiplier; }
    public Runnable getOnTick() { return onTick; }
    public void setOnTick(Runnable onTick) { this.onTick = onTick; }
    public Graph getGraph() { return graph; }
    public List<Agent> getAgents() { return agents; }
    public long getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(long lastUpdate) { this.lastUpdate = lastUpdate; }
}