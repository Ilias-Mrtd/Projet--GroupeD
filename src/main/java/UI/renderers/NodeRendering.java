package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import model.graph.Node;

/**
 * Defines the rendering contract for drawing graph node vertices onto a JavaFX canvas.
 */
@FunctionalInterface
public interface NodeRendering {

    /**
     * Renders a specific structural node vertex using the provided graphics context.
     * @param gc The active graphics context of the canvas.
     * @param node The target graph node to draw.
     */
    void drawNode(GraphicsContext gc, Node node);
}