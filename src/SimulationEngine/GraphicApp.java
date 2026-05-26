package simulationEngine;

import simulationEngine.SimulationEngine;
import simulationEngine.graphRenderer.*;
import simulationEngine.graphRenderer.graph.*;

import javax.swing.JPanel;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

public class GraphicApp extends JPanel implements MouseListener {

    private final SimulationEngine engine;
    private final GraphRenderer renderedGraph;
    private final SelectionSystem selectionSystem;

    // Interfaces de rendu (conformes au diagramme — instances dans les renderers)
    private final NodeRendering nodeRendering = new NodeRenderer();
    private final EdgeRendering edgeRendering = new EdgeRenderer();

    public GraphicApp(SimulationEngine engine, GraphRenderer renderedGraph, SelectionSystem selectionSystem) {
        this.engine = engine;
        this.renderedGraph = renderedGraph;
        this.selectionSystem = selectionSystem;

        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 600));
        addMouseListener(this);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        // Délégation complète au GraphRenderer
        renderedGraph.render(engine.graph, engine.Agents, g2);

        // Panneau d'info sur la sélection courante
        drawSelectionInfo(g2);
    }

    private void drawSelectionInfo(Graphics2D g2) {
        Object sel = selectionSystem.getSelectedObject();
        if (sel == null)
            return;

        String info;
        if (sel instanceof Node n) {
            info = String.format("Nœud  id=%d  x=%.0f  y=%.0f  état=%s",
                    n.id, n.x, n.y, n.state);
        } else if (sel instanceof Edge e) {
            info = String.format("Arête id=%d  %d→%d  len=%.1f  état=%s",
                    e.id, e.source.id, e.target.id, e.length, e.state);
        } else if (sel instanceof simulationEngine.graphRenderer.agents.Agent a) {
            info = String.format("Agent id=%d  vitesse=%.1f  état=%s",
                    a.id, a.speed, a.state);
        } else {
            return;
        }

        // Fond semi-transparent
        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(10, 10, 320, 30, 8, 8);

        // Texte blanc
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(info, 18, 30);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selectionSystem.selectAct(
                new Point2D.Float(e.getX(), e.getY()),
                engine.graph,
                engine.Agents);
        repaint();
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}