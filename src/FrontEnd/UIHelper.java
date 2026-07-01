package FrontEnd;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerDateModel;
import javax.swing.table.JTableHeader;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/** Shared styling and safe input helpers used by the Swing front end. */
public final class UIHelper {

    public static final Color SIDEBAR_COLOR = new Color(35, 47, 62);
    public static final Color SIDEBAR_ACTIVE_COLOR = new Color(52, 73, 94);
    public static final Color BACKGROUND_COLOR = new Color(245, 247, 250);
    public static final Color WHITE_COLOR = Color.WHITE;
    public static final Color TEXT_COLOR = new Color(33, 37, 41);
    public static final Color SECONDARY_TEXT_COLOR = new Color(100, 110, 120);
    public static final Color BUTTON_COLOR = new Color(41, 128, 185);
    public static final Color SECONDARY_BUTTON_COLOR = new Color(108, 117, 125);
    public static final Color DANGER_COLOR = new Color(192, 57, 43);
    public static final Color BORDER_COLOR = new Color(220, 225, 230);
    public static final Color READ_ONLY_BACKGROUND_COLOR = new Color(248, 249, 250);

    public static final Font TITLE_FONT = new Font("Arial", Font.BOLD, 24);
    public static final Font PAGE_TITLE_FONT = new Font("Arial", Font.BOLD, 28);
    public static final Font SECTION_TITLE_FONT = new Font("Arial", Font.BOLD, 18);
    public static final Font NORMAL_FONT = new Font("Arial", Font.PLAIN, 15);
    public static final Font SMALL_FONT = new Font("Arial", Font.PLAIN, 13);
    public static final Font BUTTON_FONT = new Font("Arial", Font.BOLD, 14);

    public static final int YES_OPTION = JOptionPane.YES_OPTION;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private UIHelper() {
        // Utility class - no objects needed.
    }

    public static JButton createSidebarButton(String text) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(WHITE_COLOR);
        button.setBackground(SIDEBAR_COLOR);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    public static void setSidebarButtonActive(JButton button) {
        button.setBackground(SIDEBAR_ACTIVE_COLOR);
    }

    public static void setSidebarButtonInactive(JButton button) {
        button.setBackground(SIDEBAR_COLOR);
    }

    public static JButton createPrimaryButton(String text) {
        return createButton(text, BUTTON_COLOR);
    }

    public static JButton createSecondaryButton(String text) {
        return createButton(text, SECONDARY_BUTTON_COLOR);
    }

    public static JButton createDangerButton(String text) {
        return createButton(text, DANGER_COLOR);
    }

    private static JButton createButton(String text, Color backgroundColor) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setFont(BUTTON_FONT);
        button.setForeground(WHITE_COLOR);
        button.setBackground(backgroundColor);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        return button;
    }

    public static JPanel createCardPanel() {
        JPanel panel = new JPanel();
        panel.setBackground(WHITE_COLOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(16, 16, 16, 16)
        ));
        return panel;
    }

    public static JTextField createTextField() {
        JTextField textField = new JTextField();
        textField.setFont(NORMAL_FONT);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 205, 210)),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
        return textField;
    }

    public static JTextField createReadOnlyTextField() {
        JTextField textField = createTextField();
        textField.setEditable(false);
        textField.setBackground(READ_ONLY_BACKGROUND_COLOR);
        return textField;
    }

    public static JTextArea createReadOnlyTextArea() {
        JTextArea textArea = new JTextArea();
        textArea.setFont(NORMAL_FONT);
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setBackground(READ_ONLY_BACKGROUND_COLOR);
        textArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        return textArea;
    }

    /**
     * Creates a strict date control. Users can select a date or type yyyy-MM-dd.
     * readDateSpinner() rejects dates that do not exist on the calendar.
     */
    public static JSpinner createDateSpinner() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        spinner.setFont(NORMAL_FONT);

        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        editor.getFormat().setLenient(false);

        JFormattedTextField textField = editor.getTextField();
        textField.setFont(NORMAL_FONT);
        textField.setColumns(10);
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);

        spinner.setEditor(editor);
        setDateSpinnerValue(spinner, LocalDate.now());
        return spinner;
    }

    public static LocalDate readDateSpinner(JSpinner spinner, String fieldName) {
        try {
            spinner.commitEdit();
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                    fieldName + " must be a real date in yyyy-MM-dd format."
            );
        }

        Object value = spinner.getValue();
        if (!(value instanceof Date)) {
            throw new IllegalArgumentException(
                    fieldName + " must be a real date in yyyy-MM-dd format."
            );
        }

        Date date = (Date) value;
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static void setDateSpinnerValue(JSpinner spinner, LocalDate date) {
        Date value = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        spinner.setValue(value);
    }

    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }

    public static LocalDate parseStoredDate(String dateText, String fieldName) {
        try {
            return LocalDate.parse(dateText, DATE_FORMATTER);
        } catch (Exception ex) {
            throw new IllegalArgumentException(
                    fieldName + " is not stored in the expected yyyy-MM-dd format."
            );
        }
    }

    public static void styleTable(JTable table) {
        table.setFont(NORMAL_FONT);
        table.setRowHeight(28);
        table.setGridColor(BORDER_COLOR);
        table.setSelectionBackground(new Color(214, 234, 248));
        table.setSelectionForeground(TEXT_COLOR);
        table.setAutoCreateRowSorter(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(BUTTON_FONT);
        header.setBackground(SIDEBAR_COLOR);
        header.setForeground(WHITE_COLOR);
        header.setReorderingAllowed(false);
    }

    public static void addPadding(JComponent component, int top, int left, int bottom, int right) {
        component.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }

    public static void showSuccessMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void showErrorMessage(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static int showConfirmMessage(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm", JOptionPane.YES_NO_OPTION);
    }
}
