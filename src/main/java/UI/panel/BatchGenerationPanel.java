package UI.panel;

import UI.utils.UIComponents;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Sub-panel layout container coordinating quantitative batch synthesis counts,
 * map generation scaling metrics, and structural initialization hooks.
 */
public class BatchGenerationPanel extends VBox {

    private final Runnable onGenerateGraph;
    private final Runnable onSpawnAgents;

    private final Label lblGenNodes;
    private final Label lblGenAgents;

    private int genGridSide = 4;
    private int genAgentCount = 10;

    /**
     * Constructs automated synthesis execution control block items.
     */
    public BatchGenerationPanel(Runnable onGenerateGraph, Runnable onSpawnAgents) {
        this.onGenerateGraph = onGenerateGraph;
        this.onSpawnAgents = onSpawnAgents;

        setSpacing(10);

        Label lblGenSection = new Label("Batch Matrix Generation");
        lblGenSection.setStyle(UIComponents.SECTION_TITLE_STYLE);

        lblGenNodes = new Label("Grid Map: " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " vertices)");
        lblGenNodes.setStyle(UIComponents.BASE_LABEL_STYLE);

        Button btnGenNodesMinus = UIComponents.createSmallButton("−");
        Button btnGenNodesPlus = UIComponents.createSmallButton("+");

        btnGenNodesMinus.setOnAction(e -> {
            if (genGridSide > 2) {
                genGridSide--;
                updateGridNodeLabel();
            }
        });
        btnGenNodesPlus.setOnAction(e -> {
            if (genGridSide < 12) {
                genGridSide++;
                updateGridNodeLabel();
            }
        });

        HBox genNodesBox = new HBox(6, btnGenNodesMinus, lblGenNodes, btnGenNodesPlus);
        genNodesBox.setStyle("-fx-alignment: center-left;");

        Button btnGenGraphAction = UIComponents.buildButton("🏙 Generate Synthesized Graph", "#303F9F");
        btnGenGraphAction.setOnAction(e -> {
            if (onGenerateGraph != null) onGenerateGraph.run();
        });

        lblGenAgents = new Label("Entity Profiles: " + genAgentCount);
        lblGenAgents.setStyle(UIComponents.BASE_LABEL_STYLE);

        Button btnGenAgentsMinus = UIComponents.createSmallButton("−");
        Button btnGenAgentsPlus = UIComponents.createSmallButton("+");

        btnGenAgentsMinus.setOnAction(e -> {
            if (genAgentCount > 1) {
                genAgentCount--;
                updateAgentCountLabel();
            }
        });
        btnGenAgentsPlus.setOnAction(e -> {
            if (genAgentCount < 100) {
                genAgentCount++;
                updateAgentCountLabel();
            }
        });

        HBox genAgentsBox = new HBox(6, btnGenAgentsMinus, lblGenAgents, btnGenAgentsPlus);
        genAgentsBox.setStyle("-fx-alignment: center-left;");

        Button btnSpawnAgentsAction = UIComponents.buildButton("👥 Spawn Agent Clusters", "#00796B");
        btnSpawnAgentsAction.setOnAction(e -> {
            if (onSpawnAgents != null) onSpawnAgents.run();
        });

        getChildren().addAll(lblGenSection, genNodesBox, btnGenGraphAction, genAgentsBox, btnSpawnAgentsAction);
    }

    private void updateGridNodeLabel() {
        lblGenNodes.setText("Grid Map: " + genGridSide + "x" + genGridSide + " (" + (genGridSide * genGridSide) + " vertices)");
    }

    private void updateAgentCountLabel() {
        lblGenAgents.setText("Entity Profiles: " + genAgentCount);
    }

    public int getGenGridSide() { return genGridSide; }
    public int getGenAgentCount() { return genAgentCount; }
}