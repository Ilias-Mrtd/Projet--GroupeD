package model.graph;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Queue;

import model.agents.Agent;

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

    private Queue<Agent> waitingQueue = new LinkedList<>();

    public Edge(int id, Node source, Node target, int capacity, boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.length = Math
                .sqrt(Math.pow((source.getX() - target.getX()), 2) + Math.pow((source.getY() - target.getY()), 2));
        this.capacity = capacity;
        this.direction = direction;
    }

    private static final long serialVersionUID = 1L;

    public enum edgeState {
        OUT,
        AVAILABLE,
        FULL
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
                setState(edgeState.FULL);
            }
            return true;
        }
        return false;
    }

    public void leave() {
        if (getCurrentOccupants() > 0) {
            setCurrentOccupants(getCurrentOccupants() - 1);
        }
        if (!isFull()) {
            setState(edgeState.AVAILABLE);
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

    @Override
    public String toString() {
        String s = "id:" + getId() + "\r \n"
                + "source:" + getSource().getId() + "\r \n"
                + "target:" + getTarget().getId() + "\r \n"
                + "length:" + getLength() + "\r \n"
                + "capacity:" + getCapacity() + "\r \n"
                + "direction:" + hasDirection() + "\r \n";

        return s;
    }

    public float getSpeedModifier() {
        return speedModifier;
    }

    public void setSpeedModifier(float speedModifier) {
        this.speedModifier = speedModifier;
    }

    public edgeState getState() {
        return this.state;
    }

    public void setState(edgeState state) {
        this.state = state;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Node getSource() {
        return this.source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getTarget() {
        return this.target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    public boolean hasDirection() {
        return this.direction;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    public double getLength() {
        return this.length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isSelected() {
        return this.isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public int getCurrentOccupants() {
        return this.currentOccupants;
    }

    public void setCurrentOccupants(int currentOccupants) {
        this.currentOccupants = currentOccupants;
    }

    public int getExpectedOccupants() {
        return this.expectedOccupants;
    }

    public void setExpectedOccupants(int expectedOccupants) {
        this.expectedOccupants = expectedOccupants;
    }

    public Queue<Agent> getWaitingQueue() {
        return this.waitingQueue;
    }

    public void setWaitingQueue(Queue<Agent> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

}