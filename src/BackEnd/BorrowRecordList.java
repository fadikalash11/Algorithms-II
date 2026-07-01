package BackEnd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

public class BorrowRecordList {
    private static final String DONE = "Done .";
    private static final int MAX_BORROW_LIMIT = 3;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static BorrowRecord head = null;

    public static String borrowBookWithRecord(
            int recordId,
            int bookNumber,
            String studentId,
            String expectedReturnDate
    ) {
        if (recordId <= 0) {
            return "Record ID must be greater than 0.";
        }

        if (bookNumber <= 0) {
            return "Book number must be greater than 0.";
        }

        if (isBlank(studentId)) {
            return "Student ID is required.";
        }

        Student student = StudentRegistry.findStudentById(studentId);
        if (student == null) {
            return "Student ID was not found. Register the student first.";
        }

        if (searchByRecordId(recordId) != null) {
            return "Borrow record already exists.";
        }

        LocalDate today = LocalDate.now();
        LocalDate expectedDate = parseDate(expectedReturnDate, "Expected return date");
        if (expectedDate == null) {
            return "Expected return date must be a real date in yyyy-MM-dd format.";
        }

        if (expectedDate.isBefore(today)) {
            return "Expected return date cannot be before the borrow date.";
        }

        if (!canBorrowMore(student.getStudentId())) {
            return "Borrowing failed. The student has reached the maximum borrow limit.";
        }

        String borrowResult = BookTree.BorrowBook(bookNumber);
        if (!DONE.equals(borrowResult)) {
            return borrowResult;
        }

        BorrowRecord newRecord = new BorrowRecord(
                recordId,
                bookNumber,
                student.getStudentId(),
                student.getStudentName(),
                today.format(DATE_FORMATTER),
                expectedDate.format(DATE_FORMATTER)
        );
        appendRecord(newRecord);
        return DONE;
    }

    public static BorrowRecord searchByRecordId(int recordId) {
        BorrowRecord cur = head;
        while (cur != null) {
            if (cur.recordId == recordId) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    public static ArrayList<BorrowRecord> searchByStudentName(String studentName) {
        ArrayList<BorrowRecord> result = new ArrayList<>();
        if (isBlank(studentName)) {
            return result;
        }

        String normalizedName = studentName.trim();
        BorrowRecord cur = head;
        while (cur != null) {
            if (cur.studentName.equalsIgnoreCase(normalizedName)) {
                result.add(cur);
            }
            cur = cur.next;
        }
        return result;
    }

    public static ArrayList<BorrowRecord> searchByStudentId(String studentId) {
        ArrayList<BorrowRecord> result = new ArrayList<>();
        if (isBlank(studentId)) {
            return result;
        }

        String normalizedId = studentId.trim();
        BorrowRecord cur = head;
        while (cur != null) {
            if (cur.studentId.equalsIgnoreCase(normalizedId)) {
                result.add(cur);
            }
            cur = cur.next;
        }
        return result;
    }

    public static String updateExpectedReturnDate(int recordId, String newExpectedReturnDate) {
        if (recordId <= 0) {
            return "Record ID must be greater than 0.";
        }

        BorrowRecord record = searchByRecordId(recordId);
        if (record == null) {
            return "Borrow record not found.";
        }

        if (record.returned) {
            return "Expected return date cannot be changed because this book was returned.";
        }

        LocalDate newDate = parseDate(newExpectedReturnDate, "Expected return date");
        if (newDate == null) {
            return "Expected return date must be a real date in yyyy-MM-dd format.";
        }

        LocalDate borrowDate = parseDate(record.borrowDate, "Borrow date");
        if (borrowDate == null) {
            return "Borrow date is stored in an invalid format.";
        }

        if (newDate.isBefore(borrowDate)) {
            return "Expected return date cannot be before the borrow date.";
        }

        record.expectedReturnDate = newDate.format(DATE_FORMATTER);
        return DONE;
    }

    public static String returnBorrowedBook(int recordId) {
        if (recordId <= 0) {
            return "Record ID must be greater than 0.";
        }

        BorrowRecord record = searchByRecordId(recordId);
        if (record == null) {
            return "Borrow record not found.";
        }

        if (record.returned) {
            return "This borrow record is already returned.";
        }

        String result = BookTree.returnBook(record.bookNumber);
        if (!DONE.equals(result)) {
            return result;
        }

        record.returned = true;
        return DONE;
    }

    public static int countActiveBorrowRecords(String studentId) {
        if (isBlank(studentId)) {
            return 0;
        }

        int count = 0;
        String normalizedId = studentId.trim();
        BorrowRecord cur = head;
        while (cur != null) {
            if (cur.studentId.equalsIgnoreCase(normalizedId) && !cur.returned) {
                count++;
            }
            cur = cur.next;
        }
        return count;
    }

    public static boolean canBorrowMore(String studentId) {
        return countActiveBorrowRecords(studentId) < MAX_BORROW_LIMIT;
    }

    public static ArrayList<BorrowRecord> getAllRecords() {
        ArrayList<BorrowRecord> records = new ArrayList<>();
        BorrowRecord cur = head;
        while (cur != null) {
            records.add(cur);
            cur = cur.next;
        }
        return records;
    }

    public static void printAllRecords() {
        if (head == null) {
            System.out.println("There are no borrow records.");
            return;
        }

        BorrowRecord cur = head;
        while (cur != null) {
            System.out.println(cur);
            cur = cur.next;
        }
    }

    private static void appendRecord(BorrowRecord newRecord) {
        if (head == null) {
            head = newRecord;
            return;
        }

        BorrowRecord cur = head;
        while (cur.next != null) {
            cur = cur.next;
        }
        cur.next = newRecord;
    }

    private static int countBorrowRecords() {
        int count = 0;
        BorrowRecord cur = head;
        while (cur != null) {
            count++;
            cur = cur.next;
        }
        return count;
    }

    // The most borrowed books
    private static int findBookIndex(int[] bookNumbers, int size, int bookNumber) {
        for (int i = 0; i < size; i++) {
            if (bookNumbers[i] == bookNumber) {
                return i;
            }
        }
        return -1;
    }

    public static String getMostBorrowedBooksReport() {
        int recordsCount = countBorrowRecords();
        if (recordsCount == 0) {
            return "There are no borrow records.";
        }

        int[] bookNumbers = new int[recordsCount];
        int[] borrowCounts = new int[recordsCount];
        int uniqueBooks = 0;

        BorrowRecord cur = head;
        while (cur != null) {
            int index = findBookIndex(bookNumbers, uniqueBooks, cur.bookNumber);
            if (index == -1) {
                bookNumbers[uniqueBooks] = cur.bookNumber;
                borrowCounts[uniqueBooks] = 1;
                uniqueBooks++;
            } else {
                borrowCounts[index]++;
            }
            cur = cur.next;
        }

        int max = borrowCounts[0];
        for (int i = 1; i < uniqueBooks; i++) {
            if (borrowCounts[i] > max) {
                max = borrowCounts[i];
            }
        }

        StringBuilder report = new StringBuilder("Most borrowed book(s):\n");
        for (int i = 0; i < uniqueBooks; i++) {
            if (borrowCounts[i] == max) {
                report.append("Book Number: ")
                        .append(bookNumbers[i])
                        .append(", Borrow Count: ")
                        .append(borrowCounts[i])
                        .append("\n");
            }
        }
        return report.toString();
    }

    // The most read authors
    private static int findAuthorIndex(String[] authors, int size, String author) {
        for (int i = 0; i < size; i++) {
            if (authors[i].equalsIgnoreCase(author)) {
                return i;
            }
        }
        return -1;
    }

    public static String getMostReadAuthorsReport() {
        int recordsCount = countBorrowRecords();
        if (recordsCount == 0) {
            return "There are no borrow records.";
        }

        String[] authors = new String[recordsCount];
        int[] authorCounts = new int[recordsCount];
        int uniqueAuthors = 0;

        BorrowRecord cur = head;
        while (cur != null) {
            Book book = BookTree.search(cur.bookNumber);
            if (book != null) {
                String author = book.getAuthor();
                int index = findAuthorIndex(authors, uniqueAuthors, author);
                if (index == -1) {
                    authors[uniqueAuthors] = author;
                    authorCounts[uniqueAuthors] = 1;
                    uniqueAuthors++;
                } else {
                    authorCounts[index]++;
                }
            }
            cur = cur.next;
        }

        if (uniqueAuthors == 0) {
            return "No authors found for the borrowed books.";
        }

        int max = authorCounts[0];
        for (int i = 1; i < uniqueAuthors; i++) {
            if (authorCounts[i] > max) {
                max = authorCounts[i];
            }
        }

        StringBuilder report = new StringBuilder("Most read author(s):\n");
        for (int i = 0; i < uniqueAuthors; i++) {
            if (authorCounts[i] == max) {
                report.append("Author: ")
                        .append(authors[i])
                        .append(", Borrow Count: ")
                        .append(authorCounts[i])
                        .append("\n");
            }
        }
        return report.toString();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static LocalDate parseDate(String text, String fieldName) {
        if (isBlank(text)) {
            return null;
        }

        try {
            return LocalDate.parse(text.trim(), DATE_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }
}
