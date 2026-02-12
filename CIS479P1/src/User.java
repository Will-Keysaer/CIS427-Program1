public class User {
    private int id;// Unique user ID
    private String email;
    private String firstName;
    private String lastName;
    private String userName;
    private String password;
    private double balance;

    public User(int id, String email, String firstName, String lastName, String userName, String password, double balance) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userName = userName;
        this.password = password;
        this.balance = balance;
    }



    public String getEmail() {
        return email;
    }


    public String getFirstName() {
        return firstName;
    }



    public String getLastName() {
        return lastName;
    }



    public String getUserName() {
        return userName;
    }



    public String getPassword() {
        return password;
    }


    public double getBalance() {
        return balance;
    }


    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", password='" + password + '\'' +
                ", balance=" + balance +
                '}';
    }
}