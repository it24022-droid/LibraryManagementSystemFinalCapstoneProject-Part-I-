package library;

/** A book title kept in the library catalogue. */
public class Book {
    private final int id;
    private String title;
    private String author;
    private String category;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String title, String author, String category, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.category = category;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCategory() { return category; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
    public boolean isAvailable() { return availableCopies > 0; }

    public void borrowCopy() { if (availableCopies > 0) availableCopies--; }
    public void returnCopy() { if (availableCopies < totalCopies) availableCopies++; }

    public boolean matches(String keyword) {
        String k = keyword.toLowerCase();
        return title.toLowerCase().contains(k) || author.toLowerCase().contains(k)
                || category.toLowerCase().contains(k);
    }

    String toCsv() { return id + "," + title + "," + author + "," + category + "," + totalCopies + "," + availableCopies; }
}
