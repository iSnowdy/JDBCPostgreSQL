package NorthPoleDatabase.Build;

// https://www.w3api.com/Java/InputMismatchException/

import java.sql.ResultSet;
import java.sql.SQLException;

public class Employee extends Person implements EmpOps {
    // ENUM for rol
    private Rol rol;
    private String addressATM;
    private String cityATM;

    public Employee(String DNI, String name, String pin) {
        super(DNI, name, pin);
        this.rol = Rol.E; // default value in the DDL
    }
    // Abstract methods
    @Override
    protected void makeTransfer() throws ExitException {
        boolean validAccounts = false;
        String answer;
        int originAccount;
        int destinationAccount;
        int amountToTransfer;

        System.out.println("Initializing Transfer Operation (Employee)...");

        while (!validAccounts) {
            System.out.println("Please provide the following information");
            System.out.print("Origin account: ");
            originAccount = ScannerCreator.nextInt();
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

    // Refills the amount of bills an ATM has. It will call two method. (1) A method to
    // validate that the address of the ATM is valid and then (2) a method that will
    // validate user input before updating the ATM
    @Override
    public void refillBank() throws ExitException {
        // Handle weird inputs
        System.out.println("Initializing Refill Bank operation...");
        // Call to Person method to prompt for bills
        // Not implemented in Client / Employee as to not repeat code twice
        this.promptBills(this.addressATM, this.cityATM, -1);
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

        System.out.println("Initializing Add Client Operation...");

        while (!validClientInfo) {
            System.out.println("Please provide the following information");
            do {
                System.out.print("Client's DNI: ");
                DNI = ScannerCreator.nextLine();
            } while (!(validateDNI(DNI)));
            System.out.print("Client's name: ");
            name = ScannerCreator.nextLine();
            System.out.println("Automatically generating PIN number for the client...");
            pin = generatePIN();

            System.out.println("Are you sure you want to ADD a CLIENT to the database with the following information (Y/n)?");
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
                    System.out.println("A client with this DNI already exists. Please try again");
                }
            } else if (answer.equalsIgnoreCase("n")) {
                System.out.println("Would you like to try again (Y/n)?");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else {
                System.out.println("Invalid input. Please enter 'Y' for yes or 'n' for no");
            }
        }
    }
    // Deletes a client from the DB. It has several security questions
    @Override
    public void deleteClient() throws ExitException {
        boolean validClient = false;
        String answer;
        String DNI;
        String name;

        System.out.println("Initializing Client Deletion Operation...");

        while(!validClient) {
            System.out.println("Please provide the following information");
            do {
                System.out.print("Client's DNI: ");
                DNI = ScannerCreator.nextLine();
            } while (!validateDNI(DNI));
            System.out.print("Client's name: ");
            name = ScannerCreator.nextLine();

            System.out.println("Are you sure you want to DELETE the CLIENT with the following information (Y/n)?");
            System.out.println("DNI: " + DNI);
            System.out.println("Name: " + name);
            answer = ScannerCreator.nextLine();

            if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                if (JDBCPostgreSQL.getClient(DNI) == null) {
                    System.out.println("Client not found. Please validate the input data");
                } else {
                    JDBCPostgreSQL.deleteUser(DNI, name);
                    validClient = true;
                }
            } else if (answer.equalsIgnoreCase("n")) {
                System.out.println("Would you like to try again (Y/n)?");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else {
                System.out.println("Invalid input. Please enter 'Y' for yes or 'n' for no");
            }
        }
    }
    // Adds an account to an existing client. Client existence validation is implemented
    @Override
    public void addAccount() throws ExitException {
        boolean validClient = false;
        String answer;
        String DNI;

        System.out.println("Initializing Account Addition Operation...");

        while (!validClient) {
            System.out.println("Please provide the following information");
            System.out.print("Client's DNI: ");
            DNI = ScannerCreator.nextLine();

            if (JDBCPostgreSQL.getClient(DNI) == null) {
                System.out.println("Client not found. Please validate the input data");
                System.out.println("Would you like to try again (Y/n)?");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else { // The client exists and the operation can continue
                System.out.println("Are you sure you want to ADD an ACCOUNT " +
                        "to the database with the following information (Y/n)?");
                System.out.println("DNI: " + DNI);
                answer = ScannerCreator.nextLine();
                if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                    JDBCPostgreSQL.insertAccount(DNI);
                    validClient = true;
                } else if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                } else {
                    System.out.println("Invalid input. Please enter 'Y' for yes or 'n' for no");
                }
            }
        }
    }
    // Same as adding but deletes
    @Override
    public void deleteAccount() throws ExitException {
        boolean validClient = false;
        String answer;
        String DNI;
        int accountToDelete = 0;

        System.out.println("Initializing Account Deletion Operation...");

        while (!validClient) {
            System.out.println("Please provide the following information");
            System.out.print("Client's DNI: ");
            DNI = ScannerCreator.nextLine();

            if (JDBCPostgreSQL.getClient(DNI) == null) {
                System.out.println("Client not found. Please validate the input data");
                System.out.println("Would you like to try again (Y/n)?");
                answer = ScannerCreator.nextLine();
                if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                }
            } else {
                System.out.println("Are you sure you want to DELETE an account with the following information (Y/n)?");
                System.out.println("DNI: " + DNI);
                answer = ScannerCreator.nextLine();
                if (answer.isEmpty() || answer.equalsIgnoreCase("y")) {
                    printBalance(DNI);

                    int numberOfAccounts = JDBCPostgreSQL.accountAmount(DNI);
                    if (numberOfAccounts > 1) {
                        System.out.println("Please choose which account to delete: ");
                        accountToDelete = ScannerCreator.nextInt();
                    } else {
                        try {
                            ResultSet resultSet = JDBCPostgreSQL.getAccount(DNI);
                            accountToDelete = resultSet.getInt("numero");
                        } catch (SQLException sqlException) {
                            System.err.println("Error accessing the ResultSet " +
                                    "for deletion of account " + DNI);
                        }
                    }
                    JDBCPostgreSQL.deleteAccount(DNI, accountToDelete);
                    validClient = true;
                } else if (answer.equalsIgnoreCase("n")) {
                    System.out.println("Returning to the main menu...");
                    throw new ExitException("User chose to exit");
                } else {
                    System.out.println("Invalid input. Please enter 'Y' or 'n' for no");
                }
            }
        }
    }
    // Generates a random PIN assigned to a client when the account is created
    // for the first time. Then the user should be prompted to change it. Maybe?
    private String generatePIN() {
        // Math random returns a value of [0.0, 1.0)
        // We need to cast it to int and also magnify it to represent a
        // 4 digit value between the range of (1000, 9999)
        return String.valueOf((int)(Math.random() * 9000) + 1000);
    }

    public void setAddressATM(String addressATM) {
        this.addressATM = addressATM;
    }
    public void setCityATM(String cityATM) {
        this.cityATM = cityATM;
    }
}