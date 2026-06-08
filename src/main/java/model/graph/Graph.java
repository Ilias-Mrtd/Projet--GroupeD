package model.graph;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * Represents the topological map framework managing structural graph layouts.
 * It coordinates parallel lists of spatial nodes and their associated adjacency 
 * edge matrices, providing utilities for layout mutations and calculations.
 * * @author Group D
 * @version 1.0
 * @see java.io.Serializable
 */
public class Graph implements Serializable {
    
    private List<Node> Nodes = new ArrayList<>(); // Nodes stored in arbitrary order
    private List<List<Edge>> Edges = new ArrayList<>(); // Edge adjacency lists matching the node order

    private static final long serialVersionUID = 1L;

    /**
     * Generates a unique pseudo-random identification integer for new nodes.
     * Continuously scans existing tracking collections to prevent identity collision.
     * * @return A unique numerical tracking identifier within bounds [0, 500).
     */
    private int newNodeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == newId) {
                newId = random.nextInt(500);
                i = 0; // Restarts the loop sequence until a distinct identity is verified
            }
        }

        return newId;
    }

    /**
     * Generates a unique pseudo-random identification integer for new edges.
     * Deeply scans nested adjacency arrays to guarantee absolute identifier uniqueness.
     * * @return A unique numerical tracking identifier within bounds [0, 500).
     */
    private int newEdgeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < getEdges().size(); i++) {
            for (int j = 0; j < getEdges().get(i).size(); j++) {
                if (getEdges().get(i).get(j).getId() == newId) {
                    newId = random.nextInt(500);
                    i = 0; // Restarts the nested loop sequence until a distinct identity is verified
                }
            }
        }

        return newId;
    }

    /**
     * Spawns a physical Node into the system with an automated unique identity key
     * and maps an empty list inside the parallel structural adjacency edge array.
     * * @param x Horizontal spatial coordinate mapping translation offsets.
     * @param y Vertical spatial coordinate mapping translation offsets.
     * @param capacity Density limitations threshold rule constraints for occupancy.
     */
    public void addNode(int x, int y, int capacity) {
        // Node instantiation with unique id
        int newId = newNodeId();
        Node newNode = new Node(newId, x, y, capacity);

        // Append components onto parallel matrices tracking layouts
        getNodes().add(newNode);
        List<Edge> emptyList = new ArrayList<>();
        getEdges().add(emptyList); // Map out empty tracking array space

        System.out.println("Node " + newId + " successfully added to Graph");
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
        // Edge instantiation with unique id
        int newId = newEdgeId();
        Edge newEdge = new Edge(newId, source, target, capacity, direction);

        // Assign connection footprints inside matching tracking components index maps
        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == source.getId()) {
                getEdges().get(i).add(newEdge);
            }
            if (direction && getNodes().get(i).getId() == target.getId()) {
                getEdges().get(i).add(newEdge);
            }
        }

        System.out.println("Edge " + newId + " successfully added to Graph");
    }

    /**
     * Internal removal utility scanning nested array slots to safely prune 
     * out matched structural pathway references.
     * * @param edgeToRemove The target Edge element instance to destroy.
     */
    private void removeEdge(Edge edgeToRemove) {
        for (List<Edge> edgeList : getEdges()) {
            for (int j = 0; j < edgeList.size(); j++) {
                if (edgeList.get(j).getId() == edgeToRemove.getId()) {
                    edgeList.remove(j);
                }
            }
        }
        System.out.println("Edge " + edgeToRemove.getId() + " has been successfully removed.");
    }

    /**
     * Evicts a target node configuration layer from the topology tracking array.
     * Triggers cleanups to safely flush and prune connected structural pathways.
     * * @param nodeToRemove The specific structural Node object targeted for deletion.
     * @return true if the node element matches and structural changes complete; false if missing.
     */
    public boolean removeNode(Node nodeToRemove) {
        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == nodeToRemove.getId()) {
                int length = getEdges().get(i).size();
                for (int j = 0; j < length; j++) {
                    removeEdge(getEdges().get(i).get(0));
                }
                getEdges().remove(i);
                getNodes().remove(i);
                System.out.println("Node " + nodeToRemove.getId() + " has been successfully removed.");
                return true;
            }
        }
        System.out.println("Node not found in Graph: " + this.toString());
        return false;
    }

    /**
     * Generates a formatted textual ledger matrix serialization summary representation.
     * Maps each indexed Node layout row key next to its linked connecting paths array data.
     * * @return Formatted layout print stream tracking graph connectivity mapping details.
     */
    @Override
    public String toString() {
        String s = new String();

        for (int i = 0; i < getNodes().size(); i++) {
            s += getNodes().get(i).getId() + ":";
            for (int j = 0; j < getEdges().get(i).size(); j++) {
                s += getEdges().get(i).get(j).getId() + ",";
            }
            s += "\r\n";
        }

        return s;
    }

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

    /**
     * Recalculates spatial geometric lengths across all tracked components path segments.
     * Employs standard Euclidean metrics via hypotenuse evaluation checks to sync measurements.
     */
    public void refreshEdgeLengths() {
        for (List<Edge> edgeList : getEdges()) {
            for (Edge edge : edgeList) {
                edge.setLength(Math.hypot(edge.getSource().getX() - edge.getTarget().getX(),
                        edge.getSource().getY() - edge.getTarget().getY()));
            }
        }
    }
}