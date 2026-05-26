import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import simulationEngine.SimulationEngine;
import simulationEngine.graphRenderer.agents.Agent;
import simulationEngine.graphRenderer.graph.Graph;
import simulationEngine.graphRenderer.graph.Node;

/**
 * Classe principale pour lancer le MVP (Minimum Viable Product).
 * @author ARNOUX Antoine, ADEM Ben-Halima, PELLERIN Corentin,
 *         RIVOHERISSON Tsiky, MOURTADA Ilias
 */
public class AppProjet {

    public static void main(String[] args) {
        System.out.println("=== DÉMARRAGE DU TEST MVP - THÉMATIQUE 3 ===");

        // ── 1. Construction du graphe ─────────────────────────────────────
        Graph monGraphe = new Graph();

        monGraphe.addNode(150, 300, 5);   // n0 — gauche
        monGraphe.addNode(400, 150, 5);   // n1 — centre-haut
        monGraphe.addNode(650, 300, 5);   // n2 — droite

        Node n0 = monGraphe.Nodes.get(0);
        Node n1 = monGraphe.Nodes.get(1);
        Node n2 = monGraphe.Nodes.get(2);

        monGraphe.addEdge(n0, n1, 2, true);
        monGraphe.addEdge(n1, n2, 2, true);

        // ── 2. Moteur de simulation ───────────────────────────────────────
        SimulationEngine engine = new SimulationEngine(monGraphe);

        // ── 3. Agent ─────────────────────────────────────────────────────
        Agent monAgent = new Agent("007", 5.0f, "AVAILABLE");
        monAgent.currentNode = n0;
        engine.addAgent(monAgent);

        System.out.println("Agent ID : " + monAgent.id);
        System.out.println("État     : " + monAgent.state);

        // ── 4. Objectifs ─────────────────────────────────────────────────
        monAgent.addObjective(n2);
        monAgent.addObjective(n0);

        // ── 5. Fenêtre Swing (sur l'EDT) ─────────────────────────────────
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("MVP Simulation — Groupe D");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.add(engine.panel);   // GraphicApp étend JPanel
            frame.pack();              // taille = getPreferredSize() du panel
            frame.setLocationRelativeTo(null);  // centré à l'écran
            frame.setVisible(true);

            // ── 6. Démarrage de la boucle de simulation ──────────────────
            engine.start();
        });

        System.out.println("=== INTERFACE LANCÉE — fermez la fenêtre pour quitter ===");
    }
}