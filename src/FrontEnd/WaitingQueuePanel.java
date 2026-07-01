package FrontEnd;

import BackEnd.BookWaitingQueue;
import BackEnd.BorrowRecord;
import BackEnd.Student;
import BackEnd.StudentRegistry;
import BackEnd.WaitingRequest;

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
 * Waiting-queue UI. It accepts only Student ID and displays central student
 * data as read-only, leaving queue priority and borrowing rules to the backend.
 */
public class WaitingQueuePanel extends JPanel {

    private JTable requestsTable;
    private DefaultTableModel tableModel;

    private JTextField addRequestIdField;
    private JTextField addBookNumberField;
    private JTextField addStudentIdField;
    private JTextField addStudentNameDisplayField;
    private JTextField addGraduatingStatusDisplayField;
    private JSpinner addRequestDateSpinner;

    private JTextField browseBookNumberField;
    private JTextArea browseRequestInfoArea;

    private JTextField serveBookNumberField;
    private JTextField serveRecordIdField;
    private JTextField serveBorrowDateDisplayField;
    private JSpinner serveExpectedReturnDateSpinner;
    private JTextArea serveRequestInfoArea;

    public WaitingQueuePanel() {
        initComponents();
        refreshRequestsTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Request ID",
                        "Book Number",
                        "Student ID",
                        "Student Name",
                        "Graduating Student",
                        "Request Date"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        requestsTable = new JTable(tableModel);
        UIHelper.styleTable(requestsTable);
        requestsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureTableColumns();
        requestsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                loadSelectedRequestIntoActionTabs();
            }
        });

        JScrollPane requestsScrollPane = new JScrollPane(requestsTable);
        requestsScrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(500, 0));
        rightPanel.add(createActionTabs(), BorderLayout.CENTER);

        add(requestsScrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }

    private void configureTableColumns() {
        int[] widths = {90, 100, 105, 145, 145, 110};
        requestsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            requestsTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Waiting Queue Management");
        titleLabel.setFont(UIHelper.PAGE_TITLE_FONT);
        titleLabel.setForeground(UIHelper.TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(
                "Graduating priority is always taken from the central student profile."
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
        tabs.addTab("Add Request", createAddRequestTab());
        tabs.addTab("Browse", createBrowseTab());
        tabs.addTab("Serve Next", createServeNextTab());
        tabs.setToolTipTextAt(0, "Add a request for an unavailable book");
        tabs.setToolTipTextAt(1, "View waiting requests by book");
        tabs.setToolTipTextAt(2, "Serve the next priority student for a book");
        return tabs;
    }

    private JPanel createAddRequestTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        addRequestIdField = UIHelper.createTextField();
        addBookNumberField = UIHelper.createTextField();
        addStudentIdField = UIHelper.createTextField();
        addStudentNameDisplayField = UIHelper.createReadOnlyTextField();
        addGraduatingStatusDisplayField = UIHelper.createReadOnlyTextField();
        addRequestDateSpinner = UIHelper.createDateSpinner();

        addStudentIdField.getDocument().addDocumentListener(new SimpleDocumentListener(this::clearRequestStudentDisplay));

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Students must be registered first. Their name and graduating status are loaded automatically.");
        addFormRow(panel, gbc, 1, "Request ID", addRequestIdField);
        addFormRow(panel, gbc, 2, "Book Number", addBookNumberField);
        addFormRow(panel, gbc, 3, "Student ID", addStudentIdField);

        JButton loadStudentButton = UIHelper.createSecondaryButton("Load Student");
        loadStudentButton.addActionListener(e -> loadStudentForRequest());
        addFullWidthComponent(panel, gbc, 4, loadStudentButton);

        addFormRow(panel, gbc, 5, "Student Name", addStudentNameDisplayField);
        addFormRow(panel, gbc, 6, "Graduating Status", addGraduatingStatusDisplayField);
        addFormRow(panel, gbc, 7, "Request Date", addRequestDateSpinner);

        JButton addRequestButton = UIHelper.createPrimaryButton("Add Waiting Request");
        addRequestButton.addActionListener(e -> addWaitingRequest());
        addFullWidthComponent(panel, gbc, 8, addRequestButton);

        return panel;
    }

    private JPanel createBrowseTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        browseBookNumberField = UIHelper.createTextField();
        browseRequestInfoArea = UIHelper.createReadOnlyTextArea();
        browseRequestInfoArea.setRows(8);
        browseRequestInfoArea.setText("Enter a Book Number to view its waiting requests or next priority request.");

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Use the Book Number to view all requests or only the next priority student for that book.");
        addFormRow(panel, gbc, 1, "Book Number", browseBookNumberField);

        JButton showRequestsButton = UIHelper.createSecondaryButton("Show Requests for Book");
        showRequestsButton.addActionListener(e -> showRequestsByBook());
        addFullWidthComponent(panel, gbc, 2, showRequestsButton);

        JButton viewNextButton = UIHelper.createPrimaryButton("View Next Priority Request");
        viewNextButton.addActionListener(e -> viewNextRequest());
        addFullWidthComponent(panel, gbc, 3, viewNextButton);
        addFullWidthComponent(panel, gbc, 4, browseRequestInfoArea);

        return panel;
    }

    private JPanel createServeNextTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        serveBookNumberField = UIHelper.createTextField();
        serveRecordIdField = UIHelper.createTextField();
        serveBorrowDateDisplayField = UIHelper.createReadOnlyTextField();
        serveBorrowDateDisplayField.setText(UIHelper.formatDate(LocalDate.now()));
        serveBorrowDateDisplayField.setToolTipText("Created automatically by the backend when the request is served.");
        serveExpectedReturnDateSpinner = UIHelper.createDateSpinner();
        serveRequestInfoArea = UIHelper.createReadOnlyTextArea();
        serveRequestInfoArea.setRows(8);
        serveRequestInfoArea.setText("Enter a Book Number and load the next request before serving it.");

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Serving a request creates a borrow record for the next priority student. Borrow Date is automatic.");
        addFormRow(panel, gbc, 1, "Book Number", serveBookNumberField);

        JButton loadNextButton = UIHelper.createSecondaryButton("Load Next Request");
        loadNextButton.addActionListener(e -> loadNextRequestForService());
        addFullWidthComponent(panel, gbc, 2, loadNextButton);

        addFullWidthComponent(panel, gbc, 3, serveRequestInfoArea);
        addFormRow(panel, gbc, 4, "New Borrow Record ID", serveRecordIdField);
        addFormRow(panel, gbc, 5, "Borrow Date", serveBorrowDateDisplayField);
        addFormRow(panel, gbc, 6, "Expected Return Date", serveExpectedReturnDateSpinner);

        JButton serveButton = UIHelper.createPrimaryButton("Serve Next Request");
        serveButton.addActionListener(e -> serveNextRequest());
        addFullWidthComponent(panel, gbc, 7, serveButton);

        return panel;
    }

    private void loadStudentForRequest() {
        try {
            String studentId = readRequiredText(addStudentIdField, "Student ID");
            Student student = StudentRegistry.findStudentById(studentId);
            if (student == null) {
                clearRequestStudentDisplay();
                UIHelper.showErrorMessage(this, "Student ID was not found. Register the student first.");
                return;
            }

            addStudentIdField.setText(student.getStudentId());
            addStudentNameDisplayField.setText(student.getStudentName());
            addGraduatingStatusDisplayField.setText(student.isGraduatingStudent() ? "Yes" : "No");
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void addWaitingRequest() {
        try {
            int requestId = readPositiveInteger(addRequestIdField, "Request ID");
            int bookNumber = readPositiveInteger(addBookNumberField, "Book Number");
            String studentId = readRequiredText(addStudentIdField, "Student ID");
            LocalDate requestDate = UIHelper.readDateSpinner(addRequestDateSpinner, "Request Date");

            String message = BookWaitingQueue.addRequest(
                    requestId,
                    bookNumber,
                    studentId,
                    UIHelper.formatDate(requestDate)
            );

            if (isDone(message)) {
                UIHelper.showSuccessMessage(this, "Waiting request added successfully.");
                refreshRequestsTable();
                clearAddRequestForm();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void showRequestsByBook() {
        try {
            int bookNumber = readPositiveInteger(browseBookNumberField, "Book Number");
            ArrayList<WaitingRequest> requests = BookWaitingQueue.getRequestsByBookNumber(bookNumber);
            if (requests.isEmpty()) {
                UIHelper.showErrorMessage(this, "There are no waiting requests for this book.");
                return;
            }

            tableModel.setRowCount(0);
            for (WaitingRequest request : requests) {
                addRequestToTable(request);
            }
            browseRequestInfoArea.setText(
                    requests.size() + " waiting request(s) found for Book Number " + bookNumber + "."
            );
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void viewNextRequest() {
        try {
            int bookNumber = readPositiveInteger(browseBookNumberField, "Book Number");
            WaitingRequest request = BookWaitingQueue.peekNextRequest(bookNumber);
            if (request == null) {
                UIHelper.showErrorMessage(this, "There is no waiting request for this book.");
                return;
            }
            browseRequestInfoArea.setText(buildRequestInfo(request));
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void loadNextRequestForService() {
        try {
            int bookNumber = readPositiveInteger(serveBookNumberField, "Book Number");
            WaitingRequest request = BookWaitingQueue.peekNextRequest(bookNumber);
            if (request == null) {
                UIHelper.showErrorMessage(this, "There is no waiting request for this book.");
                return;
            }
            serveRequestInfoArea.setText(buildRequestInfo(request));
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void serveNextRequest() {
        try {
            int bookNumber = readPositiveInteger(serveBookNumberField, "Book Number");
            int recordId = readPositiveInteger(serveRecordIdField, "New Borrow Record ID");
            LocalDate expectedReturnDate = UIHelper.readDateSpinner(
                    serveExpectedReturnDateSpinner,
                    "Expected Return Date"
            );

            WaitingRequest nextRequest = BookWaitingQueue.peekNextRequest(bookNumber);
            if (nextRequest == null) {
                UIHelper.showErrorMessage(this, "There is no waiting request for this book.");
                return;
            }

            serveRequestInfoArea.setText(buildRequestInfo(nextRequest));
            int choice = UIHelper.showConfirmMessage(
                    this,
                    "Create a borrow record for the next priority student?\n\n"
                            + buildRequestInfo(nextRequest)
                            + "\n\nBorrow Date: " + UIHelper.formatDate(LocalDate.now())
                            + "\nExpected Return Date: " + UIHelper.formatDate(expectedReturnDate)
            );
            if (choice != UIHelper.YES_OPTION) {
                return;
            }

            String message = BookWaitingQueue.serveNextRequest(
                    bookNumber,
                    recordId,
                    UIHelper.formatDate(expectedReturnDate)
            );

            if (isDone(message)) {
                BorrowRecord record = BackEnd.BorrowRecordList.searchByRecordId(recordId);
                UIHelper.showSuccessMessage(
                        this,
                        "Request served successfully.\n\n"
                                + "Student: " + nextRequest.getStudentName() + " (" + nextRequest.getStudentId() + ")\n"
                                + "Borrow Date: " + (record == null ? UIHelper.formatDate(LocalDate.now()) : record.getBorrowDate())
                );
                refreshRequestsTable();
                clearServeForm();
            } else {
                UIHelper.showErrorMessage(this, message);
            }
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    public void refreshRequestsTable() {
        tableModel.setRowCount(0);
        for (WaitingRequest request : BookWaitingQueue.getAllRequests()) {
            addRequestToTable(request);
        }
    }

    private void addRequestToTable(WaitingRequest request) {
        tableModel.addRow(new Object[]{
                request.getRequestId(),
                request.getBookNumber(),
                request.getStudentId(),
                request.getStudentName(),
                request.isGraduatingStudent() ? "Yes" : "No",
                request.getRequestDate()
        });
    }

    private void loadSelectedRequestIntoActionTabs() {
        int selectedViewRow = requestsTable.getSelectedRow();
        if (selectedViewRow == -1) {
            return;
        }

        int selectedModelRow = requestsTable.convertRowIndexToModel(selectedViewRow);
        int requestId = Integer.parseInt(String.valueOf(tableModel.getValueAt(selectedModelRow, 0)));
        WaitingRequest request = BookWaitingQueue.searchByRequestId(requestId);
        if (request == null) {
            return;
        }

        browseBookNumberField.setText(String.valueOf(request.getBookNumber()));
        serveBookNumberField.setText(String.valueOf(request.getBookNumber()));
        browseRequestInfoArea.setText(buildRequestInfo(request));
        serveRequestInfoArea.setText(buildRequestInfo(request));
    }

    private String buildRequestInfo(WaitingRequest request) {
        return "Request ID: " + request.getRequestId()
                + "\nBook Number: " + request.getBookNumber()
                + "\nStudent ID: " + request.getStudentId()
                + "\nStudent Name: " + request.getStudentName()
                + "\nGraduating Student: " + (request.isGraduatingStudent() ? "Yes" : "No")
                + "\nRequest Date: " + request.getRequestDate();
    }

    private void clearRequestStudentDisplay() {
        if (addStudentNameDisplayField != null) {
            addStudentNameDisplayField.setText("");
        }
        if (addGraduatingStatusDisplayField != null) {
            addGraduatingStatusDisplayField.setText("");
        }
    }

    private void clearAddRequestForm() {
        addRequestIdField.setText("");
        addBookNumberField.setText("");
        addStudentIdField.setText("");
        clearRequestStudentDisplay();
        UIHelper.setDateSpinnerValue(addRequestDateSpinner, LocalDate.now());
        requestsTable.clearSelection();
    }

    private void clearServeForm() {
        serveBookNumberField.setText("");
        serveRecordIdField.setText("");
        serveBorrowDateDisplayField.setText(UIHelper.formatDate(LocalDate.now()));
        UIHelper.setDateSpinnerValue(serveExpectedReturnDateSpinner, LocalDate.now());
        serveRequestInfoArea.setText("Enter a Book Number and load the next request before serving it.");
        requestsTable.clearSelection();
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
