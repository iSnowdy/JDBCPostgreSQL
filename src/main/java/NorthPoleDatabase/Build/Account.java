package NorthPoleDatabase.Build;

public class Account {
    // PK and NOT NULL attributes are defined as final
    private final int ID;
    private final String DNI;
    private float balance;

    // Used when we add a client and, it automatically creates an account
    public Account(int ID, String DNI) {
        this.ID = Account.this.ID;
        this.DNI = DNI;
    }
    // To manually add an account (by the Employee)
    public Account(int ID, String DNI, float balance) {
        this.ID = Account.this.ID;
        this.DNI = DNI;
        this.balance = balance;
    }


    // Getters and Setters
    public float getBalance() {
        return balance;
    }
    public void setBalance(float balance) {
        this.balance = balance;
    }

    public String getDNI() {
        return DNI;
    }

    public int getID() {
        return ID;
    }
}
