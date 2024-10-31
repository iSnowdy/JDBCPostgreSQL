package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Person {
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

    public void checkBalance() throws ExitException {
        boolean validUser = false;
        String answer;
        String inputDNI;
        System.out.println("Initializing Check Balance Operation...");

        while (!validUser) {
            System.out.println("To ensure protection, please type in the client's DNI: ");
            inputDNI = ScannerCreator.nextLine();

            if (this instanceof Employee) {
                if (JDBCPostgreSQL.getClient(inputDNI) == null) {
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

    public void changePIN() throws ExitException {
        boolean validUser = false;
        String answer;
        String inputDNI;
        String inputPin;
        System.out.println("Initializing PIN change Operation...");

        while (!validUser) {
            System.out.println("To ensure protection, please type in again the DNI: ");
            inputDNI = ScannerCreator.nextLine();

            if (this instanceof Employee) {
                // If the user doing this operation is an employee, query the DB to
                // check if he has provided a valid DNI
                if (JDBCPostgreSQL.getClient(inputDNI) == null) {
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
                        System.out.print("Please provide the new PIN number: ");
                        inputPin = promptPIN();
                        JDBCPostgreSQL.updatePIN(inputDNI, inputPin);
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
                        inputPin = promptPIN();
                        JDBCPostgreSQL.updatePIN(inputDNI, inputPin);
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

    private String promptPIN() {
        String inputPin;
        do {
            System.out.println("Please provide the new PIN number: ");
            inputPin = ScannerCreator.next();
            if (inputPin.length() != 4) {
                System.err.println("Invalid PIN format. It must be composed of 4 characters");
            }
        } while (inputPin.length() != 4);
        return inputPin;
    }

    public boolean validateDNI(String DNI) {
        String regex = "^[XYZ0-9][0-9]{7}[A-Z]$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(DNI);
        if (!matcher.matches()) {
            System.out.println("Invalid DNI format. Please try again with the " +
                    "following desktop: ^[XYZ0-9][0-9]{7}[A-Z]$\"");
            return false;
        }
        return true;
    }
    // Method that will prompt the user for the amount of each bill
    // and at the same time validate that input. If the user wants to stop the
    // flow of the program, writing 'exit' will throw a customized exception
    // that will cascade to the main menu (hopefully)
    public int[] promptBills(String addressATM, String cityATM, int account) throws ExitException {
        System.out.println("Now, you will be prompted how many of each bill do you want to deposit/withdraw");
        System.out.println("If, at any point, you would like to go back, type -1");
        // Prompts the user and at the same time validates it to make sure
        // we do not get an invalid input and pass it to the SQL Query
        int totalAmount = 0;
        try {
            // Envelope this in a transaction. I do this because we are updating two things at once:
            // the ATM and the client's account (assuming it is not a transaction use for this method).
            // So if one of those is not properly completed, we do not want the other one to complete
            JDBCPostgreSQL.getConnection().setAutoCommit(false);

            System.out.print("5€ bills: ");
            this.bills[0] = this.validateBillsInput(ScannerCreator.nextInt());
            totalAmount += bills[0] * 5;
            System.out.print("10€ bills: ");
            this.bills[1] = this.validateBillsInput(ScannerCreator.nextInt());
            totalAmount += bills[1] * 10;
            System.out.print("20€ bills: ");
            this.bills[2] = this.validateBillsInput(ScannerCreator.nextInt());
            totalAmount += bills[2] * 20;
            System.out.print("50€ bills: ");
            this.bills[3] = this.validateBillsInput(ScannerCreator.nextInt());
            totalAmount += bills[3] * 50;

            // -1 is the code that says it is an employee who is doing the transaction
            // otherwise it is a client and we also need to UPDATE the client's account
            // to deposit the money
            // Consider changing this. Kinda dirty
            if (account != -1) {
                JDBCPostgreSQL.depositToAccount(account, totalAmount);
                JDBCPostgreSQL.updateATM(this.bills, addressATM, cityATM);
                return this.getBills(); // Exists the method preemptively
                // Finalize the transaction inside the updateATM method
                // So no need to change it here. Also avoids transaction conflict
            }
            // Now that we have a valid input by the user stored in an
            // int array, we can call the PreparedStatement to UPDATE the DB
            JDBCPostgreSQL.updateATM(this.bills, addressATM, cityATM);
        } catch (ExitException exitException) {
            try {
                JDBCPostgreSQL.getConnection().rollback();
            } catch (SQLException sqlException) {
                System.err.println("Error during rollback");
            }
            System.out.println("Exiting operation. Returning to the main menu...");
        } catch (SQLException sqlException) {
            try {
                JDBCPostgreSQL.getConnection().rollback();
            } catch (SQLException sqlException1) {
                System.err.println("Error during rollback");
            }
            System.err.println("Error while trying to update the ATM. Returning to the main menu...");
            sqlException.printStackTrace();
        } finally {
            try {
                JDBCPostgreSQL.getConnection().setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
            }
        }
        return this.getBills();
    }
    // Method to validate bills and control the flow using a customized exception
    // Only positive values of int will be accepted
    public int validateBillsInput(int input) throws ExitException {
        try {
            if (input == -1) {
                // Not sure if this would get the flow of the
                // program back to OpMenu
                throw new ExitException("User chose to back");
            }
            if (input < 0) {
                System.err.println("The number must be non-negative");
                // Recursion to keep asking until a valid input is given :D
                return validateBillsInput(ScannerCreator.nextInt());
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
        return 0;
    }
    // Method to print the information of one or more accounts of the client
    protected ArrayList<Integer> printBalance(String inputDNI) {
        // Call to the client's DNI ResultSet and then call to check get the
        // account using the
        try {
            var accountsList = new ArrayList<Integer>();
            ResultSet resultSet = JDBCPostgreSQL.getAccount(inputDNI);

            String accountDNI = resultSet.getString("dni_titular");
            // We need to do this in order to not keep one tuple and get less
            // information. And if we were to not do it like this, and call
            // ResultSet.previous() on the JDBC method, we would not get the
            // getString() information (pointing to null)
            resultSet.previous();
            int accountNumber;
            int balance;
            int total = 0;

            System.out.println("------ ACCOUNT INFORMATION ------");
            System.out.println("|         DNI: " + accountDNI + "        |");
            System.out.println("---------------------------------");
            while (resultSet.next()) {
                accountNumber = resultSet.getInt("numero");
                accountsList.add(accountNumber); // Store the account ID (used in Client withdrawal operation)
                balance = resultSet.getInt("saldo");
                System.out.println("|       Account Number: " + accountNumber + "       |");
                System.out.println("|         Balance: " + balance + "         |");
                total += balance;
            }
            System.out.println("---------------------------------");
            System.out.println("|          Total: " + total + "          |");
            System.out.println("------ ACCOUNT INFORMATION ------\n");
            // accountsList.add(total); // Adds the total amount at the end of the List
            resultSet.close();
            return accountsList;
        } catch (SQLException sqlException) {
            System.err.println("Error reading account balance ResultSet");
            sqlException.printStackTrace();
            return null;
        }
    }
    // Getters and Setters
    public String getDNI() {
        return DNI;
    }
    public int[] getBills() {
        return bills;
    }
}