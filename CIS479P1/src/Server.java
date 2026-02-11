import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    public static final int SERVER_PORT = 5432;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Server listening on port " + SERVER_PORT);

            // Check for at least one user
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
                    System.out.println("Received: " + line); // server logs input
                    String response = processRequest(line);
                    out.println(response); // send back full response to client
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
            try {
                String stockSymbol = parts[1];
                double stockAmount = Double.parseDouble(parts[2]);
                double stockPrice = Double.parseDouble(parts[3]);
                int userId = Integer.parseInt(parts[4]);

                User user = StockDB.getUserByID(userId);
                if (user == null) return "User not found.";

                double totalCost = stockAmount * stockPrice;
                if (user.getBalance() < totalCost) return "Insufficient funds.";

                // Deduct balance
                double newBalance = user.getBalance() - totalCost;
                StockDB.updateUserBalance(userId, newBalance);

                // Add or update stock
                Stock stock = StockDB.getStockByUserAndSymbol(userId, stockSymbol);
                if (stock == null) {
                    stock = new Stock(0, stockSymbol, stockSymbol, stockAmount, userId);
                    StockDB.addStock(stock);
                } else {
                    stock.setStockBalance(stock.getStockBalance() + stockAmount);
                    StockDB.updateStock(stock);
                }

                // Return multi-line response
                return "200 OK\nBOUGHT: New balance: " + stock.getStockBalance() + " " + stockSymbol +
                        ". USD balance $" + String.format("%.2f", newBalance);

            } catch (Exception e) {
                return "400 Invalid command or format";
            }
        } else if (parts[0].equalsIgnoreCase("SELL")) {
            try {
                String stockSymbol = parts[1];
                double stockAmount = Double.parseDouble(parts[2]);
                double stockPrice = Double.parseDouble(parts[3]);
                int userId = Integer.parseInt(parts[4]);

                User user = StockDB.getUserByID(userId);
                if (user == null) return "User not found.";

                Stock stock = StockDB.getStockByUserAndSymbol(userId, stockSymbol);
                if (stock == null || stock.getStockBalance() < stockAmount)
                    return "Not enough " + stockSymbol + " stock balance.";

                // Deduct stock and add USD balance
                stock.setStockBalance(stock.getStockBalance() - stockAmount);
                StockDB.updateStock(stock);

                double totalGain = stockAmount * stockPrice;
                double newBalance = user.getBalance() + totalGain;
                StockDB.updateUserBalance(userId, newBalance);

                return "200 OK\nSOLD: New balance: " + stock.getStockBalance() + " " + stockSymbol +
                        ". USD balance $" + String.format("%.2f", newBalance);

            } catch (Exception e) {
                return "400 Invalid command or format";
            }
        }

        return "400 Invalid command";
    }
}
