package simulationEngine.graphRenderer;

import java.awt.Color;
import java.awt.Graphics2D;

public class EdgeRenderer implements EdgeRendering {

    @Override
    public void drawEdge(Graphics2D g, int x0, int y0, int x1, int y1, int id) {
        // dessin de l'arete
        g.setColor(Color.GRAY);
        // changer l'epaisseur en fonction de l'etat
        g.drawLine(x0, y0, x1, y1);

        // etickette
        g.setColor(Color.WHITE);
        g.drawString("id: " + id, (x0 + x1) / 2, (y0 + y1) / 2 + 20);
    }
}
