package model.graph;

import java.io.Serializable;
import java.util.LinkedList;

import model.agents.Agent;

/**
 * Represents a structural connection (edge/pathway) between two spatial nodes in the graph matrix.
 * Manages spatial attributes, traffic bottlenecks via capacity restriction thresholds, speed factors,
 * and maintains an optimized waiting queue handling priority-based agent injection.
 * * @author Group D
 */
public class Edge implements Serializable {

    private int id;
    private Node source;
    private Node target;
    private boolean direction;
    private double length;
    private int capacity;
    private edgeState state = edgeState.AVAILABLE;
    private boolean isSelected = false;

    private int currentOccupants = 0;
    private int expectedOccupants = 0;
    private float speedModifier = 1.0f;

    private LinkedList<Agent> waitingQueue = new LinkedList<>();

    /**
     * Constructs a new Edge instance linking two spatial nodes and computes its geometric Euclidean length.
     * * @param id Unique numerical identifier for the edge.
     * @param source The origin source Node instance.
     * @param target The destination target Node instance.
     * @param capacity Maximum threshold of concurrent agents authorized on this pathway segment.
     * @param direction Orientation behavior flag (true if unidirectional, false if bidirectional).
     */
    public Edge(int id, Node source, Node target, int capacity, boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.length = Math.sqrt(Math.pow((source.getX() - target.getX()), 2) + Math.pow((source.getY() - target.getY()), 2));
        this.capacity = capacity;
        this.direction = direction;
    }

    private static final long serialVersionUID = 1L;

    /**
     * Internal operational status of the edge layout structural segment.
     */
    public enum edgeState { OUT, AVAILABLE, FULL }

    /**
     * Evaluates if the current density of active occupants has reached or surpassed the maximum capacity threshold.
     * * @return true if the edge segment is completely congested; false otherwise.
     */
    public boolean isFull() { return getCurrentOccupants() >= getCapacity(); }

    /**
     * Validates whether an agent is allowed to access this segment based on space availability and queue priority rules.
     * * @param a The tracking Agent object requesting entry validation checks.
     * @return true if the segment has available capacity and the agent holds priority at the front of the queue.
     */
    public boolean canEnter(Agent a) {
        return !isFull() && (getWaitingQueue().isEmpty() || getWaitingQueue().peek() == a);
    }

    /**
     * Processes transactional clearance registration requests for an agent seeking entry onto this path segment.
     * VIP agents bypass standard capacity blocks, forcing immediate entry regardless of congestion levels.
     * * @param a The tracking Agent attempting to cross onto this path segment.
     * @return true if entry access was successfully granted and state adjustments registered; false if rejected.
     */
    public boolean tryEnter(Agent a) {
        // VIP FORCES ENTRY REGARDLESS OF CONGESTION!
        if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
            getWaitingQueue().remove(a);
            setCurrentOccupants(getCurrentOccupants() + 1);
            if (isFull()) {
                setState(edgeState.FULL);
            }
            return true;
        }

        if (canEnter(a)) {
            getWaitingQueue().remove(a);
            setCurrentOccupants(getCurrentOccupants() + 1);
            if (isFull()) {
                setState(edgeState.FULL);
            }
            return true;
        }
        return false;
    }

    /**
     * Decrements the active occupancy tally when an entity leaves this edge segment, 
     * releasing structural locks and restoring AVAILABLE status flags as needed.
     */
    public void leave() {
        if (getCurrentOccupants() > 0) { setCurrentOccupants(getCurrentOccupants() - 1); }
        if (!isFull()) { setState(edgeState.AVAILABLE); }
    }

    /**
     * Appends an agent to the back of the line or performs priority-based insertion 
     * at the front of non-VIP lines if the agent possesses a VIP behavior model.
     * * @param a The tracking Agent item requesting queue insertion registration.
     */
    public void enqueue(Agent a) {
        if (!getWaitingQueue().contains(a)) {
            if (a.getAgentBehavior() == Agent.agentBehavior.VIP) {
                int insertIndex = 0;
                for (Agent waiting : getWaitingQueue()) {
                    if (waiting.getAgentBehavior() != Agent.agentBehavior.VIP) break;
                    insertIndex++;
                }
                getWaitingQueue().add(insertIndex, a);
            } else {
                getWaitingQueue().add(a);
            }
        }
    }

    /**
     * Removes an agent immediately from the internal queue, canceling its pending transit requests.
     * * @param a The target Agent component to evict from the line.
     */
    public void removeQueue(Agent a) { getWaitingQueue().remove(a); }

    public float getSpeedModifier() { return speedModifier; }
    public void setSpeedModifier(float speedModifier) { this.speedModifier = speedModifier; }
    public edgeState getState() { return this.state; }
    public void setState(edgeState state) { this.state = state; }
    public int getId() { return this.id; }
    public void setId(int id) { this.id = id; }
    public Node getSource() { return this.source; }
    public void setSource(Node source) { this.source = source; }
    public Node getTarget() { return this.target; }
    public void setTarget(Node target) { this.target = target; }
    public boolean hasDirection() { return this.direction; }
    public void setDirection(boolean direction) { this.direction = direction; }
    public double getLength() { return this.length; }
    public void setLength(double length) { this.length = length; }
    public int getCapacity() { return this.capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public boolean isSelected() { return this.isSelected; }
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
    public int getCurrentOccupants() { return this.currentOccupants; }
    public void setCurrentOccupants(int currentOccupants) { this.currentOccupants = currentOccupants; }
    public int getExpectedOccupants() { return this.expectedOccupants; }
    public void setExpectedOccupants(int expectedOccupants) { this.expectedOccupants = expectedOccupants; }
    public LinkedList<Agent> getWaitingQueue() { return this.waitingQueue; }
    public void setWaitingQueue(LinkedList<Agent> waitingQueue) { this.waitingQueue = waitingQueue; }
}