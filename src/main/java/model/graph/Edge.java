package model.graph;

import java.io.Serializable;
import java.util.LinkedList;
import model.agents.Agent;

/**
 * Represents a structural connection (edge/pathway) between two spatial nodes in the graph matrix.
 * Manages spatial attributes and delegates heavy traffic logic to EdgeManager to remain lightweight.
 * * @author Group D
 * @since 2026
 */
public class Edge implements Serializable {
    private static final long serialVersionUID = 1L;

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

    public enum edgeState { OUT, AVAILABLE, FULL }

    public Edge(int id, Node source, Node target, int capacity, boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.length = Math.sqrt(Math.pow((source.getX() - target.getX()), 2) + Math.pow((source.getY() - target.getY()), 2));
        this.capacity = capacity;
        this.direction = direction;
    }

    // ==========================================
    //   DELEGATION TO MANAGER
    // ==========================================

    public boolean isFull() { 
        return EdgeManager.isFull(this); 
    }

    public boolean canEnter(Agent a) { 
        return EdgeManager.canEnter(this, a); 
    }

    public boolean tryEnter(Agent a) { 
        return EdgeManager.tryEnter(this, a); 
    }

    public void leave() { 
        EdgeManager.leave(this); 
    }

    public void enqueue(Agent a) { 
        EdgeManager.enqueue(this, a); 
    }

    public void removeQueue(Agent a) { 
        EdgeManager.removeQueue(this, a); 
    }

    // ==========================================
    //           GETTERS & SETTERS
    // ==========================================
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