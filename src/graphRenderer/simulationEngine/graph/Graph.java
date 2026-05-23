package graphRenderer.simulationEngine.graph;

import java.util.List;

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

}
