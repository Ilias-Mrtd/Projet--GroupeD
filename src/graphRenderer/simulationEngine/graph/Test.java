package graphRenderer.simulationEngine.graph;

public class Test {
    public static void main(String args[]) {

        Graph testGraph = new Graph();

        testGraph.addNode(0, 0, 2);
        testGraph.addNode(0, 10, 2);
        testGraph.addNode(10, 0, 2);

        testGraph.addEdge(testGraph.Nodes.get(0), testGraph.Nodes.get(1), 2, true);
        testGraph.addEdge(testGraph.Nodes.get(1), testGraph.Nodes.get(2), 2, true);

        System.out.print(testGraph);

        testGraph.removeNode(testGraph.Nodes.get(1));

        System.out.print(testGraph);
    }
}