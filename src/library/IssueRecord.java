package library;

import java.time.LocalDate;

/** Records one book being issued to one member, and later returned. */
public class IssueRecord {
    public static final int LOAN_DAYS = 14;

    private final int id;
    private final int bookId;
    private final int memberId;
    private final LocalDate issueDate;
    private final LocalDate dueDate;
    private LocalDate returnDate; // null while the book is still out
    private double fineAmount;

    public IssueRecord(int id, int bookId, int memberId, LocalDate issueDate,
                       LocalDate dueDate, LocalDate returnDate, double fineAmount) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount;
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public LocalDate getIssueDate() { return issueDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public double getFineAmount() { return fineAmount; }
    public boolean isReturned() { return returnDate != null; }
    public boolean isOverdue() { return !isReturned() && LocalDate.now().isAfter(dueDate); }

    /** Marks the record returned and returns the fine charged. */
    public double markReturned(LocalDate date) {
        this.returnDate = date;
        this.fineAmount = new Fine(dueDate, date).getAmount();
        return fineAmount;
    }

    String toCsv() {
        return id + "," + bookId + "," + memberId + "," + issueDate + "," + dueDate + ","
                + (returnDate == null ? "" : returnDate) + "," + fineAmount;
    }
}
