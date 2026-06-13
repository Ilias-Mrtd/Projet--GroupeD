package model.graph;

import java.io.Serializable;
import java.util.LinkedList;
import model.agents.Agent;

/**
 * Represents a discrete spatial intersection point (vertex/node) within the graph topology.
 * Manages physical state, occupancy variables, and structural flags, while delegating 
 * operational traffic logic, construction adjustments, and queuing states to NodeManager.
 * * @author Group D
 * @since 2026
 */
public class Node implements Serializable {

    private int id;
    private float x;
    private float y;
    private int capacity;
    private nodeState state = nodeState.AVAILABLE;
    private boolean isSelected = false;
    private int currentOccupants = 0;
    private int expectedOccupants = 0;
    private int incomingOccupants = 0;

    private LinkedList<Agent> waitingQueue = new LinkedList<>();

    private boolean isUnderConstruction = false;
    private int savedCapacity = 1;

    private static final long serialVersionUID = 1L;

    /**
     * Operational state descriptor flags characterizing node structural availability.
     */
    public enum nodeState { OUT, AVAILABLE, FULL }

    /**
     * Constructs a new Node with specified coordinates and occupancy limits.
     * * @param id Unique numerical identifier for the node.
     * @param x The horizontal coordinate component mapping layout offsets.
     * @param y The vertical coordinate component mapping layout offsets.
     * @param capacity Maximum number of concurrent agents authorized on this node.
     */
    public Node(int id, float x, float y, int capacity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
    }

    // =========================================================================
    //   DELEGATION PATTERN (Prevents breaking changes across the codebase)
    // =========================================================================

    /**
     * Modifies the node's construction status. Enabling construction preserves the initial
     * capacity parameters before dropping operational limits down to zero and forcing a FULL status flag.
     * Disabling construction gracefully restores the baseline configuration criteria.
     * * @param underConstruction true to isolate this node for maintenance; false to re-enable it.
     */
    public void setUnderConstruction(boolean underConstruction) {
        NodeManager.setUnderConstruction(this, underConstruction);
    }

    /**
     * Evaluates if the total count of active occupants has reached or bypassed the node's maximum capacity.
     * * @return true if the node structural limits are fully saturated; false otherwise.
     */
    public boolean isFull() {
        return NodeManager.isFull(this);
    }

    /**
     * Validates if a given agent is permitted to enter the node based on current capacity
     * availability and priority placement at the front of the waiting line.
     * * @param a The tracking Agent component requesting access validation.
     * @return true if space is clear and the agent holds entry priority; false if blocked.
     */
    public boolean canEnter(Agent a) {
        return NodeManager.canEnter(this, a);
    }

    /**
     * Processes registration lookup interactions for an agent attempting to merge onto this vertex.
     * Higher-order behavioral profiles like VIP bypass typical structural capacity thresholds, 
     * forcing registration values forward instantly.
     * * @param a The tracking Agent object requesting physical access clearance.
     * @return true if structural parameters updated and entry was cleared; false if turned away.
     */
    public boolean tryEnter(Agent a) {
        return NodeManager.tryEnter(this, a);
    }

    /**
     * Forcefully increments the internal occupant counters bypassing any conditional 
     * validation workflows, matching safety status triggers as required.
     */
    public void forceEnter() {
        NodeManager.forceEnter(this);
    }

    /**
     * Decrements the active entity density registry whenever an occupant leaves the node vertex,
     * resetting state descriptors to AVAILABLE when constraints clear up.
     */
    public void leave() {
        NodeManager.leave(this);
    }

    /**
     * Appends an agent into the structural waiting registration list. Priority rules 
     * ensure that incoming VIP agents jump the queue, positioning themselves ahead of 
     * standard agents while keeping behind already queued VIP elements.
     * * @param a The tracking Agent component requesting queue insertion registration.
     */
    public void enqueue(Agent a) {
        NodeManager.enqueue(this, a);
    }

    /**
     * Removes an agent immediately from the structural waiting lineup array, canceling pending requests.
     * * @param a The target Agent instance to dismiss from the line.
     */
    public void removeQueue(Agent a) {
        NodeManager.removeQueue(this, a);
    }

    // =========================================================================
    //           GETTERS & SETTERS (Standard Encapsulation)
    // =========================================================================
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public float getX() { return x; }
    public void setX(float x) { this.x = x; }
    public float getY() { return y; }
    public void setY(float y) { this.y = y; }
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public nodeState getState() { return state; }
    public void setState(nodeState state) { this.state = state; }
    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean isSelected) { this.isSelected = isSelected; }
    public int getCurrentOccupants() { return currentOccupants; }
    public void setCurrentOccupants(int currentOccupants) { this.currentOccupants = currentOccupants; }
    public int getExpectedOccupants() { return expectedOccupants; }
    public void setExpectedOccupants(int expectedOccupants) { this.expectedOccupants = expectedOccupants; }
    public int getIncomingOccupants() { return incomingOccupants; }
    public void setIncomingOccupants(int incomingOccupants) { this.incomingOccupants = incomingOccupants; }
    public LinkedList<Agent> getWaitingQueue() { return waitingQueue; }
    public void setWaitingQueue(LinkedList<Agent> waitingQueue) { this.waitingQueue = waitingQueue; }
    public boolean isUnderConstruction() { return isUnderConstruction; }
    public void setRawUnderConstruction(boolean underConstruction) { this.isUnderConstruction = underConstruction; }
    public int getSavedCapacity() { return savedCapacity; }
    public void setSavedCapacity(int savedCapacity) { this.savedCapacity = savedCapacity; }
}