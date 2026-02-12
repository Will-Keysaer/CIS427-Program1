import java.sql.*;
import java.util.ArrayList;

/**
 * StockDB - Database access layer for managing users and stocks in a stock exchange system.
 * This class provides static methods to interact with an SQLite database for user and stock operations.
 */

public class StockDB {

    //Establishes and returns a connection to the SQLite database.
    private static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:sqlite:stock_exchange.db";
        return DriverManager.getConnection(dbUrl);
    }

    public static void addUser(User user) {
        try {
            Connection connection = getConnection();

            // Prepare SQL insert statement with parameter placeholders
            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Users (email, first_name, last_name, " +
                    "user_name, password, usd_balance) values (?, ?, ?, ?, ?, ?)");


            // Set parameter values from the User object
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getUserName());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setDouble(6, user.getBalance());

            preparedStatement.execute();

            // Close the database connection
            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    /**
     * Retrieves all users from the Users table.
     *
     * @return ArrayList<User> containing all users in the database
     */
    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        try {
            Connection connection = getConnection();

            // Create statement for executing SQL query
            Statement preparedStatement = connection.createStatement();


            ResultSet userQuery = preparedStatement.executeQuery("SELECT * FROM Users");

            // Iterate through result set and create User objects
            while (userQuery.next()) {
                users.add(new User(
                        userQuery.getInt("ID"),
                        userQuery.getString("email"),
                        userQuery.getString("first_name"),
                        userQuery.getString("last_name"),
                        userQuery.getString("user_name"),
                        userQuery.getString("password"),
                        userQuery.getDouble("usd_balance")
                ));
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
        return users;
    }

    /**
     * Adds a new stock to the Stocks table in the database.
     *
     * @param stock The Stock object containing stock details to be inserted
     */
    public static void addStock(Stock stock) {
        try {
            Connection connection = getConnection();

            // Prepare SQL insert statement for stocks
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO Stocks (stock_symbol, stock_name, stock_balance, user_id) VALUES (?, ?, ?, ?)");

            // Set parameter values from the Stock object
            preparedStatement.setString(1, stock.getStockSymbol());
            preparedStatement.setString(2, stock.getStockName());
            preparedStatement.setDouble(3, stock.getStockBalance());
            preparedStatement.setInt(4, stock.getUserID());

            preparedStatement.execute();
            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    /**
     * Retrieves a user by their unique ID from the Users table.
     *
     * @param id The unique identifier of the user to retrieve
     * @return User object if found, null if no user exists with the given ID
     */
    public static User getUserByID(int id) {
        User user = null;
        try (Connection connection = getConnection()) {

            // Prepare parameterized query to find user by ID
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Users WHERE ID = ?");
            preparedStatement.setInt(1, id);
            ResultSet userIDQuery = preparedStatement.executeQuery();

            // If a user is found, create User object from result set
            if (userIDQuery.next()) {
                user = new User(
                        userIDQuery.getInt("ID"),
                        userIDQuery.getString("email"),
                        userIDQuery.getString("first_name"),
                        userIDQuery.getString("last_name"),
                        userIDQuery.getString("user_name"),
                        userIDQuery.getString("password"),
                        userIDQuery.getDouble("usd_balance")
                );
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
        return user;
    }

    /**
     * Updates the USD balance of a specific user in the database.
     *
     * @param id The unique identifier of the user to update
     * @param newBalance The new USD balance to set for the user
     */
    public static void updateUserBalance(int id, double newBalance) {
        try (Connection connection = getConnection()) {

            // Prepare update statement for user balance
            PreparedStatement PreparedStatement = connection.prepareStatement(
                    "UPDATE Users SET usd_balance = ? WHERE ID = ?"
            );
            PreparedStatement.setDouble(1, newBalance);
            PreparedStatement.setInt(2, id);
            PreparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    /**
     * Retrieves stocks for a specific user, optionally filtered by stock symbol.
     * If stockSymbol is null, returns all stocks for the user.
     *
     * @param userId The unique identifier of the user
     * @param stockSymbol The stock symbol to filter by (can be null for all stocks)
     * @return ArrayList<Stock> containing matching stocks for the user
     */
    public static ArrayList<Stock> getStockByUserAndSymbol(int userId, String stockSymbol) {
        ArrayList<Stock> stocks = new ArrayList<>();
        try {
            Connection connection = getConnection();
            PreparedStatement PreparedStatement;

            if (stockSymbol == null) {
                // If symbol is null, get all stocks for this user
                PreparedStatement = connection.prepareStatement(
                        "SELECT * FROM Stocks WHERE user_id = ?"
                );
                PreparedStatement.setInt(1, userId);
            } else {
                // Otherwise, get only the matching stock
                PreparedStatement = connection.prepareStatement(
                        "SELECT * FROM Stocks WHERE user_id = ? AND stock_symbol = ?"
                );
                PreparedStatement.setInt(1, userId);
                PreparedStatement.setString(2, stockSymbol);
            }

            // Execute query and process results
            ResultSet stockQuery = PreparedStatement.executeQuery();
            while (stockQuery.next()) {
                stocks.add(new Stock(
                        stockQuery.getInt("ID"),
                        stockQuery.getString("stock_symbol"),
                        stockQuery.getString("stock_name"),
                        stockQuery.getDouble("stock_balance"),
                        stockQuery.getInt("user_id")
                ));
            }

            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }

        return stocks;
    }


    public static void updateStock(Stock stock) {
        try (Connection connection = getConnection()) {
            String query = "UPDATE Stocks SET stock_balance = ? WHERE ID = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setDouble(1, stock.getStockBalance());
            preparedStatement.setInt(2, stock.getId());
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }
}