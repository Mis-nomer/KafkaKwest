import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.imageio.ImageIO;
import java.util.stream.Collectors;

// ... [imports remain the same]

public class FileManagerView extends JFrame {

    private JTextField searchField;
    private JPanel fileGridPanel;
    private JScrollPane fileScrollPane;
    private JPanel sidebarPanel;
    private JButton changeDirButton;
    private JButton addFileButton;
    private FileManagerController controller;
    private JLabel fileInfoLabel;
    private JLabel fileThumbnailLabel;
    protected JList<Tag> tagList; // Made protected for controller access
    private DefaultListModel<Tag> tagListModel;
    private JButton addTagButton;
    private JButton removeTagButton;
    private JButton addSubtagButton;
    private JButton deleteFileButton;

    private String selectedFileName;

    // Define theme colors (Nord/Tokyo Night Storm)
    private final Color backgroundColor = new Color(46, 52, 64); // Dark background
    private final Color foregroundColor = new Color(216, 222, 233); // Light foreground
    private final Color accentColor = new Color(94, 129, 172); // Accent color

    public FileManagerView(FileManagerController controller) {
        super("File Manager App");
        this.controller = controller;

        // Initialize components
        searchField = new JTextField(30);
        searchField.setBackground(new Color(59, 66, 82));
        searchField.setForeground(foregroundColor);
        searchField.setBorder(BorderFactory.createLineBorder(accentColor));

        fileGridPanel = new JPanel(new WrapLayout(FlowLayout.LEFT, 10, 10));
        fileGridPanel.setBackground(backgroundColor);

        fileScrollPane = new JScrollPane(fileGridPanel);
        fileScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        fileScrollPane.setBorder(null);

        // Initialize sidebar components
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(300, getHeight()));
        sidebarPanel.setBackground(new Color(59, 66, 82));
        sidebarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        // Do not set sidebarPanel to invisible
        // sidebarPanel.setVisible(false);

        fileThumbnailLabel = new JLabel();
        fileThumbnailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileInfoLabel = new JLabel();
        fileInfoLabel.setForeground(foregroundColor);
        fileInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        tagListModel = new DefaultListModel<>();
        tagList = new JList<>(tagListModel);
        tagList.setCellRenderer(new TagListCellRenderer());
        tagList.setBackground(new Color(59, 66, 82));
        tagList.setForeground(foregroundColor);
        tagList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tagList.setAlignmentX(Component.LEFT_ALIGNMENT);

        addTagButton = createSidebarButton("Add Tag");
        addSubtagButton = createSidebarButton("Add Subtag");
        removeTagButton = createSidebarButton("Remove Tag");
        deleteFileButton = createSidebarButton("Delete File");

        changeDirButton = createButton("Change Directory");
        addFileButton = createButton("Add File");

        // Set up layout
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(backgroundColor);
        topPanel.add(new JLabel("Search:") {{
            setForeground(foregroundColor);
        }});
        topPanel.add(searchField);
        topPanel.add(changeDirButton);
        topPanel.add(addFileButton);

        // Assemble sidebar
        sidebarPanel.add(fileThumbnailLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(fileInfoLabel);
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(new JLabel("Tags:") {{
            setForeground(foregroundColor);
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }});
        sidebarPanel.add(new JScrollPane(tagList) {{
            setPreferredSize(new Dimension(250, 100));
            setAlignmentX(Component.LEFT_ALIGNMENT);
            setBackground(new Color(59, 66, 82));
            setBorder(null);
        }});
        sidebarPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebarPanel.add(addTagButton);
        sidebarPanel.add(addSubtagButton);
        sidebarPanel.add(removeTagButton);
        sidebarPanel.add(deleteFileButton);

        // Adjust JSplitPane to show both file panel and sidebar
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, fileScrollPane, sidebarPanel);
        splitPane.setDividerLocation(600);
        splitPane.setDividerSize(2);
        splitPane.setBorder(null);

        add(topPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        // Add action listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { controller.filterFiles(); }
            public void removeUpdate(DocumentEvent e) { controller.filterFiles(); }
            public void changedUpdate(DocumentEvent e) { controller.filterFiles(); }
        });

        changeDirButton.addActionListener(e -> controller.changeDirectory());
        addFileButton.addActionListener(e -> controller.addFile());
        addTagButton.addActionListener(e -> controller.addTag());
        addSubtagButton.addActionListener(e -> controller.addSubtag());
        removeTagButton.addActionListener(e -> controller.removeTag());
        deleteFileButton.addActionListener(e -> controller.deleteFile());

        // Initially disable sidebar buttons
        setSidebarButtonsEnabled(false);

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setVisible(true);

        // Apply theme colors
        applyTheme();
    }

    private void setSidebarButtonsEnabled(boolean enabled) {
        addTagButton.setEnabled(enabled);
        addSubtagButton.setEnabled(enabled);
        removeTagButton.setEnabled(enabled);
        deleteFileButton.setEnabled(enabled);
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(accentColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        return button;
    }

    private JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.LEFT_ALIGNMENT);
        button.setBackground(accentColor);
        button.setForeground(foregroundColor);
        button.setFocusPainted(false);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, button.getPreferredSize().height));
        return button;
    }

    private void applyTheme() {
        getContentPane().setBackground(backgroundColor);
    }

    public String getSearchText() {
        return searchField.getText().trim().toLowerCase();
    }

    public void updateFileList(Map<String, Set<Tag>> fileTags) {
        SwingUtilities.invokeLater(() -> {
            fileGridPanel.removeAll();
            selectedFileName = null;
            clearSidebar(); // Clear the sidebar when the file list is updated

            for (Map.Entry<String, Set<Tag>> entry : fileTags.entrySet()) {
                String fileName = entry.getKey();
                Set<Tag> tags = entry.getValue();

                JPanel filePanel = createFilePanel(fileName, tags);
                fileGridPanel.add(filePanel);
            }
            fileGridPanel.revalidate();
            fileGridPanel.repaint();
        });
    }

    void clearSidebar() {
        fileThumbnailLabel.setIcon(null);
        fileInfoLabel.setText("");
        tagListModel.clear();
        setSidebarButtonsEnabled(false);
    }

    private JPanel createFilePanel(String fileName, Set<Tag> tags) {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setPreferredSize(new Dimension(100, 100));
        panel.setBackground(new Color(59, 66, 82));
        panel.setBorder(BorderFactory.createLineBorder(accentColor));

        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        Path filePath = controller.getModel().getRootDirectory().resolve(fileName);
        try {
            if (Files.probeContentType(filePath) != null && Files.probeContentType(filePath).startsWith("image")) {
                BufferedImage img = ImageIO.read(filePath.toFile());
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                iconLabel.setIcon(new ImageIcon(scaledImg));
            } else {
                // Use a generic icon with the file extension
                String extension = getFileExtension(fileName);
                iconLabel.setIcon(getFileIcon(extension));
            }
        } catch (Exception e) {
            // Use a generic file icon in case of error
            iconLabel.setIcon(getFileIcon(null));
        }

        JLabel nameLabel = new JLabel(fileName);
        nameLabel.setForeground(foregroundColor);
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel tagsLabel = new JLabel(tagsToString(tags));
        tagsLabel.setForeground(new Color(136, 192, 208)); // Tag color
        tagsLabel.setHorizontalAlignment(SwingConstants.CENTER);

        panel.add(iconLabel, BorderLayout.CENTER);
        panel.add(nameLabel, BorderLayout.NORTH);
        panel.add(tagsLabel, BorderLayout.SOUTH);

        panel.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                selectedFileName = fileName;
                updateSidebar(fileName, tags);
            }
        });

        return panel;
    }

    void updateSidebar(String fileName, Set<Tag> tags) {
        SwingUtilities.invokeLater(() -> {
            // Display larger thumbnail
            Path filePath = controller.getModel().getRootDirectory().resolve(fileName);
            try {
                BufferedImage img;
                if (Files.probeContentType(filePath).startsWith("image")) {
                    img = ImageIO.read(filePath.toFile());
                } else {
                    img = (BufferedImage) labelToImage(new JLabel(getFileExtension(fileName).toUpperCase(), SwingConstants.CENTER));
                }
                Image scaledImg = img.getScaledInstance(200, 200, Image.SCALE_SMOOTH);
                fileThumbnailLabel.setIcon(new ImageIcon(scaledImg));
            } catch (Exception e) {
                fileThumbnailLabel.setIcon(getFileIcon(null));
            }

            // Display file information
            try {
                BasicFileAttributes attrs = Files.readAttributes(filePath, BasicFileAttributes.class);
                long size = attrs.size();
                FileTime modifiedTime = attrs.lastModifiedTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String fileInfo = String.format("<html><b>Name:</b> %s<br><b>Size:</b> %d bytes<br><b>Modified:</b> %s</html>",
                        fileName, size, sdf.format(new Date(modifiedTime.toMillis())));
                fileInfoLabel.setText(fileInfo);
            } catch (IOException e) {
                fileInfoLabel.setText("<html><b>Name:</b> " + fileName + "</html>");
            }

            // Update tag list
            tagListModel.clear();
            for (Tag tag : tags) {
                tagListModel.addElement(tag);
            }

            // Enable sidebar buttons
            setSidebarButtonsEnabled(true);

            // Revalidate and repaint the sidebar
            sidebarPanel.revalidate();
            sidebarPanel.repaint();
        });
    }

    public String getSelectedFile() {
        return selectedFileName;
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public String promptForInput(String message) {
        return JOptionPane.showInputDialog(this, message);
    }

    public Color promptForColor(Color initialColor) {
        return JColorChooser.showDialog(this, "Choose Tag Color", initialColor);
    }

    public void setChangeDirButtonListener(ActionListener listener) {
        changeDirButton.addActionListener(listener);
    }

    public FileManagerController getController() {
        return controller;
    }

    public void setSelectedFileName(String fileName) {
        this.selectedFileName = fileName;
    }

    private String tagsToString(Set<Tag> tags) {
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.joining(", "));
    }

    private String getFileExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return (idx != -1) ? fileName.substring(idx + 1).toLowerCase() : "";
    }

    private Icon getFileIcon(String extension) {
        // You can customize icons based on the file extension
        String iconText = extension != null ? extension.toUpperCase() : "FILE";
        JLabel label = new JLabel(iconText, SwingConstants.CENTER);
        label.setForeground(foregroundColor);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setBackground(new Color(76, 86, 106));
        label.setOpaque(true);
        return new ImageIcon(labelToImage(label));
    }

    private Image labelToImage(JLabel label) {
        // Calculate the preferred size of the label
        Dimension size = label.getPreferredSize();
        // Set the size of the label to its preferred size
        label.setSize(size);

        // Create a BufferedImage with the label's dimensions
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

        // Paint the label onto the BufferedImage
        label.paint(img.getGraphics());
        return img;
    }

    // Custom cell renderer for the tag list to display tags with colors
    private class TagListCellRenderer extends JLabel implements ListCellRenderer<Tag> {
        public TagListCellRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Tag> list, Tag tag, int index, boolean isSelected, boolean cellHasFocus) {
            setText(tag.getName());
            setBackground(tag.getColor());
            setForeground(foregroundColor);

            if (isSelected) {
                setBorder(BorderFactory.createLineBorder(accentColor));
            } else {
                setBorder(null);
            }
            return this;
        }
    }

}
