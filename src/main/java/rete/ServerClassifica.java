package rete;

import com.sun.net.httpserver.HttpServer;
import persistenza.GestoreDatabase;

import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class ServerClassifica {
    private final GestoreDatabase database;
    private HttpServer server;
    private int porta = 8080;
    
    private static final String SECRET_KEY = "ArgoSecret2026!";

    public ServerClassifica(GestoreDatabase database) {
        this.database = database;
    }

    public void avvia() throws Exception {
        String portaCloud = System.getenv("PORT");
        
        if (portaCloud != null) {
            this.porta = Integer.parseInt(portaCloud);
            server = HttpServer.create(new InetSocketAddress(this.porta), 0);
            configuraRotte();
            server.setExecutor(null);
            server.start();
        } else {
            for (int tentativo = 8080; tentativo <= 8090; tentativo++) {
                try {
                    server = HttpServer.create(new InetSocketAddress(tentativo), 0);
                    this.porta = tentativo;
                    configuraRotte();
                    server.setExecutor(null);
                    server.start();
                    return;
                } catch (BindException bindException) {
                    if (tentativo == 8090) {
                        throw new Exception("Porte occupate.", bindException);
                    }
                }
            }
        }
    }

    private void configuraRotte() {
        server.createContext("/classifica", exchange -> {
            String risposta;
            try {
                int righe = database.contaRigheClassifica();
                String classifica = database.leggiClassificaFormattata();
                if (classifica == null || classifica.isBlank()) {
                    risposta = "Nessun dato nella classifica. [righe=" + righe + "]";
                } else {
                    risposta = "Righe presenti: " + righe + "\n" + classifica;
                }
            } catch (Exception e) {
                risposta = "Errore del server: " + e.getMessage();
            }
            byte[] bytesRisposta = risposta.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
            exchange.sendResponseHeaders(200, bytesRisposta.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytesRisposta);
            }
        });

        server.createContext("/salva", exchange -> {
            if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                try {
                    String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String[] parti = body.split(";");
                    if (parti.length == 3) {
                        String nome = parti[0];
                        int tempo = Integer.parseInt(parti[1]);
                        String tokenRicevuto = parti[2];
                        String tokenAtteso = generaTokenSicurezza(nome, tempo);
                        
                        if (tokenAtteso.equals(tokenRicevuto)) {
                            database.salvaRisultato(nome, tempo);
                            String risposta = "OK";
                            exchange.sendResponseHeaders(200, risposta.length());
                            try (OutputStream os = exchange.getResponseBody()) { os.write(risposta.getBytes()); }
                            return;
                        }
                    }
                } catch (Exception ignored) {}
            }
            String risposta = "Errore";
            exchange.sendResponseHeaders(400, risposta.length());
            try (OutputStream os = exchange.getResponseBody()) { os.write(risposta.getBytes()); }
        });
    }

    public int getPorta() {
        return porta;
    }

    public void arresta() {
        if (server != null) server.stop(0);
    }

    private static String generaTokenSicurezza(String nome, int tempo) {
        try {
            String input = nome + tempo + SECRET_KEY;
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return "";
        }
    }
}
