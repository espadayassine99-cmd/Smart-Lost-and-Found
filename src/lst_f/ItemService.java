
package lst_f;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class ItemService {

    private static final String FILE_NAME = "items.txt";

    private final List<Item> items = new ArrayList<>();
    private int counter = 1;
    private final AuthService auth;

    // Constructeur
    public ItemService(AuthService auth) {

        if (auth == null) {
            throw new IllegalArgumentException(
                    "AuthService obligatoire."
            );
        }

        this.auth = auth;
        loadItems();
    }

    // Chargement des objets
    private void loadItems() {

        File file = new File(FILE_NAME);

        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {

                lineNumber++;

                try {

                    String[] p = line.split(";", -1);

                    if (p.length != 10) {
                        continue;
                    }

                    Item item = new Item(
                            p[0],
                            ItemType.valueOf(p[1]),
                            p[2],
                            p[3],
                            p[4],
                            LocalDate.parse(p[5]),
                            p[6],
                            Category.valueOf(p[7]),
                            ItemStatus.valueOf(p[8]),
                            p[9]
                    );

                    items.add(item);

                    int number =
                            Integer.parseInt(p[0].substring(1));

                    if (number >= counter) {
                        counter = number + 1;
                    }

                } catch (Exception e) {

                    System.out.println(
                            "Ligne " + lineNumber
                                    + " ignorée : " + e.getMessage()
                    );
                }
            }

        } catch (IOException e) {

            System.out.println(
                    "Erreur de lecture : " + e.getMessage()
            );
        }
    }

    // Sauvegarde avec un thread simple
    private void saveItems() {

        Thread thread = new Thread(() -> {

            System.out.println(
                    "Sauvegarde exécutée par : "
                            + Thread.currentThread().getName()
            );

            synchronized (items) {

                try (BufferedWriter writer =
                             new BufferedWriter(
                                     new FileWriter(FILE_NAME))) {

                    for (Item item : items) {
                        writer.write(item.toLine());
                        writer.newLine();
                    }

                    System.out.println("Sauvegarde terminée.");

                } catch (IOException e) {

                    System.out.println(
                            "Erreur de sauvegarde : "
                                    + e.getMessage()
                    );
                }
            }

        });

        thread.setName("Thread-Sauvegarde");
        thread.start();
    }

    // Vérification étudiant
    private User currentStudent() throws ItemException {

        User user = auth.getCurrentUser()
                .orElseThrow(() ->
                        new ItemException(
                                "Vous devez être connecté."
                        )
                );

        if (user.getRole() != Role.STUDENT) {

            throw new ItemException(
                    "Action réservée aux étudiants."
            );
        }

        return user;
    }

    // Vérification administrateur
    private void requireAdmin() throws ItemException {

        if (!auth.isAdmin()) {

            throw new ItemException(
                    "Action réservée à l'administrateur."
            );
        }
    }

    // Déclarer un objet perdu
    public Item declareLost(
            String name,
            String description,
            String photo,
            LocalDate date,
            String place,
            Category category
    ) throws ItemException {

        return add(
                ItemType.LOST,
                name,
                description,
                photo,
                date,
                place,
                category
        );
    }

    // Déclarer un objet trouvé
    public Item declareFound(
            String name,
            String description,
            String photo,
            LocalDate date,
            String place,
            Category category
    ) throws ItemException {

        return add(
                ItemType.FOUND,
                name,
                description,
                photo,
                date,
                place,
                category
        );
    }

    // Ajouter un objet
    private Item add(
            ItemType type,
            String name,
            String description,
            String photo,
            LocalDate date,
            String place,
            Category category
    ) throws ItemException {

        User user = currentStudent();

        name = name == null ? "" : name.trim();
        description = description == null ? "" : description.trim();
        photo = photo == null ? "" : photo.trim();
        place = place == null ? "" : place.trim();

        if (name.isBlank() || place.isBlank()) {

            throw new ItemException(
                    "Le nom et le lieu sont obligatoires."
            );
        }

        if (date == null || date.isAfter(LocalDate.now())) {

            throw new ItemException(
                    "Date invalide : elle ne peut pas être dans le futur."
            );
        }

        if (category == null) {
            throw new ItemException("Catégorie obligatoire.");
        }

        String p = photo.toLowerCase(Locale.ROOT);

        if (!p.isEmpty()
                && !p.endsWith(".jpg")
                && !p.endsWith(".jpeg")
                && !p.endsWith(".png")) {

            throw new ItemException(
                    "La photo doit être un fichier .jpg, .jpeg ou .png."
            );
        }

        if (name.contains(";")
                || description.contains(";")
                || photo.contains(";")
                || place.contains(";")
                || user.getEmail().contains(";")) {

            throw new ItemException(
                    "Le caractère ';' est interdit."
            );
        }

        Item item;

        synchronized (items) {

            item = new Item(
                    "O" + counter,
                    type,
                    name,
                    description,
                    photo,
                    date,
                    place,
                    category,
                    ItemStatus.EN_ATTENTE,
                    user.getEmail()
            );

            counter++;

            items.add(item);
        }

        saveItems();

        return item;
    }

    // Mes objets
    public List<Item> myItems() throws ItemException {

        String email = currentStudent().getEmail();

        synchronized (items) {

            return items.stream()
                    .filter(i -> i.getOwnerEmail()
                            .equalsIgnoreCase(email))
                    .collect(Collectors.toList());
        }
    }

    // Recherche avec un thread simple
    public void searchAsync(
            String keyword,
            Category category,
            String place
    ) throws ItemException {

        currentStudent();

        Thread thread = new Thread(() -> {

            System.out.println(
                    "Recherche exécutée par : "
                            + Thread.currentThread().getName()
            );

            String k = keyword == null
                    ? "" : keyword.trim().toLowerCase(Locale.ROOT);

            String pl = place == null
                    ? "" : place.trim().toLowerCase(Locale.ROOT);

            List<Item> copy;

            synchronized (items) {
                copy = new ArrayList<>(items);
            }

            List<Item> results = copy.stream()

                    .filter(i ->
                            i.getStatus() == ItemStatus.VALIDE
                    )

                    .filter(i ->
                            k.isEmpty()
                                    || i.getName().toLowerCase(Locale.ROOT)
                                    .contains(k)
                                    || i.getDescription().toLowerCase(Locale.ROOT)
                                    .contains(k)
                    )

                    .filter(i ->
                            category == null
                                    || i.getCategory() == category
                    )

                    .filter(i ->
                            pl.isEmpty()
                                    || i.getPlace().toLowerCase(Locale.ROOT)
                                    .contains(pl)
                    )

                    .collect(Collectors.toList());

            System.out.println(
                    "Nombre de résultats : " + results.size()
            );

            for (Item item : results) {
                System.out.println(item.getName());
            }

        });

        thread.setName("Thread-Recherche");
        thread.start();
    }

    // Tous les objets
    public List<Item> allItems() throws ItemException {

        requireAdmin();

        synchronized (items) {
            return new ArrayList<>(items);
        }
    }

    // Objets en attente
    public List<Item> pendingItems() throws ItemException {

        requireAdmin();

        synchronized (items) {

            return items.stream()
                    .filter(i ->
                            i.getStatus() == ItemStatus.EN_ATTENTE
                    )
                    .collect(Collectors.toList());
        }
    }

    // Valider un objet
    public void validateItem(String id) throws ItemException {

        requireAdmin();

        synchronized (items) {

            Item item = findItem(id);

            if (item.getStatus() != ItemStatus.EN_ATTENTE) {

                throw new ItemException(
                        "Cette déclaration n'est pas en attente."
                );
            }

            item.setStatus(ItemStatus.VALIDE);
        }

        saveItems();
    }

    // Rejeter un objet
    public void rejectItem(String id) throws ItemException {

        requireAdmin();

        synchronized (items) {

            Item item = findItem(id);

            if (item.getStatus() != ItemStatus.EN_ATTENTE) {

                throw new ItemException(
                        "Cette déclaration n'est pas en attente."
                );
            }

            item.setStatus(ItemStatus.REJETE);
        }

        saveItems();
    }

    // Marquer comme récupéré
    public void markRecovered(String id) throws ItemException {

        requireAdmin();

        synchronized (items) {

            Item item = findItem(id);

            if (item.getStatus() != ItemStatus.VALIDE) {

                throw new ItemException(
                        "Seul un objet validé peut être marqué comme récupéré."
                );
            }

            item.setStatus(ItemStatus.RECUPERE);
        }

        saveItems();
    }

    // Supprimer un objet
    public void deleteItem(String id) throws ItemException {

        requireAdmin();

        synchronized (items) {

            Item item = findItem(id);

            items.remove(item);
        }

        saveItems();
    }

    // Trouver un objet par ID
    private Item findItem(String id) throws ItemException {

        if (id == null || id.isBlank()) {

            throw new ItemException(
                    "Identifiant obligatoire."
            );
        }

        return items.stream()
                .filter(i ->
                        i.getId().equalsIgnoreCase(id.trim())
                )
                .findFirst()
                .orElseThrow(() ->
                        new ItemException(
                                "Objet introuvable : " + id
                        )
                );
    }
}
