package simulationEngine.algorithm;

import model.graph.*;

public interface IPathFinder {
    public void findPath(Graph graph, Node source, Node target);
}