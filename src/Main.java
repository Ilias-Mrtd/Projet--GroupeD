import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import simulationEngine.SimulationEngine;
import model.agents.Agent;
import model.graph.*;

import java.util.Random;

/**
 * Classe principale pour lancer le MVP (Minimum Viable Product).
 * 
 * @author ARNOUX Antoine, ADEM Ben-Halima, PELLERIN Corentin,
 *         RIVOHERISSON Tsiky, MOURTADA Ilias
 */
public class Main {

    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE DU TEST MVP - THÉMATIQUE 3 ===");

        // ── 1. Construction du graphe ─────────────────────────────────────
        Graph monGraphe = new Graph();

        monGraphe.addNode(150, 300, 5); // n0 — gauche
        monGraphe.addNode(400, 150, 5); // n1 — centre-haut
        monGraphe.addNode(650, 300, 5); // n2 — droite
        monGraphe.addNode(50, 50, 5); // n3
        monGraphe.addNode(200, 150, 5); // n4
        monGraphe.addNode(650, 100, 5); // n5
        monGraphe.addNode(500, 400, 5); // n6
        monGraphe.addNode(400, 50, 5); // n7
        monGraphe.addNode(350, 300, 5); // n8
        monGraphe.addNode(50, 400, 5); // n9
        monGraphe.addNode(200, 500, 5); // n10
        monGraphe.addNode(650, 500, 5); // n11

        Node n0 = monGraphe.Nodes.get(0);
        Node n1 = monGraphe.Nodes.get(1);
        Node n2 = monGraphe.Nodes.get(2);
        Node n3 = monGraphe.Nodes.get(3);
        Node n4 = monGraphe.Nodes.get(4);
        Node n5 = monGraphe.Nodes.get(5);
        Node n6 = monGraphe.Nodes.get(6);
        Node n7 = monGraphe.Nodes.get(7);
        Node n8 = monGraphe.Nodes.get(8);
        Node n9 = monGraphe.Nodes.get(9);
        Node n10 = monGraphe.Nodes.get(10);
        Node n11 = monGraphe.Nodes.get(11);

        monGraphe.addEdge(n0, n1, 1, true); // Jai remplace les  capacite pour les testes de edge capacity
        monGraphe.addEdge(n1, n2, 1, true);
        monGraphe.addEdge(n2, n11, 1, true);
        monGraphe.addEdge(n1, n5, 1, true);
        monGraphe.addEdge(n0, n4, 1, true);
        monGraphe.addEdge(n5, n7, 1, true);
        monGraphe.addEdge(n7, n3, 1, true);
        monGraphe.addEdge(n0, n3, 1, true);
        monGraphe.addEdge(n4, n7, 1, true);
        monGraphe.addEdge(n0, n9, 1, true);
        monGraphe.addEdge(n9, n10, 1, true);
        monGraphe.addEdge(n10, n8, 1, true);
        monGraphe.addEdge(n8, n2, 1, true);
        monGraphe.addEdge(n8, n6, 1, true);
        monGraphe.addEdge(n6, n11, 1, true);
        monGraphe.addEdge(n10, n11, 1, true);

        monGraphe.removeNode(n1);

        // ── 2. Moteur de simulation ───────────────────────────────────────
        SimulationEngine engine = new SimulationEngine(monGraphe);

        // ── 3. Agent ─────────────────────────────────────────────────────
        Agent monAgent1 = new Agent("007", 2.5f, "AVAILABLE");
        monAgent1.currentNode = n0;
        engine.addAgent(monAgent1);
        Agent monAgent2 = new Agent("018", 2.5f, "AVAILABLE");
        monAgent2.currentNode = n0;
        engine.addAgent(monAgent2);
        Agent monAgent3 = new Agent("057", 2.5f, "AVAILABLE");
        monAgent3.currentNode = n0;
        engine.addAgent(monAgent3);
        Agent monAgent4 = new Agent("063", 2.5f, "AVAILABLE");
        monAgent4.currentNode = n0;
        engine.addAgent(monAgent4);
        Agent monAgent5 = new Agent("023", 2.5f, "AVAILABLE");
        monAgent5.currentNode = n0;
        engine.addAgent(monAgent5);

        Random random = new Random();

        for (Agent agent : engine.Agents) {
            System.out.println("Agent ID : " + agent.id);
            System.out.println("État     : " + agent.state);

            // ── 4. Objectifs ─────────────────────────────────────────────────
            agent.addObjective(engine.graph.Nodes.get(random.nextInt(engine.graph.Nodes.size())));
            agent.addObjective(engine.graph.Nodes.get(random.nextInt(engine.graph.Nodes.size())));
        }

        // ── 5. Fenêtre Swing (sur l'EDT) ─────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MVP Simulation — Groupe D");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(engine.panel); // GraphicApp étend JPanel
            frame.pack(); // taille = getPreferredSize() du panel
            frame.setLocationRelativeTo(null); // centré à l'écran
            frame.setVisible(true);

            // ── 6. Démarrage de la boucle de simulation ──────────────────
            engine.start();
        });

        System.out.println("=== INTERFACE LANCÉE — fermez la fenêtre pour quitter ===");
    }
}