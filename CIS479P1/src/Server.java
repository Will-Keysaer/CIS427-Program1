import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    public static final int SERVER_PORT = 5432;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Server listening on port " + SERVER_PORT);

            ArrayList<User> users = StockDB.getUsers();
            if (users.isEmpty()) {
                System.out.println("No users found, creating default user...");
                User defaultUser = new User(0, "default@example.com", "Default", "User", "defaultuser", "password", 100.0);
                StockDB.addUser(defaultUser);
            }
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));

                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                String line;
                while ((line = in.readLine()) != null) {
                    System.out.println("Received: " + line);
                    out.println(line); // echo back
                }

                clientSocket.close();
                System.out.println("Client disconnected");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
