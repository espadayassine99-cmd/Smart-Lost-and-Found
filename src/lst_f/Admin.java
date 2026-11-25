package lst_f;

public class Admin extends User {
    public Admin(String id, String fullName, String email, String password) {
        super(id, fullName, email, password, Role.ADMIN);
    }
}
