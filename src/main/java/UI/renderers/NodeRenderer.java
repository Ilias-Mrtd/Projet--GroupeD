package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import model.graph.Node;

public class NodeRenderer implements NodeRendering {
    protected final int RADIUS = 30;

    @Override
    public void drawNode(GraphicsContext gc, Node node) {

        // Halo de selection
        if (node.isSelected()) {
            gc.setFill(Color.YELLOWGREEN);
            gc.fillOval(node.getX() - RADIUS / 2 - 3, node.getY() - RADIUS / 2 - 3, RADIUS + 6, RADIUS + 6);
        }

        switch (node.getState()) {
            case OUT:
                gc.setFill(Color.BLACK);
                break;
            case AVAILABLE:
                Color nodeStress = Color.GREY.interpolate(Color.RED,
                        ((double) node.getCurrentOccupants() / (double) node.getCapacity()));
                gc.setFill(nodeStress);
                break;
            case FULL:
                gc.setFill(Color.RED);
                break;
        }
        if (node.isUnderConstruction()) {
            gc.setFill(Color.BLACK);
        }

        gc.fillOval(node.getX() - RADIUS / 2, node.getY() - RADIUS / 2, RADIUS, RADIUS);

        // etickette
        gc.setFill(Color.BLACK);
        gc.fillText("id: " + node.getId(), node.getX() + 10, node.getY() + 20);
    }
}
