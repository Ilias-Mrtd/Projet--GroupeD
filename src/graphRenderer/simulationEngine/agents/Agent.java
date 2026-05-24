package graphRenderer.simulationEngine.agents;

import java.util.ArrayList;
import java.util.List;
import graphRenderer.simulationEngine.graph.*;

public class Agent implements PathFinder {

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

    @Override
    public List<Edge> findPath(Graph graph, Node source, Node target) {
        // Implemente l'algorithme de Dijkstra

    }

}
