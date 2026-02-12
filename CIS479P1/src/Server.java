import java.io.*;
import java.net.*;
import java.util.ArrayList;

/**
 * Server class that listens for incoming socket connections on a predefined port
 * and processes commands received from connected client.
 */
public class Server {

    public static final int SERVER_PORT = 4080;

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {

            System.out.println("Server listening on port " + SERVER_PORT);

            // Ensure at least one user exists if the table is initially empty
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

            // Run indefinitely to accept and handle incoming client connections.
            while (true) {

                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected");

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter out = new PrintWriter(
                        clientSocket.getOutputStream(), true);

                String line;

                // Process each line of input from the connected client.
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

    /**
     *  Processes a single textual trading command that follows a structured BUY request format.
     */

    private static String processCommand(String request) {

        try {
            // Split the request by whitespace, trim any leading/trailing spaces first.
            String[] parts = request.trim().split(" ");
            String command = parts[0].toUpperCase();

            switch (command) {

                /** /////////////////////////////////////////////////////////////////////////////////////
                 * BUY command handler
                 * Expected format: “BUY” followed by a space,
                 * followed by a stock_symbol, followed by a space, followed by a stock_amount, followed by
                 * a space, followed by the price per stock, followed by a User_ID, and followed by the
                 * newline character (i.e., '\n').
                 *///////////////////////////////////////////////////////////////////////////////////////
                case "BUY":
                    if (parts.length != 5)
                        return "400 Invalid BUY format";

                    //parse command parameters
                    String buySymbol = parts[1];
                    double buyAmount = Double.parseDouble(parts[2]);
                    double buyPrice = Double.parseDouble(parts[3]);
                    int buyUserId = Integer.parseInt(parts[4]);

                    User buyUser = StockDB.getUserByID(buyUserId);
                    if (buyUser == null)
                        return "User not found.";

                    double totalCost = buyAmount * buyPrice;

                    // Ensure the user has sufficient funds before proceeding.
                    if (buyUser.getBalance() < totalCost)
                        return "Insufficient funds.";

                    //update user balance
                    double newBalance = buyUser.getBalance() - totalCost;
                    StockDB.updateUserBalance(buyUserId, newBalance);

                    ArrayList<Stock> buyStocks =
                            StockDB.getStockByUserAndSymbol(buyUserId, buySymbol);

                    Stock stock;

                    if (buyStocks.isEmpty()) {
                        // No existing stock: create a new stock row with the purchased amount.
                        stock = new Stock(0, buySymbol, buySymbol,
                                buyAmount, buyUserId);
                        StockDB.addStock(stock);
                    } else {
                        // Otherwise, append the amount to an existing position.
                        stock = buyStocks.get(0);
                        stock.setStockBalance(
                                stock.getStockBalance() + buyAmount);
                        StockDB.updateStock(stock);
                    }

                    // Return a success response with the new USD balance
                    return "200 OK\nBOUGHT: New balance: "
                            + buyAmount + " " + buySymbol
                            + ". USD balance $"
                            + String.format("%.2f", newBalance);

                /** /////////////////////////////////////////////////////////////////////////////////////
                 * SELL command handler
                 * Expected format: “SELL” followed by a space,
                 * followed by a stock_symbol, followed by a space, followed by stock price, followed by a
                 * space, followed by a stock_amount, followed by a space, followed by a User_ID, and
                 * followed by the newline character (i.e., '\n').
                 *///////////////////////////////////////////////////////////////////////////////////////

                case "SELL":
                    if (parts.length != 5) {
                        return "400 Invalid SELL format";
                    }

                    //parse command SELL command parameters
                    String sellSymbol = parts[1];
                    double sellAmount = Double.parseDouble(parts[2]);
                    double sellPrice = Double.parseDouble(parts[3]);
                    int sellUserId = Integer.parseInt(parts[4]);

                    User sellUser = StockDB.getUserByID(sellUserId);
                    if (sellUser == null) {
                        return "User not found.";
                    }

                    ArrayList<Stock> sellStocks =
                            StockDB.getStockByUserAndSymbol(sellUserId, sellSymbol);

                    //Validate that user owns enough stock
                    if (sellStocks.isEmpty() ||
                            sellStocks.get(0).getStockBalance() < sellAmount) {
                        return "Not enough " + sellSymbol + " stock balance.";
                    }

                    Stock sellStock = sellStocks.get(0);

                    //sell stock and update stock balance
                    sellStock.setStockBalance(
                            sellStock.getStockBalance() - sellAmount);
                    StockDB.updateStock(sellStock);

                    //calculate stock earnings and update user USD balance
                    double totalGain = sellAmount * sellPrice;
                    double updatedBalance = sellUser.getBalance() + totalGain;
                    StockDB.updateUserBalance(sellUserId, updatedBalance);

                    //return success statement
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
