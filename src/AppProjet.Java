import SimulationEngine.GraphRenderer.agents.Agent;
import SimulationEngine.GraphRenderer.graph.Graph;
import SimulationEngine.GraphRenderer.graph.Node;
import SimulationEngine.SimulationEngine;

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

        SimulationEngine engine = new SimulationEngine(monGraphe);

        Agent monAgent = new Agent("007", 25.0f, "AVAILABLE");
        monAgent.currentNode = n0;
        engine.addAgent(monAgent);

        System.out.println("\n--- État Initial ---");
        System.out.println("Agent ID: " + monAgent.id);
        System.out.println("État: " + monAgent.state);

        System.out.println("\n--- Assignation des missions ---");
        monAgent.addObjective(n2); 
        monAgent.addObjective(n0);

        System.out.println("\n--- Début des Ticks de Simulation ---");
        


        for (int currentTick = 1; currentTick <= 15; currentTick++) {
            System.out.println("\n[ TICK " + currentTick + " ]");
            
            engine.tick(); 
            
            if (monAgent.state == Agent.agentState.AVAILABLE && currentTick > 1) {
                System.out.println("Simulation terminée, agents au repos.");
                break;
            }
        }

        System.out.println("\n=== FIN DU TEST MVP ===");
    } 
} 