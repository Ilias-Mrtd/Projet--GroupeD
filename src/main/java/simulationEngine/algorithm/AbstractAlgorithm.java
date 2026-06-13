package simulationEngine.algorithm;

import java.util.ArrayList;
import java.util.List;
import model.graph.*;

public abstract class AbstractAlgorithm implements IPathFinder {

    private Graph graph;
    private Node currentNode;
    private Node destination;
    private List<Node> path = new ArrayList<>();

    public AbstractAlgorithm(Graph graph, Node source, Node target) {
        this.graph = graph;
        this.currentNode = source;
        this.destination = target;
        findPath(graph, source, target);
    }

    /**
     * Finds the index of a specific node within the graph's primary tracking collection.
     * @param graph The context graph instance.
     * @param node The target node to locate.
     * @param size The upper boundary index range limit for the scan loop.
     * @return The integer index tracking slot location; -1 if missing.
     */
    public int nodeIndice(Graph graph, Node node, int size) {
        for (int i = 0; i < size; i++) {
            if (node.getId() == graph.getNodes().get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Resolves the opposite target neighbor node connected by a specific boundary edge.
     * @param node The perspective origin node framework.
     * @param edge The connecting bridge element reference.
     * @return The opposite node linked on the other side of the edge.
     */
    public Node destination(Node node, Edge edge) {
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else {
            return edge.getSource();
        }
    }

    public Graph getGraph() { return graph; }
    public void setGraph(Graph graph) { this.graph = graph; }
    public Node getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }
    public Node getDestination() { return destination; }
    public void setDestination(Node destination) { this.destination = destination; }
    public List<Node> getPath() { return path; }
    public void setPath(List<Node> path) { this.path = path; }
}