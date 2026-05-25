package SimulationEngine;

import java.util.List;
import java.util.ArrayList;

import SimulationEngine.GraphRenderer.agents.Agent;
import SimulationEngine.GraphRenderer.graph.Graph;

public class SimulationEngine {

    public Graph graph;
    public List<Agent> Agents;

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.Agents = new ArrayList<>();
    }

    public void addAgent(Agent agent) {
        agent.graph = this.graph; 
        this.Agents.add(agent);
        System.out.println("Agent " + agent.id + " ajouté au moteur de simulation.");
    }

    public void tick() {
        for (Agent agent : Agents) {
            agent.update(); 
        }
    }

}
