package NorthPoleDatabase.Build;

public class Client extends Person implements clientOps {
    // ENUM for rol
    private final Rol rol;

    public Client(String DNI, String name, String pin) {
        super(DNI, name, pin);
        this.rol = Rol.C;
    }

    // Abstract methods
    @Override
    protected void makeTransfer() {

    }

    @Override
    protected void checkBalance() {

    }

    @Override
    protected void changePin() {

    }


    // Interface methods
    @Override
    public void withdrawMoney() {

    }

    @Override
    public void depositMoney() {

    }

    @Override
    public String toString() {
        String information = super.toString();
        return
            "===== CLIENT INFORMATION =====\n" +
            information + "\n" +
            "===== CLIENT INFORMATION =====";
    }
}