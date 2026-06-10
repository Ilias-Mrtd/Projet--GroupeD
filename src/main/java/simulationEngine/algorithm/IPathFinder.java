package simulationEngine.algorithm;

import model.graph.*;

public interface IPathFinder {

    /**
     * Calculates an optimal navigation path sequence between two given graph vertices.
     * @param graph The model network topology.
     * @param source The origin entry vertex.
     * @param target The requested objective node.
     */
    public void findPath(Graph graph, Node source, Node target);
}