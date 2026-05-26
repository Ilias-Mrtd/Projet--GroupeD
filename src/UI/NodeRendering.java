package UI;

import java.awt.Graphics2D;
import model.graph.Node.nodeState;

public interface NodeRendering {
    public void drawNode(Graphics2D g, int x, int y, int id, nodeState state);

}