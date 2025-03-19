package org.example;


import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class App {
    private static final String DB_URL = "jdbc:mysql://mysql-db1:3306/mydatabase";
    private static final String DB_USER = "appuser";
    private static final String DB_PASSWORD = "apppassword";

    public static void main(String[] args) {

        try (Scanner scanner = new Scanner(System.in)) {

            System.out.print("Enter your username: ");
            String username = scanner.nextLine();

            System.out.print("Enter your password: ");
            String password = scanner.nextLine();


            boolean isAuthenticated = authenticateUser(username, password);

            if (isAuthenticated) {

                System.out.print("Enter your name: ");
                String name = scanner.nextLine();

                System.out.print("Enter your average: ");
                double average = scanner.nextDouble();


                saveToDatabase(name, average);
                System.out.println("Data successfully saved to the database.");
            } else {
                System.out.println("Authentication failed. Cannot proceed.");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static boolean authenticateUser(String username, String password) {
        try {
            URL url = new URL("http://auth-service:8081/auth");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            String jsonInputString = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\"}";

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                return response.toString().equalsIgnoreCase("yes");
            }
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }

    private static void saveToDatabase(String name, double average) {
        String sql = "INSERT INTO my_table (name, average) VALUES (?, ?)";

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, name);
            pstmt.setDouble(2, average);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }
}