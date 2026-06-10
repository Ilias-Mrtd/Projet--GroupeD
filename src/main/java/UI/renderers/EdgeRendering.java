package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import model.graph.Edge;

/**
 * Defines the rendering contract for drawing graph edge elements onto a JavaFX canvas.
 */
@FunctionalInterface
public interface EdgeRendering {

    /**
     * Renders a specific edge structure using the provided graphics context.
     * @param gc The active graphics context of the canvas.
     * @param edge The target graph edge to draw.
     */
    void drawEdge(GraphicsContext gc, Edge edge);
}