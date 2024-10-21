package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLOutput;

public class OpMenu {
    // Basic information that is passed down from the Login Menu
    private final String DNI;
    private String pin;
    private Rol rol;
    // Name of the user
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
    }

    private void promptUser() {
        printMainMenu();
        try {
            this.choice = ScannerCreator.nextInt();

            if (rol == Rol.C) {
                while (!(1 <= this.choice && this.choice <= 6)) {
                    System.out.println("Please select a valid option");
                    choice = ScannerCreator.nextInt();
                }
                clientSwitch();
            } else if (rol == Rol.E) {
                while (!(1 <= this.choice && this.choice <= 9)) {
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
        System.out.println("|              9. Exit                      |");
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
        System.out.println("|              6. Exit                      |");
        System.out.println("=============================================\n");
    }

    private void employeeSwitch() {
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
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 3 -> {
                    // Delete client logic
                    this.userEmployee.deleteClient();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 4 -> {
                    // Add account logic
                    this.userEmployee.addAccount();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 5 -> {
                    // Delete account logic
                    this.userEmployee.deleteAccount();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 6 -> {
                    // Transaction logic
                    this.userEmployee.makeTransfer();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 7 -> {
                    // Check balance logic
                    this.userEmployee.checkBalance();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 8 -> {
                    // Change PIN logic
                    this.userEmployee.changePIN();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 9 -> {
                    System.out.println("Goodbye " + this.name + "!");
                    JDBCPostgresSQL.disconnect();
                    System.exit(0);
                }
            }
        } catch (ExitException exitException) {
            System.out.println("Welcome back to the Operation Menu...");
            promptUser();
        }
    }

    private void clientSwitch() {
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

                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 3 -> {
                    // Transaction logic
                    this.userClient.makeTransfer();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 4 -> {
                    // Check balance logic
                    this.userClient.checkBalance();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 5 -> {
                    // Change PIN logic
                    this.userClient.changePIN();
                    // Always bring the user back to the main menu
                    promptUser();
                }
                case 6 -> {
                    System.out.println("Goodbye " + this.name + "!");
                    JDBCPostgresSQL.disconnect();
                    System.exit(0);
                }
            }
        } catch (ExitException exitException) {
            System.out.println("Welcome back to the Operation Menu...");
            promptUser();
        }
    }

}
