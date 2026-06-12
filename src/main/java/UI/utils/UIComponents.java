package UI.utils;

import javafx.scene.control.Button;

/**
 * Utility factory for constructing consistently styled JavaFX UI elements 
 * across the simulation control dashboard environment.
 */
public final class UIComponents {

    private UIComponents() {
        // Prevent instantiation of utility class
    }

    /**
     * Builds a standardized full-width control button with a custom background color.
     *
     * @param text     the visual descriptive text label for the button
     * @param hexColor the hex color string representing the background fill
     * @return a fully configured, styled JavaFX Button object instance
     */
    public static Button buildButton(String text, String hexColor) {
        Button btn = new Button(text);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setStyle(getButtonStyle(hexColor));
        return btn;
    }

    /**
     * Builds a compact, standardized utility increment/decrement button.
     *
     * @param text the symbol text (e.g., "+" or "−")
     * @return a fully configured, compact JavaFX Button object instance
     */
    public static Button createSmallButton(String text) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 13px; -fx-padding: 2 8 2 8; -fx-background-color: #3E3E42; "
                   + "-fx-text-fill: #E0E0E0; -fx-background-radius: 4; -fx-cursor: hand;");
        return btn;
    }

    /**
     * Generates the standard FX inline style layout string matching the theme matrix.
     *
     * @param hexColor the hex color configuration string
     * @return the inline CSS styling string representation
     */
    public static String getButtonStyle(String hexColor) {
        return "-fx-background-color: " + hexColor
             + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 12px; "
             + "-fx-padding: 7 10 7 10; -fx-background-radius: 4; -fx-cursor: hand;";
    }
}