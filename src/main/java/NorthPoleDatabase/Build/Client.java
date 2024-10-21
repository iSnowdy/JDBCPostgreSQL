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
                } catch (SQLException sqlException) {
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
    public void withdrawMoney() {

    }

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

    @Override
    public String toString() {
        String information = super.toString();
        return
            "===== CLIENT INFORMATION =====\n" +
            information + "\n" +
            "===== CLIENT INFORMATION =====";
    }
}