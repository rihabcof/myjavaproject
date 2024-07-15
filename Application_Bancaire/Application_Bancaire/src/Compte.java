import javax.swing.*;
import java.sql.*;

public class Compte {
    private int solde;

    public Compte() {
        this.solde = fetchSoldeFromDatabase();
    }

    private int fetchSoldeFromDatabase() {
        int solde = 0;
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/transaction", "root", "");
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT SUM(amount) AS totalAmount FROM transaction");

            if (resultSet.next()) {
                solde = resultSet.getInt("totalAmount");
            }

            resultSet.close();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return solde;
    }

    public void depotDe(int montant) {
        solde += montant;
        addTransaction("Depot", montant);
    }

    public void retraitDe(int montant) {
        if (montant > solde) {
            JOptionPane.showMessageDialog(null, "Erreur : Solde insuffisant pour effectuer le retrait.", "Erreur de retrait", JOptionPane.ERROR_MESSAGE);
        } else {
            solde -= montant;
            addTransaction("Retrait", montant);
        }
    }

    public int getSolde() {
        return solde;
    }

    public void updateSolde(int amount) {
        solde += amount;
    }

    private void addTransaction(String type, int amount) {
        try {
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/transaction", "root", "");
            String sql = "INSERT INTO transaction (type, date, amount) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, type);
            statement.setDate(2, new Date(System.currentTimeMillis()));
            statement.setInt(3, amount);
            statement.executeUpdate();
            statement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
