package UI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.graph.Graph;
import model.agents.Agent;
import controllers.SelectionSystem;
import java.util.List;
import java.util.ArrayList;

/**
 * GraphCanvas — zone de dessin principale.
 *
 * Délègue tout le rendu métier au GraphRenderer.
 * Affiche une bannière d'instruction quand SelectionSystem est en mode LINKING_EDGE.
 */
public class GraphCanvas extends Canvas {

    private Graph graph;
    private List<Agent> agents;
    private final GraphRenderer renderer;
    private SelectionSystem selectionSystem;

    public GraphCanvas(Graph graph, GraphRenderer renderer) {
        this.graph    = graph;
        this.agents   = new ArrayList<>();
        this.renderer = renderer;

        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());

        setWidth(800);
        setHeight(600);
    }

    public void setAgents(List<Agent> agents) {
        this.agents = agents;
    }

    /**
     * Associe le SelectionSystem et branche les écouteurs souris.
     * On écoute TOUS les clics (gauche + droit) sur un seul handler.
     */
    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;

        // Un seul listener : le SelectionSystem trie lui-même bouton gauche/droit
        this.setOnMouseClicked(event -> {
            if (getSelectionSystem() != null) {
                getSelectionSystem().handleMouseClick(event);
            }
        });
    }

    public SelectionSystem getSelectionSystem() {
        return this.selectionSystem;
    }
    
    public Graph getGraph() {
        return graph;
    }

    public void setGraph(Graph graph) {
        this.graph = graph;
    }

    public List<Agent> getAgents() {
        return agents;
    }

    public GraphRenderer getRenderer() {
        return renderer;
    }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0, 0, getWidth(), getHeight());

        renderer.draw(gc, getGraph(), getAgents());

        // Bandeau d'aide visuel en mode liaison d'arête
        if (getSelectionSystem() != null
                && getSelectionSystem().getMode() == SelectionSystem.Mode.LINKING_EDGE) {
            drawLinkingHint(gc);
        }
    }

    /**
     * Affiche un bandeau semi-transparent en bas du canvas pour guider
     * l'utilisateur pendant la sélection des deux nœuds d'une arête.
     */
    private void drawLinkingHint(GraphicsContext gc) {
        double w = getWidth();
        double h = getHeight();
        double bannerH = 36;

        gc.setFill(Color.color(0.1, 0.1, 0.8, 0.75));
        gc.fillRect(0, h - bannerH, w, bannerH);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("System", 14));
        gc.fillText(
                "Mode création d'arête — clic gauche : choisir SOURCE puis CIBLE   |   clic droit : annuler",
                12, h - bannerH + 23
        );
    }
}