package simulationEngine;

import model.graph.*;

public interface PathFinder {
    public void findPath(Graph graph, Node source, Node target);

}