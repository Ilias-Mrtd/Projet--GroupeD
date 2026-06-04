package model.graph;

import java.util.LinkedList;
import java.util.Queue;

import model.agents.Agent;

public class Node {

    public int id;
    public float x;
    public float y;
    public int capacity;
    public nodeState state = nodeState.AVAILABLE;
    public boolean isSelected = false;
    public int currentOccupants = 0;
    public int expectedOccupants = 0;
    public int incomingOccupants = 0;

    public Queue<Agent> waitingQueue = new LinkedList<>();

    public Node(int id, float x, float y, int capacity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
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
        return this.expectedOccupants;
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
                this.state = nodeState.FULL;
            }
            return true;
        }
        return false;
    }

    public void forceEnter() {
        currentOccupants++;
        if (isFull()) {
            this.state = nodeState.FULL;
        }
    }

    public void leave() {
        if (currentOccupants > 0) {
            currentOccupants--;
        }
        if (!isFull()) {
            this.state = nodeState.AVAILABLE;
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

    public enum nodeState {
        OUT,
        AVAILABLE,
        FULL
    }

    @Override
    public String toString() {
        String s = "id:" + id + "\r \n"
                + "X:" + x + "\r \n"
                + "Y:" + y + "\r \n"
                + "capacity:" + capacity + "\r \n"
                + "state:" + state + "\r\n";

        return s;
    }
}