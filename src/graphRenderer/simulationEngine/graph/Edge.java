package graphRenderer.simulationEngine.graph;

public class Edge {

    public int id;
    public int source;
    public int target;
    public boolean direction;
    public double length;
    public int capacity;

    public Edge(int id, Node source, Node target, int capacity) {
        this.id = id;
        this.source = source.id;
        this.target = target.id;
        this.length = Math.sqrt(Math.pow((source.x - target.x), 2) + Math.pow((source.x - target.x), 2));
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        String s = "id:" + id + "\r \n"
                + "source:" + source + "\r \n"
                + "target:" + target + "\r \n"
                + "length:" + length + "\r \n"
                + "capacity:" + capacity + "\r \n"
                + "direction:" + direction;

        return s;
    }

}
