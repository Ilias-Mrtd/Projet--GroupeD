package UI;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import model.graph.Graph;
import model.graph.Node;
import model.graph.Edge;
import model.agents.Agent;
import controllers.SelectionSystem;

import java.util.List;
import java.util.ArrayList;

public class GraphCanvas extends Canvas {

    private Graph graph;
    private List<Agent> agents;
    private final GraphRenderer renderer;
    private SelectionSystem selectionSystem;
    private Runnable onInteraction;
    
    private boolean heatmapMode = false;
    private double zoomLevel = 1.0;

    public GraphCanvas(Graph graph, GraphRenderer renderer) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.renderer = renderer;

        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());

        setWidth(800);
        setHeight(600);
    }

    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;
        this.setOnMouseClicked(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseClick(event); notifyInteraction(); }});
        this.setOnMousePressed(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMousePressed(event); notifyInteraction(); }});
        this.setOnMouseDragged(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseDragged(event); notifyInteraction(); }});
        this.setOnMouseReleased(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseReleased(event); notifyInteraction(); }});
    }

    public void setOnInteraction(Runnable onInteraction) { this.onInteraction = onInteraction; }
    private void notifyInteraction() { if (onInteraction != null) { onInteraction.run(); } }

    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save(); 
        
        // FOND SOMBRE
        gc.setFill(Color.web("#1E1E1E"));
        gc.fillRect(0, 0, getWidth(), getHeight());

        // APPLIQUE LE ZOOM
        gc.scale(zoomLevel, zoomLevel);

        // GRILLE "BLUEPRINT" SOUS LA CARTE
        gc.setStroke(Color.web("#2A2A2D"));
        gc.setLineWidth(1.0);
        for (double i = 0; i < getWidth() / zoomLevel; i += 40) { gc.strokeLine(i, 0, i, getHeight() / zoomLevel); }
        for (double i = 0; i < getHeight() / zoomLevel; i += 40) { gc.strokeLine(0, i, getWidth() / zoomLevel, i); }

        if (heatmapMode && graph != null) { drawHeatmap(gc); }

        renderer.draw(gc, getGraph(), getAgents());

        gc.restore(); 

        if (getSelectionSystem() != null && getSelectionSystem().getMode() == SelectionSystem.Mode.LINKING_EDGE) {
            drawLinkingHint(gc);
        }
    }

    private void drawHeatmap(GraphicsContext gc) {
        gc.save(); 
        gc.setEffect(new GaussianBlur(35)); 
        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                double ratio = (double) edge.getCurrentOccupants() / Math.max(1, edge.getCapacity());
                if (ratio > 0.0) {
                    // Nouvelles couleurs fluo pour la Heatmap
                    Color heatColor = Color.web("#29B6F6").interpolate(Color.web("#FF1744"), Math.min(1.0, ratio));
                    gc.setStroke(heatColor.deriveColor(0, 1, 1, Math.min(0.7, ratio * 0.8)));
                    gc.setLineWidth(45); 
                    gc.strokeLine(edge.getSource().getX(), edge.getSource().getY(), edge.getTarget().getX(), edge.getTarget().getY());
                }
            }
        }
        for (Node node : graph.getNodes()) {
            double ratio = (double) node.getCurrentOccupants() / Math.max(1, node.getCapacity());
            if (ratio > 0.0) {
                Color heatColor = Color.web("#29B6F6").interpolate(Color.web("#FF1744"), Math.min(1.0, ratio));
                gc.setFill(heatColor.deriveColor(0, 1, 1, Math.min(0.9, ratio)));
                gc.fillOval(node.getX() - 50, node.getY() - 50, 100, 100);
            }
        }
        gc.restore(); 
    }

    private void drawLinkingHint(GraphicsContext gc) {
        double w = getWidth(); double h = getHeight(); double bannerH = 36;
        gc.setFill(Color.color(0.1, 0.1, 0.2, 0.9)); gc.fillRect(0, h - bannerH, w, bannerH);
        gc.setFill(Color.web("#00E5FF")); gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("Mode création d'arête — clic gauche : choisir SOURCE puis CIBLE   |   clic droit : annuler", 12, h - bannerH + 23);
    }

    public List<Agent> getAgents() { return agents; }
    public void setAgents(List<Agent> agents) { this.agents = agents; }
    public SelectionSystem getSelectionSystem() { return this.selectionSystem; }
    public Graph getGraph() { return graph; }
    public void setGraph(Graph graph) { this.graph = graph; }
    public GraphRenderer getRenderer() { return renderer; }
    public boolean isHeatmapMode() { return heatmapMode; }
    public void setHeatmapMode(boolean heatmapMode) { this.heatmapMode = heatmapMode; }
    public double getZoomLevel() { return zoomLevel; }
    public void setZoomLevel(double zoomLevel) { this.zoomLevel = zoomLevel; draw(); }
}