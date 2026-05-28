package model.agents;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;

public class Agent {

    public int id;
    public float speed = 1.0f;
    public agentState state = agentState.AVAILABLE;
    public Graph graph;
    public Node startingNode;
    public Node currentNode;
    public Edge currentEdge;
    public Node Destination;

    public float distanceTraveledOnEdge = 0.0f;
    public List<Node> objectives = new ArrayList<>();
    public List<Node> path = new ArrayList<>();

    public int maxPatience = 300;
    public int currentPatience;

    public enum agentState {
        AVAILABLE,
        CALCULATING,
        RUNNING,
        WAITING
    }

    public void setStartingNode(Node node) {
        this.startingNode = node;
        this.currentNode = node;
    }

    public Agent(String id, float speed, String state) {

        this.id = Integer.parseInt(id);
        this.speed = speed;
        this.state = agentState.valueOf(state.toUpperCase());
        this.currentPatience = this.maxPatience;
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
            this.currentPatience = this.maxPatience;
            System.out.println(">>> Agent " + id + " en route vers l'objectif actuel : Noeud " + this.Destination.id);

        }

    }

    private void applyDetour() {
        System.out.println("!!! Agent " + id + " cherche à s'écarter pour débloquer la situation !!!");
        Node detour = null;
        int index = graph.Nodes.indexOf(this.currentNode);

        if (index != -1) {
            for (Edge e : graph.Edges.get(index)) {
                Node neighbor = (e.source == this.currentNode) ? e.target : e.source;

                if (!this.path.isEmpty() && neighbor.id == this.path.get(0).id)
                    continue;

                if (neighbor.state != Node.nodeState.FULL) {
                    detour = neighbor;
                    break;
                }
            }
        }

        if (detour != null) {
            System.out.println("↪️ Agent " + id + " fait un détour temporaire vers le Noeud " + detour.id);
            this.path.clear();
            this.path.add(detour);
        } else {
            System.out.println("Agent " + id + " ne trouve aucune rue pour s'écarter, il recalcule sa route...");
            Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.Destination);
            this.path = calculator.path;
        }

        this.state = agentState.RUNNING;
        this.currentPatience = this.maxPatience;
    }

    public void update() {

        if (this.state == agentState.WAITING) {
            this.currentPatience--;

            if (this.currentPatience <= 0) {
                if (this.currentEdge != null) {
                    System.out.println("!!! Agent " + id + " perd patience et fait MARCHE ARRIÈRE sur l'arête !!!");
                    this.isRetreating = true;
                    this.state = agentState.RUNNING; 
                } else {
                    applyDetour(); 
                }
                return;
            }
        }

        if (this.state == agentState.RUNNING || this.state == agentState.WAITING) {

            // ETAPE 1 : Chercher à entrer sur l'arête suivante

            if (this.currentEdge == null) {
                if (!this.path.isEmpty()) {
                    Node nextNode = this.path.get(0);
                    Edge nextEdge = findEdgeBetween(this.currentNode, nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter()) {
                            if (this.currentNode != null) {
                                this.currentNode.leave();
                            }
                            this.currentEdge = nextEdge;
                            this.path.remove(0);
                            this.distanceTraveledOnEdge = 0.0f;
                            this.state = agentState.RUNNING;
                            this.currentPatience = this.maxPatience;
                            System.out.println("🟢 Agent " + id + " S'ENGAGE vers le noeud " + nextNode.id);
                        } else {
                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING;
                                System.out.println("🟠 Agent " + id + " PATIENTE (arête pleine vers " + nextNode.id
                                        + " | Capacité: " + nextEdge.capacity + ")");
                            }
                            return;
                        }
                    }
                } else { // Implemente le cas ou l'agent recois en objectif son noeud actuel
                    System.out.println("Objectif Noeud " + this.Destination.id + " ATTEINT !");
                    if (this.objectives.isEmpty()) { // si il a recu plusieurs fois son noeud actuel
                        this.state = agentState.AVAILABLE;
                        this.currentNode.leave(); // ICI !!!!!!!!LEAVE !!!!!!!!!!
                        System.out.println("Tous les objectifs sont terminés.");
                    } else { // Il passe a l'objectif suivant
                        startNextObjective();
                    }
                }
            }

            // ETAPE 2 : Avancer sur l'arête (Séparé de l'Etape 1)

            if (this.currentEdge != null) {

                if (this.isRetreating) {
                    this.distanceTraveledOnEdge -= this.speed;

                    if (this.distanceTraveledOnEdge <= 0.0f) {
                        this.distanceTraveledOnEdge = 0.0f;
                        
                        if (this.currentNode.tryEnter()) {
                            System.out.println("Agent " + id + " est revenu sur son noeud et libère l'arête.");
                            this.currentEdge.leave();
                            this.currentEdge = null;
                            this.isRetreating = false;
                            applyDetour(); 
                        } else {
                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING;
                                System.out.println("🛑 Agent " + id + " est bloqué en reculant (le noeud d'origine est PLEIN !)");
                            }
                        }
                    }
                }

                else{

                    if (this.distanceTraveledOnEdge < this.currentEdge.length) {
                        this.distanceTraveledOnEdge += this.speed;
                    }

                    if (this.distanceTraveledOnEdge >= this.currentEdge.length) {
                        this.distanceTraveledOnEdge = (float) this.currentEdge.length;
                        Node targetNode = (this.currentEdge.source == this.currentNode) ? this.currentEdge.target
                                : this.currentEdge.source;

                        if (targetNode.tryEnter()) {
                            this.currentNode = targetNode;
                            this.currentEdge.leave();
                            this.currentEdge = null;
                            this.state = agentState.RUNNING;
                            this.currentPatience = this.maxPatience;

                            System.out.println("Agent " + id + " EST ARRIVÉ au noeud " + currentNode.id);

                            if (this.path.isEmpty()) {
                                if (this.currentNode.id == this.Destination.id) {
                                    System.out.println("Objectif final Noeud " + this.Destination.id + " ATTEINT !");
                                    if (!this.objectives.isEmpty()) {
                                        startNextObjective();
                                    } else {
                                        this.state = agentState.AVAILABLE;
                                        this.currentNode.leave(); // ICI !!!!!!!!!!!!!!! LEAVE !!!!!!!!!!!!!!!
                                        System.out.println("Tous les objectifs sont terminés.");
                                    }
                                } else {

                                    System.out.println("🔄 Agent " + id
                                            + " a terminé son évitement. Recalcul vers l'objectif " + this.Destination.id);
                                    Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.Destination);
                                    this.path = calculator.path;
                                }
                            }
                        }

                        else {

                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING;
                                System.out.println("🛑 Agent " + id + " EST BLOQUÉ au bout de l'arête (le noeud "
                                        + targetNode.id + " est PLEIN !)");
                            }
                        }
                    }
                }
            }
        }

    }
}