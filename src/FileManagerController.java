import javax.swing.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;

public class FileManagerController {

    private final FileManagerModel model;
    private final FileManagerView view;

    public FileManagerController() {
        model = new FileManagerModel(this);
        view = new FileManagerView(this);

        // Set default root directory
        Path defaultPath = Paths.get(System.getProperty("user.home"), "FileManagerRoot");
        model.setRootDirectory(defaultPath);
    }

    public void updateFileList(Map<String, Set<Tag>> fileTags) {
        String searchText = view.getSearchText();
        List<String> filteredFiles = fileTags.entrySet().stream()
                .filter(entry -> {
                    String fileName = entry.getKey().toLowerCase();
                    return fileName.contains(searchText) || containsTag(entry.getValue(), searchText);
                })
                .map(entry -> entry.getKey() + " [" + tagsToString(entry.getValue()) + "]")
                .collect(Collectors.toList());
        view.updateFileList(filteredFiles);
    }

    private boolean containsTag(Set<Tag> tags, String searchText) {
        for (Tag tag : tags) {
            if (tag.getName().toLowerCase().contains(searchText)) {
                return true;
            }
            if (containsTag(tag.getSubtags(), searchText)) {
                return true;
            }
        }
        return false;
    }

    private String tagsToString(Set<Tag> tags) {
        return tags.stream()
                .map(Tag::toString)
                .collect(Collectors.joining(", "));
    }

    public void filterFiles() {
        updateFileList(model.getFileTags());
    }

    public void changeDirectory() {
        JFileChooser chooser = new JFileChooser(model.getRootDirectory().toFile());
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path selectedPath = chooser.getSelectedFile().toPath();
            model.stopWatchingDirectory();
            model.setRootDirectory(selectedPath);
        }
    }

    public void addFile() {
        JFileChooser chooser = new JFileChooser();
        int result = chooser.showOpenDialog(view);
        if (result == JFileChooser.APPROVE_OPTION) {
            Path sourcePath = chooser.getSelectedFile().toPath();
            model.addFile(sourcePath);
        }
    }

    public void addTag() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            String fileName = extractFileName(selectedFile);
            String tagName = view.promptForInput("Enter tag name:");
            if (tagName != null && !tagName.trim().isEmpty()) {
                Tag tag = new Tag(tagName.trim());
                model.addTag(fileName, tag);
            }
        } else {
            view.showError("Please select a file to add a tag.");
        }
    }

    public void addSubtag() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            String fileName = extractFileName(selectedFile);
            Set<Tag> tags = model.getFileTags().get(fileName);
            if (tags != null && !tags.isEmpty()) {
                String parentTagName = view.promptForInput("Enter parent tag name:");
                if (parentTagName != null && !parentTagName.trim().isEmpty()) {
                    Tag parentTag = findTagByName(tags, parentTagName.trim());
                    if (parentTag != null) {
                        String subtagName = view.promptForInput("Enter subtag name:");
                        if (subtagName != null && !subtagName.trim().isEmpty()) {
                            parentTag.addSubtag(new Tag(subtagName.trim()));
                            model.saveTags(fileName, tags);
                            updateFileList(model.getFileTags());
                        }
                    } else {
                        view.showError("Parent tag not found.");
                    }
                }
            } else {
                view.showError("No tags available to add a subtag.");
            }
        } else {
            view.showError("Please select a file to add a subtag.");
        }
    }

    public void removeTag() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            String fileName = extractFileName(selectedFile);
            Set<Tag> tags = model.getFileTags().get(fileName);
            if (tags != null && !tags.isEmpty()) {
                String tagName = view.promptForInput("Enter tag name to remove:");
                if (tagName != null && !tagName.trim().isEmpty()) {
                    Tag tag = findTagByName(tags, tagName.trim());
                    if (tag != null) {
                        tags.remove(tag);
                        model.saveTags(fileName, tags);
                        updateFileList(model.getFileTags());
                    } else {
                        view.showError("Tag not found.");
                    }
                }
            } else {
                view.showError("No tags available to remove.");
            }
        } else {
            view.showError("Please select a file to remove a tag.");
        }
    }

    public void deleteFile() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this file?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                String fileName = extractFileName(selectedFile);
                model.deleteFile(fileName);
            }
        } else {
            view.showError("Please select a file to delete.");
        }
    }

    private String extractFileName(String displayName) {
        // Remove tags from display name to get the actual file name
        int idx = displayName.indexOf(" [");
        return (idx != -1) ? displayName.substring(0, idx) : displayName;
    }

    private Tag findTagByName(Set<Tag> tags, String name) {
        for (Tag tag : tags) {
            if (tag.getName().equals(name)) {
                return tag;
            }
            Tag subtag = findTagByName(tag.getSubtags(), name);
            if (subtag != null) {
                return subtag;
            }
        }
        return null;
    }
}
