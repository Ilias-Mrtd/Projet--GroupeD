package UI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.BasicStroke;

import model.graph.Edge.edgeState;

public class EdgeRenderer implements EdgeRendering {
    private final float EPAISSEUR_LIEN = 7.0f;

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
        Stroke ancienPinceau = g.getStroke(); // epaissit le trait
        g.setStroke(new BasicStroke(EPAISSEUR_LIEN));
        g.drawLine(x0, y0, x1, y1);
        g.setStroke(ancienPinceau); // reinitialise le trait

        // etickette
        g.setColor(Color.WHITE);
        g.drawString("id: " + id, (x0 + x1) / 2, (y0 + y1) / 2 + 20);
    }
}
