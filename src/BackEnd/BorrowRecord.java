package BackEnd;

public class BorrowRecord {
    int recordId;
    int bookNumber;
    String studentId;
    String studentName;
    String borrowDate;
    String expectedReturnDate;
    boolean returned;
    BorrowRecord next;

    public BorrowRecord(
            int recordId,
            int bookNumber,
            String studentId,
            String studentName,
            String borrowDate,
            String expectedReturnDate
    ) {
        this.recordId = recordId;
        this.bookNumber = bookNumber;
        this.studentId = studentId;
        this.studentName = studentName;
        this.borrowDate = borrowDate;
        this.expectedReturnDate = expectedReturnDate;
        this.returned = false;
        this.next = null;
    }

    public int getRecordId() {
        return recordId;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public String getBorrowDate() {
        return borrowDate;
    }

    public String getExpectedReturnDate() {
        return expectedReturnDate;
    }

    public boolean isReturned() {
        return returned;
    }

    @Override
    public String toString() {
        return "Record ID: " + recordId
                + ", Book Number: " + bookNumber
                + ", Student ID: " + studentId
                + ", Student Name: " + studentName
                + ", Borrow Date: " + borrowDate
                + ", Expected Return Date: " + expectedReturnDate
                + ", Returned: " + returned;
    }
}
