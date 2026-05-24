package graphRenderer.simulationEngine.agents;

import graphRenderer.simulationEngine.graph.*;

public interface PathFinder {
    public void findPath(Graph graph, Node source, Node target);

}