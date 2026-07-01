package FrontEnd;

import BackEnd.Book;
import BackEnd.BookTree;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

/**
 * Front-end only panel for book-related actions.
 *
 * Each operation has its own tab so that values intended for one action cannot
 * accidentally be reused by another action. All book rules remain in BookTree.
 */
public class BooksPanel extends JPanel {

    private JTable booksTable;
    private DefaultTableModel tableModel;
    private JTextArea selectedBookInfoArea;

    private JTextField addBookNumberField;
    private JTextField addAuthorField;
    private JTextField addCopiesField;

    private JTextField findBookNumberField;

    private JTextField manageBookNumberField;
    private JTextField copyQuantityField;

    private JTextField deleteBookNumberField;

    public BooksPanel() {
        initComponents();
        refreshBooksTable();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);

        tableModel = new DefaultTableModel(
                new Object[]{
                        "Book Number",
                        "Author",
                        "Total Copies",
                        "Borrowed Copies",
                        "Available Copies"
                },
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        booksTable = new JTable(tableModel);
        UIHelper.styleTable(booksTable);
        booksTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        configureTableColumns();
        booksTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                showSelectedBook();
            }
        });

        JScrollPane booksScrollPane = new JScrollPane(booksTable);
        booksScrollPane.setBorder(BorderFactory.createLineBorder(UIHelper.BORDER_COLOR));

        JPanel rightPanel = new JPanel(new BorderLayout(12, 12));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(400, 0));
        rightPanel.add(createSelectedBookPanel(), BorderLayout.NORTH);
        rightPanel.add(createActionTabs(), BorderLayout.CENTER);

        add(booksScrollPane, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
    }


    private void configureTableColumns() {
        int[] widths = {105, 165, 110, 125, 120};
        booksTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for (int i = 0; i < widths.length; i++) {
            booksTable.getColumnModel().getColumn(i).setPreferredWidth(widths[i]);
        }
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Books Management");
        titleLabel.setFont(UIHelper.PAGE_TITLE_FONT);
        titleLabel.setForeground(UIHelper.TEXT_COLOR);

        JLabel subtitleLabel = new JLabel(
                "Select a book to view its details, then use the matching action tab."
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

    private JPanel createSelectedBookPanel() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new BorderLayout(8, 8));

        JLabel title = new JLabel("Selected Book");
        title.setFont(UIHelper.SECTION_TITLE_FONT);
        title.setForeground(UIHelper.TEXT_COLOR);

        selectedBookInfoArea = UIHelper.createReadOnlyTextArea();
        selectedBookInfoArea.setRows(5);
        selectedBookInfoArea.setText("Select a row from the table to view book details.");

        panel.add(title, BorderLayout.NORTH);
        panel.add(selectedBookInfoArea, BorderLayout.CENTER);
        return panel;
    }

    private JTabbedPane createActionTabs() {
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UIHelper.NORMAL_FONT);
        tabs.addTab("Add", createAddBookTab());
        tabs.addTab("Find", createFindBookTab());
        tabs.addTab("Copies", createManageCopiesTab());
        tabs.addTab("Delete", createDeleteBookTab());
        tabs.setToolTipTextAt(0, "Add a new book");
        tabs.setToolTipTextAt(1, "Find a book by its number");
        tabs.setToolTipTextAt(2, "Add or delete copies");
        tabs.setToolTipTextAt(3, "Delete a book");
        return tabs;
    }

    private JPanel createAddBookTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        addBookNumberField = UIHelper.createTextField();
        addAuthorField = UIHelper.createTextField();
        addCopiesField = UIHelper.createTextField();

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Use this tab only to add a new book.<br>All three fields are required.");
        addFormRow(panel, gbc, 1, "Book Number", addBookNumberField);
        addFormRow(panel, gbc, 2, "Author", addAuthorField);
        addFormRow(panel, gbc, 3, "Initial Copies", addCopiesField);

        JButton addButton = UIHelper.createPrimaryButton("Add Book");
        addButton.addActionListener(e -> addBook());
        addFullWidthComponent(panel, gbc, 4, addButton);

        return panel;
    }

    private JPanel createFindBookTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        findBookNumberField = UIHelper.createTextField();

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Enter a book number to show one book<br>in the table.");
        addFormRow(panel, gbc, 1, "Book Number", findBookNumberField);

        JButton findButton = UIHelper.createPrimaryButton("Find Book");
        findButton.addActionListener(e -> findBook());
        addFullWidthComponent(panel, gbc, 2, findButton);

        JButton showAllButton = UIHelper.createSecondaryButton("Show All Books");
        showAllButton.addActionListener(e -> {
            refreshBooksTable();
            clearBookSelection();
        });
        addFullWidthComponent(panel, gbc, 3, showAllButton);

        return panel;
    }

    private JPanel createManageCopiesTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        manageBookNumberField = UIHelper.createTextField();
        copyQuantityField = UIHelper.createTextField();

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Enter the number of copies to add or remove.<br>");
        addFormRow(panel, gbc, 1, "Book Number", manageBookNumberField);
        addFormRow(panel, gbc, 2, "Copy Quantity", copyQuantityField);

        JButton addCopiesButton = UIHelper.createPrimaryButton("Add Copies");
        addCopiesButton.addActionListener(e -> addCopies());
        addFullWidthComponent(panel, gbc, 3, addCopiesButton);

        JButton deleteCopiesButton = UIHelper.createDangerButton("Delete Copies");
        deleteCopiesButton.addActionListener(e -> deleteCopies());
        addFullWidthComponent(panel, gbc, 4, deleteCopiesButton);

        return panel;
    }

    private JPanel createDeleteBookTab() {
        JPanel panel = UIHelper.createCardPanel();
        panel.setLayout(new GridBagLayout());

        deleteBookNumberField = UIHelper.createTextField();

        GridBagConstraints gbc = createFormConstraints();
        addDescription(panel, gbc, 0,
                "Only a book with no borrowed copies can be deleted.<br>");
        addFormRow(panel, gbc, 1, "Book Number", deleteBookNumberField);

        JButton deleteButton = UIHelper.createDangerButton("Delete Book");
        deleteButton.addActionListener(e -> deleteBook());
        addFullWidthComponent(panel, gbc, 2, deleteButton);

        return panel;
    }

    private GridBagConstraints createFormConstraints() {
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 0, 7, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;
        return gbc;
    }

    private void addDescription(JPanel panel, GridBagConstraints gbc, int row, String text) {
        JLabel description = new JLabel("<html><body style='width:330px'>" + text + "</body></html>");
        description.setFont(UIHelper.SMALL_FONT);
        description.setForeground(UIHelper.SECONDARY_TEXT_COLOR);
        description.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        addFullWidthComponent(panel, gbc, row, description);
    }

    private void addFormRow(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            String labelText,
            JTextField field
    ) {
        JLabel label = new JLabel(labelText);
        label.setFont(UIHelper.NORMAL_FONT);
        label.setForeground(UIHelper.TEXT_COLOR);

        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 1;
        panel.add(label, gbc);

        gbc.gridx = 0;
        gbc.gridy = row * 2 + 1;
        panel.add(field, gbc);
    }

    private void addFullWidthComponent(
            JPanel panel,
            GridBagConstraints gbc,
            int row,
            java.awt.Component component
    ) {
        gbc.gridx = 0;
        gbc.gridy = row * 2;
        gbc.gridwidth = 1;
        panel.add(component, gbc);
    }

    private void addBook() {
        try {
            int bookNumber = readPositiveInteger(addBookNumberField, "Book Number");
            int copies = readPositiveInteger(addCopiesField, "Initial Copies");
            String author = readRequiredText(addAuthorField, "Author");

            String message = BookTree.insert(bookNumber, copies, author);
            showBackendResult(message, "Book added successfully.", true);
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void findBook() {
        try {
            int bookNumber = readPositiveInteger(findBookNumberField, "Book Number");
            Book book = BookTree.search(bookNumber);

            if (book == null) {
                UIHelper.showErrorMessage(this, "Book not found.");
                return;
            }

            tableModel.setRowCount(0);
            addBookToTable(book);
            displayBookDetails(book);
            UIHelper.showSuccessMessage(this, "Book found.");
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void addCopies() {
        try {
            int bookNumber = readPositiveInteger(manageBookNumberField, "Book Number");
            int quantity = readPositiveInteger(copyQuantityField, "Copy Quantity");
            String message = BookTree.addCopies(bookNumber, quantity);
            showBackendResult(message, "Copies added successfully.", true);
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void deleteCopies() {
        try {
            int bookNumber = readPositiveInteger(manageBookNumberField, "Book Number");
            int quantity = readPositiveInteger(copyQuantityField, "Copy Quantity");

            int choice = UIHelper.showConfirmMessage(
                    this,
                    "Remove " + quantity + " copy/copies from book " + bookNumber + "?\n\n"
                            + "If this removes all available copies, the backend may delete the book."
            );

            if (choice != UIHelper.YES_OPTION) {
                return;
            }

            String message = BookTree.deleteCopies(bookNumber, quantity);
            showBackendResult(message, "Copies deleted successfully.", true);
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void deleteBook() {
        try {
            int bookNumber = readPositiveInteger(deleteBookNumberField, "Book Number");
            Book book = BookTree.search(bookNumber);

            String bookDescription = book == null
                    ? "Book number: " + bookNumber
                    : "Book number: " + book.getBookNumber() + "\nAuthor: " + book.getAuthor();

            int choice = UIHelper.showConfirmMessage(
                    this,
                    "Are you sure you want to delete this book?\n\n" + bookDescription
            );

            if (choice != UIHelper.YES_OPTION) {
                return;
            }

            String message = BookTree.delete(bookNumber);
            showBackendResult(message, "Book deleted successfully.", true);
        } catch (IllegalArgumentException ex) {
            UIHelper.showErrorMessage(this, ex.getMessage());
        }
    }

    private void showBackendResult(String backendMessage, String successMessage, boolean refreshAfterSuccess) {
        if (isDone(backendMessage)) {
            UIHelper.showSuccessMessage(this, successMessage);
            if (refreshAfterSuccess) {
                refreshBooksTable();
            }
            clearAllInputs();
        } else {
            UIHelper.showErrorMessage(this, backendMessage);
        }
    }

    public void refreshBooksTable() {
        tableModel.setRowCount(0);

        ArrayList<Book> books = BookTree.getBooks();
        for (Book book : books) {
            addBookToTable(book);
        }
    }

    private void addBookToTable(Book book) {
        tableModel.addRow(new Object[]{
                book.getBookNumber(),
                book.getAuthor(),
                book.getCopiesNumber(),
                book.getBorrowedCopies(),
                book.getAvailableCopies()
        });
    }

    private void showSelectedBook() {
        int selectedRow = booksTable.getSelectedRow();
        if (selectedRow == -1) {
            return;
        }

        int selectedModelRow = booksTable.convertRowIndexToModel(selectedRow);
        int bookNumber = Integer.parseInt(String.valueOf(tableModel.getValueAt(selectedModelRow, 0)));
        Book book = BookTree.search(bookNumber);

        if (book != null) {
            displayBookDetails(book);
            findBookNumberField.setText(String.valueOf(bookNumber));
            manageBookNumberField.setText(String.valueOf(bookNumber));
            deleteBookNumberField.setText(String.valueOf(bookNumber));
        }
    }

    private void displayBookDetails(Book book) {
        selectedBookInfoArea.setText(
                "Book Number: " + book.getBookNumber()
                        + "\nAuthor: " + book.getAuthor()
                        + "\nTotal Copies: " + book.getCopiesNumber()
                        + "\nBorrowed Copies: " + book.getBorrowedCopies()
                        + "\nAvailable Copies: " + book.getAvailableCopies()
        );
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

    private void clearAllInputs() {
        addBookNumberField.setText("");
        addAuthorField.setText("");
        addCopiesField.setText("");
        findBookNumberField.setText("");
        manageBookNumberField.setText("");
        copyQuantityField.setText("");
        deleteBookNumberField.setText("");
        clearBookSelection();
    }

    private void clearBookSelection() {
        booksTable.clearSelection();
        selectedBookInfoArea.setText("Select a row from the table to view book details.");
    }
}
