package BackEnd;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Stores the only source of truth for student identity during the current
 * application run. Borrowing and waiting requests must use an existing
 * Student ID instead of receiving a free-text name or graduating status.
 */
public final class StudentRegistry {
    private static final Map<String, Student> studentsByNormalizedId = new LinkedHashMap<>();

    private StudentRegistry() {
        // Utility class: do not create instances.
    }

    public static String registerStudent(String studentId, String studentName, boolean graduatingStudent) {
        if (isBlank(studentId)) {
            return "Student ID is required.";
        }

        if (isBlank(studentName)) {
            return "Student name is required.";
        }

        String normalizedId = normalizeId(studentId);
        if (studentsByNormalizedId.containsKey(normalizedId)) {
            return "A student with this Student ID already exists.";
        }

        Student student = new Student(
                normalizeDisplayText(studentId),
                normalizeDisplayText(studentName),
                graduatingStudent
        );
        studentsByNormalizedId.put(normalizedId, student);
        return "Done .";
    }

    public static Student findStudentById(String studentId) {
        if (isBlank(studentId)) {
            return null;
        }
        return studentsByNormalizedId.get(normalizeId(studentId));
    }

    public static boolean studentExists(String studentId) {
        return findStudentById(studentId) != null;
    }

    public static String updateGraduatingStatus(String studentId, boolean graduatingStudent) {
        Student student = findStudentById(studentId);
        if (student == null) {
            return "Student ID was not found. Register the student first.";
        }

        student.setGraduatingStudent(graduatingStudent);
        BookWaitingQueue.refreshPriorityForStudent(student.getStudentId());
        return "Done .";
    }

    public static ArrayList<Student> getAllStudents() {
        return new ArrayList<>(studentsByNormalizedId.values());
    }

    static void resetForTests() {
        studentsByNormalizedId.clear();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private static String normalizeId(String studentId) {
        return normalizeDisplayText(studentId).toLowerCase(Locale.ROOT);
    }

    private static String normalizeDisplayText(String text) {
        return text.trim().replaceAll("\\s+", " ");
    }
}
