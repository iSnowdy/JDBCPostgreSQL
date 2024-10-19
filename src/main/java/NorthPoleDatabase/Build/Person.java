package NorthPoleDatabase.Build;

public abstract class Person {
    // PK and NOT NULL attributes are defined as final
    private final String DNI;
    private final String name;
    private String pin;
    // 'Rol' is not defined here but inside the subclasses. Also by the
    // DDL it is automatically set to C / E (default values)

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

    public String getDNI() {
        return DNI;
    }
}