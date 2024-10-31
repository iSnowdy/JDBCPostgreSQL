package NorthPoleDatabase.Build;

import java.sql.SQLException;

public class OpMenu {
    // Basic information that is passed down from the Login Menu
    private final String DNI;
    private String pin;
    private Rol rol;

    private final String name;
    // Objects for the current user
    private Client userClient;
    private Employee userEmployee;
    // switch menu choice
    private int choice;

    public OpMenu(String DNI, String pin, String name, Rol rol) throws SQLException {
        this.DNI = DNI;
        this.pin = pin;
        this.name = name;
        this.rol = rol;

        switch (rol) {
            case C -> this.userClient = new Client(this.DNI, this.pin, this. name);
            case E -> this.userEmployee = new Employee(this.DNI, this.pin, this.name);
        }
        System.out.println("Welcome " + this.name + "!");
        boolean validATM = false;

        do {
            validATM = printATMs();
            if (!validATM) {
                System.out.println("In order to continue, you MUST choose a valid ATM");
            }
        } while (!validATM);
        promptUser();
    }
    // Main method to, depending on the role of the user, one menu or the other will be printed
    // and a valid choice will be taken
    private void promptUser() {
        printMainMenu();
        try {
            this.choice = ScannerCreator.nextInt();

            if (rol == Rol.C) {
                while (!(1 <= this.choice && this.choice <= 7)) {
                    System.out.println("Please select a valid option");
                    choice = ScannerCreator.nextInt();
                }
                clientSwitch();
            } else if (rol == Rol.E) {
                while (!(1 <= this.choice && this.choice <= 10)) {
                    System.out.println("Please select a valid option");
                    choice = ScannerCreator.nextInt();
                }
                employeeSwitch();
            }
        } catch (Exception e) {
            System.err.println("Unexpected error");
            e.printStackTrace();
        }
    }

    private void printMainMenu() {
        switch (rol) {
            case C -> printClientMenu();
            case E -> printEmployeeMenu();
        }
    }
    // Employee's exclusive menu
    private void printEmployeeMenu() {
        System.out.println("=============================================");
        System.out.println("|               EMPLOYEE MENU               |");
        System.out.println("---------------------------------------------");
        System.out.println("|              1. Refill ATM                |");
        System.out.println("|              2. Add Client                |");
        System.out.println("|              3. Delete Client             |");
        System.out.println("|              4. Add Account               |");
        System.out.println("|              5. Delete Account            |");
        System.out.println("|              6. Transaction               |");
        System.out.println("|              7. Check Balance             |");
        System.out.println("|              8. Change PIN                |");
        System.out.println("|              9. Change working ATM        |");
        System.out.println("|             10. Exit                      |");
        System.out.println("=============================================\n");
    }
    // Client's exclusive menu
    private void printClientMenu() {
        System.out.println("=============================================");
        System.out.println("|                CLIENT MENU                |");
        System.out.println("---------------------------------------------");
        System.out.println("|              1. Deposit Money             |");
        System.out.println("|              2. Withdraw Money            |");
        System.out.println("|              3. Transaction               |");
        System.out.println("|              4. Check Balance             |");
        System.out.println("|              5. Change PIN                |");
        System.out.println("|              6. Change working ATM        |");
        System.out.println("|              7. Exit                      |");
        System.out.println("=============================================\n");
    }
    // Every time we enter the switch, the Scanner buffer must be cleansed
    // as to not cause conflicts between int (switch) and Strings (questions)
    private void employeeSwitch() {
        ScannerCreator.nextLine();
        try {
            switch (this.choice) {
                case 1 -> {
                    // Refill ATM SQL logic
                    this.userEmployee.refillBank();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 2 -> {
                    // Add client logic
                    this.userEmployee.addClient();
                    promptUser();
                }
                case 3 -> {
                    // Delete client logic
                    this.userEmployee.deleteClient();
                    promptUser();
                }
                case 4 -> {
                    // Add account logic
                    this.userEmployee.addAccount();
                    promptUser();
                }
                case 5 -> {
                    // Delete account logic
                    this.userEmployee.deleteAccount();
                    promptUser();
                }
                case 6 -> {
                    // Transaction logic
                    this.userEmployee.makeTransfer();
                    promptUser();
                }
                case 7 -> {
                    // Check balance logic
                    this.userEmployee.checkBalance();
                    promptUser();
                }
                case 8 -> {
                    // Change PIN logic
                    this.userEmployee.changePIN();
                    promptUser();
                }
                case 9 -> {
                    // Change the address/city of the ATM
                    printATMs();
                    promptUser();
                }
                case 10 -> {
                    // Exit. Disconnect the JDBC Driver and 0 as correct exit code
                    System.out.println("Goodbye " + this.name + "!");
                    JDBCPostgreSQL.disconnect();
                    System.exit(0); // Correct finalization
                }
            }
            // Catches any ExitException thrown from within the methods and brings the user back to the main menu
        } catch (ExitException exitException) {
            System.out.println("Welcome back to the Operation Menu...");
            promptUser();
        }
    }

    private void clientSwitch() {
        ScannerCreator.nextLine();
        try {
            switch (this.choice) {
                case 1 -> {
                    // Deposit money logic
                    this.userClient.depositMoney();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 2 -> {
                    // Withdraw logic
                    this.userClient.withdrawMoney();
                    promptUser();
                }
                case 3 -> {
                    // Transaction logic
                    this.userClient.makeTransfer();
                    promptUser();
                }
                case 4 -> {
                    // Check balance logic
                    this.userClient.checkBalance();
                    promptUser();
                }
                case 5 -> {
                    // Change PIN logic
                    this.userClient.changePIN();
                    promptUser();
                }
                case 6 -> {
                    // Change the address/city of the ATM
                    printATMs();
                    promptUser();
                }
                case 7 -> {
                    System.out.println("Goodbye " + this.name + "!");
                    JDBCPostgreSQL.disconnect();
                    System.exit(0); // Correct finalization
                }
            }
            // Catches any ExitException thrown from within the methods and brings the user back to the main menu
        } catch (ExitException exitException) {
            System.out.println("Welcome back to the Operation Menu...");
            promptUser();
        }
    }

    private boolean printATMs() {
        try {
            String addressATM;
            String cityATM;
            int id;
            int choice;

            System.out.println();
            do {
                var resultSet = JDBCPostgreSQL.getAllATM();
                resultSet.previous();
                System.out.println("----------------- ATM COLLECTION -----------------");
                System.out.println("Please choose the ATM you wish to work with");
                System.out.println("To do so, provide the ID number of the ATM");
                System.out.println("--------------------------------------------------");
                System.out.println("|ID" + "              " + "Address" + "               " + "City      |");

                while (resultSet.next()) {
                    id = resultSet.getInt("id");
                    addressATM = resultSet.getString("direccion");
                    cityATM = resultSet.getString("poblacion");

                    System.out.println("|" + id + "           " + addressATM + "       " + cityATM + "    |");
                }
                System.out.println("--------------------------------------------------");
                System.out.println("----------------- ATM COLLECTION -----------------\n");
                System.out.println("If you want to exit the program, please type -1");
                choice = ScannerCreator.nextInt();
                resultSet.close();
            } while (!validateATMChoice(choice));
            ScannerCreator.nextLine();
            System.out.println();
            return true;
        } catch (ExitException exitException) {
            System.out.println("Exiting the program...");
            System.exit(0);
        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
        return false;
    }
    // Validates that the given ATM ID exists
    private boolean validateATMChoice(int choice) throws ExitException {
        if (choice == -1) {
            throw new ExitException("User chose to exit the program");
        }

        var resultSet = JDBCPostgreSQL.getATM(choice);

        if (resultSet == null) {
            System.out.println("Invalid ATM. Please try again");
            return false;
        } else {
            String addressATM;
            String cityATM;
            try {
                addressATM = resultSet.getString("direccion");
                cityATM = resultSet.getString("poblacion");
                addATMInformation(addressATM, cityATM);
                return true;
            } catch (SQLException sqlException) {
                sqlException.printStackTrace();
                return false;
            }
        }
    }
    // If the user chooses to, they can change the working ATM. Therefore, we need to UPDATE
    // that information with setters
    private void addATMInformation(String addressATM, String cityATM) {
        switch (this.rol) {
            case C -> {
                this.userClient.setAddressATM(addressATM);
                this.userClient.setCityATM(cityATM);
            }
            case E -> {
                this.userEmployee.setAddressATM(addressATM);
                this.userEmployee.setCityATM(cityATM);
            }
        }
    }
}