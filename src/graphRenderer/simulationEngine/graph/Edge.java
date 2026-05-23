package graphRenderer.simulationEngine.graph;

public class Edge {

    public int id;
    public Node source;
    public Node target;
    public boolean direction;
    public double length;
    public int capacity;

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

}
