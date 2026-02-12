import java.io.*;
import java.net.*;
import java.util.ArrayList;

public class Server {

    public static final int SERVER_PORT = 4080;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Server listening on port " + SERVER_PORT);

            // Ensure at least one user exists
            ArrayList<User> users = StockDB.getUsers();
            if (users.isEmpty()) {
                System.out.println("No users found. Creating default user...");
                User defaultUser = new User(0,
                        "default@example.com",
                        "John",
                        "Doe",
                        "johndoe",
                        "password",
                        100.0);
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

                    String response = processCommand(line);

                    // Handle QUIT
                    if (response.equals("QUIT")) {
                        out.println("200 OK");
                        break;
                    }

                    // Handle SHUTDOWN
                    if (response.equals("SHUTDOWN")) {
                        out.println("200 OK");
                        clientSocket.close();
                        serverSocket.close();
                        System.out.println("Server shutting down...");
                        System.exit(0);
                    }

                    out.println(response);
                }

                clientSocket.close();
                System.out.println("Client disconnected");
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String processCommand(String request) {

        try {
            String[] parts = request.trim().split(" ");
            String command = parts[0].toUpperCase();

            switch (command) {

                case "BUY":
                    if (parts.length != 5)
                        return "400 Invalid BUY format";

                    String buySymbol = parts[1];
                    double buyAmount = Double.parseDouble(parts[2]);
                    double buyPrice = Double.parseDouble(parts[3]);
                    int buyUserId = Integer.parseInt(parts[4]);

                    User buyUser = StockDB.getUserByID(buyUserId);
                    if (buyUser == null)
                        return "User not found.";

                    double totalCost = buyAmount * buyPrice;

                    if (buyUser.getBalance() < totalCost)
                        return "Insufficient funds.";

                    double newBalance = buyUser.getBalance() - totalCost;
                    StockDB.updateUserBalance(buyUserId, newBalance);

                    ArrayList<Stock> buyStocks =
                            StockDB.getStockByUserAndSymbol(buyUserId, buySymbol);

                    Stock stock;

                    if (buyStocks.isEmpty()) {
                        stock = new Stock(0, buySymbol, buySymbol,
                                buyAmount, buyUserId);
                        StockDB.addStock(stock);
                    } else {
                        stock = buyStocks.get(0);
                        stock.setStockBalance(
                                stock.getStockBalance() + buyAmount);
                        StockDB.updateStock(stock);
                    }

                    return "200 OK\nBOUGHT: New balance: "
                            + buyAmount + " " + buySymbol
                            + ". USD balance $"
                            + String.format("%.2f", newBalance);


                case "SELL":
                    if (parts.length != 5)
                        return "400 Invalid SELL format";

                    String sellSymbol = parts[1];
                    double sellAmount = Double.parseDouble(parts[2]);
                    double sellPrice = Double.parseDouble(parts[3]);
                    int sellUserId = Integer.parseInt(parts[4]);

                    User sellUser = StockDB.getUserByID(sellUserId);
                    if (sellUser == null)
                        return "User not found.";

                    ArrayList<Stock> sellStocks =
                            StockDB.getStockByUserAndSymbol(sellUserId, sellSymbol);

                    if (sellStocks.isEmpty() ||
                            sellStocks.get(0).getStockBalance() < sellAmount)
                        return "Not enough " + sellSymbol + " stock balance.";

                    Stock sellStock = sellStocks.get(0);

                    sellStock.setStockBalance(
                            sellStock.getStockBalance() - sellAmount);
                    StockDB.updateStock(sellStock);

                    double totalGain = sellAmount * sellPrice;
                    double updatedBalance = sellUser.getBalance() + totalGain;
                    StockDB.updateUserBalance(sellUserId, updatedBalance);

                    return "200 OK\nSOLD: New balance: "
                            + sellStock.getStockBalance() + " "
                            + sellSymbol
                            + ". USD balance $"
                            + String.format("%.2f", updatedBalance);


                case "LIST":
                    if (parts.length != 2)
                        return "400 Invalid LIST format";

                    int listUserId = Integer.parseInt(parts[1]);

                    ArrayList<Stock> stocks = StockDB.getStockByUserAndSymbol(listUserId, null);

                    StringBuilder sb = new StringBuilder();
                    sb.append("200 OK\n");
                    sb.append("The list of records in the Stocks database for user ")
                            .append(listUserId).append(":\n");

                    for (Stock s : stocks) {
                        sb.append(s.getId())
                                .append(" ")
                                .append(s.getStockSymbol())
                                .append(" ")
                                .append(s.getStockBalance())
                                .append(" ")
                                .append(s.getUserID())
                                .append("\n");
                    }

                    return sb.toString();


                case "BALANCE":
                    if (parts.length != 2)
                        return "400 Invalid BALANCE format";

                    int balanceUserId = Integer.parseInt(parts[1]);
                    User balanceUser = StockDB.getUserByID(balanceUserId);

                    if (balanceUser == null)
                        return "User not found.";

                    return "200 OK\nBalance for user "
                            + balanceUser.getFirstName() + " "
                            + balanceUser.getLastName()
                            + ": $"
                            + String.format("%.2f",
                            balanceUser.getBalance());


                case "QUIT":
                    return "QUIT";

                case "SHUTDOWN":
                    return "SHUTDOWN";

                default:
                    return "400 Invalid command";
            }

        } catch (Exception e) {
            return "400 Invalid command or format";
        }
    }

}
