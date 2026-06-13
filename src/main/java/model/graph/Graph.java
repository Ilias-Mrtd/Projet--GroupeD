package model.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the topological map framework managing structural graph layouts.
 * It holds parallel data models for spatial nodes and adjacency edge lists,
 * delegating structural mutations and computations to GraphManager.
 * * @author Group D
 */
public class Graph implements Serializable {
    
    private List<Node> Nodes = new ArrayList<>(); // Nodes stored in arbitrary order
    private List<List<Edge>> Edges = new ArrayList<>(); // Edge adjacency lists matching the node order

    private static final long serialVersionUID = 1L;

    /**
     * Spawns a physical Node into the system with an automated unique identity key
     * and maps an empty list inside the parallel structural adjacency edge array.
     * * @param x Horizontal spatial coordinate mapping translation offsets.
     * @param y Vertical spatial coordinate mapping translation offsets.
     * @param capacity Density limitations threshold rule constraints for occupancy.
     */
    public void addNode(int x, int y, int capacity) {
        GraphManager.addNode(this, x, y, capacity);
    }

    /**
     * Spawns a path connection segment and inserts its structural reference 
     * into the adjacency lists corresponding to its source and target nodes.
     * * @param source The origin source Node anchoring the pathway index.
     * @param target The destination target Node anchoring the pathway index.
     * @param capacity Traffic capacity limitations configuration parameters.
     * @param direction Orientation rules indicator flag layer.
     */
    public void addEdge(Node source, Node target, int capacity, boolean direction) {
        GraphManager.addEdge(this, source, target, capacity, direction);
    }

    /**
     * Evicts a target node configuration layer from the topology tracking array.
     * Triggers cleanups to safely flush and prune connected structural pathways.
     * * @param nodeToRemove The specific structural Node object targeted for deletion.
     * @return true if the node element matches and structural changes complete; false if missing.
     */
    public boolean removeNode(Node nodeToRemove) {
        return GraphManager.removeNode(this, nodeToRemove);
    }

    /**
     * Recalculates spatial geometric lengths across all tracked components path segments.
     * Employs standard Euclidean metrics via hypotenuse evaluation checks to sync measurements.
     */
    public void refreshEdgeLengths() {
        GraphManager.refreshEdgeLengths(this);
    }

    /**
     * Generates a formatted textual ledger matrix serialization summary representation.
     * Maps each indexed Node layout row key next to its linked connecting paths array data.
     * * @return Formatted layout print stream tracking graph connectivity mapping details.
     */
    @Override
    public String toString() {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < getNodes().size(); i++) {
            s.append(getNodes().get(i).getId()).append(":");
            for (int j = 0; j < getEdges().get(i).size(); j++) {
                s.append(getEdges().get(i).get(j).getId()).append(",");
            }
            s.append("\r\n");
        }
        return s.toString();
    }

    // ==========================================
    //           GETTERS & SETTERS
    // ==========================================
    public List<Node> getNodes() { return this.Nodes; }
    public void setNodes(List<Node> nodes) { Nodes = nodes; }
    public void resetNodes() { Nodes.clear(); }
    public void addAllNodes(List<Node> nodes) { Nodes.addAll(nodes); }

    public List<List<Edge>> getEdges() { return this.Edges; }
    public void setEdges(List<List<Edge>> edges) { Edges = edges; }
    public void resetEdges() { Edges.clear(); }
    public void addAllEdges(List<List<Edge>> edges) { Edges.addAll(edges); }

    /**
     * Completely wipes tracking layout registries, wiping all nodes and edges concurrently.
     */
    public void clear() {
        Edges.clear();
        Nodes.clear();
    }
}