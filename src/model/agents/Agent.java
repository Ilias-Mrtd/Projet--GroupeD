package model.agents;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;

public class Agent{

    public int id;
    public float speed = 1.0f;
    public agentState state = agentState.AVAILABLE;
    public Graph graph;
    public Node currentNode;
    public Edge currentEdge;
    public Node Destination;

    public float distanceTraveledOnEdge = 0.0f;
    public List<Node> objectives = new ArrayList<>();
    public List<Node> path = new ArrayList<>();

    public enum agentState {
        AVAILABLE,
        CALCULATING,
        RUNNING,
        WAITING
    }

    public Agent(String id, float speed, String state) {

        this.id = Integer.parseInt(id);
        this.speed = speed;
        this.state = agentState.valueOf(state.toUpperCase());
    }

    private Edge findEdgeBetween(Node s, Node t) {
        int index = graph.Nodes.indexOf(s);
        if (index != -1) {
            for (Edge e : graph.Edges.get(index)) {
                if (e.source == t || e.target == t)
                    return e;
            }
        }
        return null;
    }

    public void addObjective(Node dest) {
        this.objectives.add(dest);
        System.out.println("Agent " + id + " a reçu un nouvel objectif dans sa file : Noeud " + dest.id);

        if (this.state == agentState.AVAILABLE) {
            startNextObjective();
        }
    }

    private void startNextObjective() {
        if (!this.objectives.isEmpty()) {
            this.Destination = this.objectives.remove(0);
            this.state = agentState.CALCULATING;

            if (this.graph != null && this.currentNode != null) {
                Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.Destination);
                this.path = calculator.path;
            }

            this.state = agentState.RUNNING;
            System.out.println(">>> Agent " + id + " en route vers l'objectif actuel : Noeud " + this.Destination.id);
        }
    }

    public void update() {
        
        if (this.state == agentState.RUNNING || this.state == agentState.WAITING) {

            
            // ETAPE 1 : Chercher à entrer sur l'arête suivante
            
            if (this.currentEdge == null) {
                if (!this.path.isEmpty()) {
                    Node nextNode = this.path.get(0); 
                    Edge nextEdge = findEdgeBetween(this.currentNode, nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter()) { 
                            this.currentEdge = nextEdge;
                            this.path.remove(0); 
                            this.distanceTraveledOnEdge = 0.0f;
                            this.state = agentState.RUNNING; 
                            System.out.println("🟢 Agent " + id + " S'ENGAGE vers le noeud " + nextNode.id);
                        } else {
                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING; 
                                System.out.println("🟠 Agent " + id + " PATIENTE (arête pleine vers " + nextNode.id + " | Capacité: " + nextEdge.capacity + ")");
                            }
                            return; 
                        }
                    } 
                }
            }

           
            // ETAPE 2 : Avancer sur l'arête (Séparé de l'Etape 1 !)
            
            if (this.currentEdge != null && this.state == agentState.RUNNING) {
                this.distanceTraveledOnEdge += this.speed; 

                if (this.distanceTraveledOnEdge >= this.currentEdge.length) {
                    this.distanceTraveledOnEdge = (float) this.currentEdge.length;
                    this.currentNode = (this.currentEdge.source == this.currentNode) ? this.currentEdge.target : this.currentEdge.source;
                    
                    this.currentEdge.leave(); 
                    this.currentEdge = null;
                    System.out.println("Agent " + id + " EST ARRIVÉ au noeud " + currentNode.id);

                    if (this.path.isEmpty()) {
                        System.out.println("Objectif final Noeud " + this.Destination.id + " ATTEINT !");
                        if (!this.objectives.isEmpty()) {
                            startNextObjective();
                        } else {
                            this.state = agentState.AVAILABLE;
                            System.out.println("Tous les objectifs sont terminés.");
                        }
                    }
                }
            }
        }
    
    }
}