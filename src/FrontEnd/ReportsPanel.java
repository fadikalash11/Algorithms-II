package FrontEnd;

import BackEnd.BookTree;
import BackEnd.BorrowRecordList;
import BackEnd.Book;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

public class ReportsPanel extends JPanel {

    private JTextArea reportTextArea;

    public ReportsPanel() {
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout(20, 20));
        setBackground(UIHelper.BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        add(createHeaderPanel(), BorderLayout.NORTH);
        add(createReportAreaPanel(), BorderLayout.CENTER);
        add(createButtonsPanel(), BorderLayout.EAST);
    }

    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("Reports");
        titleLabel.setFont(UIHelper.PAGE_TITLE_FONT);
        titleLabel.setForeground(UIHelper.TEXT_COLOR);

        JLabel subtitleLabel = new JLabel("Display analytical reports about books, authors, and available copies.");
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

    private JPanel createReportAreaPanel() {
        JPanel reportPanel = new JPanel(new BorderLayout());
        reportPanel.setBackground(UIHelper.WHITE_COLOR);
        reportPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new java.awt.Color(220, 225, 230)),
                BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        reportTextArea = new JTextArea();
        reportTextArea.setFont(new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 16));
        reportTextArea.setEditable(false);
        reportTextArea.setLineWrap(true);
        reportTextArea.setWrapStyleWord(true);
        reportTextArea.setText("Choose a report from the buttons on the right side.");

        JScrollPane scrollPane = new JScrollPane(reportTextArea);
        scrollPane.setBorder(null);

        reportPanel.add(scrollPane, BorderLayout.CENTER);

        return reportPanel;
    }

    private JPanel createButtonsPanel() {
        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setOpaque(false);
        containerPanel.setPreferredSize(new Dimension(280, 0));

        JPanel buttonsPanel = new JPanel(new GridLayout(5, 1, 0, 12));
        buttonsPanel.setOpaque(false);

        JButton availableBooksButton = UIHelper.createPrimaryButton("Available Books Report");
        JButton mostBorrowedBooksButton = UIHelper.createPrimaryButton("Most Borrowed Books");
        JButton mostReadAuthorsButton = UIHelper.createPrimaryButton("Most Read Authors");
        JButton refreshButton = UIHelper.createPrimaryButton("Refresh Current Report");
        JButton clearButton = UIHelper.createDangerButton("Clear");

        availableBooksButton.addActionListener(e -> showAvailableBooksReport());
        mostBorrowedBooksButton.addActionListener(e -> showMostBorrowedBooksReport());
        mostReadAuthorsButton.addActionListener(e -> showMostReadAuthorsReport());
        refreshButton.addActionListener(e -> refreshCurrentReport());
        clearButton.addActionListener(e -> clearReport());

        buttonsPanel.add(availableBooksButton);
        buttonsPanel.add(mostBorrowedBooksButton);
        buttonsPanel.add(mostReadAuthorsButton);
        buttonsPanel.add(refreshButton);
        buttonsPanel.add(clearButton);

        containerPanel.add(buttonsPanel, BorderLayout.NORTH);

        return containerPanel;
    }

    private void showAvailableBooksReport() {
        StringBuilder report = new StringBuilder();
        int totalAvailableCopies = 0;

        for (Book book : BookTree.getBooks()) {
            if (book.getAvailableCopies() > 0) {
                report.append(book.AvailableBookCopies()).append("\n");
                totalAvailableCopies += book.getAvailableCopies();
            }
        }

        if (report.length() == 0) {
            report.append("There are currently no available copies.");
        } else {
            report.append("\nTotal Available Copies: ")
                    .append(totalAvailableCopies);
        }

        reportTextArea.setText(
                formatReportTitle("Available Books Report") + report
        );
    }

    private void showMostBorrowedBooksReport() {
        String report = BorrowRecordList.getMostBorrowedBooksReport();
        reportTextArea.setText(formatReportTitle("Most Borrowed Books Report") + report);
    }

    private void showMostReadAuthorsReport() {
        String report = BorrowRecordList.getMostReadAuthorsReport();
        reportTextArea.setText(formatReportTitle("Most Read Authors Report") + report);
    }

    private void refreshCurrentReport() {
        String currentText = reportTextArea.getText();

        if (currentText.contains("Available Books Report")) {
            showAvailableBooksReport();
        } else if (currentText.contains("Most Borrowed Books Report")) {
            showMostBorrowedBooksReport();
        } else if (currentText.contains("Most Read Authors Report")) {
            showMostReadAuthorsReport();
        } else {
            UIHelper.showErrorMessage(this, "Please choose a report first.");
        }
    }

    private void clearReport() {
        reportTextArea.setText("Choose a report from the buttons on the right side.");
    }

    private String formatReportTitle(String title) {
        return "====================================\n"
                + title + "\n"
                + "====================================\n\n";
    }
}