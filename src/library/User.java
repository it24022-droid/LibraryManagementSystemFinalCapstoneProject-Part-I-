package library;

/** A system user (Admin or Librarian) who logs in to the application. */
public class User {
    public enum Role { ADMIN, LIBRARIAN }

    private final String username;
    private final String password;
    private final Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }
    public boolean checkPassword(String input) { return password.equals(input); }
    public boolean isAdmin() { return role == Role.ADMIN; }
    String toCsv() { return username + "," + password + "," + role; }
}
