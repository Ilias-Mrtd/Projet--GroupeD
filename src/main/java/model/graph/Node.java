package model.graph;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import model.agents.Agent;

public class Node implements Serializable {

    public static final double RADIUS = 16.0;
    public static final double COLLISION_CLEARANCE = 2.0;

    private int id;
    private float x;
    private float y;
    private int capacity;
    private nodeState state = nodeState.AVAILABLE;
    private boolean isSelected = false;
    private int currentOccupants = 0;
    private int expectedOccupants = 0;
    private int incomingOccupants = 0;

    private Queue<Agent> waitingQueue = new LinkedList<>();

    private boolean isUnderConstruction = false;
    private int savedCapacity = 1;

    public Node(int id, float x, float y, int capacity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
    }

    private static final long serialVersionUID = 1L;

    public void setUnderConstruction(boolean underConstruction) {
        this.isUnderConstruction = underConstruction;
        if (underConstruction) {
            this.savedCapacity = this.capacity;
            this.capacity = 0;

            this.setState(nodeState.FULL);
        } else {

            this.capacity = this.savedCapacity;

            if (this.currentOccupants < this.capacity) {
                this.setState(nodeState.AVAILABLE);
            }
        }
    }

    public boolean isFull() {
        return getCurrentOccupants() >= getCapacity();
    }

    public boolean canEnter(Agent a) {
        return !isFull() && (getWaitingQueue().isEmpty() || getWaitingQueue().peek() == a);
    }

    public boolean tryEnter(Agent a) {
        if (canEnter(a)) {
            getWaitingQueue().remove(a);
            setCurrentOccupants(getCurrentOccupants() + 1);
            if (isFull()) {
                setState(nodeState.FULL);
            }
            return true;
        }
        return false;
    }

    public void forceEnter() {
        setCurrentOccupants(getCurrentOccupants() + 1);
        if (isFull()) {
            setState(nodeState.FULL);
        }
    }

    public void leave() {
        if (getCurrentOccupants() > 0) {
            setCurrentOccupants(getCurrentOccupants() - 1);
        }
        if (!isFull()) {
            setState(nodeState.AVAILABLE);
        }
    }

    public void enqueue(Agent a) {
        if (!getWaitingQueue().contains(a)) {
            getWaitingQueue().add(a);
        }
    }

    public void removeQueue(Agent a) {
        getWaitingQueue().remove(a);
    }

    public enum nodeState {
        OUT,
        AVAILABLE,
        FULL
    }

    @Override
    public String toString() {
        String s = "id:" + getId() + "\r \n"
                + "X:" + getX() + "\r \n"
                + "Y:" + getY() + "\r \n"
                + "capacity:" + getCapacity() + "\r \n"
                + "state:" + getState() + "\r\n";

        return s;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public nodeState getState() {
        return state;
    }

    public void setState(nodeState state) {
        this.state = state;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getCurrentOccupants() {
        return currentOccupants;
    }

    public void setCurrentOccupants(int currentOccupants) {
        this.currentOccupants = currentOccupants;
    }

    public int getExpectedOccupants() {
        return expectedOccupants;
    }

    public void setExpectedOccupants(int expectedOccupants) {
        this.expectedOccupants = expectedOccupants;
    }

    public int getIncomingOccupants() {
        return incomingOccupants;
    }

    public void setIncomingOccupants(int incomingOccupants) {
        this.incomingOccupants = incomingOccupants;
    }

    public Queue<Agent> getWaitingQueue() {
        return waitingQueue;
    }

    public void setWaitingQueue(Queue<Agent> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

    public boolean isUnderConstruction() {
        return isUnderConstruction;
    }
}
