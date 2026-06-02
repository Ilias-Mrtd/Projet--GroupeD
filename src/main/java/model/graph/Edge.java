package model.graph;

import java.util.LinkedList;
import java.util.Queue;

import model.agents.Agent;

public class Edge {

    public int id;
    public Node source;
    public Node target;
    public boolean direction;
    public double length;
    public int capacity;
    public edgeState state = edgeState.AVAILABLE;
    public boolean isSelected = false;

    public int currentOccupants = 0;
    public int expectedOccupants = 0;

    public Queue<Agent> waitingQueue = new LinkedList<>();

    public Edge(int id, Node source, Node target, int capacity, boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.length = Math.sqrt(Math.pow((source.x - target.x), 2) + Math.pow((source.y - target.y), 2));
        this.capacity = capacity;
        this.direction = direction;
    }
    
    public enum edgeState {
        OUT,
        AVAILABLE,
        FULL
    }

    public boolean isFull() {
        return currentOccupants >= capacity;
    }

    public boolean canEnter(Agent a) {
        return !isFull() && (waitingQueue.isEmpty() || waitingQueue.peek() == a);
    }

    public boolean tryEnter(Agent a) {
        if (canEnter(a)) {
            waitingQueue.remove(a);
            currentOccupants++;
            if (isFull()) {
                this.state = edgeState.FULL;
            }
            return true;
        }
        return false;
    }

    public void leave() {
        if (currentOccupants > 0) {
            currentOccupants--;
        }
        if (!isFull()) {
            this.state = edgeState.AVAILABLE;
        }
    }

    public void enqueue(Agent a) {
        if (!waitingQueue.contains(a)) {
            waitingQueue.add(a);
        }
    }

    public void removeQueue(Agent a) {
        waitingQueue.remove(a);
    }

    @Override
    public String toString() {
        String s = "id:" + id + "\r \n"
                + "source:" + source.id + "\r \n"
                + "target:" + target.id + "\r \n"
                + "length:" + length + "\r \n"
                + "capacity:" + capacity + "\r \n"
                + "direction:" + direction + "\r \n";

        return s;
    }

    public void setState(edgeState state) {
        this.state = state;
    }
    
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Node getSource() {
        return source;
    }

    public void setSource(Node source) {
        this.source = source;
    }

    public Node getTarget() {
        return target;
    }

    public void setTarget(Node target) {
        this.target = target;
    }

    public boolean isDirection() {
        return direction;
    }

    public void setDirection(boolean direction) {
        this.direction = direction;
    }

    public double getLength() {
        return length;
    }

    public void setLength(double length) {
        this.length = length;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public edgeState getState() {
        return state;
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

    public Queue<Agent> getWaitingQueue() {
        return waitingQueue;
    }

    public void setWaitingQueue(Queue<Agent> waitingQueue) {
        this.waitingQueue = waitingQueue;
    }

}