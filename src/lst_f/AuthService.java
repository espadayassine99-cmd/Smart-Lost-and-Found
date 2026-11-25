
package lst_f;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class AuthService {

    private static final String FILE_NAME = "users.dat";

    private Map<String, User> users;
    private int counter;
    private User currentUser;

    public AuthService() {

        users = new HashMap<>();
        counter = 1;
        currentUser = null;

        
        loadUsers();

        
        if (users.isEmpty()) {

            User admin = new Admin(
                    "U0",
                    "Administrateur",
                    "admin@emsi.ma",
                    "admin123"
            );

            users.put(admin.getEmail().toLowerCase(), admin);

            saveUsers();
        }
    }

    public Student register(
            String fullName,
            String email,
            String password,
            String studentCode
    ) throws AuthException {

        validate(fullName, email, password, studentCode);

        String key = email.trim().toLowerCase();

        if (users.containsKey(key)) {
            throw new UserAlreadyExistsException(email);
        }

        Student student = new Student(
                "U" + counter,
                fullName.trim(),
                key,
                password,
                studentCode.trim()
        );

        users.put(key, student);

        counter++;

        saveUsers();

        return student;
    }

    // Connexion
    public User login(String email, String password)
            throws AuthException {

        if (email == null || password == null) {
            throw new InvalidCredentialsException();
        }

        String key = email.trim().toLowerCase();

        User user = users.get(key);

        if (user == null || !user.checkPassword(password)) {
            throw new InvalidCredentialsException();
        }

        currentUser = user;

        return currentUser;
    }

    // Déconnexion
    public void logout() {
        currentUser = null;
    }

    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    public boolean isAdmin() {

        return currentUser != null
                && currentUser.getRole() == Role.ADMIN;
    }

    public List<Student> listStudents() {

        return users.values().stream()

                .filter(user -> user.getRole() == Role.STUDENT)

                .map(user -> (Student) user)

                .sorted((s1, s2) ->
                        s1.getFullName()
                                .compareToIgnoreCase(s2.getFullName()))

                .collect(Collectors.toList());
    }

    public long countByRole(Role role) {

        return users.values().stream()

                .filter(user -> user.getRole() == role)

                .count();
    }

    public void deleteStudent(String email)
            throws AuthException {

        if (email == null || email.isBlank()) {
            throw new InvalidInputException("Email obligatoire.");
        }

        String key = email.trim().toLowerCase();

        User user = users.get(key);

        if (user == null || user.getRole() != Role.STUDENT) {
            throw new InvalidInputException("Étudiant introuvable.");
        }

        users.remove(key);

        saveUsers();
    }

    private void saveUsers() {

        try (ObjectOutputStream output =
                     new ObjectOutputStream(
                             new FileOutputStream(FILE_NAME))) {

            output.writeObject(users);
            output.writeInt(counter);

        } catch (IOException e) {

            System.out.println(
                    "Erreur lors de la sauvegarde des utilisateurs : "
                            + e.getMessage()
            );
        }
    }

    @SuppressWarnings("unchecked")
    private void loadUsers() {

        try (ObjectInputStream input =
                     new ObjectInputStream(
                             new FileInputStream(FILE_NAME))) {

            users = (Map<String, User>) input.readObject();

            counter = input.readInt();

        } catch (java.io.FileNotFoundException e) {

            users = new HashMap<>();
            counter = 1;

        } catch (IOException | ClassNotFoundException e) {

            System.out.println(
                    "Erreur lors du chargement des utilisateurs : "
                            + e.getMessage()
            );

            users = new HashMap<>();
            counter = 1;
        }
    }

    private void validate(
            String fullName,
            String email,
            String password,
            String code
    ) throws InvalidInputException {

        if (fullName == null || fullName.isBlank()) {
            throw new InvalidInputException(
                    "Le nom est obligatoire."
            );
        }

        if (email == null
                || !email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {

            throw new InvalidInputException(
                    "Email invalide."
            );
        }

        if (password == null || password.length() < 6) {
            throw new InvalidInputException(
                    "Le mot de passe doit contenir au moins 6 caractères."
            );
        }

        if (code == null || code.isBlank()) {
            throw new InvalidInputException(
                    "Le code étudiant est obligatoire."
            );
        }
    }
}
