package graphRenderer.simulationEngine.agents;

import graphRenderer.simulationEngine.graph.*;
import java.util.List;

public interface PathFinder {
    public List<Edge> findPath(Graph graph, Node source, Node target);

}