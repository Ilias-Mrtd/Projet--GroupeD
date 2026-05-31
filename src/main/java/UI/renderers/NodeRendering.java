package UI.renderers;

import javafx.scene.canvas.GraphicsContext;

import model.graph.Node;

public interface NodeRendering {
    public void drawNode(GraphicsContext gc, Node node);

}