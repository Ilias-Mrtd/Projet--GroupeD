package UI.renderers;

import javafx.scene.canvas.GraphicsContext;

import model.agents.Agent;

public interface AgentRendering {
    void drawAgent(GraphicsContext gc, Agent agent);
}
