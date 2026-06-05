package simulationEngine;

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

    // L'Observer : une action générique à exécuter à chaque fin de boucle
    private Runnable onTick;

    // Pour stocker le temps de la frame précédente
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
                .thenComparingInt(a -> a.getAgentBehavior().getPriority())); // Priority of HURRIED > PATIENT
        // > BROKEN

        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().forceEnter();
            agent.setStartingNode(agent.getCurrentNode());
        }
        System.out.println("Agent " + agent.getId() + " ajouté au moteur de simulation.");
    }

    @Override
    public void start() {
        this.lastUpdate = 0; // Évite un bond dans le temps quand on fait "Pause" puis "Play"
        super.start();
    }

    @Override
    public void handle(long now) {
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        // 1. Calcul du Delta Time APPLIQUÉ avec la vitesse
        double deltaTime = ((now - lastUpdate) / 1_000_000_000.0) * timeMultiplier;
        lastUpdate = now;

        performStep(deltaTime);
    }

    // Extrait la logique d'une frame pour l'utiliser avec le bouton Step
    public void performStep(double deltaTime) {
        // 2. Mise à jour de la logique (La physique et les décisions)
        for (Agent agent : agents) {
            agent.update(deltaTime);
        }

        // 3. Notifier l'interface graphique qu'elle peut se redessiner
        if (onTick != null) {
            onTick.run();
        }
    }

    // Exécute 1 frame fixe manuellement
    public void doSingleStep() {
        performStep((1.0 / 60.0) * timeMultiplier);
    }

    public void restartSimulation() {
        this.stop();

        for (Agent a : agents) {
            // Libérer proprement les ressources occupées
            if (a.getCurrentEdge() != null)
                a.getCurrentEdge().leave();
            if (a.getCurrentNode() != null)
                a.getCurrentNode().leave();

            // Remettre à zéro
            a.setDistanceTraveledOnEdge(0.0f);
            a.setCurrentEdge(null);
            a.setState(Agent.agentState.AVAILABLE);
            a.getObjectives().clear();
            a.getPath().clear();
            a.setRetreating(false);

            // Retour au nœud de départ
            a.setStartingNode(a.getStartingNode());
            a.getCurrentNode().forceEnter();

            // Reinitialisation des etats des noeuds
            Random random = new Random();
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

            // Reassignation d'objetifs
            for (Agent agent : this.agents) {
                System.out.println("Agent ID : " + agent.getId());
                System.out.println("État     : " + agent.getState());

                // Objectifs
                agent.addObjective(this.graph.getNodes().get(random.nextInt(this.graph.getNodes().size())));
                agent.addObjective(this.graph.getNodes().get(random.nextInt(this.graph.getNodes().size())));
            }
        }

        lastUpdate = 0;

        // Redessiner après reset
        if (onTick != null) {
            onTick.run();
        }

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