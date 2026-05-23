package graphRenderer.simulationEngine.graph;

import java.util.List;
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
    public List<Node> Nodes; // Les noeuds dans un ordre quelconque
    public List<List<Edge>> Edges; // Les listes d'aretes dans le meme ordre

    private int newId() {
        Random random = new Random();
        int newId = random.nextInt(100);

        for (int i = 0; i < Nodes.size(); i++) {
            if (Nodes.get(i).id == newId) {
                newId = random.nextInt(100);
                i = 0; // recommence la boucle tant que un id unique n'a pas ete creer
            }
        }

        return newId;
    }

    public String addNode(int x, int y, int capacity) {

        // Creation du noeud avec nouvel id unique
        int newId = newId();
        Node newNode = new Node(newId, x, y, capacity);

        // Ajout du noeud a Nodes
        Nodes.add(newNode);

        return newNode.toString();
    }
}
