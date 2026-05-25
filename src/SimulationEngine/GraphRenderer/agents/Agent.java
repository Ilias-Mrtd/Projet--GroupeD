package SimulationEngine.GraphRenderer.agents;

import java.util.ArrayList;
import java.util.List;

import SimulationEngine.GraphRenderer.graph.*;

public class Agent extends Dijkstra {

    public int id;
    public float speed = 1.0f;
    public agentState state = agentState.AVAILABLE;
    public Graph graph;
    public Node currentNode;
    public Edge currentEdge;
    public Node Destination;
    public List<Edge> path = new ArrayList<>();

    public enum agentState {
        AVAILABLE,
        CALCULATING,
        RUNNING,
        WAITING
    }

    public Agent(String id, float speed, String state) {

    super(new Graph(), null, null); 
    this.id = Integer.parseInt(id);
    this.speed = speed;
    this.state = agentState.valueOf(state.toUpperCase());
}

    public void setDestination(Node dest) {
        this.Destination = dest;
        this.state = agentState.CALCULATING;
        
        if (this.graph != null && this.currentNode != null) {
            findPath(this.graph, this.currentNode, this.Destination);
        }
        
        this.state = agentState.RUNNING;
    }

    public void update() {
        if (this.state == agentState.RUNNING) {
            System.out.println("Agent " + this.id + " en mouvement vers " + this.Destination.id);
        }
    }

}
