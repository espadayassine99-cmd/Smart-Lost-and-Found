package lst_f;

import java.util.Objects;
import java.io.Serializable;

public abstract class User implements Serializable  {
	private static final long serialVersionUID = 1L;
    private final String id;
    private String fullName;
    private final String email;
    private String password;
    private final Role role;

    protected User(String id, String fullName, String email, String password, Role role) {
        this.id = id;
        this.fullName = fullName;
        this.email = email.toLowerCase();
        this.password = password;
        this.role = role;
    }
    public String getId() { return id; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public Role getRole() { return role; }

    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPassword(String password) { this.password = password; }

    public boolean checkPassword(String input) {
        return Objects.equals(password, input);
    }

    @Override
    public String toString() {
        return "[" + role + "] " + fullName + " <" + email + ">";
    }
}