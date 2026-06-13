package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.graph.Edge;

public class EdgeRenderer implements EdgeRendering {
    private final float EDGE_WIDTH = 8.0f;

    /**
     * Renders a single graph edge on the canvas, applying status colors,
     * traffic stress gradients, selection highlights, and directional markers.
     * * @param gc   The active graphics context of the canvas.
     * @param edge The target graph edge to draw.
     */
    @Override
    public void drawEdge(GraphicsContext gc, Edge edge) {
        // Cache node coordinates to avoid redundant reference lookups
        double xSource = edge.getSource().getX();
        double ySource = edge.getSource().getY();
        double xTarget = edge.getTarget().getX();
        double yTarget = edge.getTarget().getY();

        // 1. Render selection halo highlight
        if (edge.isSelected()) {
            gc.setStroke(Color.web("#00E5FF"));
            gc.setLineWidth(EDGE_WIDTH + 10.0f);
            gc.strokeLine(xSource, ySource, xTarget, yTarget);
        }

        // 2. Render asphalt road background base
        gc.setStroke(Color.web("#263238"));
        gc.setLineWidth(EDGE_WIDTH + 6.0f);
        gc.strokeLine(xSource, ySource, xTarget, yTarget);

        // 3. Determine edge inner stroke color based on current status state
        switch (edge.getState()) {
            case OUT:
                gc.setStroke(Color.web("#37474F"));
                break;
            case AVAILABLE:
                // Dynamic stress interpolation shifting from sky blue to absolute red
                double occupancyRatio = (edge.getCapacity() > 0)
                        ? (double) edge.getCurrentOccupants() / (double) edge.getCapacity()
                        : 0.0;
                Color edgeStress = Color.web("#4FC3F7").interpolate(Color.web("#F44336"), occupancyRatio);
                gc.setStroke(edgeStress);
                break;
            case FULL:
                gc.setStroke(Color.web("#D32F2F"));
                break;
        }

        // 4. Render the inner core status track
        gc.setLineWidth(EDGE_WIDTH);
        gc.strokeLine(xSource, ySource, xTarget, yTarget);
        gc.setLineWidth(1.0f);

        // Cache midpoint for labels and arrows
        double midX = (xSource + xTarget) / 2.0;
        double midY = (ySource + yTarget) / 2.0;

        // 5. Render directional indicator arrows where layout limits traffic orientation
        if (!edge.hasDirection()) {
            double angle = Math.atan2(yTarget - ySource, xTarget - xSource);
            double headAngle = Math.PI / 6.0;
            int lineLength = 8;
            int headLength = 7;

            double x2 = midX + (lineLength * Math.cos(angle));
            double y2 = midY + (lineLength * Math.sin(angle));
            double x1 = midX - (lineLength * Math.cos(angle));
            double y1 = midY - (lineLength * Math.sin(angle));

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5f);
            gc.strokeLine(x1, y1, x2, y2);

            // Draw directional arrow wings
            gc.strokeLine(x2, y2,
                    x2 - (headLength * Math.cos(angle - headAngle)),
                    y2 - (headLength * Math.sin(angle - headAngle)));
            gc.strokeLine(x2, y2,
                    x2 - (headLength * Math.cos(angle + headAngle)),
                    y2 - (headLength * Math.sin(angle + headAngle)));
            gc.setLineWidth(1.0f);
        }
        
        // 6. ONLY display the edge ID label if the edge is selected
        if (edge.isSelected()) {
            String text = "Edge: " + edge.getId();
            
            // Pill Badge (Semi-transparent black background)
            gc.setFill(Color.color(0.1, 0.1, 0.1, 0.85));
            gc.fillRoundRect(midX + 5, midY + 5, 65, 18, 10, 10);
            
            // Cyan Text
            gc.setFill(Color.web("#00E5FF")); 
            gc.fillText(text, midX + 10, midY + 18);
        }
    }
}