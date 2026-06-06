module ProjetGroupeD {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    // Permissions de réflexion pour JavaFX
    opens application to javafx.graphics, javafx.fxml;
    opens controllers to javafx.graphics, javafx.fxml, javafx.controls;
    opens UI to javafx.graphics, javafx.fxml;
    opens UI.renderers to javafx.fxml, javafx.graphics;

    // Exportations de tes packages
    exports application;
    exports UI;
    exports UI.renderers;
    exports simulationEngine.algorithm;
    exports simulationEngine.engine;
    exports model.graph;
    exports model.agents;
    exports controllers;
}