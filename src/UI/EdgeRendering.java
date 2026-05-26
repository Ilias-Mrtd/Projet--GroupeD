package UI;

import java.awt.Graphics2D;
import model.graph.Edge.edgeState;

public interface EdgeRendering {
    public void drawEdge(Graphics2D g, int x0, int y0, int x1, int y1, int id, edgeState state);
}