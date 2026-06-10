package model.graph;

import java.util.List;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

public class Graph implements Serializable {
    // Le graph ex:
    // Liste de Noeuds:Liste d'aretes
    // 3: [2,4,5]
    // 6: [5]
    // 4: [3,7]
    // 7: []
    // 1: [1]
    // 2: [1,2,3]
    private List<Node> Nodes = new ArrayList<>(); // Les noeuds dans un ordre quelconque
    private List<List<Edge>> Edges = new ArrayList<>(); // Les listes d'aretes dans le meme ordre

    private static final long serialVersionUID = 1L;

    private int newNodeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == newId) {
                newId = random.nextInt(500);
                i = 0; // recommence la boucle tant que un id unique n'a pas ete creer
            }
        }

        return newId;
    }

    private int newEdgeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < getEdges().size(); i++) {
            for (int j = 0; j < getEdges().get(i).size(); j++) {
                if (getEdges().get(i).get(j).getId() == newId) {
                    newId = random.nextInt(500);
                    i = 0; // recommence la boucle tant que un id unique n'a pas ete creer
                }
            }
        }

        return newId;
    }

    private static final double EDGE_CLEARANCE         = 6.0;

    public boolean isNodePositionAvailable(double x, double y, double radius, Node ignoredNode) {
        for (Node node : getNodes()) {
            if (node == ignoredNode) continue;
            double dx = node.getX() - x;
            double dy = node.getY() - y;
            if (Math.hypot(dx, dy) < (radius + Node.RADIUS + Node.COLLISION_CLEARANCE)) {
                return false;
            }
        }

        for (List<Edge> edgeList : getEdges()) {
            for (Edge edge : edgeList) {
                if (isPointNearEdge(x, y, edge, radius + EDGE_CLEARANCE)) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean canAddEdge(Node source, Node target, boolean direction) {
        if (source == null || target == null || source.getId() == target.getId()) {
            return false;
        }

        for (List<Edge> edgeList : getEdges()) {
            for (Edge existing : edgeList) {
                if (sameConnection(existing, source, target)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean sameConnection(Edge edge, Node source, Node target) {
        boolean directMatch = edge.getSource().getId() == source.getId() && edge.getTarget().getId() == target.getId();
        boolean reverseMatch = edge.getSource().getId() == target.getId() && edge.getTarget().getId() == source.getId();
        return directMatch || reverseMatch;
    }

    private boolean isPointNearEdge(double x, double y, Edge edge, double minDistance) {
        double x1 = edge.getSource().getX();
        double y1 = edge.getSource().getY();
        double x2 = edge.getTarget().getX();
        double y2 = edge.getTarget().getY();

        double dx = x2 - x1;
        double dy = y2 - y1;
        double segmentSquared = Math.pow(dx, 2) + Math.pow(dy, 2);
        if (segmentSquared == 0) {
            return Math.hypot(x - x1, y - y1) <= minDistance;
        }

        double t = ((x - x1) * dx + (y - y1) * dy) / segmentSquared;
        t = Math.max(0, Math.min(1, t));

        double projectionX = x1 + t * dx;
        double projectionY = y1 + t * dy;
        return Math.hypot(x - projectionX, y - projectionY) <= minDistance;
    }

    public void addNode(int x, int y, int capacity) {
        if (!isNodePositionAvailable(x, y, Node.RADIUS, null)) {
            System.out.println("Node placement rejected: position overlaps existing node or edge.");
            return;
        }

        // Creation du noeud avec nouvel id unique
        int newId = newNodeId();
        Node newNode = new Node(newId, x, y, capacity);

        // Ajout du noeud a Nodes
        getNodes().add(newNode);
        List<Edge> emptyList = new ArrayList<>();
        getEdges().add(emptyList); // Ajout d'une liste d'aretes vide

        System.out.println("Node " + newId + " succesfuly added to Graph");
    }

    public void addEdge(Node source, Node target, int capacity, boolean direction) {
        if (!canAddEdge(source, target, direction)) {
            System.out.println("Edge creation rejected: same connection already exists or invalid endpoint.");
            return;
        }

        // Creation de l'arete avec nouvel id unique
        int newId = newEdgeId();
        Edge newEdge = new Edge(newId, source, target, capacity, direction);

        // Ajout de l'arete a Edges
        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == source.getId()) {
                getEdges().get(i).add(newEdge);// exception
            }
            if (direction && getNodes().get(i).getId() == target.getId()) {
                getEdges().get(i).add(newEdge);// exception
            }
        }

        System.out.println("Edge " + newId + " succesfuly added to Graph");
    }

    private void removeEdge(Edge edgeToRemove) {
        for (List<Edge> edgeList : getEdges()) {
            for (int j = 0; j < edgeList.size(); j++) {
                if (edgeList.get(j).getId() == edgeToRemove.getId()) {
                    edgeList.remove(j);
                }
            }
        }
        System.out.println("Edge " + edgeToRemove.getId() + " has been succesfuly removed.");
    }

    public boolean removeNode(Node nodeToRemove) {
        for (int i = 0; i < getNodes().size(); i++) {
            if (getNodes().get(i).getId() == nodeToRemove.getId()) {
                int length = getEdges().get(i).size();
                for (int j = 0; j < length; j++) {
                    removeEdge(getEdges().get(i).get(0));
                }
                getEdges().remove(i);
                getNodes().remove(i);
                System.out.println("Node" + nodeToRemove.getId() + " has succesfully removed.");
                return true;
            }
        }
        System.out.println("Node not found in Graph:" + this.toString());
        return false;
    }

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

    public List<Node> getNodes() {
        return this.Nodes;
    }

    public void setNodes(List<Node> nodes) {
        Nodes = nodes;
    }

    public void resetNodes() {
        Nodes.clear();
    }

    public void addAllNodes(List<Node> nodes) {
        Nodes.addAll(nodes);
    }

    public List<List<Edge>> getEdges() {
        return this.Edges;
    }

    public void setEdges(List<List<Edge>> edges) {
        Edges = edges;
    }

    public void resetEdges() {
        Edges.clear();
    }

    public void addAllEdges(List<List<Edge>> edges) {
        Edges.addAll(edges);
    }

    public void clear() {
        Edges.clear();
        Nodes.clear();
    }

    public void refreshEdgeLengths() {
        for (List<Edge> edgeList : getEdges()) {
            for (Edge edge : edgeList) {
                edge.setLength(Math.hypot(edge.getSource().getX() - edge.getTarget().getX(),
                        edge.getSource().getY() - edge.getTarget().getY()));
            }
        }
    }
}
