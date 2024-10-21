package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Person {
    // PK and NOT NULL attributes are defined as final
    private final String DNI;
    private final String name;
    private String pin;
    // 'Rol' is not defined here but inside the subclasses. Also by the
    // DDL it is automatically set to C / E (default values)
    private int[] bills = new int[4];

    public Person(String DNI, String name, String pin) {
        this.DNI = DNI;
        this.name = name;
        this.pin = pin;
    }

    protected abstract void makeTransfer() throws ExitException;

    public void changePIN() throws ExitException {
        boolean validUser = false;
        String answer;
        String inputDNI;
        System.out.println("Initializing PIN change Operation...");

        while (!validUser) {
            System.out.print("To ensure protection, please type in again the DNI: ");
            inputDNI = ScannerCreator.nextLine();

            if (this instanceof Employee) {
                // If the user doing this operation is an employee, query the DB to
                // check if he has provided a valid DNI
                if (JDBCPostgresSQL.getClient(inputDNI) == null) {
                    System.out.println("There are no client matches with the provided DNI");
                    System.out.println("Would you like to try again? (Y/n)");
                    answer = ScannerCreator.nextLine();
                    if (answer.equals("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    }
                } else {
                    System.out.println("Are you sure you want to CHANGE the PIN of the account with DNI " + inputDNI + "?");
                    answer = ScannerCreator.nextLine();
                    if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                        JDBCPostgresSQL.updatePIN(inputDNI);
                        validUser = true;
                    } else if (answer.equalsIgnoreCase("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    } else {
                        System.out.println("Invalid input. Please enter 'Y' or 'n' for no");
                    }
                }
            } else if (this instanceof Client) {
                // If the user on the other hand is a client, we just need to check
                // if the DNI of the Object matches the input
                if (!this.DNI.equals(inputDNI)) {
                    System.out.println("Your DNI and the provided DNI do not match");
                    System.out.println("Would you like to try again? (Y/n)");
                    answer = ScannerCreator.nextLine();
                    if (answer.equals("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    }
                } else {
                    System.out.println("Are you sure you want to CHANGE the PIN of your account (Y/n)?");
                    answer = ScannerCreator.nextLine();
                    if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                        JDBCPostgresSQL.updatePIN(inputDNI);
                        validUser = true;
                    } else if (answer.equalsIgnoreCase("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    } else {
                        System.out.println("Invalid input. Please enter 'Y' or 'n' for no");
                    }
                }
            }
        }
    }

    public void checkBalance() throws ExitException {
        boolean validUser = false;
        String answer;
        String inputDNI;
        System.out.println("Initializing Check Balance Operation...");

        while (!validUser) {
            System.out.print("To ensure protection, please type in again the DNI: ");
            inputDNI = ScannerCreator.nextLine();

            if (this instanceof Employee) {
                if (JDBCPostgresSQL.getClient(inputDNI) == null) {
                    System.out.println("There are no client matches with the provided DNI");
                    System.out.println("Would you like to try again? (Y/n)");
                    answer = ScannerCreator.nextLine();
                    if (answer.equals("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    }
                } else {
                    System.out.println("Are you sure you want to CHECK the BALANCE of the account with DNI " + inputDNI + "?");
                    answer = ScannerCreator.nextLine();
                    if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                        printBalance(inputDNI);
                        validUser = true;
                    } else if (answer.equalsIgnoreCase("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    } else {
                        System.out.println("Invalid input. Please enter 'Y' or 'n' for no");
                    }
                }
            } else if (this instanceof Client) {
                if (!this.DNI.equals(inputDNI)) {
                    System.out.println("Your DNI and the provided DNI do not match");
                    System.out.println("Would you like to try again? (Y/n)");
                    answer = ScannerCreator.nextLine();
                    if (answer.equals("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    }
                } else {
                    System.out.println("Are you sure you want to CHECK your account's BALANCE? (Y/n)?");
                    answer = ScannerCreator.nextLine();
                    if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                        printBalance(inputDNI);
                        validUser = true;
                    } else if (answer.equalsIgnoreCase("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    } else {
                        System.out.println("Invalid input. Please enter 'Y' or 'n' for no");
                    }
                }
            }
        }
    }

    public boolean validateDNI(String DNI) {
        String regex = "^[XYZ0-9][0-9]{7}[A-Z]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(DNI);
        if (!matcher.matches()) {
            System.out.println("Invalid DNI format. Please try again with the" +
                    "following desktop: ^[XYZ0-9][0-9]{7}[A-Z]$\"");
            return false;
        }
        return true;
    }

    public boolean validateATM(String address, String city) {
            // This static method returns true if any information at all is returned with
            // a SELECT using the given parameters
        if (!(JDBCPostgresSQL.validateATMUPDATE(address, city))) {
            System.out.println("Invalid address or city. Please try again...");
            System.out.println("If you want to exit the operation, please type EXIT");
            return false;
        } else {
            return true;
        }
    }
    // Method that will prompt the user for the amount of each bill and
    // and the same time validate that input. If the user wants to stop the
    // flow of the program, writing 'exit' will throw a customized exception
    // that will cascade to the main menu (hopefully)
    public int[] promptBills(String addressATM, String cityATM) throws ExitException {
        System.out.println("Now, you will be prompted how many of each bill do you want to deposit/withdraw");
        System.out.println("If, at any point, you would like to go back, type -1");
        // Prompts the user and at the same time validates it to make sure
        // we do not get an invalid input and pass it to the SQL Query
        try {
            System.out.println("5€ bills: ");
            this.bills[0] = this.validateBillsInput(ScannerCreator.nextInt());
            System.out.println("10€ bills: ");
            this.bills[1] = this.validateBillsInput(ScannerCreator.nextInt());
            System.out.println("20€ bills: ");
            this.bills[2] = this.validateBillsInput(ScannerCreator.nextInt());
            System.out.println("50€ bills: ");
            this.bills[3] = this.validateBillsInput(ScannerCreator.nextInt());
            // Now that we have a valid input by the user stored in an
            // int array, we can call the PreparedStatement to UPDATE the DB
            JDBCPostgresSQL.updateATM(this.bills, addressATM, cityATM);
        } catch (ExitException exitException) {
            System.out.println("Exiting operation. Returning to the main menu...");
        } catch (SQLException sqlException) {
            System.err.println("Error while trying to update the ATM. Returning to the main menu...");
            sqlException.printStackTrace();
        }
        return this.getBills();
    }
    // Method to validate bills and control the flow using a customized exception
    // Only positive values of int will be accepted
    public int validateBillsInput(int input) throws ExitException {
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
    // Method to print the information of one or more accounts of the client
    private void printBalance(String inputDNI) {
        // Call to the client's DNI ResultSet and then call to check get the
        // account using the
        try {
            ResultSet resultSet = JDBCPostgresSQL.getAccount(inputDNI);

            String accountDNI = resultSet.getString("dni_titular");
            int accountNumber;
            int balance;

            System.out.println("------ ACCOUNT INFORMATION ------");
            System.out.println("|         DNI: " + accountDNI + "          |");
            while(resultSet.next()) {
                accountNumber = resultSet.getInt("numero");
                balance = resultSet.getInt("saldo");
                System.out.println("|       Account Number: " + accountNumber + "       |");
                System.out.println("|         Balance: " + balance + "         |");
            }
            System.out.println("------ ACCOUNT INFORMATION ------");
            resultSet.close();
        } catch (SQLException sqlException) {
            System.err.println("Error reading account balance ResultSet");
            sqlException.printStackTrace();
        }
    }

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

    public int[] getBills() {
        return bills;
    }
}