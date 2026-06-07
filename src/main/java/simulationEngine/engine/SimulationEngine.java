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

    public void addAgent(Agent agent) {
        agent.setGraph(getGraph());
        this.agents.add(agent);
        agents.sort(Comparator.comparingInt((Agent a) -> a.getCurrentPriority())
                .thenComparingInt(a -> a.getAgentBehavior().getPriority()));

        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().forceEnter();
            agent.setStartingNode(agent.getCurrentNode());
        }
        System.out.println("Agent " + agent.getId() + " ajouté au moteur de simulation.");
    }

    public void evictAgentsFromNode(Node node) {
        for (Agent a : agents) {
            if (a.getCurrentEdge() != null) {
                if (a.getCurrentEdge().getSource() == node || a.getCurrentEdge().getTarget() == node) {
                    a.getCurrentEdge().leave();
                    a.setCurrentEdge(null);
                    a.setDistanceTraveledOnEdge(0.0f);
                    Node source = a.getCurrentNode();
                    if (source != null && source != node) {
                        source.forceEnter();
                        a.getPath().clear();
                        a.logMsg("🚨 Arête détruite en cascade ! Retour forcé.");
                    }
                }
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
                    a.logMsg("🚨 Téléportation d'urgence (Forte congestion) !");
                } else {
                    a.logMsg("🚨 Nœud détruit sans aucun voisin. L'agent tombe dans le vide.");
                    a.releaseAll();
                }
            }
        }
    }

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
                    a.logMsg("🚨 Arête détruite ! Retour forcé au nœud précédent.");
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

    public void performStep(double deltaTime) {
        

        // 1. EFFET GYROPHARE : Les VIP forcent les autres à s'arrêter

        for (Agent a : agents) {
            a.setYieldingToVIP(false); // Réinitialise tout le monde
        }

        for (Agent vip : agents) {
            if (vip.getAgentBehavior() == Agent.agentBehavior.VIP && vip.getState() != Agent.agentState.OUT) {
                // S'il est sur une route, tous les autres sur cette route s'arrêtent
                if (vip.getCurrentEdge() != null) {
                    for (Agent other : agents) {
                        if (other != vip && other.getCurrentEdge() == vip.getCurrentEdge()) {
                            other.setYieldingToVIP(true);
                        }
                    }
                }
                // S'il est à un carrefour, tout le carrefour se fige
                if (vip.getCurrentNode() != null && vip.getCurrentEdge() == null) {
                    for (Agent other : agents) {
                        if (other != vip && other.getCurrentNode() == vip.getCurrentNode() && other.getCurrentEdge() == null) {
                            other.setYieldingToVIP(true);
                        }
                    }
                }
            }
        }

        // 2. Mise à jour de la logique (La physique et les décisions)
        for (Agent agent : agents) {
            agent.update(deltaTime);
        }

        // 3. Notifier l'interface graphique qu'elle peut se redessiner
        if (onTick != null) {
            onTick.run();
        }
    }

    public void doSingleStep() {
        performStep((1.0 / 60.0) * timeMultiplier);
    }

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
            a.getCurrentNode().forceEnter();

            Random random = new Random();
            for (Node node : this.graph.getNodes()) {
                node.setCurrentOccupants(0);
                node.setExpectedOccupants(0);
                node.setIncomingOccupants(0);
                if (node.isUnderConstruction()) node.setState(nodeState.FULL);
                else node.setState(nodeState.AVAILABLE);
            }
            for (Agent agent : this.agents) {
                agent.addObjective(this.graph.getNodes().get(random.nextInt(this.graph.getNodes().size())));
                agent.addObjective(this.graph.getNodes().get(random.nextInt(this.graph.getNodes().size())));
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