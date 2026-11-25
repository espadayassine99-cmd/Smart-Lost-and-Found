package lst_f;

import java.time.LocalDate;


public class Item {
    private String id;
    private ItemType type;
    private String name;
    private String description;
    private String photo;
    private LocalDate date;
    private String place;
    private Category category;
    private ItemStatus status;
    private String ownerEmail;
    public Item(String id, ItemType type, String name, String description, String photo,
                LocalDate date, String place, Category category,
                ItemStatus status, String ownerEmail) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.photo = photo;
        this.date = date;
        this.place = place;
        this.category = category;
        this.status = status;
        this.ownerEmail = ownerEmail;
    }

    public String getId() { return id; }
    public ItemType getType() { return type; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getPhoto() { return photo; }
    public LocalDate getDate() { return date; }
    public String getPlace() { return place; }
    public Category getCategory() { return category; }
    public ItemStatus getStatus() { return status; }
    public String getOwnerEmail() { return ownerEmail; }

    public void setStatus(ItemStatus status) { this.status = status; }

    public String toLine() {
        return id + ";" + type + ";" + name + ";" + description + ";" + photo + ";"
                + date + ";" + place + ";" + category + ";" + status + ";" + ownerEmail;
    }

    @Override
    public String toString() {
        return id + " | " + type + " | " + name + " | " + description + " | " + category
                + " | " + place + " | " + date + " | " + status;
    }
}