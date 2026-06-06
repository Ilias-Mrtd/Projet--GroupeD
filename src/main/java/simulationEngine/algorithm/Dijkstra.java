package simulationEngine.algorithm;

import java.util.ArrayList;
import java.util.List;

import model.graph.*;

public class Dijkstra implements IPathFinder {

    private Graph graph;
    private Node currentNode;
    private Node destination;
    private List<Node> path = new ArrayList<>();

    private static final double TRAFFIC_PENALTY = 5000.0;

    @Override
    public String toString() {
        String s = getCurrentNode().getId() + ":";
        for (Node node : getPath()) {
            s += node.getId() + ";";
        }
        return s;
    }

    public Dijkstra(Graph graph, Node source, Node target) {
        this.graph = graph;
        this.currentNode = source;
        this.destination = target;
        findPath(graph, source, target);
    }

    private int nodeIndice(Graph graph, Node node, int size) {
        for (int i = 0; i < size; i++) {
            if (node.getId() == graph.getNodes().get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    private Node destination(Node node, Edge edge) {
        if (edge.getSource() == node) {
            return edge.getTarget();
        } else {
            return edge.getSource();
        }
    }

    @Override
    public void findPath(Graph graph, Node source, Node target) {
        List<Node> nearestVertice = new ArrayList<>(); // liste du noeud precedent
        List<Boolean> foundVertice = new ArrayList<>(); // liste de confirmation
        List<Double> pathLength = new ArrayList<>(); // distances a la source
        int indiceSource = 0;
        int graphSize = graph.getNodes().size();

        // initialisation
        for (int i = 0; i < graphSize; i++) {
            foundVertice.add(false);
            nearestVertice.add(null);
            pathLength.add(Double.POSITIVE_INFINITY);
            if (graph.getNodes().get(i).getId() == source.getId()) {
                foundVertice.set(i, true);
                indiceSource = i;
                pathLength.set(i, 0.0);
            }
        }

        int indiceMinimum = indiceSource;

        for (int j = 0; j < graphSize - 1; j++) {
            if (graph.getNodes().get(indiceMinimum).getId() == target.getId() && foundVertice.get(indiceMinimum)) {
                System.out.println("Chemin trouver !");
                break;
            } else {
                double minimumLength = Double.POSITIVE_INFINITY;
                if (graph.getEdges().get(indiceSource).size() > 0) {
                    // 1er phase
                    for (int i = 0; i < graph.getEdges().get(indiceSource).size(); i++) {
                        Edge edge = graph.getEdges().get(indiceSource).get(i);
                        if (edge.hasDirection()) {
                            Node destNode = destination(graph.getNodes().get(indiceSource), edge);

                            // NOUVEAU : On ignore totalement les noeuds en travaux !
                            if (destNode.isUnderConstruction()) continue;

                            // NOUVEAU : On divise la longueur par le multiplicateur de vitesse
                            double dynamicCost = (edge.getLength() / edge.getSpeedModifier())
                                    + (edge.getExpectedOccupants() * TRAFFIC_PENALTY)
                                    + (destNode.getExpectedOccupants() * TRAFFIC_PENALTY);

                            int destIndex = nodeIndice(graph, destNode, graphSize);

                            if (pathLength.get(indiceSource) + dynamicCost < pathLength.get(destIndex)) {
                                nearestVertice.set(destIndex, graph.getNodes().get(indiceSource));
                                pathLength.set(destIndex, pathLength.get(indiceSource) + dynamicCost);
                            }
                        } else {
                            if (edge.getTarget() != graph.getNodes().get(indiceSource)) {
                                Node destNode = edge.getTarget();

                                // NOUVEAU : Pareil pour le sens inverse
                                if (destNode.isUnderConstruction()) continue;

                                // NOUVEAU : Division par la vitesse
                                double dynamicCost = (edge.getLength() / edge.getSpeedModifier())
                                        + (edge.getExpectedOccupants() * TRAFFIC_PENALTY)
                                        + (destNode.getExpectedOccupants() * TRAFFIC_PENALTY);

                                int destIndex = nodeIndice(graph, destNode, graphSize);

                                if (pathLength.get(indiceSource) + dynamicCost < pathLength.get(destIndex)) {
                                    nearestVertice.set(destIndex, graph.getNodes().get(indiceSource));
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
                            if (graph.getEdges().size() > indiceSource && graph.getEdges().get(indiceMinimum).size() > i) {
                                minimumLength = graph.getEdges().get(indiceSource).get(i).getLength();
                                indiceMinimum = i;
                            } else {
                                // Case where the path doesn't exist ಥ_ಥ
                                System.out.println("The assigned objective is not accessible for this agent.");
                                // Il serait interessant de reassigner l'objectif a un autre agent
                                getPath().clear();
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
                if (auxNode == null) {
                    getPath().clear();
                    break;
                } else {
                    if (auxNode.getId() == source.getId()) {
                        break;
                    } else {
                        if (graph.getNodes().get(u).getId() == auxNode.getId()) {
                            getPath().add(0, graph.getNodes().get(u));
                            auxNode = nearestVertice.get(u);
                        }
                    }
                }
                if (auxNode == null) {
                    getPath().clear();
                    break;
                } else {
                    if (auxNode.getId() == source.getId()) {
                        break;
                    }
                }
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

    public Graph getGraph() { return graph; }
    public void setGraph(Graph graph) { this.graph = graph; }

    public Node getCurrentNode() { return currentNode; }
    public void setCurrentNode(Node currentNode) { this.currentNode = currentNode; }

    public Node getDestination() { return destination; }
    public void setDestination(Node destination) { this.destination = destination; }

    public List<Node> getPath() { return path; }
    public void setPath(List<Node> path) { this.path = path; }

    public static double getTrafficPenalty() { return TRAFFIC_PENALTY; }

}