import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

class DisplayManager {
    private final JPanel mainPanel;
    private final JTextField directoryField;
    private final DefaultTableModel tableModel;
    private final JButton searchButton;
    private final JButton createButton;

    // Define Gruvbox color palette
    private final Color backgroundColor = new Color(40, 40, 40); // Dark background
    private final Color foregroundColor = new Color(235, 219, 178); // Light text color
    private final Color accentColor = new Color(250, 189, 47); // Yellow accent
    private final Color buttonColor = new Color(204, 36, 29); // Red for buttons

    public DisplayManager(ProjectManager projectManager) {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(backgroundColor); // Set background color

        // Input panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(backgroundColor); // Set background color
        inputPanel.setBorder(new EmptyBorder(10, 10, 10, 10)); // Add padding

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Directory input
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(createLabel(), gbc);

        directoryField = createTextField();
        gbc.gridx = 1;
        gbc.gridy = 0;
        inputPanel.add(directoryField, gbc);

        JButton browseButton = createButton("Browse...");
        browseButton.addActionListener(_ -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int option = fileChooser.showOpenDialog(projectManager);
            if (option == JFileChooser.APPROVE_OPTION) {
                directoryField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });
        gbc.gridx = 2;
        gbc.gridy = 0;
        inputPanel.add(browseButton, gbc);

        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(backgroundColor);
        searchButton = createButton("Search Projects");
        createButton = createButton("Create Metadata");
        buttonPanel.add(searchButton);
        buttonPanel.add(createButton);
        mainPanel.add(buttonPanel, BorderLayout.CENTER);

        // Table setup
        String[] columnNames = {"Project", "Name", "Type", "Status", "Priority", "Created", "Updated"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column != 0; // Make all cells except the first column editable
            }
        };
        JTable table = new JTable(tableModel);
        table.setBackground(backgroundColor);
        table.setForeground(foregroundColor);
        table.setFont(new Font("Consolas", Font.PLAIN, 12));
        // Grey-brown for headers
        Color tableHeaderColor = new Color(146, 131, 116);
        table.getTableHeader().setBackground(tableHeaderColor);
        table.getTableHeader().setForeground(foregroundColor);
        table.getTableHeader().setFont(new Font("Consolas", Font.BOLD, 14));
        table.setGridColor(accentColor);

        JScrollPane scrollPane = new JScrollPane(table);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Listener for table model changes
        tableModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int column = e.getColumn();
                String projectName = (String) tableModel.getValueAt(row, 0); // Get the project name
                String columnName = tableModel.getColumnName(column); // Get the column name
                String newValue = (String) tableModel.getValueAt(row, column); // Get the new value

                // Inform the project manager about the change
                projectManager.updateMetadata(projectName, columnName, newValue);
            }
        });
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JButton getSearchButton() {
        return searchButton;
    }

    public JButton getCreateButton() {
        return createButton;
    }

    public JTextField getDirectoryField() {
        return directoryField;
    }

    public DefaultTableModel getTableModel() {
        return tableModel;
    }

    private JLabel createLabel() {
        JLabel label = new JLabel("Directory:");
        label.setForeground(foregroundColor);
        label.setFont(new Font("Roboto", Font.BOLD, 14));
        return label;
    }

    private JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setBackground(backgroundColor);
        textField.setForeground(foregroundColor);
        textField.setBorder(BorderFactory.createLineBorder(accentColor, 1));
        textField.setFont(new Font("Arial", Font.PLAIN, 14));
        return textField;
    }

    private JButton createButton(String text) {
        JButton button = new JButton(text);
        button.setBackground(buttonColor);
        button.setForeground(foregroundColor);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBorder(BorderFactory.createLineBorder(accentColor, 2));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setOpaque(true);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accentColor, 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)));
        return button;
    }
}
