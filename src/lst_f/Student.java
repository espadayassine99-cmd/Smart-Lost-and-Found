package lst_f;

public class Student extends User {
    private final String studentCode; // ex : numéro étudiant EMSI

    public Student(String id, String fullName, String email, String password, String studentCode) {
        super(id, fullName, email, password, Role.STUDENT);
        this.studentCode = studentCode;
    }

    public String getStudentCode() { return studentCode; }

    @Override
    public String toString() {
        return super.toString() + " (code: " + studentCode + ")";
    }
}