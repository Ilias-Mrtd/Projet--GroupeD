package model.graph;

public class Edge {

    public int id;
    public Node source;
    public Node target;
    public boolean direction;
    public double length;
    public int capacity;
    public edgeState state = edgeState.AVAILABLE;

    public int currentOccupants = 0;

    public enum edgeState {
        OUT,
        AVAILABLE,
        FULL
    }

    public boolean isFull() {
        return currentOccupants >= capacity;
    }

    public boolean tryEnter() {
        if (!isFull()) {
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

    public Edge(int id, Node source, Node target, int capacity, boolean direction) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.length = Math.sqrt(Math.pow((source.x - target.x), 2) + Math.pow((source.y - target.y), 2));
        this.capacity = capacity;
        this.direction = direction;
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
}
