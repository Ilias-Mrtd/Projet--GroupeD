package simulationEngine.algorithm;

import java.util.ArrayList;
import java.util.List;
import model.graph.*;

public class AStar extends Algo {

    private static final double TRAFFIC_PENALTY = 5000.0;

    public AStar(Graph graph, Node source, Node target) {
        super(graph, source, target);
    }

    private double heuristic(Node a, Node b) {
        return Math.sqrt(Math.pow(a.getX() - b.getX(), 2) + Math.pow(a.getY() - b.getY(), 2));
    }

    @Override
    public void findPath(Graph graph, Node source, Node target) {
        int graphSize = graph.getNodes().size();
        List<Node> nearestVertice = new ArrayList<>(graphSize);
        List<Boolean> closedSet = new ArrayList<>(graphSize);
        List<Double> gScore = new ArrayList<>(graphSize); 
        List<Double> fScore = new ArrayList<>(graphSize); 

        int sourceIndex = -1;
        int targetIndex = -1;

        for (int i = 0; i < graphSize; i++) {
            closedSet.add(false);
            nearestVertice.add(null);
            gScore.add(Double.POSITIVE_INFINITY);
            fScore.add(Double.POSITIVE_INFINITY);
            
            Node n = graph.getNodes().get(i);
            if (n.getId() == source.getId()) sourceIndex = i;
            if (n.getId() == target.getId()) targetIndex = i;
        }

        if (sourceIndex == -1 || targetIndex == -1) return;

        gScore.set(sourceIndex, 0.0);
        fScore.set(sourceIndex, heuristic(source, target));

        for (int j = 0; j < graphSize; j++) {
            
            double minF = Double.POSITIVE_INFINITY;
            int current = -1;
            for (int i = 0; i < graphSize; i++) {
                if (!closedSet.get(i) && fScore.get(i) < minF) {
                    minF = fScore.get(i);
                    current = i;
                }
            }

            if (current == -1) break;
            if (graph.getNodes().get(current).getId() == target.getId()) break;

            closedSet.set(current, true);
            Node currentNode = graph.getNodes().get(current);

            for (Edge edge : graph.getEdges().get(current)) {
                Node neighbor = null;
                
                // CORRECTION : true = Bidirectionnel, false = Sens unique !
                if (!edge.hasDirection()) {
                    if (edge.getSource() == currentNode) {
                        neighbor = edge.getTarget();
                    } else {
                        continue; // Sens interdit !
                    }
                } else {
                    neighbor = (edge.getSource() == currentNode) ? edge.getTarget() : edge.getSource();
                }

                if (neighbor == null || neighbor.isUnderConstruction()) continue;

                int neighborIndex = nodeIndice(graph, neighbor, graphSize);
                if (closedSet.get(neighborIndex)) continue;

                double dynamicCost = (edge.getLength() / edge.getSpeedModifier())
                        + (edge.getExpectedOccupants() * TRAFFIC_PENALTY)
                        + (neighbor.getExpectedOccupants() * TRAFFIC_PENALTY);

                double tentativeGScore = gScore.get(current) + dynamicCost;

                if (tentativeGScore < gScore.get(neighborIndex)) {
                    nearestVertice.set(neighborIndex, currentNode);
                    gScore.set(neighborIndex, tentativeGScore);
                    fScore.set(neighborIndex, tentativeGScore + heuristic(neighbor, target));
                }
            }
        }

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
}