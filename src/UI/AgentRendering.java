package UI;

import java.awt.Graphics2D;
import model.agents.Agent.agentState;

public interface AgentRendering {
    void drawAgent(Graphics2D g, int x, int y, int id, agentState state);
}
