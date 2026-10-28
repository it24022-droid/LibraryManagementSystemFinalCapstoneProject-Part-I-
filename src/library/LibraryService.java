package library;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/** Holds all library data, applies the business rules and saves everything to CSV files. */
public class LibraryService {
    private final Path dir;
    private final List<User> users = new ArrayList<>();
    private final List<Book> books = new ArrayList<>();
    private final List<Member> members = new ArrayList<>();
    private final List<IssueRecord> records = new ArrayList<>();

    public LibraryService(String dataFolder) {
        this.dir = Paths.get(dataFolder);
        load();
        if (users.isEmpty()) seed();
    }

    // ---------- login ----------
    public User login(String username, String password) {
        for (User u : users)
            if (u.getUsername().equals(username) && u.checkPassword(password)) return u;
        return null;
    }

    // ---------- books ----------
    public List<Book> getBooks() { return books; }

    public Book findBook(int id) {
        for (Book b : books) if (b.getId() == id) return b;
        return null;
    }

    public List<Book> searchBooks(String keyword) {
        if (keyword == null || keyword.isBlank()) return books;
        return books.stream().filter(b -> b.matches(keyword.trim())).collect(Collectors.toList());
    }

    public Book addBook(String title, String author, String category, int copies) {
        Book b = new Book(nextId(books.stream().mapToInt(Book::getId)), clean(title), clean(author), clean(category), copies, copies);
        books.add(b);
        save();
        return b;
    }

    public boolean deleteBook(int id) {
        boolean onLoan = records.stream().anyMatch(r -> r.getBookId() == id && !r.isReturned());
        if (onLoan) return false;
        boolean removed = books.removeIf(b -> b.getId() == id);
        save();
        return removed;
    }

    // ---------- members ----------
    public List<Member> getMembers() { return members; }

    public Member findMember(int id) {
        for (Member m : members) if (m.getId() == id) return m;
        return null;
    }

    public Member addMember(String name, String email, String phone) {
        Member m = new Member(nextId(members.stream().mapToInt(Member::getId)), clean(name), clean(email), clean(phone));
        members.add(m);
        save();
        return m;
    }

    // ---------- issue / return ----------
    public List<IssueRecord> getRecords() { return records; }

    /** Issues a book. Returns null on success, otherwise an error message. */
    public String issueBook(int bookId, int memberId) {
        Book b = findBook(bookId);
        if (b == null) return "Book not found.";
        if (findMember(memberId) == null) return "Member not found.";
        if (!b.isAvailable()) return "No copies of this book are available.";
        long active = records.stream().filter(r -> r.getMemberId() == memberId && !r.isReturned()).count();
        if (active >= 3) return "Member already has 3 books (limit reached).";
        LocalDate today = LocalDate.now();
        records.add(new IssueRecord(nextId(records.stream().mapToInt(IssueRecord::getId)), bookId, memberId,
                today, today.plusDays(IssueRecord.LOAN_DAYS), null, 0));
        b.borrowCopy();
        save();
        return null;
    }

    /** Returns a book. Gives the fine charged, or -1 if the record is invalid. */
    public double returnBook(int recordId) {
        for (IssueRecord r : records) {
            if (r.getId() == recordId && !r.isReturned()) {
                double fine = r.markReturned(LocalDate.now());
                Book b = findBook(r.getBookId());
                if (b != null) b.returnCopy();
                save();
                return fine;
            }
        }
        return -1;
    }

    public List<IssueRecord> getOverdueRecords() {
        return records.stream().filter(IssueRecord::isOverdue).collect(Collectors.toList());
    }

    // ---------- storage ----------
    private int nextId(java.util.stream.IntStream ids) { return ids.max().orElse(0) + 1; }
    private String clean(String s) { return s == null ? "" : s.replace(",", " ").trim(); }

    private void seed() {
        users.add(new User("admin", "admin123", User.Role.ADMIN));
        users.add(new User("librarian", "lib123", User.Role.LIBRARIAN));
        addBook("Clean Code", "Robert C. Martin", "Programming", 3);
        addBook("Introduction to Algorithms", "Thomas H. Cormen", "Computer Science", 2);
        addBook("Database System Concepts", "Abraham Silberschatz", "Database", 2);
        addBook("Pather Panchali", "Bibhutibhushan Bandyopadhyay", "Novel", 1);
        addMember("Rahim Uddin", "rahim@example.com", "01700000001");
        addMember("Nusrat Jahan", "nusrat@example.com", "01700000002");
        save();
    }

    private void save() {
        try {
            Files.createDirectories(dir);
            write("users.csv", users.stream().map(User::toCsv).collect(Collectors.toList()));
            write("books.csv", books.stream().map(Book::toCsv).collect(Collectors.toList()));
            write("members.csv", members.stream().map(Member::toCsv).collect(Collectors.toList()));
            write("records.csv", records.stream().map(IssueRecord::toCsv).collect(Collectors.toList()));
        } catch (IOException e) {
            System.err.println("Could not save data: " + e.getMessage());
        }
    }

    private void write(String name, List<String> lines) throws IOException {
        Files.write(dir.resolve(name), lines);
    }

    private List<String[]> read(String name) {
        Path p = dir.resolve(name);
        List<String[]> rows = new ArrayList<>();
        if (!Files.exists(p)) return rows;
        try {
            for (String line : Files.readAllLines(p))
                if (!line.isBlank()) rows.add(line.split(",", -1));
        } catch (IOException e) {
            System.err.println("Could not read " + name);
        }
        return rows;
    }

    private void load() {
        for (String[] f : read("users.csv")) users.add(new User(f[0], f[1], User.Role.valueOf(f[2])));
        for (String[] f : read("books.csv"))
            books.add(new Book(Integer.parseInt(f[0]), f[1], f[2], f[3], Integer.parseInt(f[4]), Integer.parseInt(f[5])));
        for (String[] f : read("members.csv")) members.add(new Member(Integer.parseInt(f[0]), f[1], f[2], f[3]));
        for (String[] f : read("records.csv"))
            records.add(new IssueRecord(Integer.parseInt(f[0]), Integer.parseInt(f[1]), Integer.parseInt(f[2]),
                    LocalDate.parse(f[3]), LocalDate.parse(f[4]), f[5].isEmpty() ? null : LocalDate.parse(f[5]),
                    Double.parseDouble(f[6])));
    }
}
