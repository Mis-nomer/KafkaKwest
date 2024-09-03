import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

class SearchManager {
    private final DisplayManager displayManager;

    public SearchManager(DisplayManager displayManager) {
        this.displayManager = displayManager;
    }

    public void searchProjects() {
        String dir = displayManager.getDirectoryField().getText();

        if (dir.isEmpty()) {
            JOptionPane.showMessageDialog(null, "Error: Directory not specified", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        File directory = new File(dir);
        if (!directory.isDirectory()) {
            JOptionPane.showMessageDialog(null, "Error: '" + dir + "' is not a valid directory", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        displayManager.getTableModel().setRowCount(0); // Clear previous results

        boolean found = false;

        for (File projectDir : Objects.requireNonNull(directory.listFiles(File::isDirectory))) {
            File metadataFile = new File(projectDir, "metadata.yaml");
            if (!metadataFile.exists()) continue;

            Map<String, String> metadata = readMetadata(metadataFile);
            String projectName = projectDir.getName();

            displayManager.getTableModel().addRow(new Object[]{
                    projectName,
                    metadata.get("name"),
                    metadata.get("type"),
                    metadata.get("status"),
                    metadata.get("priority"),
                    metadata.get("first created"),
                    metadata.get("last updated")
            });
            found = true;
        }

        if (!found) {
            JOptionPane.showMessageDialog(null, "No matching projects found in " + dir, "Information", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private Map<String, String> readMetadata(File file) {
        Map<String, String> metadata = new HashMap<>();
        try {
            List<String> lines = Files.readAllLines(Paths.get(file.getPath()));
            for (String line : lines) {
                String[] parts = line.split(": ", 2);
                if (parts.length == 2) {
                    metadata.put(parts[0].toLowerCase().trim(), parts[1].trim());
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Error reading file: " + file.getPath(), "Error", JOptionPane.ERROR_MESSAGE);
        }
        return metadata;
    }
}
