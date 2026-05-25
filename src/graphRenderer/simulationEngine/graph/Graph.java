package graphRenderer.simulationEngine.graph;

import java.util.List;
import java.util.ArrayList;
import java.util.Random;

public class Graph {
    // Le graph ex:
    // Liste de Noeuds:Liste d'aretes
    // 3: [2,4,5]
    // 6: [5]
    // 4: [3,7]
    // 7: []
    // 1: [1]
    // 2: [1,2,3]
    public List<Node> Nodes = new ArrayList<>(); // Les noeuds dans un ordre quelconque
    public List<List<Edge>> Edges = new ArrayList<>(); // Les listes d'aretes dans le meme ordre

    private int newNodeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < Nodes.size(); i++) {
            if (Nodes.get(i).id == newId) {
                newId = random.nextInt(500);
                i = 0; // recommence la boucle tant que un id unique n'a pas ete creer
            }
        }

        return newId;
    }

    private int newEdgeId() {
        Random random = new Random();
        int newId = random.nextInt(500);

        for (int i = 0; i < Edges.size(); i++) {
            for (int j = 0; j < Edges.get(i).size(); j++) {
                if (Edges.get(i).get(j).id == newId) {
                    newId = random.nextInt(500);
                    i = 0; // recommence la boucle tant que un id unique n'a pas ete creer
                }
            }
        }

        return newId;
    }

    public String addNode(int x, int y, int capacity) {

        // Creation du noeud avec nouvel id unique
        int newId = newNodeId();
        Node newNode = new Node(newId, x, y, capacity);

        // Ajout du noeud a Nodes
        Nodes.add(newNode);
        Edges.add(new ArrayList<>()); // Ajout d'une liste d'aretes vide

        return newNode.toString();
    }

    public String addEdge(Node source, Node target, int capacity, boolean direction) {
        // Creation de l'arete avec nouvel id unique
        int newId = newEdgeId();
        Edge newEdge = new Edge(newId, source, target, capacity, direction);

        // Ajout de l'arete a Edges
        for (int i = 0; i < Nodes.size(); i++) {
            if (Nodes.get(i).id == source.id) {
                Edges.get(i).add(newEdge);// exception
            }
            if (direction && Nodes.get(i).id == target.id) {
                Edges.get(i).add(newEdge);// exception
            }
        }

        return newEdge.toString();
    }

    private void removeEdge(Edge edgeToRemove) {
        for (List<Edge> edgeList : Edges) {
            for (int j = 0; j < edgeList.size(); j++) {
                if (edgeList.get(j).id == edgeToRemove.id) {
                    edgeList.remove(j);
                }
            }
        }
        System.out.println("Edge " + edgeToRemove.id + " as been removed.");
    }

    public boolean removeNode(Node nodeToRemove) {
        for (int i = 0; i < Nodes.size(); i++) {
            if (Nodes.get(i).id == nodeToRemove.id) {
                int length = Edges.get(i).size();
                for (int j = 0; j < length; j++) {
                    removeEdge(Edges.get(i).get(0));
                }
                Edges.remove(i);
                Nodes.remove(i);
                System.out.println("Node succesfully removed.");
                return true;
            }
        }
        System.out.println("Node not found.");
        return false;
    }

    @Override
    public String toString() {
        String s = new String();

        for (int i = 0; i < Nodes.size(); i++) {
            s += Nodes.get(i).id + ":";
            for (int j = 0; j < Edges.get(i).size(); j++) {
                s += Edges.get(i).get(j).id + ",";
            }
            s += "\r\n";
        }

        return s;
    }
}
