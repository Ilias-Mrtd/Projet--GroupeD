module ProjetGroupeD {
    requires javafx.controls;
    requires javafx.fxml;
    requires transitive javafx.graphics;

    opens UI.renderers to javafx.fxml;

    exports app;
    exports UI;
    exports UI.renderers;
    exports controllers;
    exports simulationEngine;
    exports model.graph;
    exports model.agents;
}