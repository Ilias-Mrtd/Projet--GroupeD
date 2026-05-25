import SimulationEngine.GraphRenderer.agents.Agent;
import SimulationEngine.GraphRenderer.graph.Graph;
import SimulationEngine.GraphRenderer.graph.Node;

/**
 * Classe principale pour lancer le MVP (Minimum Viable Product).
 * @author ARNOUX Antoine, ADEM Ben-Halima, PELLERIN Corentin, RIVOHERISSON Tsiky, MOURTADA Ilias
 */
public class AppProjet {
    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE DU TEST MVP - THÉMATIQUE 3 ===");

        Graph monGraphe = new Graph();
        
        monGraphe.addNode(0, 0, 5);   
        monGraphe.addNode(50, 0, 5);  
        monGraphe.addNode(100, 0, 5);

        
        Node n0 = monGraphe.Nodes.get(0);
        Node n1 = monGraphe.Nodes.get(1);
        Node n2 = monGraphe.Nodes.get(2);

        monGraphe.addEdge(n0, n1, 2, true);
        monGraphe.addEdge(n1, n2, 2, true);

        Agent monAgent = new Agent("007", 20.0f, "AVAILABLE");
        monAgent.graph = monGraphe;
        monAgent.currentNode = n0;

        System.out.println("\n--- État Initial ---");
        System.out.println("Agent ID: " + monAgent.id);
        System.out.println("État: " + monAgent.state);

        System.out.println("\n--- Calcul du trajet ---");
        monAgent.addObjective(n2); 
        monAgent.addObjective(n0);
        
        System.out.println("Nouvel état: " + monAgent.state);

        System.out.println("\n--- Simulation ---");
        System.out.println("\n--- Début de la marche ---");
        for (int i = 1; i <= 15; i++) {
        System.out.println("\n[Tour " + i + "]");
        monAgent.update();

        if (monAgent.state == Agent.agentState.AVAILABLE && i > 1) break;
    }

        System.out.println("\n=== FIN DU TEST MVP ===");
    } 
} 