import javax.swing.*;
import java.io.File;

public class ProjectManager extends JFrame {
    private final DisplayManager displayManager;
    private final MetadataManager metadataManager;
    private final SearchManager searchManager;

    public ProjectManager() {
        setTitle("Project Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Initialize Display Manager
        displayManager = new DisplayManager(this);

        // Initialize other managers
        metadataManager = new MetadataManager(displayManager);
        searchManager = new SearchManager(displayManager);

        // Add the display panel
        add(displayManager.getMainPanel());

        // Set up event listeners
        displayManager.getSearchButton().addActionListener(_ -> searchManager.searchProjects());
        displayManager.getCreateButton().addActionListener(_ -> metadataManager.createMetadata());

        pack(); // Adjust frame size to fit the content
    }

    public void updateMetadata(String projectName, String fieldName, String newValue) {
        String directory = displayManager.getDirectoryField().getText();
        if (directory.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Error: Directory not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File projectDir = new File(directory, projectName);
        if (!projectDir.isDirectory()) {
            JOptionPane.showMessageDialog(this, "Error: '" + projectName + "' is not a valid project directory", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File metadataFile = new File(projectDir, "metadata.yaml");
        if (!metadataFile.exists()) {
            JOptionPane.showMessageDialog(this, "Error: metadata.yaml does not exist in " + projectDir, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Update the metadata file
        metadataManager.updateMetadataFile(metadataFile, fieldName, newValue);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ProjectManager::new);
    }
}
