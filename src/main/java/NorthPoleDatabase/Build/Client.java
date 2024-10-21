package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Client extends Person implements ClientOps {
    // ENUM for rol
    private Rol rol;

    public Client(String DNI, String name, String pin) {
        super(DNI, name, pin);
        this.rol = Rol.C; // default value in the DDL
    }

    // Abstract methods
    @Override
    protected void makeTransfer() throws ExitException {
        boolean validAccounts = false;
        String answer;
        int originAccount = 0;
        int destinationAccount;
        int amountToTransfer = 0;
        // Clears the buffer inside Scanner to avoid conflicts between String and int
        ScannerCreator.nextLine();

        System.out.println("Initializing Transfer Operation (Client)...");

        while (!validAccounts) {
            System.out.println("Please provide the following information");
            // Logic to find out if the client has one or more accounts
            int numberOfAccounts = JDBCPostgresSQL.accountAmount(this.getDNI());

            if (numberOfAccounts > 1) {
                System.out.print("Origin account: ");
                originAccount = ScannerCreator.nextInt();
            } else {
                // If, on the other hand, the client only has one account, we can just
                // extract if from the DB. This way we prevent the client from typing it
                try {
                    ResultSet resultSet = JDBCPostgresSQL.getAccount(this.getDNI());
                    originAccount = resultSet.getInt("numero");
                    resultSet.close(); // Free memory. We do not need the ResultSet anymore
                } catch (SQLException sqlException) {
                    System.err.println("Error. Could not access to the account's information");
                    sqlException.printStackTrace();
                }
            }
            // Whether the client has 1 or more accounts, we need to ask for the destination account
            // Hence why this is outside the if-else block
            System.out.print("Destination account: ");
            destinationAccount = ScannerCreator.nextInt();

            if (JDBCPostgresSQL.getAccount(originAccount) == null ||
                    JDBCPostgresSQL.getAccount(destinationAccount) == null) {
                System.out.println("One of the accounts provided does not exist. Would you like to try again? (Y/n)");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else {
                System.out.print("Type in the amount you want to transfer: ");
                amountToTransfer = ScannerCreator.nextInt();
                System.out.println("Are you sure you want to make the following TRANSACTION? (Y/n)");
                System.out.println("Origin account: " + originAccount);
                System.out.println("Destination account: " + destinationAccount);
                System.out.println("Amount: " + amountToTransfer);

                answer = ScannerCreator.nextLine();
                if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                    JDBCPostgresSQL.updateAccounts(originAccount, destinationAccount, amountToTransfer);
                    validAccounts = true;
                } else if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                } else {
                    System.out.println("Invalid input. Please enter 'Y' for yes and 'n' for no");
                }
            }
            // Clears the buffer inside Scanner to avoid conflicts between String and int
            ScannerCreator.nextLine();
        }
    }

    // Interface methods
    @Override
    public void withdrawMoney() throws ExitException {
        String addressATM;
        String cityATM;

        boolean validAccounts = false;
        String answer;
        int originAccount = 0;
        int destinationAccount = 0;
        int withdrawAmount = 0;
        ScannerCreator.nextLine();

        System.out.println("Initializing Withdraw Operation (Client)...");
        while (!validAccounts) {
            System.out.println("Please provide the following information");
            // First, validate the ATM that the client is working with
            do {
                System.out.print("Please write the address of the ATM you wish to access: ");
                addressATM = ScannerCreator.nextLine();
                if (addressATM.equalsIgnoreCase("exit")) {
                    throw new ExitException("User chose to exit");
                }
                System.out.print("Please write the city of the ATM you wish to access: ");
                cityATM = ScannerCreator.nextLine();
            } while (!(this.validateATM(addressATM, cityATM)));
            // Second, ask for the amount to withdraw and check if that is indeed possible
            // The account number here will be overwritten if the client has more than one
            // account to their name. I chose to follow this flow in order to not summon another
            // ResultSet type of Object inside the method of validation. Like this, resources
            // are somehow managed better
            try {
                ResultSet resultSet = JDBCPostgresSQL.getAccount(this.getDNI());

                System.out.print("Type in the amount you want to withdraw: ");
                withdrawAmount = ScannerCreator.nextInt();
                System.out.println("Checking for cash availability...");
                while (!(this.validateWithdrawalAmount(withdrawAmount))) {
                    System.out.println("Invalid amount. Please enter an amount that is a multiple of 5");
                }
                // Now that we know we have a valid client input, we can calculate the least
                // amount of bills neede to withdraw that quantity and then check if that would
                // be possible
                int[] billsToWithdraw = billsToWithdraw(withdrawAmount);
                if (this.withdrawalValidation(resultSet, billsToWithdraw)) {
                    billsToWithdraw = convertToNegative(billsToWithdraw);
                    // Now that we know it is possible to make the operation, ask for
                    // the account number information before updating the ATM and
                    // the client's account balance
                    int numberOfAccounts = JDBCPostgresSQL.accountAmount(this.getDNI());
                    if (numberOfAccounts > 1) {
                        System.out.print("Account number to withdraw money from: ");
                        originAccount = ScannerCreator.nextInt();
                        // Validates that the account given by input exists
                        while (JDBCPostgresSQL.getAccount(originAccount) == null) {
                            System.out.println("The provided account number does not exist. Would you like to try again? (Y/n)");
                            answer = ScannerCreator.nextLine();
                            if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                                System.out.print("Account number to withdraw money from: ");
                                originAccount = ScannerCreator.nextInt();
                            } else if (answer.equalsIgnoreCase("n")) {
                                System.out.println("Returning to the main menu...");
                                throw new ExitException("User chose to exit");
                            } else {
                                System.out.println("Invalid input. Please enter 'Y' for yes and 'n' for no");
                            }
                        }
                    } else {
                        originAccount = resultSet.getInt("numero");
                    }
                    // Finally, update everything
                    JDBCPostgresSQL.updateATM(billsToWithdraw, addressATM, cityATM);

                    validAccounts = true;
                } else {
                    System.out.println("Would you like to try a different amount? (Y/n)");
                    answer = ScannerCreator.nextLine();
                    if (answer.equalsIgnoreCase("n")) {
                        System.out.println("Returning to the main menu...");
                        throw new ExitException("User chose to exit");
                    }
                }
            } catch (SQLException sqlException) {
                System.err.println("Error. Could not access to the account's information");
                sqlException.printStackTrace();
            }
        }
    }
    //
    @Override
    public void depositMoney() throws ExitException {
        String addressATM;
        String cityATM;

        boolean validAccounts = false;
        String answer;
        int destinationAccount;

        ScannerCreator.nextLine();

        System.out.println("Initializing Deposit Operation...");
        System.out.println("Please provide the following information");

        do {
            System.out.print("Please write the address of the ATM you wish to access: ");
            addressATM = ScannerCreator.nextLine();
            if (addressATM.equalsIgnoreCase("exit")) {
                throw new ExitException("User chose to exit");
            }
            System.out.print("Please write the city of the ATM you wish to access: ");
            cityATM = ScannerCreator.nextLine();
        } while (!(this.validateATM(addressATM, cityATM)));


        System.out.print("Destination account: ");
        destinationAccount = ScannerCreator.nextInt();

        if (JDBCPostgresSQL.getAccount(destinationAccount) == null) {
            System.out.println("The provided account number does not exist. Would you like to try again? (Y/n)");
            answer = ScannerCreator.nextLine();
            if (answer.equalsIgnoreCase("n")) {
                System.out.println("Returning to the main menu...");
                throw new ExitException("User chose to exit");
            }
        } else {
            // Call to Person method to prompt for bills
            // Not implemented in Client / Employee as to not repeat code twice
            promptBills(addressATM, cityATM);
        }
    }
    // Like this we will only accept input that we can withdraw. So for example
    // if the user wanted to withdraw 123€, we would not let them because
    // we have no way of giving those 3€
    private boolean validateWithdrawalAmount(int amountToWithdraw) {
        if (amountToWithdraw < 0) {
            return false;
        } else return amountToWithdraw % 5 != 0;
    }
    // Method that returns how many bills of each amount is needed
    // given a certain amount of money to withdraw
    private int[] billsToWithdraw(int amountToWithdraw) {
        int[] possibleBills = {50, 20, 10, 5};
        int[] withdrawBills = new int[possibleBills.length];
        int leftOverAmount = amountToWithdraw; // Copy the parameter

        // Iterates through each possible bill. If it is possible to give
        // money back using that bill, then insert how many bills of that
        // amount are needed to fulfill that amount
        for (int i = 0; i < withdrawBills.length; i++) {
            if (leftOverAmount >= possibleBills[i]) {
                withdrawBills[i] = leftOverAmount / possibleBills[i];
                // 525 --> 525 / 50 = 10 (10 bills). leftOverAmount %= 50 == 25 and so on
                leftOverAmount %= withdrawBills[i];
            }
        }
        return withdrawBills;
    }
    // Validates that it is indeed possible to withdraw that many bills of each quantity
    private boolean withdrawalValidation(ResultSet resultSet, int[] withdrawBills) throws ExitException {
        try {
            int size = withdrawBills.length;
            int[] availableBills = new int[size];
            for (int i = 0; i < size; i++) {
                availableBills[i] = resultSet.getInt(i + 4);
                // Column index instead of name. Bills data starts at index 4 through 7
                if (withdrawBills[i] > availableBills[i]) {
                    System.out.println("Not enough bills inside the ATM");
                    return false;
                }
            }
        } catch (SQLException sqlException) {
            System.err.println("Error during withdrawal validation");
            sqlException.printStackTrace();
            return false;
        }
        return true;
    }
    // Converting the Array of bills that we want to pass on to the UPDATE
    // method to negative will prevent us from writing a whole new method
    // to UPDATE a negative amount
    private int[] convertToNegative(int[] billsAmount) {
        for (int i = 0; i < billsAmount.length; i++) {
            billsAmount[i] *= -1;
        }
        return billsAmount;
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