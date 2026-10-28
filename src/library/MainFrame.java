package library;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/** Main dashboard with tabs for Books, Members, Issue/Return and Reports. */
public class MainFrame extends JFrame {
    private final LibraryService service;
    private final User user;

    private final DefaultTableModel bookModel = model("ID", "Title", "Author", "Category", "Total", "Available");
    private final DefaultTableModel memberModel = model("ID", "Name", "Email", "Phone");
    private final DefaultTableModel recordModel = model("Record", "Book", "Member", "Issued", "Due", "Returned", "Fine (Tk)");
    private final DefaultTableModel overdueModel = model("Record", "Book", "Member", "Due", "Days late", "Fine so far (Tk)");
    private final JTextField searchField = new JTextField(20);

    public MainFrame(LibraryService service, User user) {
        super("Library Management System - " + user.getUsername() + " (" + user.getRole() + ")");
        this.service = service;
        this.user = user;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(850, 520);
        setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Books", booksTab());
        tabs.addTab("Members", membersTab());
        tabs.addTab("Issue / Return", issueTab());
        tabs.addTab("Overdue Report", overdueTab());
        tabs.addChangeListener(e -> refreshAll());
        add(tabs);

        JButton logout = new JButton("Logout");
        logout.addActionListener(e -> { new LoginFrame(service).setVisible(true); dispose(); });
        JPanel top = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        top.add(new JLabel("Logged in as " + user.getUsername() + " (" + user.getRole() + ")   "));
        top.add(logout);
        add(top, BorderLayout.NORTH);
        refreshAll();
    }

    private static DefaultTableModel model(String... cols) {
        return new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
    }

    // ---------- Books ----------
    private JPanel booksTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JButton searchBtn = new JButton("Search");
        searchBtn.addActionListener(e -> refreshBooks());
        JButton showAll = new JButton("Show all");
        showAll.addActionListener(e -> { searchField.setText(""); refreshBooks(); });
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Search (title/author/category):"));
        north.add(searchField); north.add(searchBtn); north.add(showAll);

        JTable table = new JTable(bookModel);
        JButton add = new JButton("Add Book");
        add.addActionListener(e -> addBookDialog());
        JButton del = new JButton("Delete Selected (Admin)");
        del.setEnabled(user.isAdmin());
        del.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row < 0) { msg("Select a book first."); return; }
            int id = (int) bookModel.getValueAt(row, 0);
            if (service.deleteBook(id)) refreshAll(); else msg("Cannot delete: book is currently issued.");
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(add); south.add(del);

        p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(table), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    private void addBookDialog() {
        JTextField title = new JTextField(), author = new JTextField(), cat = new JTextField(), copies = new JTextField("1");
        Object[] fields = {"Title:", title, "Author:", author, "Category:", cat, "Copies:", copies};
        if (JOptionPane.showConfirmDialog(this, fields, "Add Book", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
        try {
            int n = Integer.parseInt(copies.getText().trim());
            if (title.getText().isBlank() || author.getText().isBlank() || n < 1) throw new NumberFormatException();
            service.addBook(title.getText(), author.getText(), cat.getText(), n);
            refreshAll();
        } catch (NumberFormatException ex) {
            msg("Please fill title, author and a valid number of copies.");
        }
    }

    // ---------- Members ----------
    private JPanel membersTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JButton add = new JButton("Register Member");
        add.addActionListener(e -> {
            JTextField name = new JTextField(), email = new JTextField(), phone = new JTextField();
            Object[] f = {"Name:", name, "Email:", email, "Phone:", phone};
            if (JOptionPane.showConfirmDialog(this, f, "Register Member", JOptionPane.OK_CANCEL_OPTION) != JOptionPane.OK_OPTION) return;
            if (name.getText().isBlank()) { msg("Name is required."); return; }
            service.addMember(name.getText(), email.getText(), phone.getText());
            refreshAll();
        });
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        south.add(add);
        p.add(new JScrollPane(new JTable(memberModel)), BorderLayout.CENTER);
        p.add(south, BorderLayout.SOUTH);
        return p;
    }

    // ---------- Issue / Return ----------
    private JPanel issueTab() {
        JPanel p = new JPanel(new BorderLayout(5, 5));
        JTextField bookId = new JTextField(5), memberId = new JTextField(5), recordId = new JTextField(5);
        JButton issue = new JButton("Issue Book");
        issue.addActionListener(e -> {
            try {
                String err = service.issueBook(Integer.parseInt(bookId.getText().trim()), Integer.parseInt(memberId.getText().trim()));
                if (err != null) msg(err); else { msg("Book issued. Due in " + IssueRecord.LOAN_DAYS + " days."); refreshAll(); }
            } catch (NumberFormatException ex) { msg("Enter numeric Book ID and Member ID."); }
        });
        JButton ret = new JButton("Return Book");
        ret.addActionListener(e -> {
            try {
                double fine = service.returnBook(Integer.parseInt(recordId.getText().trim()));
                if (fine < 0) msg("Record not found or already returned.");
                else { msg(fine > 0 ? "Returned late. Fine: " + fine + " Tk" : "Returned on time. No fine."); refreshAll(); }
            } catch (NumberFormatException ex) { msg("Enter a numeric Record ID."); }
        });
        JPanel north = new JPanel(new FlowLayout(FlowLayout.LEFT));
        north.add(new JLabel("Book ID:")); north.add(bookId);
        north.add(new JLabel("Member ID:")); north.add(memberId); north.add(issue);
        north.add(new JLabel("   Record ID:")); north.add(recordId); north.add(ret);
        p.add(north, BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(recordModel)), BorderLayout.CENTER);
        return p;
    }

    // ---------- Overdue ----------
    private JPanel overdueTab() {
        JPanel p = new JPanel(new BorderLayout());
        p.add(new JLabel("  Books not returned after the due date (fine: Tk " + Fine.RATE_PER_DAY + " per day)"), BorderLayout.NORTH);
        p.add(new JScrollPane(new JTable(overdueModel)), BorderLayout.CENTER);
        return p;
    }

    // ---------- refresh ----------
    private void refreshAll() { refreshBooks(); refreshMembers(); refreshRecords(); refreshOverdue(); }

    private void refreshBooks() {
        bookModel.setRowCount(0);
        for (Book b : service.searchBooks(searchField.getText()))
            bookModel.addRow(new Object[]{b.getId(), b.getTitle(), b.getAuthor(), b.getCategory(), b.getTotalCopies(), b.getAvailableCopies()});
    }

    private void refreshMembers() {
        memberModel.setRowCount(0);
        for (Member m : service.getMembers())
            memberModel.addRow(new Object[]{m.getId(), m.getName(), m.getEmail(), m.getPhone()});
    }

    private void refreshRecords() {
        recordModel.setRowCount(0);
        for (IssueRecord r : service.getRecords())
            recordModel.addRow(new Object[]{r.getId(), bookName(r.getBookId()), memberName(r.getMemberId()),
                    r.getIssueDate(), r.getDueDate(), r.isReturned() ? r.getReturnDate() : "Not returned", r.getFineAmount()});
    }

    private void refreshOverdue() {
        overdueModel.setRowCount(0);
        List<IssueRecord> list = service.getOverdueRecords();
        for (IssueRecord r : list) {
            Fine f = new Fine(r.getDueDate(), java.time.LocalDate.now());
            overdueModel.addRow(new Object[]{r.getId(), bookName(r.getBookId()), memberName(r.getMemberId()),
                    r.getDueDate(), f.getDaysLate(), f.getAmount()});
        }
    }

    private String bookName(int id) { Book b = service.findBook(id); return b == null ? "(deleted)" : b.getTitle(); }
    private String memberName(int id) { Member m = service.findMember(id); return m == null ? "(unknown)" : m.getName(); }
    private void msg(String s) { JOptionPane.showMessageDialog(this, s); }
}
