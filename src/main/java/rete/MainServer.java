package rete;

import persistenza.GestoreDatabase;

public class MainServer {
    public static void main(String[] args) {
        try {
            GestoreDatabase db = new GestoreDatabase();
            ServerClassifica server = new ServerClassifica(db);
            server.avvia();
            System.out.println("Server REST avviato con successo sulla porta: " + server.getPorta());
        } catch (Exception e) {
            System.err.println("Errore avvio server: " + e.getMessage());
        }
    }
}
