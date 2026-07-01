package BackEnd;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Comparator;

public class BookWaitingQueue {
    private static final String DONE = "Done .";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private static WaitingRequest front = null;
    private static long nextArrivalOrder = 0;

    public static String addRequest(
            int requestId,
            int bookNumber,
            String studentId,
            String requestDate
    ) {
        if (requestId <= 0) {
            return "Request ID must be greater than 0.";
        }

        if (bookNumber <= 0) {
            return "Book Number must be greater than 0.";
        }

        if (isBlank(studentId)) {
            return "Student ID is required.";
        }

        if (!isValidDate(requestDate)) {
            return "Request date must be a real date in yyyy-MM-dd format.";
        }

        if (searchByRequestId(requestId) != null) {
            return "Waiting request already exists.";
        }

        Student student = StudentRegistry.findStudentById(studentId);
        if (student == null) {
            return "Student ID was not found. Register the student first.";
        }

        Book book = BookTree.search(bookNumber);
        if (book == null) {
            return "The book doesn't exist .";
        }

        if (book.getAvailableCopies() > 0) {
            return "This book is currently available, so a waiting request is not needed.";
        }

        if (hasWaitingRequestForStudentAndBook(bookNumber, student.getStudentId())) {
            return "This student already has a waiting request for this book.";
        }

        WaitingRequest newRequest = new WaitingRequest(
                requestId,
                bookNumber,
                student.getStudentId(),
                requestDate.trim(),
                nextArrivalOrder++
        );
        insertByPriority(newRequest);
        return DONE;
    }

    public static WaitingRequest peekNextRequest(int bookNumber) {
        WaitingRequest cur = front;
        while (cur != null) {
            if (cur.bookNumber == bookNumber) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    public static String validateServeNextRequest(int bookNumber) {
        if (bookNumber <= 0) {
            return "Book Number must be greater than 0.";
        }

        Book book = BookTree.search(bookNumber);
        if (book == null) {
            return "The book doesn't exist .";
        }

        if (peekNextRequest(bookNumber) == null) {
            return "There is no waiting request for this book.";
        }

        if (book.getAvailableCopies() <= 0) {
            return "There is no available copy for this book yet.";
        }

        return DONE;
    }

    public static String serveNextRequest(
            int bookNumber,
            int recordId,
            String expectedReturnDate
    ) {
        String validationMessage = validateServeNextRequest(bookNumber);
        if (!DONE.equals(validationMessage)) {
            return validationMessage;
        }

        if (recordId <= 0) {
            return "Record ID must be greater than 0.";
        }

        WaitingRequest nextRequest = peekNextRequest(bookNumber);
        String borrowResult = BorrowRecordList.borrowBookWithRecord(
                recordId,
                bookNumber,
                nextRequest.getStudentId(),
                expectedReturnDate
        );

        if (!DONE.equals(borrowResult)) {
            return borrowResult;
        }

        removeNextRequest(bookNumber);
        return DONE;
    }

    static void refreshPriorityForStudent(String studentId) {
        if (front == null) {
            return;
        }

        ArrayList<WaitingRequest> requests = getAllRequests();
        requests.sort(Comparator
                .comparing(WaitingRequest::isGraduatingStudent)
                .reversed()
                .thenComparingLong(WaitingRequest::getArrivalOrder));

        front = null;
        WaitingRequest tail = null;
        for (WaitingRequest request : requests) {
            request.next = null;
            if (front == null) {
                front = request;
                tail = request;
            } else {
                tail.next = request;
                tail = request;
            }
        }
    }

    public static WaitingRequest searchByRequestId(int requestId) {
        WaitingRequest cur = front;
        while (cur != null) {
            if (cur.requestId == requestId) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    public static ArrayList<WaitingRequest> getAllRequests() {
        ArrayList<WaitingRequest> requests = new ArrayList<>();
        WaitingRequest cur = front;
        while (cur != null) {
            requests.add(cur);
            cur = cur.next;
        }
        return requests;
    }

    public static ArrayList<WaitingRequest> getRequestsByBookNumber(int bookNumber) {
        ArrayList<WaitingRequest> requests = new ArrayList<>();
        WaitingRequest cur = front;
        while (cur != null) {
            if (cur.bookNumber == bookNumber) {
                requests.add(cur);
            }
            cur = cur.next;
        }
        return requests;
    }

    public static void printAllRequests() {
        if (front == null) {
            System.out.println("There are no waiting requests.");
            return;
        }

        WaitingRequest cur = front;
        while (cur != null) {
            System.out.println(cur);
            cur = cur.next;
        }
    }

    private static void insertByPriority(WaitingRequest newRequest) {
        if (front == null) {
            front = newRequest;
            return;
        }

        if (newRequest.isGraduatingStudent() && !front.isGraduatingStudent()) {
            newRequest.next = front;
            front = newRequest;
            return;
        }

        WaitingRequest cur = front;
        while (cur.next != null) {
            if (newRequest.isGraduatingStudent() && !cur.next.isGraduatingStudent()) {
                break;
            }
            cur = cur.next;
        }

        newRequest.next = cur.next;
        cur.next = newRequest;
    }

    private static void removeNextRequest(int bookNumber) {
        if (front == null) {
            return;
        }

        if (front.bookNumber == bookNumber) {
            WaitingRequest removedRequest = front;
            front = front.next;
            removedRequest.next = null;
            return;
        }

        WaitingRequest cur = front;
        while (cur.next != null) {
            if (cur.next.bookNumber == bookNumber) {
                WaitingRequest removedRequest = cur.next;
                cur.next = cur.next.next;
                removedRequest.next = null;
                return;
            }
            cur = cur.next;
        }
    }

    private static boolean hasWaitingRequestForStudentAndBook(int bookNumber, String studentId) {
        WaitingRequest cur = front;
        while (cur != null) {
            if (cur.bookNumber == bookNumber && cur.studentId.equalsIgnoreCase(studentId.trim())) {
                return true;
            }
            cur = cur.next;
        }
        return false;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static boolean isValidDate(String dateText) {
        if (isBlank(dateText)) {
            return false;
        }
        try {
            LocalDate.parse(dateText.trim(), DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
