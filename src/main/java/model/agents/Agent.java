package model.agents;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;
import simulationEngine.Dijkstra;

public class Agent {

    public int id;
    public float speed = 0.5f;
    public agentState state = agentState.AVAILABLE;
    public Graph graph;
    public Node startingNode;
    public Node currentNode;
    public Edge currentEdge;
    public Node destination;
    public boolean isSelected = false;

    private int isBlockedSince = 0;
    private Node auxNode;

    public float distanceTraveledOnEdge = 0.0f;
    public List<Node> objectives = new ArrayList<>();
    public List<Node> path = new ArrayList<>();

    public int maxPatience = 300;
    public int currentPatience;

    public EndBehavior endBehavior = EndBehavior.RANDOM_WANDER;
    public List<Node> reservedNodes = new ArrayList<>();
    public List<Edge> reservedEdges = new ArrayList<>();

    public boolean isRetreating = false;

    public enum EndBehavior {
        STOP,
        REMOVE,
        RANDOM_WANDER
    }

    public enum agentState {
        AVAILABLE,
        CALCULATING,
        RUNNING,
        WAITING,
        OUT
    }

    public void setStartingNode(Node node) {
        this.startingNode = node;
        this.currentNode = node;
    }

    private void handleEndBehavior() {
        System.out.println("Agent " + id + " a terminé tous ses objectifs. Comportement : " + endBehavior);
        clearReservations();

        switch (this.endBehavior) {
            case STOP:
                this.state = agentState.AVAILABLE;
                break;
            case REMOVE:
                this.state = agentState.OUT;
                this.currentNode.leave();
                break;
            case RANDOM_WANDER:
                this.state = agentState.AVAILABLE;
                /*
                 * if (graph != null && !graph.Nodes.isEmpty()) {
                 * int randomIndex = (int) (Math.random() * graph.Nodes.size());
                 * Node randomNode = graph.Nodes.get(randomIndex);
                 * System.out.println(
                 * "🎲 L'agent " + id + " choisit une nouvelle destination aléatoire : " +
                 * randomNode.id);
                 * addObjective(randomNode);
                 * }
                 */
                break;
        }
    }

    private void makeReservations() {
        clearReservations();
        Node prev = this.currentNode;
        for (Node n : this.path) {
            n.expectedOccupants++;
            reservedNodes.add(n);
            Edge e = findEdgeBetween(prev, n);
            if (e != null) {
                e.expectedOccupants++;
                reservedEdges.add(e);
            }
            prev = n;
        }
    }

    private void clearReservations() {
        for (Node n : reservedNodes) {
            if (n.expectedOccupants > 0)
                n.expectedOccupants--;
        }
        reservedNodes.clear();

        for (Edge e : reservedEdges) {
            if (e.expectedOccupants > 0)
                e.expectedOccupants--;
        }
        reservedEdges.clear();
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
            this.destination = this.objectives.remove(0);
            this.state = agentState.CALCULATING;

            if (this.graph != null && this.currentNode != null) {
                Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.destination);
                this.path = calculator.path;
                makeReservations();
            }

            this.state = agentState.RUNNING;
            this.currentPatience = this.maxPatience;
            System.out.println(">>> Agent " + id + " en route vers l'objectif actuel : Noeud " + this.destination.id);

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
            makeReservations();
        } else {
            System.out.println("Agent " + id + " ne trouve aucune rue pour s'écarter, il recalcule sa route...");
            Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.destination);
            this.path = calculator.path;
            makeReservations();
        }

        this.state = agentState.RUNNING;
        this.currentPatience = this.maxPatience;
    }

    private boolean isPathClearAhead(int depth) {
        if (this.path.size() < 2)
            return true;

        Node prev = this.path.get(0);
        int limit = Math.min(depth, this.path.size());

        for (int i = 1; i < limit; i++) {
            Node nextNode = this.path.get(i);
            Edge nextEdge = findEdgeBetween(prev, nextNode);

            if (nextEdge != null && nextEdge.currentOccupants >= nextEdge.capacity) {
                return false;
            }
            if (nextNode.currentOccupants + nextNode.incomingOccupants >= nextNode.capacity) {
                return false;
            }
            prev = nextNode;
        }
        return true;
    }

    public void update(double deltaTime) {

        if (this.state == agentState.WAITING) {

            if (this.currentNode == this.startingNode && this.currentEdge == null) {
                if (Math.random() < 0.5)
                    this.currentPatience--;
            } else {
                this.currentPatience -= 2;
            }

            if (this.currentPatience <= 0) {

                if (this.isRetreating) {
                    this.currentNode.removeQueue(this);
                } else if (this.currentEdge == null && !this.path.isEmpty()) {
                    Edge nextEdge = findEdgeBetween(this.currentNode, this.path.get(0));
                    if (nextEdge != null)
                        nextEdge.removeQueue(this);
                } else if (this.currentEdge != null) {
                    Node targetNode = (this.currentEdge.source == this.currentNode) ? this.currentEdge.target
                            : this.currentEdge.source;
                    targetNode.removeQueue(this);
                }

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

            // Verifier qu'une arete n'a pas ete suprimer du chemin
            if (this.state == agentState.RUNNING && this.currentNode != null && this.currentEdge == null) {
                if (this.auxNode != null && this.currentNode == this.auxNode) {
                    this.isBlockedSince++;
                    if (this.isBlockedSince > 3) {
                        this.isBlockedSince = 0;
                        System.out.println("Etape 3");
                        this.path.clear();
                        this.objectives.add(0, destination);
                        startNextObjective();
                    }
                } else {
                    this.auxNode = this.currentNode;
                }
            }

            // ETAPE 1 : Chercher à entrer sur l'arête suivante

            if (this.currentEdge == null) {
                if (!this.path.isEmpty()) {
                    if (!isPathClearAhead(2)) {
                        if (this.state != agentState.WAITING) {
                            this.state = agentState.WAITING;
                            System.out.println("🧠 Agent " + id + " anticipe un bouchon et fait du SMART WAITING.");
                        }
                        return;
                    }

                    Node nextNode = this.path.get(0);
                    Edge nextEdge = findEdgeBetween(this.currentNode, nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter(this)) {

                            if (reservedEdges.contains(nextEdge)) {
                                if (nextEdge.expectedOccupants > 0)
                                    nextEdge.expectedOccupants--;
                                reservedEdges.remove(nextEdge);
                            }

                            if (this.currentNode != null) {
                                this.currentNode.leave();
                            }
                            this.currentEdge = nextEdge;
                            this.path.remove(0);
                            this.distanceTraveledOnEdge = 0.0f;
                            this.state = agentState.RUNNING;
                            this.currentPatience = this.maxPatience;

                            nextNode.incomingOccupants++;

                            System.out.println("🟢 Agent " + id + " S'ENGAGE vers le noeud " + nextNode.id);
                        } else {

                            nextEdge.enqueue(this);
                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING;
                                System.out.println("🟠 Agent " + id + " PATIENTE (arête pleine vers " + nextNode.id
                                        + " | Capacité: " + nextEdge.capacity + ")");
                            }
                            return;
                        }
                    }
                } else {
                    System.out.println("Objectif Noeud " + this.destination.id + " ATTEINT !");
                    if (this.objectives.isEmpty()) {
                        handleEndBehavior();
                    } else {
                        startNextObjective();
                    }
                }
            }

            // ETAPE 2 : Avancer sur l'arête (Séparé de l'Etape 1)

            if (this.currentEdge != null) {

                if (this.isRetreating) {
                    this.distanceTraveledOnEdge -= this.speed * deltaTime * 60;

                    if (this.distanceTraveledOnEdge <= 0.0f) {
                        this.distanceTraveledOnEdge = 0.0f;

                        if (this.currentNode.tryEnter(this)) {
                            System.out.println("Agent " + id + " est revenu sur son noeud et libère l'arête.");
                            Node targetNode = (this.currentEdge.source == this.currentNode) ? this.currentEdge.target
                                    : this.currentEdge.source;
                            if (targetNode.incomingOccupants > 0)
                                targetNode.incomingOccupants--;

                            this.currentEdge.leave();
                            this.currentEdge = null;
                            this.isRetreating = false;
                            applyDetour();
                        } else {
                            this.currentNode.enqueue(this);
                            if (this.state != agentState.WAITING) {
                                this.state = agentState.WAITING;
                                System.out.println(
                                        "🛑 Agent " + id + " est bloqué en reculant (le noeud d'origine est PLEIN !)");
                            }
                        }
                    }
                }

                else {

                    if (this.distanceTraveledOnEdge < this.currentEdge.length) {
                        this.distanceTraveledOnEdge += this.speed * deltaTime * 60;
                    }

                    if (this.distanceTraveledOnEdge >= this.currentEdge.length) {
                        this.distanceTraveledOnEdge = (float) this.currentEdge.length;
                        Node targetNode = (this.currentEdge.source == this.currentNode) ? this.currentEdge.target
                                : this.currentEdge.source;

                        if (targetNode.tryEnter(this)) {

                            if (targetNode.incomingOccupants > 0)
                                targetNode.incomingOccupants--;

                            if (reservedNodes.contains(targetNode)) {
                                if (targetNode.expectedOccupants > 0)
                                    targetNode.expectedOccupants--;
                                reservedNodes.remove(targetNode);
                            }

                            this.currentNode = targetNode;
                            this.currentEdge.leave();
                            this.currentEdge = null;
                            this.state = agentState.RUNNING;
                            this.currentPatience = this.maxPatience;

                            System.out.println("Agent " + id + " EST ARRIVÉ au noeud " + currentNode.id);

                            if (this.path.isEmpty()) {
                                if (this.currentNode.id == this.destination.id) {
                                    System.out.println("Objectif final Noeud " + this.destination.id + " ATTEINT !");
                                    if (!this.objectives.isEmpty()) {
                                        startNextObjective();
                                    } else {
                                        handleEndBehavior();
                                    }
                                } else {

                                    System.out.println(
                                            "🔄 Agent " + id + " a terminé son évitement. Recalcul vers l'objectif "
                                                    + this.destination.id);
                                    Dijkstra calculator = new Dijkstra(this.graph, this.currentNode, this.destination);
                                    this.path = calculator.path;

                                    makeReservations();

                                }
                            }
                        }

                        else {
                            targetNode.enqueue(this);
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

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public agentState getState() {
        return state;
    }

    public void setState(agentState state) {
        this.state = state;
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Node getStartingNode() {
        return startingNode;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public Edge getCurrentEdge() {
        return currentEdge;
    }

    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public float getDistanceTraveledOnEdge() {
        return distanceTraveledOnEdge;
    }

    public void setDistanceTraveledOnEdge(float distanceTraveledOnEdge) {
        this.distanceTraveledOnEdge = distanceTraveledOnEdge;
    }

    public List<Node> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<Node> objectives) {
        this.objectives = objectives;
    }

    public List<Node> getPath() {
        return path;
    }

    public void setPath(List<Node> path) {
        this.path = path;
    }

    public int getMaxPatience() {
        return maxPatience;
    }

    public void setMaxPatience(int maxPatience) {
        this.maxPatience = maxPatience;
    }

    public int getCurrentPatience() {
        return currentPatience;
    }

    public void setCurrentPatience(int currentPatience) {
        this.currentPatience = currentPatience;
    }

    public EndBehavior getEndBehavior() {
        return endBehavior;
    }

    public void setEndBehavior(EndBehavior endBehavior) {
        this.endBehavior = endBehavior;
    }

    public List<Node> getReservedNodes() {
        return reservedNodes;
    }

    public void setReservedNodes(List<Node> reservedNodes) {
        this.reservedNodes = reservedNodes;
    }

    public List<Edge> getReservedEdges() {
        return reservedEdges;
    }

    public void setReservedEdges(List<Edge> reservedEdges) {
        this.reservedEdges = reservedEdges;
    }

    public boolean isRetreating() {
        return isRetreating;
    }

    public void setRetreating(boolean isRetreating) {
        this.isRetreating = isRetreating;
    }

}