package library;

import javax.swing.*;
import java.awt.*;

/** Login window for Admin and Librarian. */
public class LoginFrame extends JFrame {
    private final LibraryService service;
    private final JTextField usernameField = new JTextField(15);
    private final JPasswordField passwordField = new JPasswordField(15);

    public LoginFrame(LibraryService service) {
        super("Library Management System - Login");
        this.service = service;
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JLabel title = new JLabel("Library Management System", SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 20f));
        title.setBorder(BorderFactory.createEmptyBorder(15, 10, 5, 10));

        JPanel form = new JPanel(new GridLayout(2, 2, 8, 8));
        form.setBorder(BorderFactory.createEmptyBorder(10, 30, 10, 30));
        form.add(new JLabel("Username:")); form.add(usernameField);
        form.add(new JLabel("Password:")); form.add(passwordField);

        JButton loginBtn = new JButton("Login");
        loginBtn.addActionListener(e -> doLogin());
        getRootPane().setDefaultButton(loginBtn);
        JLabel hint = new JLabel("Demo: admin / admin123   or   librarian / lib123", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(11f));

        JPanel bottom = new JPanel(new GridLayout(2, 1, 4, 4));
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 30, 15, 30));
        bottom.add(loginBtn); bottom.add(hint);

        add(title, BorderLayout.NORTH);
        add(form, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(null);
    }

    private void doLogin() {
        User u = service.login(usernameField.getText().trim(), new String(passwordField.getPassword()));
        if (u == null) {
            JOptionPane.showMessageDialog(this, "Invalid username or password.", "Login failed", JOptionPane.ERROR_MESSAGE);
            return;
        }
        new MainFrame(service, u).setVisible(true);
        dispose();
    }
}
