package UI.utils;

import javafx.scene.control.Button;

/**
 * Utility factory for constructing consistently styled JavaFX UI elements 
 * and centralizing application theme matrix styles.
 */
public final class UIComponents {

    // Centralized Design System Constants
    public static final String PANEL_BACKGROUND = "-fx-background-color: #252526; -fx-border-color: #3E3E42; -fx-border-width: 0 0 0 1;";
    public static final String INSPECTOR_SCROLL_BG = "-fx-background: #1E1E1E;";
    
    public static final String TITLE_LABEL_STYLE = "-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #00E5FF;";
    public static final String SECTION_TITLE_STYLE = "-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #E0E0E0;";
    public static final String BASE_LABEL_STYLE = "-fx-font-size: 12px; -fx-text-fill: #CCCCCC;";
    public static final String CHKBOX_STYLE = "-fx-text-fill: #CCCCCC;";
    
    public static final String DETAILS_TEXT_STYLE = "-fx-font-size: 13px; -fx-text-fill: #CCCCCC; -fx-padding: 5px;";
    public static final String MONO_LOG_STYLE = "-fx-font-size: 11px; -fx-font-family: monospace;";
    public static final String MONO_SCOREBOARD_STYLE = "-fx-font-size: 12px; -fx-font-family: monospace; -fx-text-fill: #00E5FF;";
    
    public static final String SMALL_BUTTON_STYLE = "-fx-font-size: 13px; -fx-padding: 2 8 2 8; -fx-background-color: #3E3E42; -fx-text-fill: #E0E0E0; -fx-background-radius: 4; -fx-cursor: hand;";

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
        btn.setStyle(SMALL_BUTTON_STYLE);
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