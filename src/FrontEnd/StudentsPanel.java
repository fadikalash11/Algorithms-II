package FrontEnd;

import BackEnd.Student;
import BackEnd.StudentRegistry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Small central student-management screen. Students are registered once here;
 * borrowing and waiting-queue screens only look up their Student ID.
 */
public class StudentsPanel extends JPanel {

    private JTable studentsTable;
    private DefaultTableModel tableModel;

    private JTextField registerStudentIdField;
    private JTextField registerStudentNameField;
    private JCheckBox registerGraduatingCheckBox;

    private JTextField updateStudentIdField;
    private JTextField updateStudentNameField;
    private JCheckBox updateGraduatingCheckBox;

    public StudentsPanel() {
        initComponents();
        refreshStudentsTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{"Student ID", "Student Name", "Graduating Student"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        studentsTable = new JTable(tableModel);
        UIHelper.styleTable(studentsTable);
        studentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        studentsTable.getColumnModel().getColumn(0).setPreferredWidth(125);
        studentsTable.getColumnModel().getColumn(1).setPreferredWidth(190);
        studentsTable.getColumnModel().getColumn(2).setPreferredWidth(140);
        studentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedStudentForUpdate();
            }
        });

        JScrollPane tableScrollPane = new JScrollPane(studentsTable);
        tableScrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(430, 0));
        rightPanel.add(createActionTabs(), BorderLayout.CENTER);

        add(tableScrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Student Management");
        titleLabel.setFont(UIHelper.PAGE_TITLE_FONT);
        titleLabel.setForeground(UIHelper.TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(
                "Register each student once. Borrowing and waiting requests then use the same Student ID."
        );
        subtitleLabel.setFont(UIHelper.NORMAL_FONT);
        subtitleLabel.setForeground(UIHelper.SECONDARY_TEXT_COLOR);
        subtitleLabel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));

        JPanel textPanel = new JPanel(new BorderLayout());
        textPanel.setOpaque(false);
        textPanel.add(titleLabel, BorderLayout.NORTH);
        textPanel.add(subtitleLabel, BorderLayout.CENTER);

        headerPanel.add(textPanel, BorderLayout.WEST);
        return headerPanel;
    }

    private JTabbedPane createActionTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.NORMAL_FONT);
        tabs.addTab("Register", createRegisterTab());
        tabs.addTab("Update Status", createUpdateStatusTab());
        tabs.setToolTipTextAt(0, "Register a new central student profile");
        tabs.setToolTipTextAt(1, "Change a student's current graduating status");
        return tabs;
    }

    private JPanel createRegisterTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        registerStudentIdField = UIHelper.createTextField();
        registerStudentNameField = UIHelper.createTextField();
        registerGraduatingCheckBox = new JCheckBox("Graduating Student");
        registerGraduatingCheckBox.setFont(UIHelper.NORMAL_FONT);
        registerGraduatingCheckBox.setOpaque(false);

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "A Student ID can be registered only once and is permanently linked to this name.");
        addFormRow(panel, gbc, 1, "Student ID", registerStudentIdField);
        addFormRow(panel, gbc, 2, "Student Name", registerStudentNameField);
        addFormRow(panel, gbc, 3, "Status", registerGraduatingCheckBox);

        JButton registerButton = UIHelper.createPrimaryButton("Register Student");
        registerButton.addActionListener(e -> registerStudent());
        addFullWidthComponent(panel, gbc, 4, registerButton);

        return panel;
    }

    private JPanel createUpdateStatusTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        updateStudentIdField = UIHelper.createTextField();
        updateStudentNameField = UIHelper.createReadOnlyTextField();
        updateGraduatingCheckBox = new JCheckBox("Graduating Student");
        updateGraduatingCheckBox.setFont(UIHelper.NORMAL_FONT);
        updateGraduatingCheckBox.setOpaque(false);

        updateStudentIdField.getDocument().addDocumentListener(new SimpleDocumentListener(this::clearLoadedStudent));

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Load an existing student to update only the current graduating status. The name stays read-only.");
        addFormRow(panel, gbc, 1, "Student ID", updateStudentIdField);

        JButton loadButton = UIHelper.createSecondaryButton("Load Student");
        loadButton.addActionListener(e -> loadStudentForUpdate());
        addFullWidthComponent(panel, gbc, 2, loadButton);

        addFormRow(panel, gbc, 3, "Student Name", updateStudentNameField);
        addFormRow(panel, gbc, 4, "Status", updateGraduatingCheckBox);

        JButton updateButton = UIHelper.createPrimaryButton("Update Status");
        updateButton.addActionListener(e -> updateGraduatingStatus());
        addFullWidthComponent(panel, gbc, 5, updateButton);

        return panel;
    }

    private void registerStudent() {
        try {
            String studentId = readRequiredText(registerStudentIdField, "Student ID");
            String studentName = readRequiredText(registerStudentNameField, "Student Name");

            String message = StudentRegistry.registerStudent(
                    studentId,
                    studentName,
                    registerGraduatingCheckBox.isSelected()
            );

            if (isDone(message)) {
                UIHelper.showSuccessMessage(this, "Student registered successfully.");
                registerStudentIdField.setText("");
                registerStudentNameField.setText("");
                registerGraduatingCheckBox.setSelected(false);
                refreshStudentsTable();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void loadStudentForUpdate() {
        try {
            String studentId = readRequiredText(updateStudentIdField, "Student ID");
            Student student = StudentRegistry.findStudentById(studentId);
            if (student == null) {
                clearLoadedStudent();
                UIHelper.showErrorMessage(this, "Student ID was not found. Register the student first.");
                return;
            }

            updateStudentIdField.setText(student.getStudentId());
            updateStudentNameField.setText(student.getStudentName());
            updateGraduatingCheckBox.setSelected(student.isGraduatingStudent());
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void updateGraduatingStatus() {
        try {
            String studentId = readRequiredText(updateStudentIdField, "Student ID");
            String message = StudentRegistry.updateGraduatingStatus(
                    studentId,
                    updateGraduatingCheckBox.isSelected()
            );

            if (isDone(message)) {
                Student student = StudentRegistry.findStudentById(studentId);
                updateStudentNameField.setText(student == null ? "" : student.getStudentName());
                UIHelper.showSuccessMessage(
                        this,
                        "Student status updated successfully. Related waiting requests were reprioritized."
                );
                refreshStudentsTable();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void loadSelectedStudentForUpdate() {
        int selectedViewRow = studentsTable.getSelectedRow();
        if (selectedViewRow == -1) {
            return;
        }

        int selectedModelRow = studentsTable.convertRowIndexToModel(selectedViewRow);
        String studentId = String.valueOf(tableModel.getValueAt(selectedModelRow, 0));
        Student student = StudentRegistry.findStudentById(studentId);
        if (student == null) {
            return;
        }

        updateStudentIdField.setText(student.getStudentId());
        updateStudentNameField.setText(student.getStudentName());
        updateGraduatingCheckBox.setSelected(student.isGraduatingStudent());
    }

    private void clearLoadedStudent() {
        if (updateStudentNameField != null) {
            updateStudentNameField.setText("");
        }
        if (updateGraduatingCheckBox != null) {
            updateGraduatingCheckBox.setSelected(false);
        }
    }

    public void refreshStudentsTable() {
        tableModel.setRowCount(0);
        for (Student student : StudentRegistry.getAllStudents()) {
            tableModel.addRow(new Object[]{
                    student.getStudentId(),
                    student.getStudentName(),
                    student.isGraduatingStudent() ? "Yes" : "No"
            });
        }
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(6, 0, 6, 0);
        return gbc;
    }

    private void addDescription(JPanel panel, GridBagConstraints gbc, int row, String htmlText) {
        JLabel label = new JLabel("<html>" + htmlText + "</html>");
        label.setFont(UIHelper.SMALL_FONT);
        label.setForeground(UIHelper.SECONDARY_TEXT_COLOR);
        addFullWidthComponent(panel, gbc, row, label);
    }

    private void addFormRow(JPanel panel, GridBagConstraints gbc, int row, String labelText, java.awt.Component component) {
        GridBagConstraints labelConstraints = (GridBagConstraints) gbc.clone();
        labelConstraints.gridx = 0;
        labelConstraints.gridy = row;
        labelConstraints.weightx = 0.35;
        labelConstraints.gridwidth = 1;
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.NORMAL_FONT);
        panel.add(label, labelConstraints);

        GridBagConstraints componentConstraints = (GridBagConstraints) gbc.clone();
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.weightx = 0.65;
        componentConstraints.gridwidth = 1;
        panel.add(component, componentConstraints);
    }

    private void addFullWidthComponent(JPanel panel, GridBagConstraints gbc, int row, java.awt.Component component) {
        GridBagConstraints constraints = (GridBagConstraints) gbc.clone();
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 2;
        constraints.weightx = 1;
        panel.add(component, constraints);
    }

    private String readRequiredText(JTextField field, String fieldName) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return text;
    }

    private boolean isDone(String message) {
        return "Done .".equals(message);
    }

    private static class SimpleDocumentListener implements DocumentListener {
        private final Runnable action;

        private SimpleDocumentListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void insertUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            action.run();
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            action.run();
        }
    }
}
