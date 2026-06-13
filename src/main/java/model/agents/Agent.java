package model.agents;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import model.graph.*;
import simulationEngine.algorithm.Dijkstra;
import simulationEngine.algorithm.AStar;
import simulationEngine.algorithm.AbstractAlgorithm;

/**
 * Represents an autonomous agent within the warehouse simulation.
 * The agent manages its own state, pathfinding calculations, objective queues,
 * spatial resource reservations, and movement mechanics along the graph topology.
 * * @author Group D
 */
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

    /**
     * Supported routing algorithm options for path calculation.
     */
    public enum AlgoType { DIJKSTRA, ASTAR, RANDOM }
    private AlgoType algoType = AlgoType.RANDOM;

    private List<Node> reservedNodes = new ArrayList<>();
    private List<Edge> reservedEdges = new ArrayList<>();
    private boolean isRetreating = false;
    
    // Status for "Pulled over on the side"
    private boolean yieldingToVIP = false;

    private int abandonedObjectives = 0;
    private int objectivesReached = 0;
    private int detoursTaken = 0;
    private double totalActiveTime = 0.0;
    private double totalWaitTime = 0.0;
    private double totalDistance = 0.0;
    private List<String> historyLog = new ArrayList<>();
    private static final long serialVersionUID = 1L;
    
    private double congestionTimer = 0.0; 

    /**
     * Logs a formatted timestamped message into the internal history ledger 
     * and forwards it to the standard output console.
     * * @param msg The descriptive tracking text to log.
     */
    public void logMsg(String msg) {
        String entry = String.format("[%.1fs] %s", totalActiveTime, msg);
        historyLog.add(entry);
        System.out.println("Agent " + id + " : " + msg);
    }

    public int getAbandonedObjectives() { return abandonedObjectives; }
    public int getObjectivesReached() { return objectivesReached; }
    public int getDetoursTaken() { return detoursTaken; }
    public double getTotalActiveTime() { return totalActiveTime; }
    public double getTotalWaitTime() { return totalWaitTime; }
    public double getTotalDistance() { return totalDistance; }
    public List<String> getHistoryLog() { return historyLog; }

    /**
     * Flushes and resets all tracking performance statistics, tracking timers, 
     * and historical log entries for this entity.
     */
    public void resetStats() {
        this.abandonedObjectives = 0; this.objectivesReached = 0; this.detoursTaken = 0;
        this.totalActiveTime = 0.0; this.totalWaitTime = 0.0; this.totalDistance = 0.0; this.congestionTimer = 0.0;
        this.historyLog.clear();
        if (this.startingNode != null) { logMsg("Respawned on node " + this.startingNode.getId() + " (Restart)"); }
    }

    /**
     * Behaviors applied once all objectives in the queue have been successfully processed.
     */
    public enum EndBehavior { STOP, REMOVE, RANDOM_WANDER }
    
    /**
     * Internal structural state of the agent lifecycle framework.
     */
    public enum agentState { AVAILABLE, CALCULATING, RUNNING, WAITING, OUT }
    
    /**
     * Behavioral profile defining the agent's reaction guidelines and traversal priorities.
     */
    public enum agentBehavior {
        VIP(10), HURRIED(2), PATIENT(1), BROKEN(0); 
        private final int priority;
        agentBehavior(int priority) { this.priority = priority; }
        public int getPriority() { return priority; }
    }

    /**
     * Registers the starting entry anchor point location, placing the agent onto the node matrix.
     * * @param node The physical Node location layer to instantiate positions onto.
     */
    public void setStartingNode(Node node) {
        this.startingNode = node; this.currentNode = node; this.previousNode = node;
        logMsg("Spawned on node " + node.getId());
    }

    public void setAgentBehavior(agentBehavior behavior) { this.behavior = behavior; }
    public agentBehavior getAgentBehavior() { return this.behavior; }
    public void setAlgoType(AlgoType type) { this.algoType = type; }
    public AlgoType getAlgoType() { return this.algoType; }

    /**
     * Internal terminal routing block triggered when the objective list becomes empty.
     * Clears physical tracking map footprints based on the configured EndBehavior.
     */
    private void handleEndBehavior() {
        logMsg("Completed all objectives. Final behavior: " + getEndBehavior());
        clearReservations();
        switch (getEndBehavior()) {
            case STOP: setState(agentState.AVAILABLE); break;
            case REMOVE: setState(agentState.OUT); if (getCurrentNode() != null) getCurrentNode().leave(); logMsg("Left the simulation."); break;
            case RANDOM_WANDER: setState(agentState.AVAILABLE); if (this.currentNode != null) this.currentNode.leave(); break;
        }
    }

    /**
     * Scans the pre-calculated node layout array to increment expected occupancy 
     * density counters ahead of time, preventing deadlocks unless broken.
     */
    private void makeReservations() {
        if (getAgentBehavior() == agentBehavior.BROKEN) return;
        clearReservations();
        Node prev = getCurrentNode();
        for (Node n : getPath()) {
            n.setExpectedOccupants(n.getExpectedOccupants() + 1);
            getReservedNodes().add(n);
            Edge e = findEdgeBetween(prev, n);
            if (e != null) { e.setExpectedOccupants(e.getExpectedOccupants() + 1); getReservedEdges().add(e); }
            prev = n;
        }
    }

    /**
     * Flushes and decrements expected occupation indicators across all previously requested nodes and edges.
     */
    private void clearReservations() {
        for (Node n : getReservedNodes()) { if (n.getExpectedOccupants() > 0) n.setExpectedOccupants(n.getExpectedOccupants() - 1); }
        getReservedNodes().clear();
        for (Edge e : getReservedEdges()) { if (e.getExpectedOccupants() > 0) e.setExpectedOccupants(e.getExpectedOccupants() - 1); }
        getReservedEdges().clear();
    }

    /**
     * Forces immediate evacuation from current structures, flushes tracking links, 
     * and shifts the state to OUT to remove the agent from workspace updates safely.
     */
    public void releaseAll() {
        clearReservations();
        if (getCurrentEdge() != null) {
            Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget() : getCurrentEdge().getSource();
            if (targetNode != null && targetNode.getIncomingOccupants() > 0) { targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1); }
            getCurrentEdge().leave(); getCurrentEdge().removeQueue(this); setCurrentEdge(null);
        }
        if (getCurrentNode() != null) { getCurrentNode().leave(); getCurrentNode().removeQueue(this); }
        getPath().clear(); getObjectives().clear(); setState(agentState.OUT);
    }

    /**
     * Main data constructor initializing tracking credentials.
     * * @param id    Unique numerical reference value mapping tracking signatures.
     * @param speed Spatial delta translation scale velocity magnitude factor.
     * @param state Initial lifecycle configuration flag layer.
     */
    public Agent(int id, float speed, agentState state) {
        setId(id); setSpeed(speed); setState(state); setCurrentPatience(getMaxPatience());
    }

    /**
     * Internal adjacency helper lookup mapping structural pathways connectivity.
     * * @param s The starting source graph node model element.
     * @param t The target destination graph node model element.
     * @return The connecting Edge reference if validly located; null otherwise.
     */
    private Edge findEdgeBetween(Node s, Node t) {
        int index = getGraph().getNodes().indexOf(s);
        if (index != -1) {
            for (Edge e : getGraph().getEdges().get(index)) {
                if (e.getSource() == t || e.getTarget() == t) return e;
            }
        }
        return null;
    }

    /**
     * Appends a target destination element onto the objective tracking pipeline array.
     * Triggers navigation processing loops automatically if the agent is idle.
     * * @param dest The target destination Node anchor object to compute routes for.
     */
    public void addObjective(Node dest) {
        getObjectives().add(dest);
        logMsg("New objective received: Node " + dest.getId());
        if (getState() == agentState.AVAILABLE) { startNextObjective(); }
    }

    /**
     * Factories the specific discrete routing algorithm framework matching instructions parameters.
     * * @return An instantiated path solver implementation matching requested constraints.
     */
    private AbstractAlgorithm getCalculator() {
        if (this.algoType == AlgoType.DIJKSTRA) {
            logMsg("Calculating path (Forced: Dijkstra)");
            return new Dijkstra(getGraph(), getCurrentNode(), getDestination());
        } else if (this.algoType == AlgoType.ASTAR) {
            logMsg("Calculating path (Forced: A*)");
            return new AStar(getGraph(), getCurrentNode(), getDestination());
        } else {
            if (Math.random() < 0.5) {
                logMsg("Calculating path (Random -> Dijkstra)");
                return new Dijkstra(getGraph(), getCurrentNode(), getDestination());
            } else {
                logMsg("Calculating path (Random -> A*)");
                return new AStar(getGraph(), getCurrentNode(), getDestination());
            }
        }
    }

    /**
     * Extracts the upcoming destination item to start calculations, computes routing grids, 
     * sets up structural reservations, and marks state shifts.
     */
    private void startNextObjective() {
        if (!getObjectives().isEmpty()) {
            setDestination(getObjectives().remove(0));
            setState(agentState.CALCULATING);
            if (getGraph() != null && getCurrentNode() != null) {
                AbstractAlgorithm calculator = getCalculator();
                if (calculator.getPath().isEmpty() && getCurrentNode().getId() != getDestination().getId()) {
                    logMsg("❌ NO PATH to node " + getDestination().getId() + "! Objective abandoned.");
                    abandonedObjectives++; startNextObjective(); return;
                }
                setPath(calculator.getPath()); makeReservations();
            }
            setState(agentState.RUNNING); setCurrentPatience(getMaxPatience());
            logMsg(">>> En route to objective: Node " + getDestination().getId());
        } else {
            handleEndBehavior();
        }
    }

    /**
     * Computes adjacent exit branches to execute random escape maneuvers 
     * or forces dynamic path recalculated solutions when patience runs thin.
     */
    private void applyDetour() {
        detoursTaken++;
        logMsg("!!! Looking for a detour to unblock the situation !!!");
        List<Node> validDetours = new ArrayList<>();
        List<Node> fallbackDetours = new ArrayList<>();

        int index = getGraph().getNodes().indexOf(getCurrentNode());
        if (index != -1) {
            for (Edge e : getGraph().getEdges().get(index)) {
                Node neighbor = null;
                if (!e.hasDirection()) { if (e.getSource() == getCurrentNode()) { neighbor = e.getTarget(); } else continue; } 
                else { neighbor = (e.getSource() == getCurrentNode()) ? e.getTarget() : e.getSource(); }

                if (neighbor == null) continue;
                if (!getPath().isEmpty() && neighbor.getId() == getPath().get(0).getId()) continue;

                if (neighbor.getState() != Node.nodeState.FULL && !neighbor.isUnderConstruction()) {
                    if (previousNode != null && neighbor.getId() == previousNode.getId()) { fallbackDetours.add(neighbor); } 
                    else { validDetours.add(neighbor); }
                }
            }
        }

        Node detour = null;
        if (!validDetours.isEmpty()) { detour = validDetours.get((int) (Math.random() * validDetours.size())); } 
        else if (!fallbackDetours.isEmpty()) { detour = fallbackDetours.get((int) (Math.random() * fallbackDetours.size())); }

        if (detour != null) {
            logMsg("↪️ Taking a random detour to Node " + detour.getId());
            getPath().clear(); getPath().add(detour); makeReservations();
        } else {
            logMsg("No street to detour, recalculating route...");
            AbstractAlgorithm calculator = getCalculator();
            if (calculator.getPath().isEmpty() && getCurrentNode().getId() != getDestination().getId()) {
                logMsg("❌ Completely blocked towards node " + getDestination().getId() + ". Objective abandoned!");
                abandonedObjectives++; startNextObjective(); return;
            }
            setPath(calculator.getPath()); makeReservations();
        }
        setState(agentState.RUNNING); setCurrentPatience(getMaxPatience());
    }

    /**
     * Looks ahead along the upcoming path arrays to determine if subsequent 
     * segments are over capacity thresholds, enabling early waiting triggers.
     * * @param depth The look-ahead evaluation step horizon.
     * @return true if the requested preview segments are clear; false if bottlenecks are detected.
     */
    private boolean isPathClearAhead(int depth) {
        if (behavior == agentBehavior.HURRIED || behavior == agentBehavior.VIP) return true;
        if (getPath().size() < 2) return true;

        Node prev = getPath().get(0);
        int limit = Math.min(depth, getPath().size());

        for (int i = 1; i < limit; i++) {
            Node nextNode = getPath().get(i);
            Edge nextEdge = findEdgeBetween(prev, nextNode);
            if (nextEdge != null && nextEdge.getCurrentOccupants() >= nextEdge.getCapacity()) return false;
            if (nextNode.getCurrentOccupants() + nextNode.getIncomingOccupants() >= nextNode.getCapacity()) return false;
            prev = nextNode;
        }
        return true;
    }

    /**
     * Main core physics frame loop tick processor method updates.
     * Updates movement vectors, updates patience parameters, monitors structural traffic rules compliance, 
     * evaluates emergency siren halts, handles detour switching logic and updates spatial counters.
     * * @param deltaTime The frame delta execution multiplier scale factor tracking elapsed loop time.
     */
    public void update(double deltaTime) {
        
        // =======================================================
        // EMERGENCY STOP LOGIC FOR VIP PRIORITY
        // =======================================================
        if (yieldingToVIP) {
            if (getState() != agentState.WAITING) {
                setState(agentState.WAITING);
                logMsg("🚓 Sirens heard! Pulling over for VIP...");
            }
            totalWaitTime += deltaTime;
            return; // EXIT UPDATE: The agent freezes on the spot!
        }

        if (getState() == agentState.RUNNING || getState() == agentState.WAITING || getState() == agentState.CALCULATING) {
            totalActiveTime += deltaTime;
        }

        if (getState() == agentState.WAITING) {
            totalWaitTime += deltaTime;
            int decrease = 1;
            switch (getAgentBehavior()) {
                case VIP: decrease = 5; break; // VIP loses patience extremely fast
                case HURRIED: decrease = 3; break;
                case PATIENT: decrease = 1; break;
                case BROKEN: decrease = 1; break;
            }
            if (getCurrentNode() == getStartingNode() && getCurrentEdge() == null) {
                if (Math.random() < 0.5) setCurrentPatience(getCurrentPatience() - decrease);
            } else {
                setCurrentPatience(getCurrentPatience() - decrease * 2);
            }

            if (getCurrentPatience() <= 0) {
                if (isRetreating()) {
                    getCurrentNode().removeQueue(this);
                } else if (getCurrentEdge() == null && !getPath().isEmpty()) {
                    Edge nextEdge = findEdgeBetween(getCurrentNode(), getPath().get(0));
                    if (nextEdge != null) nextEdge.removeQueue(this);
                } else if (getCurrentEdge() != null) {
                    Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget() : getCurrentEdge().getSource();
                    targetNode.removeQueue(this);
                }

                if (getCurrentEdge() != null) {
                    logMsg("!!! Lost patience, REVERSING on the edge !!!");
                    setRetreating(true); setState(agentState.RUNNING);
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
                        logMsg("Step 3: Routine recalculation.");
                        getPath().clear(); getObjectives().add(0, destination); startNextObjective();
                    }
                } else {
                    setAuxNode(getCurrentNode());
                }
            }

            if (getCurrentEdge() == null) {
                
                if (getCurrentNode() != null && getCurrentNode().getCurrentOccupants() > getCurrentNode().getCapacity()) {
                    if (behavior != agentBehavior.VIP) { // <-- VIP IGNORES PENALTY
                        if (congestionTimer < 2.0) { 
                            congestionTimer += deltaTime;
                            if (getState() != agentState.WAITING) {
                                setState(agentState.WAITING);
                                logMsg("⚠️ Heavy Node Congestion! 2s penalty applied...");
                            }
                            return; 
                        }
                    }
                }
                congestionTimer = 0.0; 

                if (!getPath().isEmpty()) {
                    if (!isPathClearAhead(2)) {
                        if (getState() != agentState.WAITING) {
                            setState(agentState.WAITING);
                            logMsg("🧠 Anticipating a traffic jam, doing SMART WAITING.");
                        }
                        return;
                    }

                    Node nextNode = getPath().get(0);
                    Edge nextEdge = findEdgeBetween(getCurrentNode(), nextNode);

                    if (nextEdge != null) {
                        if (nextEdge.tryEnter(this)) {
                            if (getReservedEdges().contains(nextEdge)) {
                                if (nextEdge.getExpectedOccupants() > 0) nextEdge.setExpectedOccupants(nextEdge.getExpectedOccupants() - 1);
                                getReservedEdges().remove(nextEdge);
                            }
                            if (getCurrentNode() != null) getCurrentNode().leave();
                            setCurrentEdge(nextEdge); getPath().remove(0); setDistanceTraveledOnEdge(0.0f);
                            setState(agentState.RUNNING); setCurrentPatience(getMaxPatience());

                            nextNode.setIncomingOccupants(nextNode.getIncomingOccupants() + 1);
                            logMsg("🟢 ENTERING towards node " + nextNode.getId());

                        } else {
                            nextEdge.enqueue(this);
                            if (getState() != agentState.WAITING) setState(agentState.WAITING);
                            return;
                        }
                    }
                } else {
                    if (getCurrentNode() != null && getDestination() != null && getCurrentNode().getId() == getDestination().getId()) {
                        logMsg("✅ Objective Node " + getDestination().getId() + " REACHED!");
                        objectivesReached++;
                    }
                    if (getObjectives().isEmpty()) { handleEndBehavior(); } else { startNextObjective(); }
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
                            Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget() : getCurrentEdge().getSource();
                            if (targetNode.getIncomingOccupants() > 0) targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);
                            getCurrentEdge().leave(); setCurrentEdge(null); setRetreating(false);
                            logMsg("Returned to node and freed the edge."); applyDetour();
                        } else {
                            getCurrentNode().enqueue(this);
                            if (getState() != agentState.WAITING) setState(agentState.WAITING);
                        }
                    }
                } else {
                    if (getDistanceTraveledOnEdge() + distMoved <= getCurrentEdge().getLength()) { totalDistance += distMoved; } 
                    else { totalDistance += (getCurrentEdge().getLength() - getDistanceTraveledOnEdge()); }

                    if (getDistanceTraveledOnEdge() < getCurrentEdge().getLength()) {
                        setDistanceTraveledOnEdge(getDistanceTraveledOnEdge() + distMoved);
                    }

                    if (getDistanceTraveledOnEdge() >= getCurrentEdge().getLength()) {
                        setDistanceTraveledOnEdge((float) getCurrentEdge().getLength());
                        Node targetNode = (getCurrentEdge().getSource() == getCurrentNode()) ? getCurrentEdge().getTarget() : getCurrentEdge().getSource();

                        if (targetNode.tryEnter(this)) {
                            if (targetNode.getIncomingOccupants() > 0) targetNode.setIncomingOccupants(targetNode.getIncomingOccupants() - 1);
                            if (getReservedNodes().contains(targetNode)) {
                                if (targetNode.getExpectedOccupants() > 0) targetNode.setExpectedOccupants(targetNode.getExpectedOccupants() - 1);
                                getReservedNodes().remove(targetNode);
                            }
                            previousNode = getCurrentNode(); setCurrentNode(targetNode); getCurrentEdge().leave();
                            setCurrentEdge(null); setState(agentState.RUNNING); setCurrentPatience(getMaxPatience());
                            logMsg("📍 ARRIVED at node " + getCurrentNode().getId());

                            if (getPath().isEmpty()) {
                                if (getCurrentNode().getId() == getDestination().getId()) {
                                    logMsg("✅ Final Objective Node " + getDestination().getId() + " REACHED!");
                                    objectivesReached++;
                                    if (!getObjectives().isEmpty()) { startNextObjective(); } else { handleEndBehavior(); }
                                } else {
                                    logMsg("🔄 Finished detouring. Recalculating path to objective " + getDestination().getId());
                                    AbstractAlgorithm calculator = getCalculator();
                                    if (calculator.getPath().isEmpty()) {
                                        logMsg("❌ Route destroyed to node " + getDestination().getId() + ". Objective abandoned!");
                                        abandonedObjectives++; startNextObjective();
                                    } else {
                                        setPath(calculator.getPath()); makeReservations();
                                    }
                                }
                            }
                        } else {
                            targetNode.enqueue(this);
                            if (getState() != agentState.WAITING) setState(agentState.WAITING);
                        }
                    }
                }
            }
        }
    }

    public void setId(int id) { this.id = id; }
    public int getId() { return this.id; }
    public float getSpeed() { return this.speed; }
    public void setSpeed(float speed) { this.speed = speed; }
    public agentState getState() { return this.state; }
    public void setState(agentState state) { this.state = state; }
    public Graph getGraph() { return this.graph; }
    public void setGraph(Graph graph) { this.graph = graph; }
    public Node getStartingNode() { return this.startingNode; }
    public Node getCurrentNode() { return this.currentNode; }
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }
    public Edge getCurrentEdge() { return this.currentEdge; }
    public void setCurrentEdge(Edge currentEdge) { this.currentEdge = currentEdge; }
    public Node getDestination() { return this.destination; }
    public void setDestination(Node destination) { this.destination = destination; }
    public boolean isSelected() { return this.isSelected; }
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
    public float getDistanceTraveledOnEdge() { return this.distanceTraveledOnEdge; }
    public void setDistanceTraveledOnEdge(float distanceTraveledOnEdge) { this.distanceTraveledOnEdge = distanceTraveledOnEdge; }
    public List<Node> getObjectives() { return this.objectives; }
    public void setObjectives(List<Node> objectives) { this.objectives = objectives; }
    public List<Node> getPath() { return this.path; }
    public void setPath(List<Node> path) { this.path = path; }
    public int getMaxPatience() { return this.maxPatience; }
    public void setMaxPatience(int maxPatience) { this.maxPatience = maxPatience; }
    public int getCurrentPatience() { return this.currentPatience; }
    public void setCurrentPatience(int currentPatience) { this.currentPatience = currentPatience; }
    public EndBehavior getEndBehavior() { return this.endBehavior; }
    public void setEndBehavior(EndBehavior endBehavior) { this.endBehavior = endBehavior; }
    public List<Node> getReservedNodes() { return this.reservedNodes; }
    public void setReservedNodes(List<Node> reservedNodes) { this.reservedNodes = reservedNodes; }
    public List<Edge> getReservedEdges() { return this.reservedEdges; }
    public void setReservedEdges(List<Edge> reservedEdges) { this.reservedEdges = reservedEdges; }
    public boolean isRetreating() { return this.isRetreating; }
    public void setRetreating(boolean isRetreating) { this.isRetreating = isRetreating; }
    public Node getAuxNode() { return this.auxNode; }
    public void setAuxNode(Node node) { this.auxNode = node; }
    public int isBlockedSince() { return this.isBlockedSince; }
    public void setBlockedSince(int n) { this.isBlockedSince = n; }
    public void setPriority(int priority) { this.currentPriority = priority; }
    public int getCurrentPriority() { return this.currentPriority; }
    
    public void setYieldingToVIP(boolean yielding) { this.yieldingToVIP = yielding; }
    public boolean isYieldingToVIP() { return yieldingToVIP; }
}