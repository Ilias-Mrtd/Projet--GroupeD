package model.graph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Multi-threaded operational service managing mutations, sequential identifier assignments,
 * adjacency topology configurations, and geometric metrics calculations for the network Graph.
 * * @author Group D
 * @since 2026
 */
public class GraphManager implements Serializable {
    private static final long serialVersionUID = 1L;

    public GraphManager() {}

    /**
     * Generates a unique pseudo-random identification integer for new nodes.
     * Continuously scans existing tracking collections to prevent identity collision.
     * * @param graph The context Graph object to reference.
     * @return A unique numerical tracking identifier within bounds [0, 500).
     */
    private static int newNodeId(Graph graph) {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < graph.getNodes().size(); i++) {
            if (graph.getNodes().get(i).getId() == newId) {
                newId = random.nextInt(500);
                i = -1; // Restarts the loop sequence until a distinct identity is verified
            }
        }
        return newId;
    }

    /**
     * Generates a unique pseudo-random identification integer for new edges.
     * Deeply scans nested adjacency arrays to guarantee absolute identifier uniqueness.
     * * @param graph The context Graph object to reference.
     * @return A unique numerical tracking identifier within bounds [0, 500).
     */
    private static int newEdgeId(Graph graph) {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < graph.getEdges().size(); i++) {
            for (int j = 0; j < graph.getEdges().get(i).size(); j++) {
                if (graph.getEdges().get(i).get(j).getId() == newId) {
                    newId = random.nextInt(500);
                    i = -1; // Restarts the nested loop sequence until a distinct identity is verified
                    break;
                }
            }
        }
        return newId;
    }

    /**
     * Spawns a physical Node into the system with an automated unique identity key
     * and maps an empty list inside the parallel structural adjacency edge array.
     * * @param graph The context Graph to execute modifications upon.
     * @param x Horizontal spatial coordinate mapping translation offsets.
     * @param y Vertical spatial coordinate mapping translation offsets.
     * @param capacity Density limitations threshold rule constraints for occupancy.
     */
    public static void addNode(Graph graph, int x, int y, int capacity) {
        int newId = newNodeId(graph);
        Node newNode = new Node(newId, x, y, capacity);

        graph.getNodes().add(newNode);
        List<Edge> emptyList = new ArrayList<>();
        graph.getEdges().add(emptyList);

        System.out.println("Node " + newId + " successfully added to Graph");
    }

    /**
     * Spawns a path connection segment and inserts its structural reference 
     * into the adjacency lists corresponding to its source and target nodes.
     * * @param graph The context Graph to execute modifications upon.
     * @param source The origin source Node anchoring the pathway index.
     * @param target The destination target Node anchoring the pathway index.
     * @param capacity Traffic capacity limitations configuration parameters.
     * @param direction Orientation rules indicator flag layer.
     */
    public static void addEdge(Graph graph, Node source, Node target, int capacity, boolean direction) {
        int newId = newEdgeId(graph);
        Edge newEdge = new Edge(newId, source, target, capacity, direction);

        for (int i = 0; i < graph.getNodes().size(); i++) {
            if (graph.getNodes().get(i).getId() == source.getId()) {
                graph.getEdges().get(i).add(newEdge);
            }
            if (direction && graph.getNodes().get(i).getId() == target.getId()) {
                graph.getEdges().get(i).add(newEdge);
            }
        }
        System.out.println("Edge " + newId + " successfully added to Graph");
    }

    /**
     * Internal removal utility scanning nested array slots to safely prune 
     * out matched structural pathway references.
     * * @param graph The context Graph to update.
     * @param edgeToRemove The target Edge element instance to destroy.
     */
    private static void removeEdge(Graph graph, Edge edgeToRemove) {
        for (List<Edge> edgeList : graph.getEdges()) {
            for (int j = 0; j < edgeList.size(); j++) {
                if (edgeList.get(j).getId() == edgeToRemove.getId()) {
                    edgeList.remove(j);
                    j--; // Adjusts counter index after internal element removal
                }
            }
        }
        System.out.println("Edge " + edgeToRemove.getId() + " has been successfully removed.");
    }

    /**
     * Evicts a target node configuration layer from the topology tracking array.
     * Triggers cleanups to safely flush and prune connected structural pathways.
     * * @param graph The context Graph to execute modifications upon.
     * @param nodeToRemove The specific structural Node object targeted for deletion.
     * @return true if the node element matches and structural changes complete; false if missing.
     */
    public static boolean removeNode(Graph graph, Node nodeToRemove) {
        for (int i = 0; i < graph.getNodes().size(); i++) {
            if (graph.getNodes().get(i).getId() == nodeToRemove.getId()) {
                int length = graph.getEdges().get(i).size();
                for (int j = 0; j < length; j++) {
                    removeEdge(graph, graph.getEdges().get(i).get(0));
                }
                graph.getEdges().remove(i);
                graph.getNodes().remove(i);
                System.out.println("Node " + nodeToRemove.getId() + " has been successfully removed.");
                return true;
            }
        }
        System.out.println("Node not found in Graph: " + graph.toString());
        return false;
    }

    /**
     * Recalculates spatial geometric lengths across all tracked components path segments.
     * Employs standard Euclidean metrics via hypotenuse evaluation checks to sync measurements.
     * * @param graph The context Graph to process.
     */
    public static void refreshEdgeLengths(Graph graph) {
        for (List<Edge> edgeList : graph.getEdges()) {
            for (Edge edge : edgeList) {
                edge.setLength(Math.hypot(edge.getSource().getX() - edge.getTarget().getX(),
                        edge.getSource().getY() - edge.getTarget().getY()));
            }
        }
    }
}