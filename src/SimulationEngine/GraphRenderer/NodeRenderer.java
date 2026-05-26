package simulationEngine.graphRenderer;

import java.awt.Color;
import java.awt.Graphics2D;

public class NodeRenderer implements NodeRendering {
    private final int TAILLE = 30;

    @Override
    public void drawNode(Graphics2D g, int x, int y, int id) {
        // plus tard la couleur changeras selon l'etat du noeud
        g.setColor(Color.DARK_GRAY);
        // le noeud representer par un oval
        g.fillOval(x, y, TAILLE, TAILLE);

        // etickette
        g.setColor(Color.WHITE);
        g.drawString("id: " + id, x + 10, y + 20);
    }
}
