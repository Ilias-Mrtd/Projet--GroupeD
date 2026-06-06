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

    public void addNode(int x, int y, int capacity) {

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
