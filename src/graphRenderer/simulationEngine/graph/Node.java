package graphRenderer.simulationEngine.graph;

public class Node {

    public int id;
    public int x; // abscisse
    public int y; // ordonnee
    public int capacity;
    public nodeState state = nodeState.AVAILIABLE;

    public enum nodeState {
        OUT,
        AVAILIABLE,
        FULL
    }

    public Node(int id, int x, int y, int capacity) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.capacity = capacity;
    }

}
