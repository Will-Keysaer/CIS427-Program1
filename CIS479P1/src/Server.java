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
                    String response = processRequest(line);
                    out.println(response);
                }

                clientSocket.close();
                System.out.println("Client disconnected");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processRequest(String request) {

        String[] parts = request.split(" ");

        if (parts[0].equalsIgnoreCase("BUY")) {

            String stockSymbol = parts[1];
            double stockAmount = Double.parseDouble(parts[2]);
            double stockPrice = Double.parseDouble(parts[3]);
            int ID = (int) Double.parseDouble(parts[4]);


            User user = StockDB.getUserByID(ID);

            if (user == null) return "User not found.";

            double totalCost = stockAmount * stockPrice;

            if (user.getBalance() < totalCost)
                return "Insufficient funds.";

            // Deduct balance
            double newBalance = user.getBalance() - totalCost;
            StockDB.updateUserBalance(user.getId(), newBalance);

            // Add stock
            Stock stock = new Stock(0, stockSymbol, stockSymbol, stockAmount, user.getId());
            StockDB.addStock(stock);

            return "Stock purchased successfully.";

        }
        else if (parts[0].equalsIgnoreCase("SELL")){
            String stockSymbol = parts[1];
            double stockAmount = Double.parseDouble(parts[2]);
            double stockPrice = Double.parseDouble(parts[3]);
            int ID = (int) Double.parseDouble(parts[4]);

            double totalAmount = stockAmount * stockPrice;

            User user = StockDB.getUserByID(ID);
            double newBalance = user.getBalance() + totalAmount;
            StockDB.updateUserBalance(user.getId(), newBalance);
        }

        return "Invalid command.";
    }


}
