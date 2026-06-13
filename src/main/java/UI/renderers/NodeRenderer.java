package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.graph.Node;

public class NodeRenderer implements NodeRendering {
    private static final int RADIUS = 30;

    /**
     * Renders a single graph node vertex on the canvas, applying status colors,
     * capacity stress gradients, selection highlights, and construction overlays.
     * @param gc The active graphics context of the canvas.
     * @param node The target graph node to draw.
     */
    @Override
    public void drawNode(GraphicsContext gc, Node node) {
        // Cache node coordinates and metrics to optimize repetitive geometry drawing operations
        double x = node.getX();
        double y = node.getY();
        double halfRadius = RADIUS / 2.0;

        // 1. Render dark background stroke edge to provide depth perception
        gc.setFill(Color.web("#263238"));
        gc.fillOval(x - halfRadius - 4, y - halfRadius - 4, RADIUS + 8, RADIUS + 8);

        // 2. Render fluorescent cyan selection halo highlight if active
        if (node.isSelected()) {
            gc.setFill(Color.web("#00E5FF"));
            gc.fillOval(x - halfRadius - 6, y - halfRadius - 6, RADIUS + 12, RADIUS + 12);
        }

        // 3. Determine structural node color base according to operational metrics
        switch (node.getState()) {
            case OUT:
                gc.setFill(Color.web("#37474F"));
                break;
            case AVAILABLE:
                // Dynamic occupancy stress shifting linearly from sky blue to absolute red
                double occupancyRatio = (node.getCapacity() > 0)
                        ? (double) node.getCurrentOccupants() / (double) node.getCapacity()
                        : 0.0;
                Color nodeStress = Color.web("#4FC3F7").interpolate(Color.web("#F44336"), occupancyRatio);
                gc.setFill(nodeStress);
                break;
            case FULL:
                gc.setFill(Color.web("#D32F2F"));
                break;
        }

        // 4. Override color tracking if node is flagged under active construction works
        if (node.isUnderConstruction()) {
            gc.setFill(Color.web("#FFC107")); // Construction amber yellow
        }

        // 5. Render core node surface circle
        gc.fillOval(x - halfRadius, y - halfRadius, RADIUS, RADIUS);

        // 6. ONLY display the ID label if the node is currently selected by the user
        if (node.isSelected()) {
            String text = "ID: " + node.getId();
            
            // Pill Badge (Semi-transparent black background)
            gc.setFill(Color.color(0.1, 0.1, 0.1, 0.85));
            gc.fillRoundRect(x + 15, y + 10, 50, 18, 10, 10);
            
            // Cyan Text
            gc.setFill(Color.web("#00E5FF"));
            gc.fillText(text, x + 20, y + 23);
        }
    }
}