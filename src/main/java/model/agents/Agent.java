package model.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.graph.*;

/**
 * Represents an autonomous agent within the warehouse simulation.
 * Manages its own state, pathfinding calculations, objective queues,
 * spatial resource reservations, and movement mechanics along the graph topology.
 * Delegates computational logic to AgentManager to maintain a clean architecture.
 * * @author Group D
 * @since 2026
 */
public class Agent implements Serializable {
    private static final long serialVersionUID = 1L;

    // Public Enums retained for external API compatibility
    public enum AlgoType { DIJKSTRA, ASTAR, RANDOM }
    public enum EndBehavior { STOP, REMOVE, RANDOM_WANDER }
    public enum agentState { AVAILABLE, CALCULATING, RUNNING, WAITING, OUT }
    public enum agentBehavior {
        VIP(10), HURRIED(2), PATIENT(1), BROKEN(0); 
        private final int priority;
        agentBehavior(int priority) { this.priority = priority; }
        public int getPriority() { return priority; }
    }

    // Core fields
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
    private AlgoType algoType = AlgoType.RANDOM;

    private List<Node> reservedNodes = new ArrayList<>();
    private List<Edge> reservedEdges = new ArrayList<>();
    private boolean isRetreating = false;
    private boolean yieldingToVIP = false;

    // Metrics fields
    private int abandonedObjectives = 0;
    private int objectivesReached = 0;
    private int detoursTaken = 0;
    private double totalActiveTime = 0.0;
    private double totalWaitTime = 0.0;
    private double totalDistance = 0.0;
    private final List<String> historyLog = new ArrayList<>();
    private double congestionTimer = 0.0; 

    // Internal engine manager delegator
    private final AgentManager manager;

    /**
     * Main data constructor initializing tracking credentials.
     *
     * @param id     Unique numerical reference value mapping tracking signatures.
     * @param speed  Spatial delta translation scale velocity magnitude factor.
     * @param state  Initial lifecycle configuration flag layer.
     */
    public Agent(int id, float speed, agentState state) {
        this.id = id;
        this.speed = speed;
        this.state = state;
        this.currentPatience = this.maxPatience;
        this.manager = new AgentManager(); // Initializes delegation link
    }

    /**
     * Main core physics frame loop tick processor method updates.
     * Delegates internally to AgentManager.
     *
     * @param deltaTime The frame delta execution multiplier scale factor tracking elapsed loop time.
     */
    public void update(double deltaTime) {
        manager.update(this, deltaTime);
    }

    /**
     * Appends a target destination element onto the objective tracking pipeline array.
     *
     * @param dest The target destination Node anchor object to compute routes for.
     */
    public void addObjective(Node dest) {
        manager.addObjective(this, dest);
    }

    /**
     * Forces immediate evacuation from current structures, flushes tracking links, 
     * and shifts the state to OUT to remove the agent from workspace updates safely.
     */
    public void releaseAll() {
        manager.releaseAll(this);
    }

    /**
     * Logs a formatted timestamped message into the internal history ledger.
     *
     * @param msg The descriptive tracking text to log.
     */
    public void logMsg(String msg) {
        String entry = String.format("[%.1fs] %s", totalActiveTime, msg);
        historyLog.add(entry);
        System.out.println("Agent " + id + " : " + msg);
    }

    /**
     * Flushes and resets all tracking performance statistics, tracking timers, 
     * and historical log entries for this entity.
     */
    public void resetStats() {
        this.abandonedObjectives = 0; 
        this.objectivesReached = 0; 
        this.detoursTaken = 0;
        this.totalActiveTime = 0.0; 
        this.totalWaitTime = 0.0; 
        this.totalDistance = 0.0; 
        this.congestionTimer = 0.0;
        this.historyLog.clear();
        if (this.startingNode != null) { 
            logMsg("Respawned on node " + this.startingNode.getId() + " (Restart)"); 
        }
    }

    /**
     * Registers the starting entry anchor point location, placing the agent onto the node matrix.
     *
     * @param node The physical Node location layer to instantiate positions onto.
     */
    public void setStartingNode(Node node) {
        this.startingNode = node; 
        this.currentNode = node; 
        this.previousNode = node;
        logMsg("Spawned on node " + node.getId());
    }

    // --- STANDARD COMPATIBILITY GETTERS & SETTERS ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public agentState getState() { return state; }
    public void setState(agentState state) { this.state = state; }
    public Graph getGraph() { return graph; }
    public void setGraph(Graph graph) { this.graph = graph; }
    public Node getStartingNode() { return startingNode; }
    public Node getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }
    public Node getPreviousNode() { return previousNode; }
    public void setPreviousNode(Node previousNode) { this.previousNode = previousNode; }
    public Edge getCurrentEdge() { return currentEdge; }
    public void setCurrentEdge(Edge currentEdge) { this.currentEdge = currentEdge; }
    public Node getDestination() { return destination; }
    public void setDestination(Node destination) { this.destination = destination; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
    public int isBlockedSince() { return isBlockedSince; }
    public void setBlockedSince(int n) { this.isBlockedSince = n; }
    public Node getAuxNode() { return auxNode; }
    public void setAuxNode(Node node) { this.auxNode = node; }
    public float getDistanceTraveledOnEdge() { return distanceTraveledOnEdge; }
    public void setDistanceTraveledOnEdge(float dist) { this.distanceTraveledOnEdge = dist; }
    public List<Node> getObjectives() { return objectives; }
    public void setObjectives(List<Node> objectives) { this.objectives = objectives; }
    public List<Node> getPath() { return path; }
    public void setPath(List<Node> path) { this.path = path; }
    public int getMaxPatience() { return maxPatience; }
    public void setMaxPatience(int maxPatience) { this.maxPatience = maxPatience; }
    public int getCurrentPatience() { return currentPatience; }
    public void setCurrentPatience(int currentPatience) { this.currentPatience = currentPatience; }
    public void setPriority(int priority) { this.currentPriority = priority; }
    public int getCurrentPriority() { return currentPriority; }
    public EndBehavior getEndBehavior() { return endBehavior; }
    public void setEndBehavior(EndBehavior endBehavior) { this.endBehavior = endBehavior; }
    public void setAgentBehavior(agentBehavior behavior) { this.behavior = behavior; }
    public agentBehavior getAgentBehavior() { return this.behavior; }
    public void setAlgoType(AlgoType type) { this.algoType = type; }
    public AlgoType getAlgoType() { return this.algoType; }
    public List<Node> getReservedNodes() { return reservedNodes; }
    public void setReservedNodes(List<Node> reservedNodes) { this.reservedNodes = reservedNodes; }
    public List<Edge> getReservedEdges() { return reservedEdges; }
    public void setReservedEdges(List<Edge> reservedEdges) { this.reservedEdges = reservedEdges; }
    public boolean isRetreating() { return isRetreating; }
    public void setRetreating(boolean retreating) { this.isRetreating = retreating; }
    public void setYieldingToVIP(boolean yielding) { this.yieldingToVIP = yielding; }
    public boolean isYieldingToVIP() { return yieldingToVIP; }
    public double getCongestionTimer() { return congestionTimer; }
    public void setCongestionTimer(double congestionTimer) { this.congestionTimer = congestionTimer; }

    // --- STATISTICS COUNTERS INCREMENTORS ---
    public int getAbandonedObjectives() { return abandonedObjectives; }
    public void incrementAbandonedObjectives() { this.abandonedObjectives++; }
    public int getObjectivesReached() { return objectivesReached; }
    public void incrementObjectivesReached() { this.objectivesReached++; }
    public int getDetoursTaken() { return detoursTaken; }
    public void incrementDetoursTaken() { this.detoursTaken++; }
    public double getTotalActiveTime() { return totalActiveTime; }
    public void addActiveTime(double dt) { this.totalActiveTime += dt; }
    public double getTotalWaitTime() { return totalWaitTime; }
    public void addWaitTime(double dt) { this.totalWaitTime += dt; }
    public double getTotalDistance() { return totalDistance; }
    public void addDistance(double d) { this.totalDistance += d; }
    public List<String> getHistoryLog() { return historyLog; }
}