package UI;

import java.awt.Color;
import java.awt.Graphics2D;

import model.graph.Node.nodeState;

public class NodeRenderer implements NodeRendering {
    private final int TAILLE = 30;

    @Override
    public void drawNode(Graphics2D g, int x, int y, int id, nodeState state) {
        switch (state) {
            case OUT:
                g.setColor(Color.LIGHT_GRAY);
                break;
            case AVAILABLE:
                g.setColor(Color.DARK_GRAY);
                break;
            case FULL:
                g.setColor(Color.RED);
                break;
        }
        g.fillOval(x, y, TAILLE, TAILLE);

        // etickette
        g.setColor(Color.BLACK);
        g.drawString("id: " + id, x + 10, y + 20);
    }
}
