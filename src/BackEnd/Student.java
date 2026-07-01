package BackEnd;

public class Student {
    private final String studentId;
    private final String studentName;
    private boolean graduatingStudent;

    public Student(String studentId, String studentName, boolean graduatingStudent) {
        this.studentId = studentId;
        this.studentName = studentName;
        this.graduatingStudent = graduatingStudent;
    }

    public String getStudentId() {
        return studentId;
    }

    public String getStudentName() {
        return studentName;
    }

    public boolean isGraduatingStudent() {
        return graduatingStudent;
    }

    void setGraduatingStudent(boolean graduatingStudent) {
        this.graduatingStudent = graduatingStudent;
    }
}
