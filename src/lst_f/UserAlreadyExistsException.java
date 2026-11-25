package lst_f;

public class UserAlreadyExistsException extends AuthException {
    public UserAlreadyExistsException(String email) {
        super("Un compte existe déjà avec l'email : " + email);
    }
}