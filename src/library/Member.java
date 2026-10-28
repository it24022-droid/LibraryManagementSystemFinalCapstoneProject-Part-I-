package library;

/** A registered library member who can borrow books. */
public class Member {
    private final int id;
    private String name;
    private String email;
    private String phone;

    public Member(int id, String name, String email, String phone) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }

    @Override public String toString() { return id + " - " + name; }
    String toCsv() { return id + "," + name + "," + email + "," + phone; }
}
