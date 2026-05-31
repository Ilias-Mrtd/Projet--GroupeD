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