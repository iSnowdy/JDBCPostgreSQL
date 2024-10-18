package NorthPoleDatabase.Build;

public class Employee extends Person implements EmpOps {
    private final Rol rol;

    public Employee(String DNI, String name, String pin) {
        super(DNI, name, pin);
        this.rol = Rol.E;
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
    public void refillBank() {

    }

    @Override
    public void addClient() {

    }

    @Override
    public void deleteClient() {

    }

    @Override
    public void addAccount() {

    }

    @Override
    public void deleteAccount() {

    }

    @Override
    public String toString() {
        String information = super.toString();
        return
            "===== EMPLOYEE INFORMATION =====\n" +
            information + "\n" +
            "===== EMPLOYEE INFORMATION =====";
    }
}
