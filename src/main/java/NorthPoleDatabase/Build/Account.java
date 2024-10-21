package NorthPoleDatabase.Build;

public class Account {
    // PK and NOT NULL attributes are defined as final
    private final String DNI;
    private int balance;

    // Used when we add a client and, it automatically creates an account
    public Account(String DNI) {
        this.DNI = DNI;
    }
    // To manually add an account (by the Employee)
    public Account(String DNI, int balance) {
        this.DNI = DNI;
        this.balance = balance;
    }

    // Getters and Setters
    public float getBalance() {
        return balance;
    }
    public void setBalance(int balance) {
        this.balance = balance;
    }

    public String getDNI() {
        return DNI;
    }
}
