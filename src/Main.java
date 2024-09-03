import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // Create an instance of the ProjectManager class
            ProjectManager projectManagerApp = new ProjectManager();
            // Make the GUI visible
            projectManagerApp.setVisible(true);
        });
    }
}