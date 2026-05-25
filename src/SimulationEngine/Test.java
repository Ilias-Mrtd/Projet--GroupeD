package simulationEngine;

import simulationEngine.graphRenderer.agents.Agent;
import simulationEngine.graphRenderer.graph.*;

public class Test {
    public static void main(String[] args) {

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

        Agent monAgent1 = new Agent("006", 25.0f, "AVAILABLE");
        monAgent1.currentNode = n0;
        engine.addAgent(monAgent1);

        Agent monAgent2 = new Agent("007", 25.0f, "AVAILABLE");
        monAgent2.currentNode = n1;
        engine.addAgent(monAgent2);

        monAgent2.addObjective(n2);

        engine.tick();
        System.out.println(1);

        Agent monAgent3 = new Agent("008", 25.0f, "AVAILABLE");
        monAgent3.currentNode = n2;
        engine.addAgent(monAgent3);

        monAgent1.addObjective(n2);
        engine.tick();
        System.out.println(2);
        monAgent2.addObjective(n0);
        monAgent3.addObjective(n0);

        engine.tick();
        System.out.println(3);
        engine.tick();
        System.out.println(4);
        engine.tick();
        System.out.println(5);
        engine.tick();

    }
}
