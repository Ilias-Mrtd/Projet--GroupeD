package SimulationEngine.GraphRenderer;

import java.awt.Graphics2D;

public interface AgentRendering {
    void drawAgent(Graphics2D g, int x, int y, int id);
}
