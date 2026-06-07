package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.graph.Node;

public class NodeRenderer implements NodeRendering {
    protected final int RADIUS = 30;

    @Override
    public void drawNode(GraphicsContext gc, Node node) {

        // Bordure sombre pour donner un effet de volume
        gc.setFill(Color.web("#263238"));
        gc.fillOval(node.getX() - RADIUS / 2 - 4, node.getY() - RADIUS / 2 - 4, RADIUS + 8, RADIUS + 8);

        // Halo de selection CYAN FLUORESCENT
        if (node.isSelected()) {
            gc.setFill(Color.web("#00E5FF"));
            gc.fillOval(node.getX() - RADIUS / 2 - 6, node.getY() - RADIUS / 2 - 6, RADIUS + 12, RADIUS + 12);
        }

        switch (node.getState()) {
            case OUT:
                gc.setFill(Color.web("#37474F"));
                break;
            case AVAILABLE:
                // Du Bleu clair au Rouge
                Color nodeStress = Color.web("#4FC3F7").interpolate(Color.web("#F44336"),
                        ((double) node.getCurrentOccupants() / (double) node.getCapacity()));
                gc.setFill(nodeStress);
                break;
            case FULL:
                gc.setFill(Color.web("#D32F2F"));
                break;
        }
        if (node.isUnderConstruction()) {
            gc.setFill(Color.web("#FFC107")); // Jaune travaux
        }

        gc.fillOval(node.getX() - RADIUS / 2, node.getY() - RADIUS / 2, RADIUS, RADIUS);

        // Etiquette blanche pour être lisible
        gc.setFill(Color.web("#FFFFFF"));
        gc.fillText("id: " + node.getId(), node.getX() + 15, node.getY() + 20);
    }
}