package model.graph;

public class Node {

    public int id;
    public float x; // abscisse
    public float y; // ordonnee
    public int capacity;
    public nodeState state = nodeState.AVAILABLE;

    public int currentOccupants = 0;

    public boolean isFull() {
        return currentOccupants >= capacity;
    }

    public boolean tryEnter() {
        if (!isFull()) {
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

    public enum nodeState {
        OUT,
        AVAILABLE,
        FULL
    }

    public Node(int id, float x, float y, int capacity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
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
