package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import model.agents.Agent;

/**
 * Defines the rendering contract for drawing agent entities onto a JavaFX canvas.
 */
@FunctionalInterface
public interface AgentRendering {

    /**
     * Renders a specific agent entity using the provided graphics context.
     * @param gc The active graphics context of the canvas.
     * @param agent The target tracking agent to draw.
     */
    void drawAgent(GraphicsContext gc, Agent agent);
}