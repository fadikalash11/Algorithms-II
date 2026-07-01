package BackEnd;

/**
 * A waiting request keeps only the Student ID. The student's name and current
 * graduating status are always read from StudentRegistry so they cannot become
 * contradictory between requests.
 */
public class WaitingRequest {
    int requestId;
    int bookNumber;
    String studentId;
    String requestDate;
    long arrivalOrder;
    WaitingRequest next;

    public WaitingRequest(
            int requestId,
            int bookNumber,
            String studentId,
            String requestDate,
            long arrivalOrder
    ) {
        this.requestId = requestId;
        this.bookNumber = bookNumber;
        this.studentId = studentId;
        this.requestDate = requestDate;
        this.arrivalOrder = arrivalOrder;
        this.next = null;
    }

    public int getRequestId() {
        return requestId;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        Student student = StudentRegistry.findStudentById(studentId);
        return student == null ? "Unknown Student" : student.getStudentName();
    }

    public boolean isGraduatingStudent() {
        Student student = StudentRegistry.findStudentById(studentId);
        return student != null && student.isGraduatingStudent();
    }

    public String getRequestDate() {
        return requestDate;
    }

    long getArrivalOrder() {
        return arrivalOrder;
    }

    @Override
    public String toString() {
        return "Request ID: " + requestId
                + ", Book Number: " + bookNumber
                + ", Student ID: " + studentId
                + ", Student Name: " + getStudentName()
                + ", Graduating Student: " + isGraduatingStudent()
                + ", Request Date: " + requestDate;
    }
}
