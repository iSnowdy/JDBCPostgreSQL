package NorthPoleDatabase.Build;

public abstract class Person {
    private String DNI;
    private String name;
    private String pin;

    public Person(String DNI, String name, String pin) {
        this.DNI = DNI;
        this.name = name;
        this.pin = pin;
    }

    protected abstract void makeTransfer();
    protected abstract void checkBalance();
    protected abstract void changePin();

    @Override
    public String toString() {
        return  "DNI: " + this.DNI + "\n" +
                "Name: " + this.name + "\n" +
                "PIN: " + this.pin;
    }


    // Getters and Setters
    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDNI() {
        return DNI;
    }

    public void setDNI(String DNI) {
        this.DNI = DNI;
    }
}