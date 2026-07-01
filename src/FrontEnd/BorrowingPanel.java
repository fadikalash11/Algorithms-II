package FrontEnd;

import BackEnd.BorrowRecord;
import BackEnd.BorrowRecordList;
import BackEnd.Student;
import BackEnd.StudentRegistry;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
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
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Front-end borrowing panel. It asks for only Student ID and retrieves the
 * student profile from StudentRegistry. Backend methods remain responsible for
 * borrow-limit and date business rules.
 */
public class BorrowingPanel extends JPanel {

    private JTable recordsTable;
    private DefaultTableModel tableModel;

    private JTextField borrowRecordIdField;
    private JTextField borrowBookNumberField;
    private JTextField borrowStudentIdField;
    private JTextField borrowStudentNameDisplayField;
    private JTextField borrowGraduatingStatusDisplayField;
    private JTextField borrowDateDisplayField;
    private JSpinner borrowExpectedReturnDateSpinner;

    private JTextField returnRecordIdField;
    private JTextArea returnRecordInfoArea;

    private JTextField updateRecordIdField;
    private JSpinner updateExpectedReturnDateSpinner;
    private JTextArea updateRecordInfoArea;

    private JTextField searchStudentIdField;
    private JTextField searchStudentNameField;

    public BorrowingPanel() {
        initComponents();
        refreshRecordsTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Record ID",
                        "Book Number",
                        "Student ID",
                        "Student Name",
                        "Borrow Date",
                        "Expected Return Date",
                        "Returned"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        recordsTable = new JTable(tableModel);
        UIHelper.styleTable(recordsTable);
        recordsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureTableColumns();
        recordsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRecordIntoActionTabs();
            }
        });

        JScrollPane recordsScrollPane = new JScrollPane(recordsTable);
        recordsScrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(480, 0));
        rightPanel.add(createActionTabs(), BorderLayout.CENTER);

        add(recordsScrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void configureTableColumns() {
        int[] widths = {85, 100, 115, 140, 105, 150, 85};
        recordsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            recordsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Borrowing Management");
        titleLabel.setFont(UIHelper.PAGE_TITLE_FONT);
        titleLabel.setForeground(UIHelper.TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(
                "Borrow dates are created automatically by the backend. Student details are loaded from Student ID."
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
        tabs.addTab("Borrow", createBorrowTab());
        tabs.addTab("Return", createReturnTab());
        tabs.addTab("Update Date", createUpdateReturnDateTab());
        tabs.addTab("Search", createSearchTab());
        tabs.setToolTipTextAt(0, "Borrow a book for a registered student");
        tabs.setToolTipTextAt(1, "Return a borrowed book");
        tabs.setToolTipTextAt(2, "Update an expected return date");
        tabs.setToolTipTextAt(3, "Search borrowing records");
        return tabs;
    }

    private JPanel createBorrowTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        borrowRecordIdField = UIHelper.createTextField();
        borrowBookNumberField = UIHelper.createTextField();
        borrowStudentIdField = UIHelper.createTextField();
        borrowStudentNameDisplayField = UIHelper.createReadOnlyTextField();
        borrowGraduatingStatusDisplayField = UIHelper.createReadOnlyTextField();
        borrowDateDisplayField = UIHelper.createReadOnlyTextField();
        borrowDateDisplayField.setToolTipText("Created automatically by the backend when the book is borrowed.");
        borrowDateDisplayField.setText(UIHelper.formatDate(LocalDate.now()));
        borrowExpectedReturnDateSpinner = UIHelper.createDateSpinner();

        borrowStudentIdField.getDocument().addDocumentListener(new SimpleDocumentListener(this::clearBorrowStudentDisplay));

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Enter a registered Student ID, then load the profile. Name and status cannot be edited here.");
        addFormRow(panel, gbc, 1, "Record ID", borrowRecordIdField);
        addFormRow(panel, gbc, 2, "Book Number", borrowBookNumberField);
        addFormRow(panel, gbc, 3, "Student ID", borrowStudentIdField);

        JButton loadStudentButton = UIHelper.createSecondaryButton("Load Student");
        loadStudentButton.addActionListener(e -> loadStudentForBorrow());
        addFullWidthComponent(panel, gbc, 4, loadStudentButton);

        addFormRow(panel, gbc, 5, "Student Name", borrowStudentNameDisplayField);
        addFormRow(panel, gbc, 6, "Graduating Status", borrowGraduatingStatusDisplayField);
        addFormRow(panel, gbc, 7, "Borrow Date", borrowDateDisplayField);
        addFormRow(panel, gbc, 8, "Expected Return Date", borrowExpectedReturnDateSpinner);

        JButton borrowButton = UIHelper.createPrimaryButton("Borrow Book");
        borrowButton.addActionListener(e -> borrowBook());
        addFullWidthComponent(panel, gbc, 9, borrowButton);

        return panel;
    }

    private JPanel createReturnTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        returnRecordIdField = UIHelper.createTextField();
        returnRecordInfoArea = UIHelper.createReadOnlyTextArea();
        returnRecordInfoArea.setRows(6);
        returnRecordInfoArea.setText("Enter a Record ID and load the record before returning it.");

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Only Record ID is needed. Select a record in the table to fill it automatically.");
        addFormRow(panel, gbc, 1, "Record ID", returnRecordIdField);

        JButton loadButton = UIHelper.createSecondaryButton("Load Record");
        loadButton.addActionListener(e -> loadRecordForReturn());
        addFullWidthComponent(panel, gbc, 2, loadButton);
        addFullWidthComponent(panel, gbc, 3, returnRecordInfoArea);

        JButton returnButton = UIHelper.createPrimaryButton("Return Book");
        returnButton.addActionListener(e -> returnBook());
        addFullWidthComponent(panel, gbc, 4, returnButton);

        return panel;
    }

    private JPanel createUpdateReturnDateTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        updateRecordIdField = UIHelper.createTextField();
        updateExpectedReturnDateSpinner = UIHelper.createDateSpinner();
        updateRecordInfoArea = UIHelper.createReadOnlyTextArea();
        updateRecordInfoArea.setRows(6);
        updateRecordInfoArea.setText("Enter a Record ID and load the record before changing its expected return date.");

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "The backend rejects invalid dates, dates before the borrow date, and changes after return.");
        addFormRow(panel, gbc, 1, "Record ID", updateRecordIdField);

        JButton loadButton = UIHelper.createSecondaryButton("Load Record");
        loadButton.addActionListener(e -> loadRecordForUpdate());
        addFullWidthComponent(panel, gbc, 2, loadButton);

        addFullWidthComponent(panel, gbc, 3, updateRecordInfoArea);
        addFormRow(panel, gbc, 4, "Expected Return Date", updateExpectedReturnDateSpinner);

        JButton updateButton = UIHelper.createPrimaryButton("Update Expected Return Date");
        updateButton.addActionListener(e -> updateExpectedReturnDate());
        addFullWidthComponent(panel, gbc, 5, updateButton);

        return panel;
    }

    private JPanel createSearchTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        searchStudentIdField = UIHelper.createTextField();
        searchStudentNameField = UIHelper.createTextField();

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Search by Student ID for exact records, or use Student Name to show all matching records.");
        addFormRow(panel, gbc, 1, "Student ID", searchStudentIdField);

        JButton searchByIdButton = UIHelper.createSecondaryButton("Search by Student ID");
        searchByIdButton.addActionListener(e -> searchByStudentId());
        addFullWidthComponent(panel, gbc, 2, searchByIdButton);

        addFormRow(panel, gbc, 3, "Student Name", searchStudentNameField);

        JButton searchByNameButton = UIHelper.createSecondaryButton("Search by Student Name");
        searchByNameButton.addActionListener(e -> searchByStudentName());
        addFullWidthComponent(panel, gbc, 4, searchByNameButton);

        JButton showAllButton = UIHelper.createPrimaryButton("Show All Records");
        showAllButton.addActionListener(e -> refreshRecordsTable());
        addFullWidthComponent(panel, gbc, 5, showAllButton);

        return panel;
    }

    private void loadStudentForBorrow() {
        try {
            String studentId = readRequiredText(borrowStudentIdField, "Student ID");
            Student student = StudentRegistry.findStudentById(studentId);
            if (student == null) {
                clearBorrowStudentDisplay();
                UIHelper.showErrorMessage(this, "Student ID was not found. Register the student first.");
                return;
            }

            borrowStudentIdField.setText(student.getStudentId());
            borrowStudentNameDisplayField.setText(student.getStudentName());
            borrowGraduatingStatusDisplayField.setText(student.isGraduatingStudent() ? "Yes" : "No");
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void borrowBook() {
        try {
            int recordId = readPositiveInteger(borrowRecordIdField, "Record ID");
            int bookNumber = readPositiveInteger(borrowBookNumberField, "Book Number");
            String studentId = readRequiredText(borrowStudentIdField, "Student ID");
            LocalDate expectedReturnDate = UIHelper.readDateSpinner(
                    borrowExpectedReturnDateSpinner,
                    "Expected Return Date"
            );

            String message = BorrowRecordList.borrowBookWithRecord(
                    recordId,
                    bookNumber,
                    studentId,
                    UIHelper.formatDate(expectedReturnDate)
            );

            if (isDone(message)) {
                BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
                UIHelper.showSuccessMessage(
                        this,
                        "Book borrowed successfully.\n\n"
                                + "Student: " + record.getStudentName() + " (" + record.getStudentId() + ")\n"
                                + "Borrow Date: " + record.getBorrowDate()
                );
                refreshRecordsTable();
                clearBorrowForm();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void loadRecordForReturn() {
        try {
            int recordId = readPositiveInteger(returnRecordIdField, "Record ID");
            BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
            if (record == null) {
                UIHelper.showErrorMessage(this, "Borrow record not found.");
                return;
            }
            returnRecordInfoArea.setText(buildRecordInfo(record));
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void returnBook() {
        try {
            int recordId = readPositiveInteger(returnRecordIdField, "Record ID");
            BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
            if (record == null) {
                UIHelper.showErrorMessage(this, "Borrow record not found.");
                return;
            }

            returnRecordInfoArea.setText(buildRecordInfo(record));
            int choice = UIHelper.showConfirmMessage(
                    this,
                    "Return this book?\n\n" + buildRecordInfo(record)
            );
            if (choice != UIHelper.YES_OPTION) {
                return;
            }

            String message = BorrowRecordList.returnBorrowedBook(recordId);
            if (isDone(message)) {
                UIHelper.showSuccessMessage(this, "Book returned successfully.");
                refreshRecordsTable();
                returnRecordIdField.setText("");
                returnRecordInfoArea.setText("Enter a Record ID and load the record before returning it.");
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void loadRecordForUpdate() {
        try {
            int recordId = readPositiveInteger(updateRecordIdField, "Record ID");
            BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
            if (record == null) {
                UIHelper.showErrorMessage(this, "Borrow record not found.");
                return;
            }

            updateRecordInfoArea.setText(buildRecordInfo(record));
            UIHelper.setDateSpinnerValue(
                    updateExpectedReturnDateSpinner,
                    UIHelper.parseStoredDate(record.getExpectedReturnDate(), "Expected Return Date")
            );
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void updateExpectedReturnDate() {
        try {
            int recordId = readPositiveInteger(updateRecordIdField, "Record ID");
            LocalDate expectedReturnDate = UIHelper.readDateSpinner(
                    updateExpectedReturnDateSpinner,
                    "Expected Return Date"
            );

            String message = BorrowRecordList.updateExpectedReturnDate(
                    recordId,
                    UIHelper.formatDate(expectedReturnDate)
            );

            if (isDone(message)) {
                BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
                updateRecordInfoArea.setText(buildRecordInfo(record));
                UIHelper.showSuccessMessage(this, "Expected return date updated successfully.");
                refreshRecordsTable();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void searchByStudentId() {
        try {
            String studentId = readRequiredText(searchStudentIdField, "Student ID");
            ArrayList<BorrowRecord> records = BorrowRecordList.searchByStudentId(studentId);
            showSearchResults(records, "No records found for this Student ID.");
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void searchByStudentName() {
        try {
            String studentName = readRequiredText(searchStudentNameField, "Student Name");
            ArrayList<BorrowRecord> records = BorrowRecordList.searchByStudentName(studentName);
            showSearchResults(records, "No records found for this Student Name.");
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void showSearchResults(ArrayList<BorrowRecord> records, String emptyMessage) {
        if (records.isEmpty()) {
            UIHelper.showErrorMessage(this, emptyMessage);
            return;
        }

        tableModel.setRowCount(0);
        for (BorrowRecord record : records) {
            addRecordToTable(record);
        }
    }

    public void refreshRecordsTable() {
        tableModel.setRowCount(0);
        for (BorrowRecord record : BorrowRecordList.getAllRecords()) {
            addRecordToTable(record);
        }
    }

    private void addRecordToTable(BorrowRecord record) {
        tableModel.addRow(new Object[]{
                record.getRecordId(),
                record.getBookNumber(),
                record.getStudentId(),
                record.getStudentName(),
                record.getBorrowDate(),
                record.getExpectedReturnDate(),
                record.isReturned() ? "Yes" : "No"
        });
    }

    private void loadSelectedRecordIntoActionTabs() {
        int selectedViewRow = recordsTable.getSelectedRow();
        if (selectedViewRow == -1) {
            return;
        }

        int selectedModelRow = recordsTable.convertRowIndexToModel(selectedViewRow);
        int recordId = Integer.parseInt(String.valueOf(tableModel.getValueAt(selectedModelRow, 0)));
        BorrowRecord record = BorrowRecordList.searchByRecordId(recordId);
        if (record == null) {
            return;
        }

        returnRecordIdField.setText(String.valueOf(record.getRecordId()));
        updateRecordIdField.setText(String.valueOf(record.getRecordId()));
        returnRecordInfoArea.setText(buildRecordInfo(record));
        updateRecordInfoArea.setText(buildRecordInfo(record));
        UIHelper.setDateSpinnerValue(
                updateExpectedReturnDateSpinner,
                UIHelper.parseStoredDate(record.getExpectedReturnDate(), "Expected Return Date")
        );
    }

    private String buildRecordInfo(BorrowRecord record) {
        return "Record ID: " + record.getRecordId()
                + "\nBook Number: " + record.getBookNumber()
                + "\nStudent: " + record.getStudentName() + " (" + record.getStudentId() + ")"
                + "\nBorrow Date: " + record.getBorrowDate()
                + "\nExpected Return Date: " + record.getExpectedReturnDate()
                + "\nReturned: " + (record.isReturned() ? "Yes" : "No");
    }

    private void clearBorrowStudentDisplay() {
        if (borrowStudentNameDisplayField != null) {
            borrowStudentNameDisplayField.setText("");
        }
        if (borrowGraduatingStatusDisplayField != null) {
            borrowGraduatingStatusDisplayField.setText("");
        }
    }

    private void clearBorrowForm() {
        borrowRecordIdField.setText("");
        borrowBookNumberField.setText("");
        borrowStudentIdField.setText("");
        clearBorrowStudentDisplay();
        borrowDateDisplayField.setText(UIHelper.formatDate(LocalDate.now()));
        UIHelper.setDateSpinnerValue(borrowExpectedReturnDateSpinner, LocalDate.now());
        recordsTable.clearSelection();
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
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
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.NORMAL_FONT);
        panel.add(label, labelConstraints);

        GridBagConstraints componentConstraints = (GridBagConstraints) gbc.clone();
        componentConstraints.gridx = 1;
        componentConstraints.gridy = row;
        componentConstraints.weightx = 0.65;
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

    private int readPositiveInteger(JTextField field, String fieldName) {
        String text = field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        try {
            int value = Integer.parseInt(text);
            if (value <= 0) {
                throw new IllegalArgumentException(fieldName + " must be greater than 0.");
            }
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number.");
        }
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
