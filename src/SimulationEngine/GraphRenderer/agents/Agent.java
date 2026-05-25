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


    public float distanceTraveledOnEdge = 0.0f;

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

    private Edge findEdgeBetween(Node s, Node t) {
    int index = graph.Nodes.indexOf(s);
    if (index != -1) {
        for (Edge e : graph.Edges.get(index)) {
            if (e.source == t || e.target == t) return e;
        }
    }
    return null;
    }

    public void update() {
        if (this.state == agentState.RUNNING) {
            
            if (currentEdge == null && !this.path.isEmpty()) {
                Node nextNode = this.path.remove(0); 
                this.currentEdge = findEdgeBetween(this.currentNode, nextNode);
                this.distanceTraveledOnEdge = 0.0f;
                if (this.currentEdge != null) {
                    System.out.println("Agent " + id + " commence le segment vers " + nextNode.id);
            }
            }


            if (currentEdge != null) {
                distanceTraveledOnEdge += speed;
                
                
                if (distanceTraveledOnEdge >= currentEdge.length) {
       
                this.distanceTraveledOnEdge = (float) currentEdge.length; 
                
               
                this.currentNode = (currentEdge.source == currentNode) ? currentEdge.target : currentEdge.source;
                this.currentEdge = null; 
                System.out.println("Agent " + id + " est arrivé au noeud " + currentNode.id);

               
                if (this.path.isEmpty()) {
                    this.state = agentState.AVAILABLE;
                    System.out.println("Trajet terminé. État : " + this.state);
                }
            }
        }
    }
}
}
