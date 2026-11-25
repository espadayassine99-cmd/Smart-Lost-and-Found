package lst_f;

public class InvalidCredentialsException extends AuthException {
    public InvalidCredentialsException() {
        super("Email ou mot de passe incorrect.");
    }
}