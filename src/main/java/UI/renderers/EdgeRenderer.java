package UI.renderers;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import model.graph.Edge;

public class EdgeRenderer implements EdgeRendering {
    private final float EDGE_WIDTH = 8.0f;

    @Override
    public void drawEdge(GraphicsContext gc, Edge edge) {

        // Halo autour de l'arete
        if (edge.isSelected()) {
            gc.setStroke(Color.YELLOWGREEN);
            gc.setLineWidth(EDGE_WIDTH + 6.0f);
            gc.strokeLine(edge.getSource().getX(), edge.getSource().getY(), edge.getTarget().getX(), edge.getTarget().getY());
        }

        switch (edge.getState()) {
            case OUT:
                gc.setStroke(Color.BLACK);
                break;
            case AVAILABLE:
                Color edgeStress = Color.GREY.interpolate(Color.RED,
                        ((double) edge.getCurrentOccupants() / (double) edge.getCapacity()));
                gc.setStroke(edgeStress);
                break;
            case FULL:
                gc.setStroke(Color.RED);
                break;
        }

        gc.setLineWidth(EDGE_WIDTH);
        gc.strokeLine(edge.getSource().getX(), edge.getSource().getY(), edge.getTarget().getX(), edge.getTarget().getY());
        gc.setLineWidth(1.0f);

        // etickette
        gc.setFill(Color.WHITE); // setFill et non setStroke pour le fillText !
        gc.fillText("id: " + edge.getId(), (edge.getSource().getX() + edge.getTarget().getX()) / 2, (edge.getSource().getY() + edge.getTarget().getY()) / 2 + 20);

        // fleche directionelle
        if (!edge.hasDirection()) {
            int mx = (int) ((edge.getSource().getX() + edge.getTarget().getX()) / 2);
            int my = (int) ((edge.getSource().getY() + edge.getTarget().getY()) / 2);

            double angle = Math.atan2(edge.getTarget().getY() - edge.getSource().getY(),
                    edge.getTarget().getX() - edge.getSource().getX());
            double headAng = Math.PI / 6;
            int len = 8;
            int head = 7;

            int x2 = mx + (int) (len * Math.cos(angle));
            int y2 = my + (int) (len * Math.sin(angle));
            int x1 = mx - (int) (len * Math.cos(angle));
            int y1 = my - (int) (len * Math.sin(angle));

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.2f);
            gc.strokeLine(x1, y1, x2, y2);
            gc.strokeLine(x2, y2,
                    x2 - (int) (head * Math.cos(angle - headAng)),
                    y2 - (int) (head * Math.sin(angle - headAng)));
            gc.strokeLine(x2, y2,
                    x2 - (int) (head * Math.cos(angle + headAng)),
                    y2 - (int) (head * Math.sin(angle + headAng)));
            gc.setLineWidth(1.0f);
        }
    }
}
