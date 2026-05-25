package simulationEngine.graphRenderer.agents;

import simulationEngine.graphRenderer.graph.*;

public interface PathFinder {
    public void findPath(Graph graph, Node source, Node target);

}