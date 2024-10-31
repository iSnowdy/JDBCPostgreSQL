package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Client extends Person implements ClientOps {
    // ENUM for rol
    private Rol rol;
    private String addressATM;
    private String cityATM;

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
        int amountToTransfer;

        System.out.println("Initializing Transfer Operation (Client)...");

        while (!validAccounts) {
            System.out.println("Please provide the following information");
            // Logic to find out if the client has one or more accounts
            int numberOfAccounts = JDBCPostgreSQL.accountAmount(this.getDNI());

            if (numberOfAccounts > 1) {
                System.out.print("Origin account: ");
                originAccount = ScannerCreator.nextInt();
            } else {
                // If, on the other hand, the client only has one account, we can just
                // extract if from the DB. This way we prevent the client from typing it
                try {
                    ResultSet resultSet = JDBCPostgreSQL.getAccount(this.getDNI());
                    originAccount = resultSet.getInt("numero");
                    resultSet.close(); // Free memory. We do not need the ResultSet anymore
                } catch (SQLException sqlException) {
                    System.err.println("Error. Could not access to the account's information");
                    sqlException.printStackTrace();
                }
            }
            // Whether the client has 1 or more accounts, we need to ask for the destination account
            System.out.print("Destination account: ");
            destinationAccount = ScannerCreator.nextInt();

            if (JDBCPostgreSQL.getAccount(originAccount) == null ||
                    JDBCPostgreSQL.getAccount(destinationAccount) == null) {
                System.out.println("One of the accounts provided does not exist. Would you like to try again? (Y/n)");
                ScannerCreator.nextLine();
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

                ScannerCreator.nextLine();
                answer = ScannerCreator.nextLine();
                if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                    JDBCPostgreSQL.updateAccounts(originAccount, destinationAccount, amountToTransfer);
                    validAccounts = true;
                } else if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                } else {
                    System.out.println("Invalid input. Please enter 'Y' for yes and 'n' for no");
                }
            }
        }
    }
    // Interface methods
    @Override
    public void withdrawMoney() throws ExitException {
        boolean validAccounts = false;
        String answer;
        int numberOfAccounts;
        int totalAmountInAccounts = 0;
        int originAccount;
        int withdrawAmount;

        System.out.println("Initializing Withdraw Operation (Client)...");
        while (!validAccounts) {
            System.out.println("Please provide the following information");
            // First, ask for the amount to withdraw and check if that is indeed possible
            // The account number here will be overwritten if the client has more than one
            // account to their name. I chose to follow this flow in order to not summon another
            // ResultSet type of Object inside the method of validation. Like this, resources
            // are somehow managed better
            try {
                printBalance(this.getDNI());

                ResultSet resultSetClient = JDBCPostgreSQL.getAccount(this.getDNI());
                ResultSet resultSetATM = JDBCPostgreSQL.getATM(this.addressATM, this.cityATM);

                do {
                    System.out.print("Type in the amount you want to withdraw: ");
                    withdrawAmount = ScannerCreator.nextInt();
                } while (!(this.validateWithdrawalAmount(withdrawAmount)));
                System.out.println("Checking for cash availability...");
                // Now that we know we have a valid client input, we can calculate the least
                // amount of bills needed to withdraw that quantity and then check if that would
                // be possible
                int[] availableBills = getAvailableBills(resultSetATM);
                int[] billsToWithdraw = billsToWithdraw(availableBills, withdrawAmount);
                // Check availability in the ATM and the client's account
                if (billsToWithdraw != null) {
                    billsToWithdraw = convertToNegative(billsToWithdraw);
                    // Now that we know it is possible to make the operation, ask for
                    // the account number information before updating the ATM and
                    // the client's account balance
                    numberOfAccounts = JDBCPostgreSQL.accountAmount(this.getDNI());
                    if (numberOfAccounts > 1) {
                        // Since the client has more than 1 account, we maybe need the total money across
                        // all its accounts to complete the withdrawal
                        totalAmountInAccounts = calculateTotalAmount(resultSetClient);
                        System.out.print("Account number to withdraw money from: ");
                        originAccount = ScannerCreator.nextInt();
                        // Validates that the account given by input exists
                        while (JDBCPostgreSQL.getAccount(originAccount) == null) {
                            System.out.println("The provided account number does not exist. Would you like to try again? (Y/n)");
                            ScannerCreator.nextLine();
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
                        originAccount = resultSetClient.getInt("numero");
                    }
                    // No longer needed
                    resultSetATM.close();
                    resultSetClient.close();
                    // Now we know where the client is, how much they want to withdraw and from what
                    // account. Final confirmation is if they have enough balance to finalize the
                    // operation
                    if (JDBCPostgreSQL.checkBalance(this.getDNI(), originAccount, withdrawAmount)) {
                        JDBCPostgreSQL.updateATM(billsToWithdraw, this.addressATM, this.cityATM);
                        JDBCPostgreSQL.updateAccounts(originAccount, withdrawAmount);
                        validAccounts = true;
                    } else {
                        ScannerCreator.nextLine();
                        if (numberOfAccounts > 1 && withdrawAmount <= totalAmountInAccounts) {
                            System.out.println("You do not have enough balance in the selected account. But,\n" +
                                    "if you choose to, you can withdraw the amount from multiple accounts.");
                            System.out.println("Do you want to withdraw money from multiple accounts? (Y/n)");
                            answer = ScannerCreator.nextLine();
                            if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                                promptAccounts(withdrawAmount, billsToWithdraw);
                                return; // Operation done. Exit
                            }
                        }
                        System.out.println("You do not have enough balance in the selected account " +
                                "to proceed with the operation. \nWould you like to try again? (Y/n)");
                        answer = ScannerCreator.nextLine();
                        if (answer.equalsIgnoreCase("n")) {
                            System.out.println("Returning to the main menu...");
                            throw new ExitException("User chose to exit");
                        }
                    }
                } else { // The ATM did not have enough bills to withdraw the amount the client wanted
                    ScannerCreator.nextLine();
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

    @Override
    public void depositMoney() throws ExitException {
        String answer;
        int withdrawAccount = 0;

        System.out.println("Initializing Deposit Operation...");
        System.out.println("Please provide the following information");

        int numberOfAccounts = JDBCPostgreSQL.accountAmount(this.getDNI());

        printBalance(this.getDNI());

        if (numberOfAccounts > 1) {
            System.out.println("Account that you wish to deposit money in: ");
            withdrawAccount = ScannerCreator.nextInt();
        } else {
            try {
                ResultSet resultSet = JDBCPostgreSQL.getAccount(this.getDNI());
                withdrawAccount = resultSet.getInt("numero");
                resultSet.close();
            } catch (SQLException sqlException) {
                System.err.println("Error. Could not access to the account's information");
                sqlException.printStackTrace();
            }
        }

        if (JDBCPostgreSQL.getAccount(withdrawAccount) == null) {
            System.out.println("The provided account number does not exist. Would you like to try again? (Y/n)");
            ScannerCreator.nextLine();
            answer = ScannerCreator.nextLine();
            if (answer.equalsIgnoreCase("n")) {
                System.out.println("Returning to the main menu...");
                throw new ExitException("User chose to exit");
            }
        } else {
            // Call to Person method to prompt for bills
            // Not implemented in Client / Employee as to not repeat code twice
            // The method will also UPDATE the DB for the ATM and the client
            promptBills(this.addressATM, this.cityATM, withdrawAccount);
        }
    }
    // Validation of input for €
    private boolean validateWithdrawalAmount(int amountToWithdraw) throws ExitException {
        if (amountToWithdraw == -1) {
            throw new ExitException("User chose to exit");
        }
        // Invalid if negative value or not a multiple of 5
        if (!(amountToWithdraw > 0 && amountToWithdraw % 5 == 0)) {
            System.out.println("Invalid amount. Please enter an amount that is a multiple of 5");
            System.out.println("If you wish to leave this operation, type -1 instead");
            return false;
        }
        return true;
    }
    // Method that returns how many bills of each amount is needed
    // given a certain amount of money to withdraw
    private int[] billsToWithdraw(int[] availableBills, int amountToWithdraw) {
        System.out.println("Amount to withdraw: " + amountToWithdraw);

        int[] possibleBills = {50, 20, 10, 5};  // From 50 to 5 because we want to get as few bills as possible
        int[] withdrawBills = new int[4];

        for (int i = 0; i < possibleBills.length; i++) {
            if (amountToWithdraw <= 0) break; // We have enough bills; leave

            int billsNeeded = amountToWithdraw / possibleBills[i];
            // Math.min???
            if (billsNeeded > availableBills[i]) {
                withdrawBills[i] = availableBills[i]; // Takes all available bills
            } else { // Meaning there are more bills in the ATM than we need. So we only take what we need
                withdrawBills[i] = billsNeeded; // Takes only what we need
            }
            // Deduce
            amountToWithdraw -= withdrawBills[i] * possibleBills[i];
        }
        if (amountToWithdraw > 0) {
            System.out.println("It was not possible to withdraw that amount with the available bills");
            return null; // Control this so we can restart the flow if null
        }
        return withdrawBills;
    }
    // Extracts the amount of bills currently available in a certain ATM
    private int[] getAvailableBills(ResultSet resultSet) {
        try {
            int[] availableBills = new int[4];
            for (int i = 0; i < availableBills.length; i++) {
                availableBills[i] = resultSet.getInt(i + 4);
                // Column index instead of name. Bills data starts at index 4 through 7
            }
            return availableBills;
        } catch (SQLException sqlException) {
            System.err.println("Error during withdrawal validation");
            sqlException.printStackTrace();
            return null;
        }
    }
    // Converting the Array of bills that we want to pass on to the UPDATE
    // method to negative will prevent us from writing a whole new method
    // to UPDATE a negative amount
    private int[] convertToNegative(int[] billsAmount) {
        // Inverts the array (to create it I used 50€ as the first element, not last)
        int lenght = billsAmount.length;
        for (int i = 0; i < lenght / 2; i++) {
            int temp = billsAmount[i];
            billsAmount[i] = billsAmount[lenght - 1 - i];
            billsAmount[lenght - 1 - i] = temp;
        }
        for (int i = 0; i < lenght; i++) {
            billsAmount[i] *= -1;
        }
        return billsAmount;
    }

    private int calculateTotalAmount(ResultSet resultSet) {
        try {
            // Moves back the cursor (it moved to the next position upon initializing the ResultSet
            resultSet.previous();
            int total = 0;
            while (resultSet.next()) {
                total += resultSet.getInt("saldo");
            }
            System.out.println("Total amount: " + total);
            return total;
        } catch (SQLException sqlException) {
            System.err.println("Error calculating total amount");
            sqlException.printStackTrace();
            return 0;
        }
    }
    // Method to handle the case where the client has not enough balance in one account
    // but does across all their accounts
    private void promptAccounts(int amountToWithdraw, int[] billsWithdraw) throws ExitException {
        try {
            // ArrayList with the ID's of the accounts
            var accountList = printBalance(this.getDNI());

            System.out.println("If you want to exit at any point, type -1");
            System.out.println("Choose from which accounts do you want to withdraw the money (" +
                    amountToWithdraw + "€) from: ");

            int[] chosenAccounts = new int[accountList.size()];
            // Amount of money from the accounts that will update as we iterate
            var dynamicAmmount = new int[accountList.size()];

            int accumulatedAmount = 0;

            for (int i = 0; i < chosenAccounts.length; i++) {
                System.out.print("Account " + (i + 1) + ": ");
                int account;
                do {
                    account = ScannerCreator.nextInt();

                    if (account == -1) throw new ExitException("User chose to exit");

                    if (!accountList.contains(account)) {
                        System.out.println("Invalid account. Please try again");
                    }
                } while (!accountList.contains(account));

                var accountSet = JDBCPostgreSQL.getAccount(account);

                // Get the balance of that account now that it is valid and store the choice
                int accountBalance = accountSet.getInt("saldo");
                chosenAccounts[i] = account;

                int amountToDeduct = Math.min(amountToWithdraw - accumulatedAmount, accountBalance);

                if (amountToDeduct % 5 != 0) amountToDeduct -= amountToDeduct % 5; // Make sure to only get multiples of 5
                // Now store the value in an array to later iterate through it while updating
                dynamicAmmount[i] = amountToDeduct;
                accumulatedAmount += amountToDeduct;

                if (accumulatedAmount >= amountToWithdraw) {
                    JDBCPostgreSQL.updateATM(billsWithdraw, this.addressATM, this.cityATM);
                    for (int j = 0; j < dynamicAmmount.length; j++) {
                        if (chosenAccounts[j] != 0) JDBCPostgreSQL.updateAccounts(chosenAccounts[j], dynamicAmmount[j]);
                    }
                    accountSet.close();
                    System.out.println("Withdrawal completed successfully");
                    return;
                }
            }
        } catch (SQLException sqlException) {
            System.err.println("Error during multiple accounts withdrawal validation");
            sqlException.printStackTrace();
        }
    }

    public void setAddressATM(String addressATM) {
        this.addressATM = addressATM;
    }
    public void setCityATM(String cityATM) {
        this.cityATM = cityATM;
    }
}