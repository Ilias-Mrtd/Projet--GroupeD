package UI.renderers;

import javafx.scene.canvas.GraphicsContext;

import model.graph.Edge;

public interface EdgeRendering {
    public void drawEdge(GraphicsContext gc, Edge edge);
}