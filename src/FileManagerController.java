import javax.swing.*;
import java.awt.*;
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
        Map<String, Set<Tag>> filteredFiles = fileTags.entrySet().stream()
                .filter(entry -> {
                    String fileName = entry.getKey().toLowerCase();
                    return fileName.contains(searchText) || containsTag(entry.getValue(), searchText);
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
            String tagName = view.promptForInput("Enter tag name:");
            if (tagName != null && !tagName.trim().isEmpty()) {
                Color tagColor = view.promptForColor(Color.GRAY);
                Tag tag = new Tag(tagName.trim(), tagColor != null ? tagColor : Color.GRAY);
                model.addTag(selectedFile, tag);
                view.updateSidebar(selectedFile, model.getFileTags().get(selectedFile));
            }
        } else {
            view.showError("Please select a file to add a tag.");
        }
    }
    public void addSubtag() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            Set<Tag> tags = model.getFileTags().get(selectedFile);
            if (tags != null && !tags.isEmpty()) {
                Tag parentTag = view.getSelectedTag();
                if (parentTag != null) {
                    String subtagName = view.promptForInput("Enter subtag name:");
                    if (subtagName != null && !subtagName.trim().isEmpty()) {
                        Color tagColor = view.promptForColor(Color.GRAY);
                        Tag subtag = new Tag(subtagName.trim(), tagColor != null ? tagColor : Color.GRAY);
                        parentTag.addSubtag(subtag);
                        model.saveTags(selectedFile, tags);
                        view.updateSidebar(selectedFile, tags);
                    }
                } else {
                    view.showError("Please select a parent tag from the tree.");
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
            Set<Tag> tags = model.getFileTags().get(selectedFile);
            if (tags != null && !tags.isEmpty()) {
                Tag tagToRemove = view.getSelectedTag();
                if (tagToRemove != null) {
                    boolean removed = removeTagFromSet(tags, tagToRemove);
                    if (removed) {
                        model.saveTags(selectedFile, tags);
                        view.updateSidebar(selectedFile, tags);
                    } else {
                        view.showError("Tag could not be removed.");
                    }
                } else {
                    view.showError("Please select a tag to remove from the tree.");
                }
            } else {
                view.showError("No tags available to remove.");
            }
        } else {
            view.showError("Please select a file to remove a tag.");
        }
    }

    private boolean removeTagFromSet(Set<Tag> tags, Tag tagToRemove) {
        Iterator<Tag> iterator = tags.iterator();
        while (iterator.hasNext()) {
            Tag tag = iterator.next();
            if (tag.equals(tagToRemove)) {
                iterator.remove();
                return true;
            } else {
                boolean removed = removeTagFromSet(tag.getSubtags(), tagToRemove);
                if (removed) {
                    return true;
                }
            }
        }
        return false;
    }
    public void deleteFile() {
        String selectedFile = view.getSelectedFile();
        if (selectedFile != null) {
            int confirm = JOptionPane.showConfirmDialog(view, "Are you sure you want to delete this file?", "Confirm Delete", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                model.deleteFile(selectedFile);
                view.setSelectedFileName(null);
                view.clearSidebar();
            }
        } else {
            view.showError("Please select a file to delete.");
        }
    }


    public FileManagerModel getModel() {
        return model;
    }
}
