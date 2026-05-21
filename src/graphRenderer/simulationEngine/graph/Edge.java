package graphRenderer.simulationEngine.graph;

public class Edge {

    public int id;
    public int source;
    public int target;
    public int capacity;

    public Edge(int id, int source, int target, int capacity) {
        this.id = id;
        this.source = source;
        this.target = target;
        this.capacity = capacity;
    }

}
