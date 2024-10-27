import javax.swing.SwingUtilities;

public class FileManagerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(FileManagerController::new);
    }
}
