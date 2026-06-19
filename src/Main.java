import javax.swing.*;

public static void main(String[] args) {
    SwingUtilities.invokeLater(() -> {
        TrainNetworkGUI gui = new TrainNetworkGUI();
        gui.setLocationRelativeTo(null); // فتح النافذة في منتصف الشاشة
        gui.setVisible(true);
    });
}