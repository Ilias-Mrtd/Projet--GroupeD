package simulationEngine.algorithm;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;

public abstract class Algo implements IPathFinder {

    private Graph graph;
    private Node currentNode;
    private Node destination;
    private List<Node> path = new ArrayList<>();

    public Algo(Graph graph, Node source, Node target) {
        this.graph = graph;
        this.currentNode = source;
        this.destination = target;
        findPath(graph, source, target);
    }

    public int nodeIndice(Graph graph, Node node, int size) {
        for (int i = 0; i < size; i++) {
            if (node.getId() == graph.getNodes().get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    public Node destination(Node node, Edge edge) {
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else {
            return edge.getSource();
        }
    }

    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public Node getCurrentNode() {
        return currentNode;
    }

    public void setCurrentNode(Node currentNode) {
        this.currentNode = currentNode;
    }

    public Node getDestination() {
        return destination;
    }

    public void setDestination(Node destination) {
        this.destination = destination;
    }

    public List<Node> getPath() {
        return path;
    }

    public void setPath(List<Node> path) {
        this.path = path;
    }
}
