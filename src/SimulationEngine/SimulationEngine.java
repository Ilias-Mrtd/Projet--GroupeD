package simulationEngine;

import java.util.List;
import java.util.ArrayList;

import simulationEngine.graphRenderer.agents.Agent;
import simulationEngine.graphRenderer.graph.Graph;

public class SimulationEngine {

    public Graph graph;
    public List<Agent> Agents;
    // insere le panel
    // inserer le timer

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.Agents = new ArrayList<>();

        // initialiser le panel
        // initialiser le timer
    }

    public void addAgent(Agent agent) {
        agent.graph = this.graph;
        this.Agents.add(agent);
        System.out.println("Agent " + agent.id + " ajouté au moteur de simulation.");
    }

    /**
     * Debut de la simulation
     * public void start() {
     * timer.start();
     * }
     */

    public void tick() {
        for (Agent agent : Agents) {
            agent.update();
        }
        // graphRenderer.repaint() ;rafraichissement du graph
    }
}