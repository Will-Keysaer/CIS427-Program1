public class Stock {

    private int id;
    private String stockSymbol;
    private String stockName;
    private double stockBalance;
    private int userID;

    public Stock(int id, String stockSymbol, String stockName, double stockBalance, int userID) {
        this.id = id;
        this.stockSymbol = stockSymbol;
        this.stockName = stockName;
        this.stockBalance = stockBalance;
        this.userID = userID;
    }


    public int getId() {
        return id;
    }


    public String getStockSymbol() {
        return stockSymbol;
    }


    public String getStockName() {
        return stockName;
    }


    public double getStockBalance() {
        return stockBalance;
    }

    public void setStockBalance(double stockBalance) {
        this.stockBalance = stockBalance;
    }

    public int getUserID() {
        return userID;
    }


    @Override
    public String toString() {
        return "Stock{" +
                "id=" + id +
                ", stockSymbol='" + stockSymbol + '\'' +
                ", stockName='" + stockName + '\'' +
                ", stockBalance=" + stockBalance +
                ", userID=" + userID +
                '}';
    }


}
