package library;

import javax.swing.*;

/** Application entry point. Run this class from IntelliJ. */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) { }
            LibraryService service = new LibraryService("data");
            new LoginFrame(service).setVisible(true);
        });
    }
}
