package simulationEngine;

import java.util.List;
import java.util.ArrayList;
import javax.swing.Timer;

import UI.GraphPanel;
import UI.GraphRenderer;
import controllers.SelectionSystem;
import model.agents.Agent;
import model.graph.Graph;

public class SimulationEngine {

    public Graph graph;
    public List<Agent> Agents;
    public GraphPanel panel; // le JPanel affiché dans la JFrame
    private Timer timer; // boucle de simulation Swing (thread EDT)

    /** Délai entre chaque tick en millisecondes (≈ 60 fps). */
    private static final int TICK_DELAY_MS = 100;

    public SimulationEngine(Graph graph) {
        this.graph = graph;
        this.Agents = new ArrayList<>();

        // Système de sélection partagé entre GraphicApp et GraphRenderer
        SelectionSystem selectionSystem = new SelectionSystem();

        // GraphRenderer : orchestre le dessin (pas un JPanel)
        GraphRenderer graphRenderer = new GraphRenderer(graph, Agents, selectionSystem);

        // GraphicApp : le JPanel hôte, gère les clics et appelle render()
        this.panel = new GraphPanel(this, graphRenderer, selectionSystem);

        // Timer Swing : appelle tick() puis repaint() à intervalle régulier
        this.timer = new Timer(TICK_DELAY_MS, e -> {
            tick();
            panel.repaint();
        });
    }

    public void addAgent(Agent agent) {
        agent.graph = this.graph;
        this.Agents.add(agent);
        System.out.println("Agent " + agent.id + " ajouté au moteur de simulation.");
    }

    public void start() {
        timer.start();
    }

    public void stop() {
        timer.stop();
    }

    public void tick() {
        for (Agent agent : Agents) {
            agent.update();
        }
    }
}