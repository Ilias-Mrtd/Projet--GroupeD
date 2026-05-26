package UI;

import java.awt.Color;
import java.awt.Graphics2D;
import model.agents.Agent.agentState;

public class AgentRenderer implements AgentRendering {
    private final int TAILLE = 10;

    @Override
    public void drawAgent(Graphics2D g, int x, int y, int id, agentState state) {
        switch (state) {
            case agentState.AVAILABLE:
                g.setColor(Color.GRAY);
                break;
            case agentState.CALCULATING:
                g.setColor(Color.GREEN);
                break;
            case agentState.WAITING:
                g.setColor(Color.YELLOW);
                break;
            case agentState.RUNNING:
                g.setColor(Color.BLUE);
                break;
        }
        g.fillOval(x, y, TAILLE, TAILLE);

        // etickette
        g.setColor(Color.BLACK);
        g.drawString("id: " + id, x + 5, y + 10);
    }
}
