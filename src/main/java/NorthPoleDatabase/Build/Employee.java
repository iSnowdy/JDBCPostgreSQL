package NorthPoleDatabase.Build;

// https://www.w3api.com/Java/InputMismatchException/

import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.Random;

public class Employee extends Person implements EmpOps {
    private Rol rol;

    private int[] bills = new int[4];

    public Employee(String DNI, String name, String pin) {
        super(DNI, name, pin);
        this.rol = Rol.E; // default value in the DDL
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
    public void refillBank() throws ExitException {
        boolean validATM = false;
        String addressATM = "";
        String cityATM = "";
        System.out.println("Initializing Refill Bank operation...");
        // First of all, we need to know which ATM are we updating
        while (!validATM) {
            System.out.print("Please write the address of the ATM you wish to access: ");
            addressATM = ScannerCreator.nextLine();
            System.out.print("Please write the city of the ATM you wish to access: ");
            cityATM = ScannerCreator.nextLine();

            // This static method returns true if any information at all is returned with
            // a SELECT using the given parameters
            if (!(JDBCPostgreSQL.validateATMUPDATE(addressATM, cityATM))) {
                System.out.println("Invalid address or city. Please try again...");
            } else {
                validATM = true;
            }
        }

        System.out.println("Now, you will be prompted how many of each bill do you want to add");
        System.out.println("If, at any point, you would like to go back, type -1");
        // Prompts the user and at the same time validates it to make sure
        // we do not get an invalid input and pass it to the SQL Query
        try {
            System.out.println("5€ bills: ");
            this.bills[0] = validateBillsInput(ScannerCreator.nextInt());
            System.out.println("10€ bills: ");
            this.bills[1] = validateBillsInput(ScannerCreator.nextInt());
            System.out.println("20€ bills: ");
            this.bills[2] = validateBillsInput(ScannerCreator.nextInt());
            System.out.println("50€ bills: ");
            this.bills[3] = validateBillsInput(ScannerCreator.nextInt());
            // Now that we have a valid input by the user stored in an
            // int array, we can call the PreparedStatement to UPDATE the DB
            JDBCPostgreSQL.updateATM(this.bills, addressATM, cityATM);
        } catch (ExitException exitException) {
            System.out.println("Exiting operation. Returning to the main menu...");
        } catch (SQLException sqlException) {
            System.err.println("Error while trying to update the ATM. Returning to the main menu...");
            sqlException.printStackTrace();
        }
    }

    // Insert to the database. Asks the user for the information, validates that
    // the client does not exist and then inserts it
    @Override
    public void addClient() throws ExitException {
        boolean validClientInfo = false;
        String answer;

        String DNI;
        String name;
        String pin;

        System.out.println("Initializing Add Client operation...");

        while (!validClientInfo) {
            System.out.println("Please provide the following information");
            System.out.print("Client's DNI: ");
            DNI = ScannerCreator.nextLine();
            System.out.print("Client's name: ");
            name = ScannerCreator.nextLine();
            System.out.println("Automatically generating PIN number for the client...");
            pin = generatePIN();

            System.out.println("Are you sure you want to add a Client to the database with the following information (Y/n)?");
            System.out.println("DNI: " + DNI);
            System.out.println("Name: " + name);
            System.out.println("PIN: " + pin);
            answer = ScannerCreator.nextLine();

            if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                // Check if the client already exists
                if (JDBCPostgreSQL.getClient(DNI) == null) {
                    System.out.println("Data is valid. Preparing to add the client to the database");
                    // Insert the new client
                    JDBCPostgreSQL.insertUser(DNI, name, pin, Rol.C);
                    validClientInfo = true; // Now that we have added the client, exit the loop
                } else {
                    System.out.println("A client with this DNI already exists. Please try again.");
                }
            } else if (answer.equalsIgnoreCase("n")) {
                System.out.println("Would you like to try again (Y/n)?");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else {
                System.out.println("Invalid input. Please enter 'Y' for yes or 'n' for no.");
            }
        }
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

    private int validateBillsInput(int input) throws ExitException {
        while (true) {
            try {
                if (input == -1) {
                    // Not sure if this would get the flow of the
                    // program back to OpMenu
                    throw new ExitException("User chose to back");
                }
                if (input < 0) {
                    System.err.println("The number must be non-negative");
                } else {
                    return input;
                }
            } catch (NullPointerException nullPointerException) {
                System.err.println("Unexpected error while prompting for bills");
                nullPointerException.printStackTrace();
            } catch (InputMismatchException inputMismatchException) {
                System.err.println("Invalid input. Please enter a valid number");
                inputMismatchException.printStackTrace();
            }
        }
    }

    private String generatePIN() {
        // Math random returns a value of [0.0, 1.0)
        // We need to cast it to int and also magnify it to represent a
        // 4 digit value between the range of (1000, 9999)
        return String.valueOf((int)(Math.random() * 9000) + 1000);
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
