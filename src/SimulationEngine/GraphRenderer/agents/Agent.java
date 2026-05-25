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

}
