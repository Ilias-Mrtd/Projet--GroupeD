package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import model.graph.Node;

public class NodeRenderer implements NodeRendering {
    protected final int RADIUS = 30;

    @Override
    public void drawNode(GraphicsContext gc, Node node) {

        // Halo de selection
        if (node.isSelected) {
            gc.setFill(Color.YELLOWGREEN);
            gc.fillOval(node.x - RADIUS / 2 - 3, node.y - RADIUS / 2 - 3, RADIUS + 6, RADIUS + 6);
        }

        switch (node.state) {
            case OUT:
                gc.setFill(Color.BLACK);
                break;
            case AVAILABLE:
                Color nodeStress = Color.GREY.interpolate(Color.RED,
                        ((double) node.currentOccupants / (double) node.capacity));
                gc.setFill(nodeStress);
                break;
            case FULL:
                gc.setFill(Color.RED);
                break;
        }
        gc.fillOval(node.x - RADIUS / 2, node.y - RADIUS / 2, RADIUS, RADIUS);

        // etickette
        gc.setFill(Color.BLACK);
        gc.fillText("id: " + node.id, node.x + 10, node.y + 20);
    }
}
