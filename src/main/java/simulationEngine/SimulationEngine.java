package simulationEngine;

import javafx.animation.AnimationTimer;
import java.util.Random;
import java.util.List;

import UI.GraphCanvas;
import model.graph.*;
import model.graph.Node.nodeState;
import model.agents.Agent;
import UI.PropertiesPanel;

public class SimulationEngine extends AnimationTimer {
    public final Graph graph;
    public final List<Agent> agents;
    private final GraphCanvas canvas;
    private final PropertiesPanel propertiesPanel;

    // Pour stocker le temps de la frame précédente
    private long lastUpdate = 0;
    
    public SimulationEngine(Graph graph, List<Agent> agents, GraphCanvas canvas, PropertiesPanel propertiesPanel) {
        this.graph = graph;
        this.agents = agents;
        this.canvas = canvas;
        this.propertiesPanel = propertiesPanel;
    }

    public Graph getGraph() {
        return graph;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public GraphCanvas getCanvas() {
        return canvas;
    }

    public PropertiesPanel getPropertiesPanel() {
        return propertiesPanel;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void addAgent(Agent agent) {
        agent.setGraph(getGraph());
        this.agents.add(agent);

        if (agent.getCurrentNode() != null) {
            agent.getCurrentNode().forceEnter();
            agent.setStartingNode(agent.getCurrentNode());
        }
        System.out.println("Agent " + agent.getId() + " ajouté au moteur de simulation.");
    }

    /**
     * Cette méthode tourne en boucle ~60 fois par seconde tant que le moteur est
     * "start()"
     * 
     * @param now Le temps actuel en nanosecondes
     */
    @Override
    public void handle(long now) {
        // Initialisation du temps au tout premier passage
        if (lastUpdate == 0) {
            lastUpdate = now;
            return;
        }

        // 1. Calcul du Delta Time (Conversion des nanosecondes en secondes)
        double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
        lastUpdate = now;

        // 2. Mise à jour de la logique (La physique et les décisions)
        for (Agent agent : agents) {
            agent.update(deltaTime);
        }

        // 3. Demander à la vue de dessiner la nouvelle frame
        canvas.draw();

        if (propertiesPanel != null) {
            propertiesPanel.refresh();
        }
    }

    /**
     * Méthode appelée par ton bouton "Relancer" dans la Toolbar
     */
    // SimulationEngine.java
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
            for (Node node : this.graph.Nodes) {
                node.currentOccupants = 0;
                node.expectedOccupants = 0;
                node.state = nodeState.AVAILABLE;
            }

            // Reassignation d'objetifs
            for (Agent agent : this.agents) {
                System.out.println("Agent ID : " + agent.getId());
                System.out.println("État     : " + agent.getState());

                // Objectifs
                agent.addObjective(this.graph.Nodes.get(11));
                agent.addObjective(this.graph.Nodes.get(random.nextInt(this.graph.Nodes.size())));
            }
        }

        lastUpdate = 0;
        canvas.draw();
        this.start();
    }
}