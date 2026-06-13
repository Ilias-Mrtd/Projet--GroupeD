package simulationEngine.algorithm;

import java.util.ArrayList;
import java.util.List;
import model.graph.*;

public class Dijkstra extends AbstractAlgorithm {

    private static final double TRAFFIC_PENALTY = 5000.0;

    public Dijkstra(Graph graph, Node source, Node target) {
        super(graph, source, target);
    }

    /**
     * Serializes the current path node chain into a formatted string sequence.
     * @return A semicolon-separated list of node IDs.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder(getCurrentNode().getId() + ":");
        for (Node node : getPath()) {
            s.append(node.getId()).append(";");
        }
        return s.toString();
    }

    /**
     * Executes the Dijkstra path minimization graph traversal algorithm.
     * @param graph The model network topology.
     * @param source The origin entry vertex.
     * @param target The requested objective node.
     */
    @Override
    public void findPath(Graph graph, Node source, Node target) {
        int graphSize = graph.getNodes().size();
        List<Node> nearestVertice = new ArrayList<>(graphSize);
        List<Boolean> foundVertice = new ArrayList<>(graphSize);
        List<Double> pathLength = new ArrayList<>(graphSize);

        // Reused inherited nodeIndice to eliminate manual ID verification loops
        int sourceIndex = nodeIndice(graph, source, graphSize);

        for (int i = 0; i < graphSize; i++) {
            foundVertice.add(false);
            nearestVertice.add(null);
            pathLength.add(Double.POSITIVE_INFINITY);
        }

        if (sourceIndex == -1) return;

        foundVertice.set(sourceIndex, true);
        pathLength.set(sourceIndex, 0.0);

        int indiceMinimum = sourceIndex;

        for (int j = 0; j < graphSize - 1; j++) {
            if (graph.getNodes().get(indiceMinimum).getId() == target.getId() && foundVertice.get(indiceMinimum)) {
                System.out.println("Chemin trouver !");
                break;
            }

            double minimumLength = Double.POSITIVE_INFINITY;
            if (!graph.getEdges().get(sourceIndex).isEmpty()) {
                
                // Scan and relax weights for all connected edges
                for (int i = 0; i < graph.getEdges().get(sourceIndex).size(); i++) {
                    Edge edge = graph.getEdges().get(sourceIndex).get(i);
                    Node destNode = null;

                    if (!edge.hasDirection()) {
                        if (edge.getSource() == graph.getNodes().get(sourceIndex)) {
                            destNode = edge.getTarget();
                        } else {
                            continue;
                        }
                    } else {
                        destNode = destination(graph.getNodes().get(sourceIndex), edge);
                    }

                    if (destNode == null || destNode.isUnderConstruction()) continue;

                    // Consolidated unified cost assignment logic pipeline
                    double dynamicCost = (edge.getLength() / edge.getSpeedModifier())
                            + (edge.getExpectedOccupants() * TRAFFIC_PENALTY)
                            + (destNode.getExpectedOccupants() * TRAFFIC_PENALTY);

                    int destIndex = nodeIndice(graph, destNode, graphSize);

                    if (pathLength.get(sourceIndex) + dynamicCost < pathLength.get(destIndex)) {
                        nearestVertice.set(destIndex, graph.getNodes().get(sourceIndex));
                        pathLength.set(destIndex, pathLength.get(sourceIndex) + dynamicCost);
                    }
                }

                // Evaluation validation phase
                for (int i = 0; i < graphSize; i++) {
                    if (!foundVertice.get(i) && pathLength.get(i) <= minimumLength) {
                        minimumLength = pathLength.get(i);
                        indiceMinimum = i;
                    }
                }
                foundVertice.set(indiceMinimum, true);
                sourceIndex = indiceMinimum;
            } else {
                // Evaluation validation phase for dead ends
                for (int i = 0; i < graphSize; i++) {
                    if (!foundVertice.get(i) && pathLength.get(i) <= minimumLength) {
                        if (graph.getEdges().size() > sourceIndex && graph.getEdges().get(indiceMinimum).size() > i) {
                            minimumLength = graph.getEdges().get(sourceIndex).get(i).getLength();
                            indiceMinimum = i;
                        } else {
                            System.out.println("The assigned objective is not accessible for this agent.");
                            getPath().clear();
                            return;
                        }
                    }
                }
                foundVertice.set(indiceMinimum, true);
                sourceIndex = indiceMinimum;
            }
        }

        // Cleaned up trace-back path building sequence using AStar standard
        Node auxNode = target;
        while (auxNode != null && auxNode.getId() != source.getId()) {
            int u = nodeIndice(graph, auxNode, graphSize);
            if (u == -1 || nearestVertice.get(u) == null) {
                getPath().clear();
                return;
            }
            getPath().add(0, auxNode);
            auxNode = nearestVertice.get(u);
        }
    }

    public static double getTrafficPenalty() {
        return TRAFFIC_PENALTY;
    }
}