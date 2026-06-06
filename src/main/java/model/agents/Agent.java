package model.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.graph.*;
import simulationEngine.algorithm.Dijkstra;
import simulationEngine.algorithm.AStar;
import simulationEngine.algorithm.Algo;

public class Agent implements Serializable {

    private int id;
    private float speed = 0.5f;
    private agentState state = agentState.AVAILABLE;
    private Graph graph;
    private Node startingNode;
    private Node currentNode;
    private Node previousNode;
    private Edge currentEdge;
    private Node destination;
    private boolean isSelected = false;

    private int isBlockedSince = 0;
    private Node auxNode;

    private float distanceTraveledOnEdge = 0.0f;
    private List<Node> objectives = new ArrayList<>();
    private List<Node> path = new ArrayList<>();

    private int maxPatience = 300;
    private int currentPatience;
    private int currentPriority = 0;

    private EndBehavior endBehavior = EndBehavior.REMOVE;
    private agentBehavior behavior = agentBehavior.PATIENT;

    public enum AlgoType {
        DIJKSTRA, ASTAR, RANDOM
    }

    private AlgoType algoType = AlgoType.RANDOM;

    private List<Node> reservedNodes = new ArrayList<>();
    private List<Edge> reservedEdges = new ArrayList<>();

    private boolean isRetreating = false;

    // =========================================
    // KPIs & STATS
    // =========================================
    private int abandonedObjectives = 0;
    private int objectivesReached = 0;
    private int detoursTaken = 0;
    private double totalActiveTime = 0.0;
    private double totalWaitTime = 0.0;
    private double totalDistance = 0.0;
    private List<String> historyLog = new ArrayList<>();
    private static final long serialVersionUID = 1L;

    private void logMsg(String msg) {
        String entry = String.format("[%.1fs] %s", totalActiveTime, msg);
        historyLog.add(entry);
        System.out.println("Agent " + id + " : " + msg);
    }

    public int getAbandonedObjectives() {
        return abandonedObjectives;
    }

    public int getObjectivesReached() {
        return objectivesReached;
    }

    public int getDetoursTaken() {
        return detoursTaken;
    }

    public double getTotalActiveTime() {
        return totalActiveTime;
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public double getTotalDistance() {
        return totalDistance;
    }

    public List<String> getHistoryLog() {
        return historyLog;
    }

    public void resetStats() {
        this.abandonedObjectives = 0;
        this.objectivesReached = 0;
        this.detoursTaken = 0;
        this.totalActiveTime = 0.0;
        this.totalWaitTime = 0.0;
        this.totalDistance = 0.0;
        this.historyLog.clear();
        if (this.startingNode != null) {
            logMsg("Réapparition sur le noeud " + this.startingNode.getId() + " (Redémarrage)");
        }
    }
    // =========================================

    public enum EndBehavior {
        STOP, REMOVE, RANDOM_WANDER
    }

    public enum agentState {
        AVAILABLE, CALCULATING, RUNNING, WAITING, OUT
    }

    public enum agentBehavior {
        HURRIED(2), PATIENT(1), BROKEN(0);

        private final int priority;

        agentBehavior(int priority) {
            this.priority = priority;
        }

        public int getPriority() {
            return priority;
        }
    }

    public void setStartingNode(Node node) {
        this.startingNode = node;
        this.currentNode = node;
        this.previousNode = node;
        logMsg("Apparition sur le noeud " + node.getId());
    }

    public void setAgentBehavior(agentBehavior behavior) {
        this.behavior = behavior;
    }

    public agentBehavior getAgentBehavior() {
        return this.behavior;
    }

    public void setAlgoType(AlgoType type) {
        this.algoType = type;
    }

    public AlgoType getAlgoType() {
        return this.algoType;
    }

    private void handleEndBehavior() {
        logMsg("A terminé tous ses objectifs. Comportement final : " + getEndBehavior());
        clearReservations();

        switch (getEndBehavior()) {
            case STOP:
                setState(agentState.AVAILABLE);
                break;
            case REMOVE:
                setState(agentState.OUT);
                if (getCurrentNode() != null)
                    getCurrentNode().leave();
                logMsg("Quitte la simulation.");
                break;
            case RANDOM_WANDER:
                setState(agentState.AVAILABLE);
                if (this.currentNode != null)
                    this.currentNode.leave();
                break;
        }
    }

    private void makeReservations() {
        if (getAgentBehavior() == agentBehavior.BROKEN)
            return;
        clearReservations();
        Node prev = getCurrentNode();
        for (Node n : getPath()) {
            n.setExpectedOccupants(n.getExpectedOccupants() + 1);
            getReservedNodes().add(n);
            Edge e = findEdgeBetween(prev, n);
            if (e != null) {
                e.setExpectedOccupants(e.getExpectedOccupants() + 1);
                getReservedEdges().add(e);
            }
            prev = n;
        }
    }

    private void clearReservations() {
        for (Node n : getReservedNodes()) {
            if (n.getExpectedOccupants() > 0)
                n.setExpectedOccupants(n.getExpectedOccupants() - 1);
        }
        getReservedNodes().clear();

        for (Edge e : getReservedEdges()) {
            if (e.getExpectedOccupants() > 0)
                e.setExpectedOccupants(e.getExpectedOccupants() - 1);
        }
        getReservedEdges().clear();
    }

    public void releaseAll() {
        clearReservations();
        if (getCurrentEdge() != null) {
            Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget()
                    : getCurrentEdge().getSource();
            if (targetNode != null && targetNode.getIncomingOccupants() > 0) {
                targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);
            }
            getCurrentEdge().leave();
            getCurrentEdge().removeQueue(this);
            setCurrentEdge(null);
        }
        if (getCurrentNode() != null) {
            getCurrentNode().leave();
            getCurrentNode().removeQueue(this);
        }
        getPath().clear();
        getObjectives().clear();
        setState(agentState.OUT);
    }

    public Agent(int id, float speed, agentState state) {
        setId(id);
        setSpeed(speed);
        setState(state);
        setCurrentPatience(getMaxPatience());
    }

    private Edge findEdgeBetween(Node s, Node t) {
        int index = getGraph().getNodes().indexOf(s);
        if (index != -1) {
            for (Edge e : getGraph().getEdges().get(index)) {
                if (e.getSource() == t || e.getTarget() == t)
                    return e;
            }
        }
        return null;
    }

    public void addObjective(Node dest) {
        getObjectives().add(dest);
        logMsg("Nouvel objectif reçu : Noeud " + dest.getId());
        if (getState() == agentState.AVAILABLE) {
            startNextObjective();
        }
    }

    private Algo getCalculator() {
        if (this.algoType == AlgoType.DIJKSTRA) {
            logMsg("Calcul (Algorithme forcé : Dijkstra)");
            return new Dijkstra(getGraph(), getCurrentNode(), getDestination());
        } else if (this.algoType == AlgoType.ASTAR) {
            logMsg("Calcul (Algorithme forcé : A*)");
            return new AStar(getGraph(), getCurrentNode(), getDestination());
        } else {
            if (Math.random() < 0.5) {
                logMsg("Calcul (Aléatoire -> Dijkstra)");
                return new Dijkstra(getGraph(), getCurrentNode(), getDestination());
            } else {
                logMsg("Calcul (Aléatoire -> A*)");
                return new AStar(getGraph(), getCurrentNode(), getDestination());
            }
        }
    }

    private void startNextObjective() {
        if (!getObjectives().isEmpty()) {
            setDestination(getObjectives().remove(0));
            setState(agentState.CALCULATING);

            if (getGraph() != null && getCurrentNode() != null) {
                Algo calculator = getCalculator();

                if (calculator.getPath().isEmpty() && getCurrentNode().getId() != getDestination().getId()) {
                    logMsg("❌ AUCUN CHEMIN vers " + getDestination().getId() + " ! Objectif abandonné.");
                    abandonedObjectives++;
                    startNextObjective();
                    return;
                }

                setPath(calculator.getPath());
                makeReservations();
            }

            setState(agentState.RUNNING);
            setCurrentPatience(getMaxPatience());
            logMsg(">>> En route vers l'objectif : Noeud " + getDestination().getId());

        } else {
            handleEndBehavior();
        }
    }

    private void applyDetour() {
        detoursTaken++;
        logMsg("!!! Cherche à s'écarter pour débloquer la situation !!!");
        List<Node> validDetours = new ArrayList<>();
        List<Node> fallbackDetours = new ArrayList<>();

        int index = getGraph().getNodes().indexOf(getCurrentNode());

        if (index != -1) {
            for (Edge e : getGraph().getEdges().get(index)) {
                Node neighbor = null;

                if (!e.hasDirection()) {
                    if (e.getSource() == getCurrentNode()) {
                        neighbor = e.getTarget();
                    } else {
                        continue;
                    }
                } else {
                    neighbor = (e.getSource() == getCurrentNode()) ? e.getTarget() : e.getSource();
                }

                if (neighbor == null)
                    continue;
                if (!getPath().isEmpty() && neighbor.getId() == getPath().get(0).getId())
                    continue;

                if (neighbor.getState() != Node.nodeState.FULL && !neighbor.isUnderConstruction()) {
                    if (previousNode != null && neighbor.getId() == previousNode.getId()) {
                        fallbackDetours.add(neighbor);
                    } else {
                        validDetours.add(neighbor);
                    }
                }
            }
        }

        Node detour = null;
        if (!validDetours.isEmpty()) {
            detour = validDetours.get((int) (Math.random() * validDetours.size()));
        } else if (!fallbackDetours.isEmpty()) {
            detour = fallbackDetours.get((int) (Math.random() * fallbackDetours.size()));
        }

        if (detour != null) {
            logMsg("↪️ Fait un détour aléatoire vers le Noeud " + detour.getId());
            getPath().clear();
            getPath().add(detour);
            makeReservations();
        } else {
            logMsg("Aucune rue pour s'écarter, recalcule la route...");
            Algo calculator = getCalculator();

            if (calculator.getPath().isEmpty() && getCurrentNode().getId() != getDestination().getId()) {
                logMsg("❌ Complètement bloqué vers " + getDestination().getId() + ". Objectif abandonné !");
                abandonedObjectives++;
                startNextObjective();
                return;
            }

            setPath(calculator.getPath());
            makeReservations();
        }

        setState(agentState.RUNNING);
        setCurrentPatience(getMaxPatience());
    }

    private boolean isPathClearAhead(int depth) {
        if (behavior == agentBehavior.HURRIED) {
            return true;
        }
        if (getPath().size() < 2)
            return true;

        Node prev = getPath().get(0);
        int limit = Math.min(depth, getPath().size());

        for (int i = 1; i < limit; i++) {
            Node nextNode = getPath().get(i);
            Edge nextEdge = findEdgeBetween(prev, nextNode);

            if (nextEdge != null && nextEdge.getCurrentOccupants() >= nextEdge.getCapacity()) {
                return false;
            }
            if (nextNode.getCurrentOccupants() + nextNode.getIncomingOccupants() >= nextNode.getCapacity()) {
                return false;
            }
            prev = nextNode;
        }
        return true;
    }

    public void update(double deltaTime) {

        if (getState() == agentState.RUNNING || getState() == agentState.WAITING
                || getState() == agentState.CALCULATING) {
            totalActiveTime += deltaTime;
        }

        if (getState() == agentState.WAITING) {
            totalWaitTime += deltaTime;

            int decrease = 1;
            switch (getAgentBehavior()) {
                case HURRIED:
                    decrease = 3;
                    break;
                case PATIENT:
                    decrease = 1;
                    break;
                case BROKEN:
                    decrease = 1;
                    break;
            }
            if (getCurrentNode() == getStartingNode() && getCurrentEdge() == null) {
                if (Math.random() < 0.5)
                    setCurrentPatience(getCurrentPatience() - decrease);
            } else {
                setCurrentPatience(getCurrentPatience() - decrease * 2);
            }

            if (getCurrentPatience() <= 0) {
                if (isRetreating()) {
                    getCurrentNode().removeQueue(this);
                } else if (getCurrentEdge() == null && !getPath().isEmpty()) {
                    Edge nextEdge = findEdgeBetween(getCurrentNode(), getPath().get(0));
                    if (nextEdge != null)
                        nextEdge.removeQueue(this);
                } else if (getCurrentEdge() != null) {
                    Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget()
                            : getCurrentEdge().getSource();
                    targetNode.removeQueue(this);
                }

                if (getCurrentEdge() != null) {
                    logMsg("!!! Perd patience et fait MARCHE ARRIÈRE sur l'arête !!!");
                    setRetreating(true);
                    setState(agentState.RUNNING);
                } else {
                    applyDetour();
                }
                return;
            }
        }

        if (getState() == agentState.RUNNING || getState() == agentState.WAITING) {

            if (getState() == agentState.RUNNING && getCurrentNode() != null && getCurrentEdge() == null) {
                if (getAuxNode() != null && getCurrentNode() == getAuxNode()) {
                    setBlockedSince(isBlockedSince() + 1);
                    if (isBlockedSince() > 3) {
                        setBlockedSince(0);
                        logMsg("Etape 3 : Recalcul de routine.");
                        getPath().clear();
                        getObjectives().add(0, destination);
                        startNextObjective();
                    }
                } else {
                    setAuxNode(getCurrentNode());
                }
            }

            if (getCurrentEdge() == null) {
                if (!getPath().isEmpty()) {
                    if (!isPathClearAhead(2)) {
                        if (getState() != agentState.WAITING) {
                            setState(agentState.WAITING);
                            logMsg("🧠 Anticipe un bouchon et fait du SMART WAITING.");
                        }
                        return;
                    }

                    Node nextNode = getPath().get(0);
                    Edge nextEdge = findEdgeBetween(getCurrentNode(), nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter(this)) {

                            if (getReservedEdges().contains(nextEdge)) {
                                if (nextEdge.getExpectedOccupants() > 0)
                                    nextEdge.setExpectedOccupants(nextEdge.getExpectedOccupants() - 1);
                                getReservedEdges().remove(nextEdge);
                            }

                            if (getCurrentNode() != null)
                                getCurrentNode().leave();
                            setCurrentEdge(nextEdge);
                            getPath().remove(0);
                            setDistanceTraveledOnEdge(0.0f);
                            setState(agentState.RUNNING);
                            setCurrentPatience(getMaxPatience());

                            nextNode.setIncomingOccupants(nextNode.getIncomingOccupants() + 1);
                            logMsg("🟢 S'ENGAGE vers le noeud " + nextNode.getId());

                        } else {
                            nextEdge.enqueue(this);
                            if (getState() != agentState.WAITING) {
                                setState(agentState.WAITING);
                            }
                            return;
                        }
                    }
                } else {
                    if (getCurrentNode() != null && getDestination() != null
                            && getCurrentNode().getId() == getDestination().getId()) {
                        logMsg("✅ Objectif Noeud " + getDestination().getId() + " ATTEINT !");
                        objectivesReached++;
                    }
                    if (getObjectives().isEmpty()) {
                        handleEndBehavior();
                    } else {
                        startNextObjective();
                    }
                }
            }

            if (getCurrentEdge() != null) {
                float distMoved = this.speed * getCurrentEdge().getSpeedModifier() * (float) deltaTime * 60f;

                if (isRetreating()) {
                    setDistanceTraveledOnEdge(getDistanceTraveledOnEdge() - distMoved);
                    totalDistance += distMoved;

                    if (getDistanceTraveledOnEdge() <= 0.0f) {
                        setDistanceTraveledOnEdge(0.0f);

                        if (getCurrentNode().tryEnter(this)) {
                            Node targetNode = (getCurrentEdge().getSource() == getCurrentNode())
                                    ? getCurrentEdge().getTarget()
                                    : getCurrentEdge().getSource();

                            if (targetNode.getIncomingOccupants() > 0)
                                targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);

                            getCurrentEdge().leave();
                            setCurrentEdge(null);
                            setRetreating(false);
                            logMsg("Est revenu sur son noeud et libère l'arête.");
                            applyDetour();
                        } else {
                            getCurrentNode().enqueue(this);
                            if (getState() != agentState.WAITING) {
                                setState(agentState.WAITING);
                            }
                        }
                    }
                } else {

                    if (getDistanceTraveledOnEdge() + distMoved <= getCurrentEdge().getLength()) {
                        totalDistance += distMoved;
                    } else {
                        totalDistance += (getCurrentEdge().getLength() - getDistanceTraveledOnEdge());
                    }

                    if (getDistanceTraveledOnEdge() < getCurrentEdge().getLength()) {
                        setDistanceTraveledOnEdge(getDistanceTraveledOnEdge() + distMoved);
                    }

                    if (getDistanceTraveledOnEdge() >= getCurrentEdge().getLength()) {
                        setDistanceTraveledOnEdge((float) getCurrentEdge().getLength());
                        Node targetNode = (getCurrentEdge().getSource() == getCurrentNode())
                                ? getCurrentEdge().getTarget()
                                : getCurrentEdge().getSource();

                        if (targetNode.tryEnter(this)) {

                            if (targetNode.getIncomingOccupants() > 0)
                                targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);

                            if (getReservedNodes().contains(targetNode)) {
                                if (targetNode.getExpectedOccupants() > 0)
                                    targetNode.setExpectedOccupants(targetNode.getExpectedOccupants() - 1);
                                getReservedNodes().remove(targetNode);
                            }

                            previousNode = getCurrentNode();
                            setCurrentNode(targetNode);
                            getCurrentEdge().leave();
                            setCurrentEdge(null);
                            setState(agentState.RUNNING);
                            setCurrentPatience(getMaxPatience());

                            logMsg("📍 EST ARRIVÉ au noeud " + getCurrentNode().getId());

                            if (getPath().isEmpty()) {
                                if (getCurrentNode().getId() == getDestination().getId()) {
                                    logMsg("✅ Objectif final Noeud " + getDestination().getId() + " ATTEINT !");
                                    objectivesReached++;
                                    if (!getObjectives().isEmpty()) {
                                        startNextObjective();
                                    } else {
                                        handleEndBehavior();
                                    }
                                } else {
                                    logMsg("🔄 A terminé son évitement. Recalcul vers l'objectif "
                                            + getDestination().getId());
                                    Algo calculator = getCalculator();

                                    if (calculator.getPath().isEmpty()) {
                                        logMsg("❌ Route détruite vers " + getDestination().getId()
                                                + ". Objectif abandonné !");
                                        abandonedObjectives++;
                                        startNextObjective();
                                    } else {
                                        setPath(calculator.getPath());
                                        makeReservations();
                                    }
                                }
                            }
                        } else {
                            targetNode.enqueue(this);
                            if (getState() != agentState.WAITING) {
                                setState(agentState.WAITING);
                            }
                        }
                    }
                }
            }
        }
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }

    public float getSpeed() {
        return this.speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public agentState getState() {
        return this.state;
    }

    public void setState(agentState state) {
        this.state = state;
    }

    public Graph getGraph() {
        return this.graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Node getStartingNode() {
        return this.startingNode;
    }

    public Node getCurrentNode() {
        return this.currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public Edge getCurrentEdge() {
        return this.currentEdge;
    }

    public void setCurrentEdge(Edge currentEdge) {
        this.currentEdge = currentEdge;
    }

    public Node getDestination() {
        return this.destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public float getDistanceTraveledOnEdge() {
        return this.distanceTraveledOnEdge;
    }

    public void setDistanceTraveledOnEdge(float distanceTraveledOnEdge) {
        this.distanceTraveledOnEdge = distanceTraveledOnEdge;
    }

    public List<Node> getObjectives() {
        return this.objectives;
    }

    public void setObjectives(List<Node> objectives) {
        this.objectives = objectives;
    }

    public List<Node> getPath() {
        return this.path;
    }

    public void setPath(List<Node> path) {
        this.path = path;
    }

    public int getMaxPatience() {
        return this.maxPatience;
    }

    public void setMaxPatience(int maxPatience) {
        this.maxPatience = maxPatience;
    }

    public int getCurrentPatience() {
        return this.currentPatience;
    }

    public void setCurrentPatience(int currentPatience) {
        this.currentPatience = currentPatience;
    }

    public EndBehavior getEndBehavior() {
        return this.endBehavior;
    }

    public void setEndBehavior(EndBehavior endBehavior) {
        this.endBehavior = endBehavior;
    }

    public List<Node> getReservedNodes() {
        return this.reservedNodes;
    }

    public void setReservedNodes(List<Node> reservedNodes) {
        this.reservedNodes = reservedNodes;
    }

    public List<Edge> getReservedEdges() {
        return this.reservedEdges;
    }

    public void setReservedEdges(List<Edge> reservedEdges) {
        this.reservedEdges = reservedEdges;
    }

    public boolean isRetreating() {
        return this.isRetreating;
    }

    public void setRetreating(boolean isRetreating) {
        this.isRetreating = isRetreating;
    }

    public Node getAuxNode() {
        return this.auxNode;
    }

    public void setAuxNode(Node node) {
        this.auxNode = node;
    }

    public int isBlockedSince() {
        return this.isBlockedSince;
    }

    public void setBlockedSince(int n) {
        this.isBlockedSince = n;
    }

    public void setPriority(int priority) {
        this.currentPriority = priority;
    }

    public int getCurrentPriority() {
        return this.currentPriority;
    }
}