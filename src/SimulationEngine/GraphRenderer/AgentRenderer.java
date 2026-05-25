package simulationEngine.graphRenderer;

import java.awt.Color;
import java.awt.Graphics2D;

public class AgentRenderer implements AgentRendering {
    private final int TAILLE = 10;

    @Override
    public void drawAgent(Graphics2D g, int x, int y, int id) {
        // plus tard la couleur changeras selon l'etat du noeud
        g.setColor(Color.BLUE);
        // le noeud representer par un oval
        g.fillOval(x, y, TAILLE, TAILLE);

        // etickette
        g.setColor(Color.WHITE);
        g.drawString("id: " + id, x + 5, y + 10);
    }
}
