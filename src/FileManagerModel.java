import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import javax.swing.*;

public class FileManagerModel {

    private Path rootDirectory;
    private final Map<String, Set<Tag>> fileTags; // Maps file names to their tags
    private WatchService watchService;
    private ExecutorService executorService;
    private final FileManagerController controller;

    public FileManagerModel(FileManagerController controller) {
        this.controller = controller;
        fileTags = new HashMap<>();
    }

    public void setRootDirectory(Path path) {
        this.rootDirectory = path;

        if (!Files.exists(rootDirectory)) {
            try {
                Files.createDirectories(rootDirectory);
            } catch (IOException e) {
                showError("Unable to create root directory: " + e.getMessage());
                System.exit(1);
            }
        }
        loadFiles();
        startWatchingDirectory();
    }

    public void loadFiles() {
        fileTags.clear();
        try {
            Files.list(rootDirectory)
                    .filter(p -> !p.toString().endsWith(".meta"))
                    .forEach(p -> {
                        String fileName = p.getFileName().toString();
                        fileTags.put(fileName, loadTags(p));
                    });
        } catch (IOException e) {
            showError("Error reading directory: " + e.getMessage());
        }
        controller.updateFileList(fileTags);
    }

    private Set<Tag> loadTags(Path filePath) {
        Path metaFile = getMetaFilePath(filePath);
        Set<Tag> tags = new HashSet<>();
        if (Files.exists(metaFile)) {
            try {
                List<String> lines = Files.readAllLines(metaFile);
                for (String line : lines) {
                    Tag tag = Tag.fromString(line);
                    if (tag != null) {
                        tags.add(tag);
                    }
                }
            } catch (IOException e) {
                showError("Error reading metadata: " + e.getMessage());
            }
        }
        return tags;
    }

    public void saveTags(String fileName, Set<Tag> tags) {
        Path filePath = rootDirectory.resolve(fileName);
        Path metaFile = getMetaFilePath(filePath);
        try {
            List<String> lines = new ArrayList<>();
            for (Tag tag : tags) {
                lines.add(tag.toString());
            }
            Files.write(metaFile, lines);
        } catch (IOException e) {
            showError("Error writing metadata: " + e.getMessage());
        }
    }

    private Path getMetaFilePath(Path filePath) {
        return filePath.resolveSibling(filePath.getFileName() + ".meta");
    }

    public void addTag(String fileName, Tag tag) {
        Set<Tag> tags = fileTags.getOrDefault(fileName, new HashSet<>());
        tags.add(tag);
        fileTags.put(fileName, tags);
        saveTags(fileName, tags);
        controller.updateFileList(fileTags);
    }

    public void removeTag(String fileName, Tag tag) {
        Set<Tag> tags = fileTags.get(fileName);
        if (tags != null) {
            tags.remove(tag);
            saveTags(fileName, tags);
            controller.updateFileList(fileTags);
        }
    }

    public void deleteFile(String fileName) {
        Path filePath = rootDirectory.resolve(fileName);
        Path metaFile = getMetaFilePath(filePath);
        try {
            Files.deleteIfExists(filePath);
            Files.deleteIfExists(metaFile);
            fileTags.remove(fileName);
            controller.updateFileList(fileTags);
        } catch (IOException e) {
            showError("Error deleting file: " + e.getMessage());
        }
    }

    public void addFile(Path sourcePath) {
        Path targetPath = rootDirectory.resolve(sourcePath.getFileName());
        try {
            Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            // Initialize empty tags for the new file
            fileTags.put(targetPath.getFileName().toString(), new HashSet<>());
            controller.updateFileList(fileTags);
        } catch (IOException e) {
            showError("Error adding file: " + e.getMessage());
        }
    }

    public void startWatchingDirectory() {
        executorService = Executors.newSingleThreadExecutor();
        try {
            watchService = FileSystems.getDefault().newWatchService();
            rootDirectory.register(watchService,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_DELETE,
                    StandardWatchEventKinds.ENTRY_MODIFY);

            executorService.submit(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        WatchKey key = watchService.take();
                        boolean refreshNeeded = false;
                        for (WatchEvent<?> event : key.pollEvents()) {
                            WatchEvent.Kind<?> kind = event.kind();
                            if (kind == StandardWatchEventKinds.OVERFLOW) continue;
                            refreshNeeded = true;
                        }
                        if (refreshNeeded) {
                            SwingUtilities.invokeLater(this::loadFiles);
                        }
                        boolean valid = key.reset();
                        if (!valid) break;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    try {
                        watchService.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });

        } catch (IOException e) {
            showError("Error watching directory: " + e.getMessage());
        }
    }

    public void stopWatchingDirectory() {
        executorService.shutdownNow();
    }

    public Path getRootDirectory() {
        return rootDirectory;
    }

    public Map<String, Set<Tag>> getFileTags() {
        return fileTags;
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(null, message);
    }
}
