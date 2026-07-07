package persistenza;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GestoreDatabase {
    private Connection connessione;

    private String urlDb() {
        return "jdbc:h2:file:./db/argo_database;AUTO_SERVER=TRUE;DB_CLOSE_DELAY=-1"; //[cite: 4]
    }

    public synchronized void inizializza() throws SQLException {
        if (connessione != null && !connessione.isClosed()) return; //[cite: 4]
        connessione = DriverManager.getConnection(urlDb(), "sa", ""); //[cite: 4]
        connessione.setAutoCommit(true); //[cite: 4]
        try (Statement stmt = connessione.createStatement()) { //[cite: 4]
            stmt.execute("CREATE TABLE IF NOT EXISTS Classifica (nome VARCHAR(50) NOT NULL, tempo INT NOT NULL)"); //[cite: 4]
        }
    }

    public synchronized void salvaRisultato(String nome, int tempo) throws SQLException {
        inizializza(); //[cite: 4]
        String sql = "INSERT INTO Classifica (nome, tempo) VALUES (?, ?)"; //[cite: 4]
        try (PreparedStatement pstmt = connessione.prepareStatement(sql)) { //[cite: 4]
            pstmt.setString(1, nome); //[cite: 4]
            pstmt.setInt(2, tempo); //[cite: 4]
            if (pstmt.executeUpdate() != 1) throw new SQLException("Insert fallito"); //[cite: 4]
        }
    }

    public synchronized int contaRigheClassifica() throws SQLException {
        inizializza(); //[cite: 4]
        try (Statement stmt = connessione.createStatement(); ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Classifica")) { //[cite: 4]
            rs.next(); //[cite: 4]
            return rs.getInt(1); //[cite: 4]
        }
    }

    public synchronized String leggiClassificaFormattata() throws SQLException {
        inizializza(); //[cite: 4]
        StringBuilder sb = new StringBuilder(); //[cite: 4]
        try (Statement stmt = connessione.createStatement(); ResultSet rs = stmt.executeQuery("SELECT nome, tempo FROM Classifica ORDER BY tempo ASC LIMIT 10")) { //[cite: 4]
            int posizione = 1; //[cite: 4]
            while (rs.next()) { //[cite: 4]
                String nome = rs.getString("nome"); //[cite: 4]
                int tempo = rs.getInt("tempo"); //[cite: 4]
                String tempoFormattato = (tempo / 60) + ":" + String.format("%02d", tempo % 60); //[cite: 4]
                sb.append(posizione).append(". ").append(nome).append(" - ").append(tempoFormattato).append("\n"); //[cite: 4]
                posizione++; //[cite: 4]
            }
        }
        return sb.toString().trim(); //[cite: 4]
    }

    public synchronized void chiudi() {
        try {
            if (connessione != null && !connessione.isClosed()) connessione.close(); //[cite: 4]
        } catch (SQLException ignored) {} //[cite: 4]
    }

    public synchronized void cancellaClassifica() throws SQLException {
        inizializza(); //[cite: 4]
        try (Statement stmt = connessione.createStatement()) { //[cite: 4]
            stmt.executeUpdate("DELETE FROM Classifica"); //[cite: 4]
        }
    }
}
