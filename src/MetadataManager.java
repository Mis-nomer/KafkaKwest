import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class MetadataManager {
    private final DisplayManager displayManager;

    public MetadataManager(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    public void createMetadata() {
        String projectDir = displayManager.getDirectoryField().getText();
        if (projectDir.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Error: Project directory not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File dir = new File(projectDir);
        if (!dir.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Error: '" + projectDir + "' is not a valid directory", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File metadataFile = new File(dir, "metadata.yaml");
        if (metadataFile.exists()) {
            JOptionPane.showMessageDialog(null, "Error: metadata.yaml already exists in " + projectDir, "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String name = JOptionPane.showInputDialog("Project Name:");
        String type = JOptionPane.showInputDialog("Project Type:");
        String status = JOptionPane.showInputDialog("Status (hiatus/active/inactive/abandoned):");
        String created = JOptionPane.showInputDialog("First Created (YYYY-MM-DD):");
        String updated = JOptionPane.showInputDialog("Last Updated (YYYY-MM-DD):");
        String priority = JOptionPane.showInputDialog("Priority (1-5):");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
            writer.write("Name: " + name + "\n");
            writer.write("Type: " + type + "\n");
            writer.write("First Created: " + created + "\n");
            writer.write("Status: " + status + "\n");
            writer.write("Last Updated: " + updated + "\n");
            writer.write("Priority: " + priority + "\n");
            JOptionPane.showMessageDialog(null, "metadata.yaml created in " + projectDir, "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error writing metadata.yaml: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void updateMetadataFile(File metadataFile, String fieldName, String newValue) {
        try {
            // Read the existing content
            List<String> lines = Files.readAllLines(metadataFile.toPath());
            Map<String, String> metadata = new HashMap<>();
            for (String line : lines) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    metadata.put(parts[0].toLowerCase().trim(), parts[1].trim());
                }
            }

            // Update the specific field
            metadata.put(fieldName.toLowerCase(), newValue);

            // Write the updated content back to the file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(metadataFile))) {
                for (Map.Entry<String, String> entry : metadata.entrySet()) {
                    writer.write(entry.getKey().substring(0, 1).toUpperCase() + entry.getKey().substring(1) + ": " + entry.getValue() + "\n");
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error updating metadata.yaml: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
