package UI;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

import model.graph.*;
import model.agents.Agent;

import java.util.List;

public class PropertiesPanel extends VBox {
    private final Graph graph;
    private final List<Agent> agents;

    private final Label titleLabel;
    private final Label infoLabel;

    public PropertiesPanel(Graph graph, List<Agent> agents) {
        this.graph = graph;
        this.agents = agents;

        // --- Style du panneau ---
        this.setPadding(new Insets(15));
        this.setSpacing(10);
        this.setPrefWidth(250); // Largeur fixe sur le côté droit
        this.setStyle("-fx-background-color: #FAFAFA; -fx-border-color: #E0E0E0; -fx-border-width: 0 0 0 1;");

        // --- Initialisation des textes ---
        titleLabel = new Label("Inspecteur");
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #333;");

        infoLabel = new Label("Cliquez sur un élément\npour voir ses détails.");
        infoLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #555;");
        infoLabel.setWrapText(true); // Permet le retour à la ligne automatique

        // On ajoute les textes à la VBox
        this.getChildren().addAll(titleLabel, infoLabel);
    }

    /**
     * Méthode appelée 60 fois par seconde par le moteur
     */
    public void refresh() {
        Object selectedItem = findSelectedItem();

        if (selectedItem instanceof Agent) {
            Agent a = (Agent) selectedItem;
            String texte = "Type : Agent\n";
            texte += "État : " + a.state + "\n";

            if (a.currentEdge != null) {
                texte += "Trajet : " + a.currentNode.id + " ➔ " + a.destination.id + "\n";
                texte += "Sur l'arete : " + a.currentEdge.id;
                texte += "Vitesse : " + a.speed + " px/s";
            } else if (a.currentNode != null) {
                texte += "Position : Sur le nœud " + a.currentNode.id;
            }
            infoLabel.setText(texte);

        } else if (selectedItem instanceof Node) {
            Node n = (Node) selectedItem;
            infoLabel.setText("Type : Nœud\nNom : " + n.id + "\nPosition : (" + n.x + " ; " + n.y + ")");

        } else if (selectedItem instanceof Edge) {
            Edge e = (Edge) selectedItem;
            String s;
            if (e.direction == true) {
                s = " <--> ";
            } else {
                s = " --> ";
            }
            infoLabel.setText("Type : Arête\nConnexion : " + e.source.id + s + e.target.id + "\nLongueur : "
                    + Math.floor(e.length) + " px");

        } else {
            infoLabel.setText("Cliquez sur un élément\npour voir ses détails.");
        }
    }

    private Object findSelectedItem() {
        for (Agent a : agents)
            if (a.isSelected)
                return a;
        for (Node n : graph.Nodes)
            if (n.isSelected)
                return n;
        for (List<Edge> edges : graph.Edges) {
            for (Edge e : edges)
                if (e.isSelected)
                    return e;
        }
        return null;
    }
}