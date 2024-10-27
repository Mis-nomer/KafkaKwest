import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;

public class FileManagerView extends JFrame {

    private final JTextField searchField;
    private final JList<String> fileList;
    private final DefaultListModel<String> listModel;

    public FileManagerView(FileManagerController controller) {
        super("File Manager App");

        // Initialize components
        searchField = new JTextField(30);
        listModel = new DefaultListModel<>();
        fileList = new JList<>(listModel);

        // Set up layout
        JPanel topPanel = new JPanel(new FlowLayout());
        topPanel.add(new JLabel("Search:"));
        topPanel.add(searchField);

        JButton changeDirButton = new JButton("Change Directory");
        JButton addFileButton = new JButton("Add File");
        JButton addTagButton = new JButton("Add Tag");
        JButton addSubtagButton = new JButton("Add Subtag");
        JButton removeTagButton = new JButton("Remove Tag");
        JButton deleteFileButton = new JButton("Delete File");

        topPanel.add(changeDirButton);
        topPanel.add(addFileButton);
        topPanel.add(addTagButton);
        topPanel.add(addSubtagButton);
        topPanel.add(removeTagButton);
        topPanel.add(deleteFileButton);

        add(topPanel, BorderLayout.NORTH);
        add(new JScrollPane(fileList), BorderLayout.CENTER);

        // Add action listeners
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { controller.filterFiles(); }
            public void removeUpdate(DocumentEvent e) { controller.filterFiles(); }
            public void changedUpdate(DocumentEvent e) { controller.filterFiles(); }
        });

        changeDirButton.addActionListener(_ -> controller.changeDirectory());
        addFileButton.addActionListener(_ -> controller.addFile());
        addTagButton.addActionListener(_ -> controller.addTag());
        addSubtagButton.addActionListener(_ -> controller.addSubtag());
        removeTagButton.addActionListener(_ -> controller.removeTag());
        deleteFileButton.addActionListener(_ -> controller.deleteFile());

        // Set up the frame
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 500);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public String getSearchText() {
        return searchField.getText().trim().toLowerCase();
    }

    public void updateFileList(java.util.List<String> files) {
        SwingUtilities.invokeLater(() -> {
            listModel.clear();
            files.forEach(listModel::addElement);
        });
    }

    public String getSelectedFile() {
        return fileList.getSelectedValue();
    }

    public void showError(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    public String promptForInput(String message) {
        return JOptionPane.showInputDialog(this, message);
    }
}
