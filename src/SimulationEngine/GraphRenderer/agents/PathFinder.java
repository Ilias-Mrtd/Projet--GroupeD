package SimulationEngine.GraphRenderer.agents;

import SimulationEngine.GraphRenderer.graph.*;

public interface PathFinder {
    public void findPath(Graph graph, Node source, Node target);

}