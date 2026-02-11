import java.sql.*;
import java.util.ArrayList;

public class StockDB {

    private static Connection getConnection() throws SQLException {
        String dbUrl = "jdbc:sqlite:stock_exchange.db"; // match your file name
        return DriverManager.getConnection(dbUrl);
    }

    public static void addUser(User user) {
        try {
            Connection connection = getConnection();

            PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO Users (email, first_name, last_name, user_name, password, usd_balance) values (?, ?, ?, ?, ?, ?)");

            // first value is index 1, it's ok to cry
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getFirstName());
            preparedStatement.setString(3, user.getLastName());
            preparedStatement.setString(4, user.getUserName());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setDouble(6, user.getBalance());

            preparedStatement.execute();

            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }

    public static ArrayList<User> getUsers() {
        ArrayList<User> users = new ArrayList<>();
        try {
            Connection connection = getConnection();
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM Users");
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("ID"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("user_name"),
                        rs.getString("password"),
                        rs.getDouble("usd_balance")
                ));
            }
            connection.close();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
        return users;
    }

    public static void addStock(Stock stock) {
        try {
            Connection connection = getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO Stocks (stock_symbol, stock_name, stock_balance, user_id) VALUES (?, ?, ?, ?)");

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

    public static User getUserByID(int id) {
        User user = null;
        try (Connection connection = getConnection()) {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM Users WHERE ID = ?");
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            if (rs.next()) {
                user = new User(
                        rs.getInt("ID"),
                        rs.getString("email"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("user_name"),
                        rs.getString("password"),
                        rs.getDouble("usd_balance")
                );
            }
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
        return user;
    }

    public static void updateUserBalance(int id, double newBalance) {
        try (Connection connection = getConnection()) {
            PreparedStatement stmt = connection.prepareStatement(
                    "UPDATE Users SET usd_balance = ? WHERE ID = ?"
            );
            stmt.setDouble(1, newBalance);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("SQLException: " + e.getMessage());
        }
    }
}