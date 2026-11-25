
package lst_f;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Scanner;

public class Main {

    static Scanner sc = new Scanner(System.in);

    static AuthService auth = new AuthService();
    static ItemService service = new ItemService(auth);

    public static void main(String[] args) {

        boolean running = true;

        while (running) {

            System.out.println(" SMART LOST & FOUND");
            System.out.println("1. Créer un compte étudiant");
            System.out.println("2. Se connecter");
            System.out.println("0. Quitter");

            String choix = lire("Choix");

            if (choix.equals("1")) {

                creerCompte();

            } else if (choix.equals("2")) {

                seConnecter();

            } else if (choix.equals("0")) {

                running = false;

            } else {

                System.out.println("Choix invalide.");
            }
        }

        System.out.println("Au revoir !");
        sc.close();
    }

    static String lire(String texte) {

        System.out.print(texte + " : ");

        return sc.nextLine().trim();
    }


    static LocalDate lireDate(String texte) throws ItemException {

        try {

            return LocalDate.parse(
                    lire(texte + " (AAAA-MM-JJ)")
            );

        } catch (DateTimeParseException e) {

            throw new ItemException("Date invalide.");
        }
    }

    // ---------- Lire une catégorie ----------

    static Category lireCategorie(boolean facultative)
            throws ItemException {

        Category[] cats = Category.values();

        if (facultative) {
            System.out.println("0. Toutes les catégories");
        }

        for (int i = 0; i < cats.length; i++) {

            System.out.println((i + 1) + ". " + cats[i]);
        }

        try {

            int n = Integer.parseInt(lire("Catégorie"));

            if (facultative && n == 0) {
                return null;
            }

            if (n < 1 || n > cats.length) {
                throw new ItemException("Catégorie invalide.");
            }

            return cats[n - 1];

        } catch (NumberFormatException e) {

            throw new ItemException("Veuillez saisir un nombre.");
        }
    }

    // ---------- Afficher les objets ----------

    static void afficher(List<Item> liste) {

        if (liste == null || liste.isEmpty()) {

            System.out.println("(Aucun objet)");

            return;
        }

        for (Item i : liste) {

            System.out.println(i);
        }
    }


    static void creerCompte() {

        try {

            String nom = lire("Nom complet");
            String email = lire("Email");
            String mdp = lire("Mot de passe");
            String code = lire("Code étudiant");

            Student s = auth.register(nom, email, mdp, code);

            System.out.println("Compte créé : " + s);

        } catch (AuthException e) {

            System.out.println("Erreur : " + e.getMessage());
        }
    }


    static void seConnecter() {

        try {

            String email = lire("Email");
            String mdp = lire("Mot de passe");

            User u = auth.login(email, mdp);

            System.out.println(
                    "Bienvenue " + u.getFullName() + " !"
            );

            if (auth.isAdmin()) {

                menuAdmin();

            } else {

                menuEtudiant();
            }

            auth.logout();

        } catch (AuthException e) {

            System.out.println("Erreur : " + e.getMessage());
        }
    }

    // ---------- Menu étudiant ----------
    static void menuEtudiant() {

        boolean continuer = true;

        while (continuer) {

            System.out.println("\n--- Menu Étudiant ---");

            System.out.println("1. Déclarer un objet perdu");
            System.out.println("2. Déclarer un objet trouvé");
            
            System.out.println("4. Mes déclarations");
            System.out.println("0. Déconnexion");

            String choix = lire("Choix");

            try {

                if (choix.equals("1")) {

                    declarerPerdu();

                } else if (choix.equals("2")) {

                    declarerTrouve();

                 

                } else if (choix.equals("4")) {

                    afficher(service.myItems());

                } else if (choix.equals("0")) {

                    continuer = false;

                } else {

                    System.out.println("Choix invalide.");
                }

            } catch (ItemException e) {

                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }


    static void menuAdmin() {

        boolean continuer = true;

        while (continuer) {

            System.out.println("\n--- Menu Administrateur ---");

            System.out.println("1. Voir tous les objets");
            System.out.println("2. Voir les déclarations en attente");
            System.out.println("3. Valider une déclaration");
            System.out.println("4. Rejeter une déclaration");
            System.out.println("5. Marquer un objet comme récupéré");
            System.out.println("6. Supprimer un objet");
            System.out.println("7. Voir les étudiants");
            System.out.println("0. Déconnexion");

            String choix = lire("Choix");

            try {

                if (choix.equals("1")) {

                    afficher(service.allItems());

                } else if (choix.equals("2")) {

                    afficher(service.pendingItems());

                } else if (choix.equals("3")) {

                    service.validateItem(
                            lire("ID de l'objet")
                    );

                    System.out.println("Déclaration validée.");

                } else if (choix.equals("4")) {

                    service.rejectItem(
                            lire("ID de l'objet")
                    );

                    System.out.println("Déclaration rejetée.");

                } else if (choix.equals("5")) {

                    service.markRecovered(
                            lire("ID de l'objet")
                    );

                    System.out.println(
                            "Objet marqué comme récupéré."
                    );

                } else if (choix.equals("6")) {

                    service.deleteItem(
                            lire("ID de l'objet")
                    );

                    System.out.println("Objet supprimé.");

                } else if (choix.equals("7")) {

                    afficherEtudiants();

                } else if (choix.equals("0")) {

                    continuer = false;

                } else {

                    System.out.println("Choix invalide.");
                }

            } catch (ItemException e) {

                System.out.println("Erreur : " + e.getMessage());
            }
        }
    }


    static void afficherEtudiants() {

        System.out.println(
                "Nombre d'étudiants : "
                + auth.countByRole(Role.STUDENT)
        );

        for (Student s : auth.listStudents()) {

            System.out.println(" - " + s);
        }
    }


    static void declarerPerdu() throws ItemException {

        String nom = lire("Nom de l'objet");
        String desc = lire("Description");
        String photo = lire("Photo (chemin, vide = aucune)");

        LocalDate date = lireDate("Date de perte");

        String lieu = lire("Lieu de perte");

        Category cat = lireCategorie(false);

        Item item = service.declareLost(
                nom,
                desc,
                photo,
                date,
                lieu,
                cat
        );

        System.out.println(
                "Objet perdu déclaré (en attente de validation) : "
                + item.getId()
        );
    }


    static void declarerTrouve() throws ItemException {

        String nom = lire("Nom de l'objet");
        String desc = lire("Description");
        String photo = lire("Photo (chemin, vide = aucune)");

        LocalDate date = lireDate("Date de découverte");

        String lieu = lire("Lieu de découverte");

        Category cat = lireCategorie(false);

        Item item = service.declareFound(
                nom,
                desc,
                photo,
                date,
                lieu,
                cat
        );

        System.out.println(
                "Objet trouvé déclaré (en attente de validation) : "
                + item.getId()
        );
    }


    
}
