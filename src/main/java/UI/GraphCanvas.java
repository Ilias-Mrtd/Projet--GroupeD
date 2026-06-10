package UI;

import javafx.geometry.Point2D;
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

/**
 * Custom canvas component handling multi-layered rendering of the operational graph matrix,
 * including tracking cameras, selection mechanics, visual grids, and analytical heatmaps.
 */
public class GraphCanvas extends Canvas {

    private static final Color BG_COLOR = Color.web("#1E1E1E");
    private static final Color GRID_COLOR = Color.web("#2A2A2D");
    private static final Color HEAT_MIN_COLOR = Color.web("#29B6F6");
    private static final Color HEAT_MAX_COLOR = Color.web("#FF1744");
    private static final Color HINT_BG_COLOR = Color.color(0.1, 0.1, 0.2, 0.9);
    private static final Color HINT_TXT_COLOR = Color.web("#00E5FF");

    private Graph graph;
    private List<Agent> agents;
    private final GraphRenderer renderer;
    private SelectionSystem selectionSystem;
    private Runnable onInteraction;
    
    private boolean heatmapMode = false;
    private double zoomLevel = 1.0;
    private boolean followAgentMode = false;

    public GraphCanvas(Graph graph, GraphRenderer renderer) {
        this.graph = graph;
        this.agents = new ArrayList<>();
        this.renderer = renderer;

        // Redraw canvas layout automatically upon resize signals
        widthProperty().addListener(evt -> draw());
        heightProperty().addListener(evt -> draw());

        setWidth(800);
        setHeight(600);
    }

    /**
     * Binds the selection controller and maps standard click/drag interaction triggers.
     * @param selectionSystem The target synchronization interaction workflow manager.
     */
    public void setSelectionSystem(SelectionSystem selectionSystem) {
        this.selectionSystem = selectionSystem;
        this.setOnMouseClicked(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseClick(event); notifyInteraction(); }});
        this.setOnMousePressed(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMousePressed(event); notifyInteraction(); }});
        this.setOnMouseDragged(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseDragged(event); notifyInteraction(); }});
        this.setOnMouseReleased(event -> { if (getSelectionSystem() != null) { getSelectionSystem().handleMouseReleased(event); notifyInteraction(); }});
    }

    public void setOnInteraction(Runnable onInteraction) { this.onInteraction = onInteraction; }
    private void notifyInteraction() { if (onInteraction != null) { onInteraction.run(); } }

    /**
     * Translates raw screen view pixel coordinates back into real world matrix positions.
     * @param x Screen viewport X coordinate.
     * @param y Screen viewport Y coordinate.
     * @return Transformed world coordinates map tracking vectors.
     */
    public Point2D screenToWorld(double x, double y) {
        if (followAgentMode && selectionSystem != null && selectionSystem.getLastSelectedAgent() != null) {
            Point2D pos = selectionSystem.computeAgentPosition(selectionSystem.getLastSelectedAgent());
            if (pos != null) {
                double cx = getWidth() / 2.0;
                double cy = getHeight() / 2.0;
                return new Point2D((x - cx) / zoomLevel + pos.getX(), (y - cy) / zoomLevel + pos.getY());
            }
        }
        return new Point2D(x / zoomLevel, y / zoomLevel);
    }

    /**
     * Clear views and render structural layers, backgrounds, nodes, edges, and contextual overlays.
     */
    public void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.save(); 
        
        // 1. Render dark high-contrast canvas background base
        gc.setFill(BG_COLOR);
        gc.fillRect(0, 0, getWidth(), getHeight());

        // 2. Evaluate and apply view tracking camera transform matrices
        Agent followedAgent = (followAgentMode && selectionSystem != null) ? selectionSystem.getLastSelectedAgent() : null;
        Point2D followPos = (followedAgent != null) ? selectionSystem.computeAgentPosition(followedAgent) : null;

        if (followPos != null) {
            gc.translate(getWidth() / 2.0, getHeight() / 2.0); 
            gc.scale(zoomLevel, zoomLevel); 
            gc.translate(-followPos.getX(), -followPos.getY()); 
        } else {
            gc.scale(zoomLevel, zoomLevel);
        }

        // 3. Render giant canvas structural structural blueprint grid lines
        gc.setStroke(GRID_COLOR);
        gc.setLineWidth(1.0);
        for (double i = -2000; i <= 4000; i += 40) { gc.strokeLine(i, -2000, i, 4000); }
        for (double i = -2000; i <= 4000; i += 40) { gc.strokeLine(-2000, i, 4000, i); }

        // 4. Render analytical layered layouts if contextual flags are active
        if (heatmapMode && graph != null) { 
            drawHeatmap(gc); 
        }

        // 5. Delegate core system nodes and tracking agent drawings to specialized render engine
        renderer.draw(gc, getGraph(), getAgents());

        gc.restore(); 

        // 6. Draw non-scaling fixed interface overlays above camera boundaries
        if (getSelectionSystem() != null && getSelectionSystem().getMode() == SelectionSystem.Mode.LINKING_EDGE) {
            drawLinkingHint(gc);
        }
    }

    /**
     * Renders a blurred color density network mapping heavy agent flow concentrations.
     */
    private void drawHeatmap(GraphicsContext gc) {
        gc.save(); 
        gc.setEffect(new GaussianBlur(35)); 

        for (List<Edge> edges : graph.getEdges()) {
            for (Edge edge : edges) {
                double ratio = (double) edge.getCurrentOccupants() / Math.max(1, edge.getCapacity());
                if (ratio > 0.0) {
                    Color heatColor = HEAT_MIN_COLOR.interpolate(HEAT_MAX_COLOR, Math.min(1.0, ratio));
                    gc.setStroke(heatColor.deriveColor(0, 1, 1, Math.min(0.7, ratio * 0.8)));
                    gc.setLineWidth(45); 
                    gc.strokeLine(edge.getSource().getX(), edge.getSource().getY(), edge.getTarget().getX(), edge.getTarget().getY());
                }
            }
        }

        for (Node node : graph.getNodes()) {
            double ratio = (double) node.getCurrentOccupants() / Math.max(1, node.getCapacity());
            if (ratio > 0.0) {
                Color heatColor = HEAT_MIN_COLOR.interpolate(HEAT_MAX_COLOR, Math.min(1.0, ratio));
                gc.setFill(heatColor.deriveColor(0, 1, 1, Math.min(0.9, ratio)));
                gc.fillOval(node.getX() - 50, node.getY() - 50, 100, 100);
            }
        }
        gc.restore(); 
    }

    /**
     * Displays real-time operation guidelines on the interface during manual construction activities.
     */
    private void drawLinkingHint(GraphicsContext gc) {
        double w = getWidth(); 
        double h = getHeight(); 
        double bannerH = 36;
        
        gc.setFill(HINT_BG_COLOR); 
        gc.fillRect(0, h - bannerH, w, bannerH);
        
        gc.setFill(HINT_TXT_COLOR); 
        gc.setFont(Font.font("Segoe UI", 14));
        gc.fillText("Edge Creation Mode — Left Click: Select SOURCE then TARGET   |   Right Click: Cancel", 12, h - bannerH + 23);
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
    
    public boolean isFollowAgentMode() { return followAgentMode; }
    public void setFollowAgentMode(boolean followAgentMode) { this.followAgentMode = followAgentMode; draw(); }
}