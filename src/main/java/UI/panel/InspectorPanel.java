package UI.panel;

import UI.utils.UIComponents;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import model.agents.Agent;
import model.graph.Edge;
import model.graph.Graph;
import model.graph.Node;
import java.util.List;

/**
 * Sub-panel layout container executing contextual structural element data logs,
 * simulation telemetry trackers, performance charts, and tracking diagnostics.
 */
public class InspectorPanel extends VBox {

    private final Graph graph;
    private final List<Agent> agents;

    private final Label infoLabel;
    private final TextArea logArea;
    private final TextArea globalStatsArea;

    /**
     * Constructs the contextual inspector layout frame tracking workspace targets.
     */
    public InspectorPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        setSpacing(10);

        Label lblInspectorSection = new Label("🔎 Inspector");
        lblInspectorSection.setStyle(UIComponents.SECTION_TITLE_STYLE);

        TabPane inspectorTabs = new TabPane();
        inspectorTabs.setPrefHeight(300);

        Tab tabDetails = new Tab("Details");
        tabDetails.setClosable(false);
        infoLabel = new Label("Click on an entity\nto view its details.");
        infoLabel.setStyle(UIComponents.DETAILS_TEXT_STYLE);
        infoLabel.setWrapText(true);
        ScrollPane detailsScroll = new ScrollPane(infoLabel);
        detailsScroll.setFitToWidth(true);
        detailsScroll.setStyle(UIComponents.INSPECTOR_SCROLL_BG);
        tabDetails.setContent(detailsScroll);

        Tab tabHistory = new Tab("History");
        tabHistory.setClosable(false);
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setStyle(UIComponents.MONO_LOG_STYLE);
        logArea.setWrapText(true);
        tabHistory.setContent(logArea);

        Tab tabGlobal = new Tab("Scoreboard");
        tabGlobal.setClosable(false);
        globalStatsArea = new TextArea();
        globalStatsArea.setEditable(false);
        globalStatsArea.setStyle(UIComponents.MONO_SCOREBOARD_STYLE);
        globalStatsArea.setWrapText(true);
        tabGlobal.setContent(globalStatsArea);

        inspectorTabs.getTabs().addAll(tabDetails, tabHistory, tabGlobal);
        getChildren().addAll(lblInspectorSection, inspectorTabs);
    }

    /**
     * Immutable data container capturing evaluated performance metrics (KPIs)
     * decoupled entirely from UI rendering engines.
     */
    private static final class AgentMetrics {
        private final double averageSpeed;
        private final double trafficEfficiency;

        private AgentMetrics(double averageSpeed, double trafficEfficiency) {
            this.averageSpeed = averageSpeed;
            this.trafficEfficiency = trafficEfficiency;
        }

        public double getAverageSpeed() { return averageSpeed; }
        public double getTrafficEfficiency() { return trafficEfficiency; }
    }

    /**
     * Pure mathematical transformation function isolating telemetry calculations.
     * Considers baseline bounds to prevent arithmetic division-by-zero errors.
     *
     * @param agent the target autonomous mobile entity to analyze
     * @return an immutable AgentMetrics object instance containing computed KPIs
     */
    private AgentMetrics computeAgentMetrics(Agent agent) {
        double activeTime = agent.getTotalActiveTime();
        if (activeTime <= 0.0) {
            return new AgentMetrics(0.0, 100.0);
        }
        double avgSpeed = (agent.getTotalDistance() / activeTime) / 60.0;
        double efficiency = ((activeTime - agent.getTotalWaitTime()) / activeTime) * 100.0;
        return new AgentMetrics(avgSpeed, efficiency);
    }

    /**
     * Evaluates metrics for targeted variables, recalculating visual representations.
     */
    public void updateInspectorContent(Object selected) {
        if (selected instanceof Agent) {
            Agent a = (Agent) selected;
            AgentMetrics metrics = computeAgentMetrics(a);

            StringBuilder sb = new StringBuilder();
            sb.append("Type     : Agent [").append(a.getAgentBehavior()).append("]\n")
              .append("ID       : ").append(a.getId()).append("\n")
              .append("Routine  : ").append(a.getAlgoType()).append("\n")
              .append("Status   : ").append(a.getState()).append("\n\n")
              .append("-- KPI & PERFORMANCE METRICS --\n")
              .append("Objectives Reached : ").append(a.getObjectivesReached()).append("\n")
              .append("Objectives Aborted : ").append(a.getAbandonedObjectives()).append("\n")
              .append("Forced Detours     : ").append(a.getDetoursTaken()).append("\n")
              .append("Total Active Time  : ").append(String.format("%.1fs", a.getTotalActiveTime())).append("\n")
              .append("Total Delay (Wait) : ").append(String.format("%.1fs", a.getTotalWaitTime())).append("\n")
              .append("Traffic Efficiency : ").append(String.format("%.1f%%", metrics.getTrafficEfficiency())).append("\n")
              .append("Target Speed Limit : ").append(String.format("%.1f", a.getSpeed())).append(" px/s\n")
              .append("Measured Net Velocity: ").append(String.format("%.1f", metrics.getAverageSpeed())).append(" px/s\n\n");

            if (a.getCurrentEdge() != null && a.getDestination() != null) {
                sb.append("On Connection Link : ").append(a.getCurrentEdge().getId()).append("\n")
                  .append("Target Destination : ").append(a.getDestination().getId());
            } else if (a.getCurrentNode() != null) {
                sb.append("Current Position   : Node ").append(a.getCurrentNode().getId());
            }

            infoLabel.setText(sb.toString());

            String logText = String.join("\n", a.getHistoryLog());
            if (!logArea.getText().equals(logText)) {
                logArea.setText(logText);
                logArea.setScrollTop(Double.MAX_VALUE);
            }

        } else if (selected instanceof Node) {
            Node n = (Node) selected;
            infoLabel.setText("Type     : Node Vertex\nID       : " + n.getId() + "\nCoordinates: (" + (int) n.getX()
                    + ", " + (int) n.getY() + ")\nIn Works : " + (n.isUnderConstruction() ? "YES (Closed)" : "No")
                    + "\nCapacity : " + n.getCapacity() + "\nStatus   : " + n.getState() + "\nOccupancy: "
                    + n.getCurrentOccupants() + "/" + n.getCapacity());
            logArea.setText("History logs are unavailable for infrastructure nodes.");
        } else if (selected instanceof Edge) {
            Edge ed = (Edge) selected;
            String directionSymbol = ed.hasDirection() ? " --> " : " <--> ";
            infoLabel.setText("Type      : Edge Connection\nID        : " + ed.getId() + "\nConnection: "
                    + ed.getSource().getId() + directionSymbol + ed.getTarget().getId() + "\nLength    : "
                    + String.format("%.1f", ed.getLength()) + "\nSpeed Mult: x"
                    + String.format("%.1f", ed.getSpeedModifier()) + "\nCapacity  : " + ed.getCapacity()
                    + "\nStatus    : " + ed.getState());
            logArea.setText("History logs are unavailable for routing edges.");
        } else {
            infoLabel.setText("Click on an entity\nto view its details.\n \n \n \n ");
            logArea.setText("");
        }

        updateGlobalScoreboard();
    }

    /** Aggregates systemic performance summaries metrics. */
    public void updateGlobalScoreboard() {
        int[] counts = new int[3];
        int[] objs = new int[3];
        int[] abds = new int[3];
        double[] act = new double[3];
        double[] wait = new double[3];

        for (Agent ag : agents) {
            int idx = 2;
            if (ag.getAlgoType() == Agent.AlgoType.DIJKSTRA) idx = 0;
            else if (ag.getAlgoType() == Agent.AlgoType.ASTAR) idx = 1;

            counts[idx]++;
            objs[idx] += ag.getObjectivesReached();
            abds[idx] += ag.getAbandonedObjectives();
            act[idx] += ag.getTotalActiveTime();
            wait[idx] += ag.getTotalWaitTime();
        }

        StringBuilder sb = new StringBuilder();
        sb.append(" GLOBAL SCOREBOARD \n========================\n\n");

        String[] teamNames = { " TEAM DIJKSTRA ROUTING", " TEAM A-STAR OPTIMIZED", " TEAM RANDOM NAVIGATOR" };
        for (int i = 0; i < 3; i++) {
            if (counts[i] == 0) continue;
            double avgEff = (act[i] > 0) ? ((act[i] - wait[i]) / act[i]) * 100.0 : 100.0;
            sb.append(teamNames[i]).append("\n  Active Agents: ").append(counts[i])
                    .append("\n  Goals Reached: ").append(objs[i])
                    .append("\n  Goals Missed : ").append(abds[i])
                    .append("\n  Net Efficiency: ").append(String.format("%.1f%%", avgEff))
                    .append("\n  Total Delay  : ").append(String.format("%.1fs", wait[i])).append("\n\n");
        }
        if (agents.isEmpty()) {
            sb.append("No active agents tracked\non spatial grid maps.");
        }

        String newText = sb.toString();
        if (!globalStatsArea.getText().equals(newText)) {
            globalStatsArea.setText(newText);
        }
    }
}