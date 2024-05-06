import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class PasswordManagerGUI extends JFrame {
    private JTextField websiteField, usernameField, passwordField;
    private JPasswordField masterPasswordField;
    private JButton addButton, displayButton, removeButton, unlockButton;
    private JTable passwordTable;
    private DefaultTableModel tableModel;
    private Connection connection;

    public PasswordManagerGUI() {
        setTitle("Password Manager");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBackground(Color.WHITE);

        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(4, 2));
        inputPanel.setBackground(Color.WHITE);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        JPanel buttonPanel = new JPanel();
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        buttonPanel.setBackground(Color.WHITE);

        websiteField = new JTextField(20);
        usernameField = new JTextField(20);
        passwordField = new JTextField(20);
        masterPasswordField = new JPasswordField(20);

        addButton = new JButton("Add Password");
        displayButton = new JButton("Display Passwords");
        removeButton = new JButton("Remove Password");
        unlockButton = new JButton("Unlock");

        websiteField.setBorder(BorderFactory.createTitledBorder("Website"));
        usernameField.setBorder(BorderFactory.createTitledBorder("Username"));
        passwordField.setBorder(BorderFactory.createTitledBorder("Password"));
        masterPasswordField.setBorder(BorderFactory.createTitledBorder("Master Password"));

        addButton.setBackground(new Color(0, 153, 51));
        addButton.setForeground(Color.WHITE);
        displayButton.setBackground(new Color(0, 102, 204));
        displayButton.setForeground(Color.WHITE);
        removeButton.setBackground(new Color(204, 0, 0));
        removeButton.setForeground(Color.WHITE);
        unlockButton.setBackground(new Color(255, 128, 0));
        unlockButton.setForeground(Color.WHITE);

        inputPanel.add(masterPasswordField);
        inputPanel.add(unlockButton);
        inputPanel.add(websiteField);
        inputPanel.add(usernameField);
        inputPanel.add(passwordField);
        inputPanel.add(addButton);
        inputPanel.add(displayButton);
        inputPanel.add(removeButton);

        tableModel = new DefaultTableModel();
        passwordTable = new JTable(tableModel);
        tableModel.addColumn("ID");
        tableModel.addColumn("Website");
        tableModel.addColumn("Username");
        tableModel.addColumn("Password");

        passwordTable.getTableHeader().setBackground(new Color(0, 102, 204));
        passwordTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(passwordTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        getContentPane().add(mainPanel);

        unlockButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                unlockPasswordManager();
            }
        });

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addPassword();
            }
        });

        displayButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                displayPasswords();
            }
        });

        removeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                removePassword();
            }
        });

        unlockButton.setEnabled(true);
        enableFunctionality(false);
    }

    private void enableFunctionality(boolean enabled) {
        websiteField.setEnabled(enabled);
        usernameField.setEnabled(enabled);
        passwordField.setEnabled(enabled);
        addButton.setEnabled(enabled);
        displayButton.setEnabled(enabled);
        removeButton.setEnabled(enabled);
    }

    private void unlockPasswordManager() {
        char[] enteredPassword = masterPasswordField.getPassword();
        String enteredPasswordStr = new String(enteredPassword);

        if (enteredPasswordStr.equals("your_master_password")) {
            enableFunctionality(true);
            masterPasswordField.setEnabled(false);
            unlockButton.setEnabled(false);
            masterPasswordField.setText("");
            displayPasswords();
        } else {
            JOptionPane.showMessageDialog(this, "Invalid master password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPassword() {
        String website = websiteField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            String sql = "INSERT INTO passwords (website, username, password) VALUES (?, ?, ?)";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, website);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3, password);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            JOptionPane.showMessageDialog(this, "Password added successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);

            websiteField.setText("");
            usernameField.setText("");
            passwordField.setText("");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error adding password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayPasswords() {
        tableModel.setRowCount(0);

        try {
            Statement statement = connection.createStatement();
            String sql = "SELECT * FROM passwords";
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String website = resultSet.getString("website");
                String username = resultSet.getString("username");
                String password = resultSet.getString("password");

                tableModel.addRow(new Object[]{id, website, username, password});
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error displaying passwords.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void removePassword() {
        int selectedRow = passwordTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a password to remove.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int selectedId = (int) passwordTable.getValueAt(selectedRow, 0);

        try {
            String sql = "DELETE FROM passwords WHERE id=?";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, selectedId);
            preparedStatement.executeUpdate();
            preparedStatement.close();

            resetIdValues(); // Call the method to reset ID values

            
            JOptionPane.showMessageDialog(this, "Password removed successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
            displayPasswords();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error removing password.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void resetIdValues() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT id FROM passwords ORDER BY id");

            int newId = 1;
            while (rs.next()) {
                int currentId = rs.getInt("id");
                String updateSQL = "UPDATE passwords SET id = ? WHERE id = ?";
                PreparedStatement updateStatement = connection.prepareStatement(updateSQL);
                updateStatement.setInt(1, newId);
                updateStatement.setInt(2, currentId);
                updateStatement.executeUpdate();
                updateStatement.close();
                newId++;
            }

            rs.close();
            stmt.close();

            // Find the maximum ID value
            int maxId = newId;

            Statement maxStmt = connection.createStatement();
            ResultSet maxRs = maxStmt.executeQuery("SELECT MAX(id) FROM passwords");
            if (maxRs.next()) {
                maxId = maxRs.getInt(1);
            }

            maxRs.close();
            maxStmt.close();

            if (newId <= maxId) {
                Statement incrementStmt = connection.createStatement();
                String incrementSQL = "ALTER TABLE passwords AUTO_INCREMENT = ?";
                incrementStmt.executeUpdate(incrementSQL);
                incrementStmt.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error resetting ID values.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/passwordmanager", "root", "admin123");
            Statement statement = connection.createStatement();
            String createTableSQL = "CREATE TABLE IF NOT EXISTS passwords (id INT AUTO_INCREMENT PRIMARY KEY, website VARCHAR(255), username VARCHAR(255), password VARCHAR(255))";
            statement.executeUpdate(createTableSQL);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database connection error.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            PasswordManagerGUI passwordManager = new PasswordManagerGUI();
            passwordManager.connectToDatabase();
            passwordManager.setVisible(true);
        });
    }
}