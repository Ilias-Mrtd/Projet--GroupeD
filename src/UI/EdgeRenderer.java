package UI;

import java.awt.Color;
import java.awt.Graphics2D;

import model.graph.Edge.edgeState;

public class EdgeRenderer implements EdgeRendering {

    @Override
    public void drawEdge(Graphics2D g, int x0, int y0, int x1, int y1, int id, edgeState state) {
        // dessin de l'arete
        switch (state) {
            case OUT:
                g.setColor(Color.LIGHT_GRAY);
                break;
            case FULL:
                g.setColor(Color.RED);
                break;
            case AVAILABLE:
                g.setColor(Color.DARK_GRAY);

        }
        g.drawLine(x0, y0, x1, y1);

        // etickette
        g.setColor(Color.WHITE);
        g.drawString("id: " + id, (x0 + x1) / 2, (y0 + y1) / 2 + 20);
    }
}
