package model.agents;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;

public class Dijkstra implements PathFinder {

    public Graph graph;
    public Node currentNode;
    public Node Destination;
    public List<Node> path = new ArrayList<>();

    private static final double TRAFFIC_PENALTY = 5000.0;

    @Override
    public String toString() {
        String s = currentNode.id + ":";
        for (Node node : path) {
            s += node.id + ";";
        }
        return s;
    }

    public Dijkstra(Graph graph, Node source, Node target) {
        this.graph = graph;
        this.currentNode = source;
        this.Destination = target;
        findPath(graph, source, target);
    }

    private int nodeIndice(Graph graph, Node node, int size) {
        for (int i = 0; i < size; i++) {
            if (node.id == graph.Nodes.get(i).id) {
                return i;
            }
        }
        return -1;
    }

    private Node destination(Node node, Edge edge) {
        if (edge.source == node) {
            return edge.target;
        } else {
            return edge.source;
        }
    }

    @Override
    public void findPath(Graph graph, Node source, Node target) {
        List<Node> nearestVertice = new ArrayList<>(); // liste du noeud precedent
        List<Boolean> foundVertice = new ArrayList<>(); // liste de confirmation
        List<Double> pathLength = new ArrayList<>(); // distances a la source
        int indiceSource = 0;
        int graphSize = graph.Nodes.size();

        // initialisation
        for (int i = 0; i < graphSize; i++) {
            foundVertice.add(false);
            nearestVertice.add(null);
            pathLength.add(Double.POSITIVE_INFINITY);
            if (graph.Nodes.get(i).id == source.id) {
                foundVertice.set(i, true);
                indiceSource = i;
                pathLength.set(i, 0.0);
            }
        }

        int indiceMinimum = indiceSource;

        for (int j = 0; j < graphSize - 1; j++) {
            if (graph.Nodes.get(indiceMinimum).id == target.id && foundVertice.get(indiceMinimum)) {
                System.out.println("Chemin trouver !");
                break;
            } else {
                double minimumLength = Double.POSITIVE_INFINITY;
                if (graph.Edges.get(indiceSource).size() > 0) {
                    // 1er phase
                    for (int i = 0; i < graph.Edges.get(indiceSource).size(); i++) {
                        Edge edge = graph.Edges.get(indiceSource).get(i);
                        if (edge.direction) {
                            Node destNode = destination(graph.Nodes.get(indiceSource), edge);

                            double dynamicCost = edge.length
                                    + (edge.expectedOccupants * TRAFFIC_PENALTY)
                                    + (destNode.expectedOccupants * TRAFFIC_PENALTY);

                            int destIndex = nodeIndice(graph, destNode, graphSize);

                            if (pathLength.get(indiceSource) + dynamicCost < pathLength.get(destIndex)) {
                                nearestVertice.set(destIndex, graph.Nodes.get(indiceSource));
                                pathLength.set(destIndex, pathLength.get(indiceSource) + dynamicCost);
                            }
                        } else {
                            if (edge.target != graph.Nodes.get(indiceSource)) {
                                Node destNode = edge.target;

                                double dynamicCost = edge.length
                                        + (edge.expectedOccupants * TRAFFIC_PENALTY)
                                        + (destNode.expectedOccupants * TRAFFIC_PENALTY);

                                int destIndex = nodeIndice(graph, destNode, graphSize);

                                if (pathLength.get(indiceSource) + dynamicCost < pathLength.get(destIndex)) {
                                    nearestVertice.set(destIndex, graph.Nodes.get(indiceSource));
                                    pathLength.set(destIndex, pathLength.get(indiceSource) + dynamicCost);
                                }
                            }
                        }
                    }

                    // phase de validation
                    for (int i = 0; i < graphSize; i++) {
                        if (pathLength.get(i) <= minimumLength && foundVertice.get(i) == false) {
                            minimumLength = pathLength.get(i);
                            indiceMinimum = i;
                        }
                    }
                    foundVertice.set(indiceMinimum, true);
                    indiceSource = indiceMinimum;
                } else {
                    // phase de validation
                    for (int i = 0; i < graphSize; i++) {
                        if (pathLength.get(i) <= minimumLength && foundVertice.get(i) == false) {
                            System.out.println("ICI1!");
                            if (graph.Edges.size() > indiceSource && graph.Edges.get(indiceMinimum).size() > i) {
                                minimumLength = graph.Edges.get(indiceSource).get(i).length;
                                indiceMinimum = i;
                            } else {
                                // Case where the path doesn't exist ಥ_ಥ
                                System.out.println("The assigned objective is not accessible for this agent.");
                                // Il serait interessant de reassigner l'objectif a un autre agent
                                path.clear();
                                return;
                            }
                        }
                        foundVertice.set(indiceMinimum, true);
                        indiceSource = indiceMinimum;
                    }
                }
            }
        }
        // creation de la liste de noeuds
        Node auxNode = target;
        for (int v = 0; v < graphSize; v++) {
            for (int u = 0; u < graphSize; u++) {
                if (auxNode.id == source.id) {
                    break;
                } else {
                    if (graph.Nodes.get(u).id == auxNode.id) {
                        path.add(0, graph.Nodes.get(u));
                        auxNode = nearestVertice.get(u);
                    }
                }
            }
            if (auxNode.id == source.id) {
                break;
            }
        }
        /**
         * Affiche le tableau de Dijkstra
         * for (Node node : graph.Nodes) {
         * System.out.print(node.id + " ");
         * }
         * System.out.println();
         * for (Node node : nearestVertice) {
         * if (node == null) {
         * System.out.print("null ");
         * } else {
         * System.out.print(node.id + " ");
         * }
         * }
         * System.out.println();
         * for (double d : pathLength) {
         * System.out.print(d + " ");
         * }
         * System.out.println();
         */
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
        return Destination;
    }

    public void setDestination(Node destination) {
        Destination = destination;
    }

    public List<Node> getPath() {
        return path;
    }

    public void setPath(List<Node> path) {
        this.path = path;
    }

    public static double getTrafficPenalty() {
        return TRAFFIC_PENALTY;
    }

}