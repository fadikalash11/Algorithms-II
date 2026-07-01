package FrontEnd;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

/** Main Swing window. Navigation refreshes tables but does not change backend data. */
public class MainFrame extends JFrame {

    private CardLayout cardLayout;
    private JPanel contentPanel;

    private JButton booksButton;
    private JButton studentsButton;
    private JButton borrowingButton;
    private JButton waitingQueueButton;
    private JButton reportsButton;
    private JButton exitButton;

    private BooksPanel booksPanel;
    private StudentsPanel studentsPanel;
    private BorrowingPanel borrowingPanel;
    private WaitingQueuePanel waitingQueuePanel;
    private ReportsPanel reportsPanel;

    public MainFrame() {
        setTitle("Digital Library Management System");
        setSize(1380, 800);
        setMinimumSize(new Dimension(1150, 680));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initLayout();
    }

    private void initLayout() {
        setLayout(new BorderLayout());

        JPanel sidebarPanel = createSidebarPanel();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        booksPanel = new BooksPanel();
        studentsPanel = new StudentsPanel();
        borrowingPanel = new BorrowingPanel();
        waitingQueuePanel = new WaitingQueuePanel();
        reportsPanel = new ReportsPanel();

        contentPanel.add(booksPanel, "books");
        contentPanel.add(studentsPanel, "students");
        contentPanel.add(borrowingPanel, "borrowing");
        contentPanel.add(waitingQueuePanel, "waitingQueue");
        contentPanel.add(reportsPanel, "reports");

        add(sidebarPanel, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);

        showPageAndRefresh("books", booksButton);
    }

    private JPanel createSidebarPanel() {
        JPanel sidebarPanel = new JPanel(new BorderLayout());
        sidebarPanel.setPreferredSize(new Dimension(240, 0));
        sidebarPanel.setBackground(UIHelper.SIDEBAR_COLOR);
        sidebarPanel.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));

        JLabel titleLabel = new JLabel(
                "<html><center>Digital Library<br>Management System</center></html>",
                SwingConstants.CENTER
        );
        titleLabel.setFont(UIHelper.TITLE_FONT);
        titleLabel.setForeground(UIHelper.WHITE_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 5, 30, 5));

        JPanel buttonsPanel = new JPanel(new GridLayout(6, 1, 0, 15));
        buttonsPanel.setOpaque(false);

        booksButton = UIHelper.createSidebarButton("Books");
        studentsButton = UIHelper.createSidebarButton("Students");
        borrowingButton = UIHelper.createSidebarButton("Borrowing");
        waitingQueueButton = UIHelper.createSidebarButton("Waiting Queue");
        reportsButton = UIHelper.createSidebarButton("Reports");
        exitButton = UIHelper.createSidebarButton("Exit");

        booksButton.addActionListener(e -> showPageAndRefresh("books", booksButton));
        studentsButton.addActionListener(e -> showPageAndRefresh("students", studentsButton));
        borrowingButton.addActionListener(e -> showPageAndRefresh("borrowing", borrowingButton));
        waitingQueueButton.addActionListener(e -> showPageAndRefresh("waitingQueue", waitingQueueButton));
        reportsButton.addActionListener(e -> showPageAndRefresh("reports", reportsButton));
        exitButton.addActionListener(e -> requestExit());

        buttonsPanel.add(booksButton);
        buttonsPanel.add(studentsButton);
        buttonsPanel.add(borrowingButton);
        buttonsPanel.add(waitingQueueButton);
        buttonsPanel.add(reportsButton);
        buttonsPanel.add(exitButton);

        sidebarPanel.add(titleLabel, BorderLayout.NORTH);
        sidebarPanel.add(buttonsPanel, BorderLayout.CENTER);
        return sidebarPanel;
    }

    private void requestExit() {
        int choice = UIHelper.showConfirmMessage(this, "Are you sure you want to exit?");
        if (choice == UIHelper.YES_OPTION) {
            System.exit(0);
        }
    }

    private void showPageAndRefresh(String pageName, JButton activeButton) {
        refreshPage(pageName);
        cardLayout.show(contentPanel, pageName);
        setActiveButton(activeButton);
    }

    private void refreshPage(String pageName) {
        switch (pageName) {
            case "books":
                booksPanel.refreshBooksTable();
                break;
            case "students":
                studentsPanel.refreshStudentsTable();
                break;
            case "borrowing":
                borrowingPanel.refreshRecordsTable();
                break;
            case "waitingQueue":
                waitingQueuePanel.refreshRequestsTable();
                break;
            case "reports":
                // Reports are refreshed manually by their report actions.
                break;
            default:
                break;
        }
    }

    private void setActiveButton(JButton activeButton) {
        UIHelper.setSidebarButtonInactive(booksButton);
        UIHelper.setSidebarButtonInactive(studentsButton);
        UIHelper.setSidebarButtonInactive(borrowingButton);
        UIHelper.setSidebarButtonInactive(waitingQueueButton);
        UIHelper.setSidebarButtonInactive(reportsButton);
        UIHelper.setSidebarButtonActive(activeButton);
    }
}
