import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Main {
    private JLabel balanceLabel;
    private JButton retrieveButton;
    private JButton quitButton;
    private JButton depositButton;
    private JButton showAllButton;
    private JTextPane textPane;
    private Compte compte;
    private Connection connection;

    public Main() {
        compte = new Compte();

        connectToDatabase();

        JFrame frame = new JFrame("Banque");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 400);

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        balanceLabel = new JLabel("Votre solde : $" + compte.getSolde() + " DT");
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 20));
        balanceLabel.setHorizontalAlignment(SwingConstants.CENTER);
        balanceLabel.setOpaque(true);
        updateBalanceLabelColor(); // Set initial color
        panel.add(balanceLabel, BorderLayout.NORTH);

        textPane = new JTextPane();
        textPane.setFont(new Font("Arial", Font.PLAIN, 14));
        textPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textPane);
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        retrieveButton = new JButton("Retrait");
        depositButton = new JButton("Depot");
        quitButton = new JButton("Quit");
        showAllButton = new JButton("showAll");

        retrieveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(frame, "Entrez le montant à retirer:");
                if (input != null && !input.isEmpty()) {
                    int amount = Integer.parseInt(input);
                    compte.retraitDe(amount);
                    updateBalanceLabel();
                    clearTransactions(); // Clear transactions from display
                }
            }
        });

        depositButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String input = JOptionPane.showInputDialog(frame, "Entrez le montant à déposer:");
                if (input != null && !input.isEmpty()) {
                    int amount = Integer.parseInt(input);
                    compte.depotDe(amount);
                    updateBalanceLabel();
                    clearTransactions(); // Clear transactions from display
                }
            }
        });

        quitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveAndQuit();
            }
        });

        showAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayAllTransactions();
            }
        });

        buttonPanel.add(retrieveButton);
        buttonPanel.add(depositButton);
        buttonPanel.add(quitButton);
        buttonPanel.add(showAllButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);
        frame.setVisible(true);
    }

    private void updateBalanceLabel() {
        balanceLabel.setText("Votre solde : $" + compte.getSolde() + " DT");
        updateBalanceLabelColor();
    }

    private void updateBalanceLabelColor() {
        int solde = compte.getSolde();
        if (solde < 100 && solde > 50) {
            balanceLabel.setBackground(Color.YELLOW);
        } else if (solde < 50) {
            balanceLabel.setBackground(Color.RED);
        } else {
            balanceLabel.setBackground(Color.GREEN);
        }
    }

    private void connectToDatabase() {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/transaction", "root", "");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

   
    private void clearTransactions() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                StyledDocument doc = textPane.getStyledDocument();
                try {
                    doc.remove(0, doc.getLength());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void displayAllTransactions() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM transaction");

            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    try {
                        StyledDocument doc = textPane.getStyledDocument();
                        doc.remove(0, doc.getLength());

                        Style style = textPane.addStyle("BoldStyle", null);
                        StyleConstants.setBold(style, true);

                        while (resultSet.next()) {
                            int id = resultSet.getInt("id");
                            String type = resultSet.getString("type");
                            String date = resultSet.getString("date");
                            int amount = resultSet.getInt("amount");
                            String time = resultSet.getString("Time");

                            doc.insertString(doc.getLength(), "Numéro de transaction: " + id + ", Type de transaction: ", null);
                            doc.insertString(doc.getLength(), type, style);
                            doc.insertString(doc.getLength(), ", Date: ", null);
                            doc.insertString(doc.getLength(), date, style);
                            doc.insertString(doc.getLength(), ", Time: ", null);
                            doc.insertString(doc.getLength(), (time != null ? time : "null"), style);
                            doc.insertString(doc.getLength(), ", Montant: " + amount + " DT\n", null);
                        }

                        resultSet.close();
                        statement.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveAndQuit() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT SUM(amount) AS totalAmount FROM transaction");

            int totalAmount = 0;
            if (resultSet.next()) {
                totalAmount = resultSet.getInt("totalAmount");
            }

            compte.updateSolde(totalAmount);

            resultSet.close();
            statement.close();

            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Main();
            }
        });
    }
}
