import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.*;
import javax.swing.tree.*;
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

public class FileManagerView extends JFrame {

    public JList<Object> tagList;
    private final JTextField searchField;
    private final JPanel fileGridPanel;
    private final JPanel sidebarPanel;
    private final JButton changeDirButton;
    private final FileManagerController controller;
    private final JLabel fileInfoLabel;
    private final JLabel fileThumbnailLabel;
    protected JTree tagTree;
    private final DefaultTreeModel tagTreeModel;
    private final JButton addTagButton;
    private final JButton removeTagButton;
    private final JButton addSubtagButton;
    private final JButton deleteFileButton;

    private String selectedFileName;

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

        JScrollPane fileScrollPane = new JScrollPane(fileGridPanel);
        fileScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        fileScrollPane.setBorder(null);

        // Initialize sidebar components
        sidebarPanel = new JPanel();
        sidebarPanel.setLayout(new BoxLayout(sidebarPanel, BoxLayout.Y_AXIS));
        sidebarPanel.setPreferredSize(new Dimension(300, getHeight()));
        sidebarPanel.setBackground(new Color(59, 66, 82));
        sidebarPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        fileThumbnailLabel = new JLabel();
        fileThumbnailLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        fileInfoLabel = new JLabel();
        fileInfoLabel.setForeground(foregroundColor);
        fileInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Initialize tag tree
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tags");
        tagTreeModel = new DefaultTreeModel(rootNode);
        tagTree = new JTree(tagTreeModel);
        tagTree.setBackground(new Color(59, 66, 82));
        tagTree.setForeground(foregroundColor);
        tagTree.setCellRenderer(new TagTreeCellRenderer());
        tagTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        addTagButton = createSidebarButton("Add Tag");
        addSubtagButton = createSidebarButton("Add Subtag");
        removeTagButton = createSidebarButton("Remove Tag");
        deleteFileButton = createSidebarButton("Delete File");

        changeDirButton = createButton("Change Directory");
        JButton addFileButton = createButton("Add File");

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
        JScrollPane treeScrollPane = new JScrollPane(tagTree);
        treeScrollPane.setPreferredSize(new Dimension(250, 150));
        treeScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        treeScrollPane.setBackground(new Color(59, 66, 82));
        treeScrollPane.setBorder(null);
        sidebarPanel.add(treeScrollPane);
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

    public void setChangeDirButtonListener(ActionListener listener) {
        changeDirButton.addActionListener(listener);
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
        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tags");
        tagTreeModel.setRoot(rootNode);
        tagTreeModel.reload();
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
                Image scaledImg = getScaledImage(img, 80, 80);
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
        nameLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JLabel tagsLabel = new JLabel(tagsToString(tags));
        tagsLabel.setForeground(new Color(136, 192, 208)); // Tag color
        tagsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        tagsLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));

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
                Image scaledImg = getScaledImage(img, 200, 200);
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

            // Update tag tree
            DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode("Tags");
            for (Tag tag : tags) {
                DefaultMutableTreeNode tagNode = createTagTreeNode(tag);
                rootNode.add(tagNode);
            }
            tagTreeModel.setRoot(rootNode);
            tagTreeModel.reload();
            expandAllNodes(tagTree, 0, tagTree.getRowCount());

            // Enable sidebar buttons
            setSidebarButtonsEnabled(true);

            // Revalidate and repaint the sidebar
            sidebarPanel.revalidate();
            sidebarPanel.repaint();
        });
    }

    private DefaultMutableTreeNode createTagTreeNode(Tag tag) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(tag);
        for (Tag subtag : tag.getSubtags()) {
            node.add(createTagTreeNode(subtag));
        }
        return node;
    }

    // Helper method to expand all nodes in the JTree
    private void expandAllNodes(JTree tree, int startingIndex, int rowCount){
        for(int i=startingIndex;i<rowCount;++i){
            tree.expandRow(i);
        }

        if(tree.getRowCount()!=rowCount){
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    public String getSelectedFile() {
        return selectedFileName;
    }

    public Tag getSelectedTag() {
        TreePath path = tagTree.getSelectionPath();
        if (path != null) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
            if (node.getUserObject() instanceof Tag) {
                return (Tag) node.getUserObject();
            }
        }
        return null;
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
        // Customize icons based on the file extension
        String iconText = extension != null ? extension.toUpperCase() : "FILE";
        JLabel label = new JLabel(iconText, SwingConstants.CENTER);
        label.setForeground(foregroundColor);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        label.setPreferredSize(new Dimension(80, 80));
        label.setBackground(new Color(76, 86, 106));
        label.setOpaque(true);

        // Set a larger font size for better readability
        label.setFont(new Font("SansSerif", Font.BOLD, 24));

        return new ImageIcon(labelToImage(label));
    }

    private Image labelToImage(JLabel label) {
        // Calculate the preferred size of the label
        Dimension size = label.getPreferredSize();
        // Set the size of the label to its preferred size
        label.setSize(size);

        // Create a BufferedImage with the label's dimensions
        BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2 = img.createGraphics();

        // Apply rendering hints for text antialiasing
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Paint the label onto the BufferedImage
        label.paint(g2);
        g2.dispose();
        return img;
    }

    private Image getScaledImage(BufferedImage srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        // Apply rendering hints for quality
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.drawImage(srcImg, 0, 0, w, h, null);
        g2.dispose();

        return resizedImg;
    }

    // Custom cell renderer for the tag tree to display tags with colors
    private class TagTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                      boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            label.setOpaque(true);
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            if (node.getUserObject() instanceof Tag tag) {
                label.setText(tag.getName());
                label.setBackground(tag.getColor());
                label.setForeground(foregroundColor);
            } else {
                // Root node
                label.setBackground(new Color(59, 66, 82));
                label.setForeground(foregroundColor);
            }
            if (sel) {
                label.setBorder(BorderFactory.createLineBorder(accentColor));
            } else {
                label.setBorder(null);
            }
            return label;
        }
    }
}
