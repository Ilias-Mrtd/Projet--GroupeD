package UI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import model.graph.Graph;
import model.agents.Agent;
import controllers.SelectionSystem;
import java.util.List;
import java.util.ArrayList;

public class GraphCanvas extends Canvas {
    private Graph graph;
    private List<Agent> agents;
    private final GraphRenderer renderer;
    private SelectionSystem selectionSystem;

    public GraphCanvas(Graph graph, GraphRenderer renderer) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.renderer = renderer;

        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());

        // Taille par défaut si pas de binding
        setWidth(800);
        setHeight(600);
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    // Associer le système de sélection (Contrôleur)
    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;

        this.setOnMouseClicked(event -> {
            if (this.selectionSystem != null) {
                this.selectionSystem.handleMouseClick(event);
            }
        });
    }

    public void draw() {
        GraphicsContext gc = this.getGraphicsContext2D();

        gc.clearRect(0, 0, this.getWidth(), this.getHeight());

        renderer.draw(gc, graph, agents);
    }
}