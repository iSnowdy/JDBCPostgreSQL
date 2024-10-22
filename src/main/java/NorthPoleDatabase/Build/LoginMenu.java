package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginMenu {

    public LoginMenu() {
        printLoginMenu();
    }

    public void printLoginMenu() {
        final String menu =
        "=============================================\n" +
        "                LOGIN MENU                  \n" +
        "=============================================\n" +
        "Please provide your DNI and PIN when prompted\n" +
        "---------------------------------------------\n";
        String DNI;
        String pin;
        // Keeps asking for the client's DNI and pin until a ResultSet
        // is returned (until it is found)
        int counter = 3;
        while (counter > 0) {
            System.out.println(menu);
            DNI = promptDNI();
            pin = promptPIN();

            ResultSet resultSet = JDBCPostgresSQL.getPerson(DNI, pin);

            try {
                if (resultSet != null && resultSet.next()) {
                    String type = resultSet.getString("type");
                    String name = resultSet.getString("nombre");

                    if (type.equals("cliente")) {
                        // Client logic
                        OpMenu opMenu = new OpMenu(DNI, pin, name, Rol.C);
                    } else if (type.equals("empleado")) {
                        // Employee logic
                        OpMenu opMenu = new OpMenu(DNI, pin, name, Rol.E);
                    }
                    // Not really needed because the flow will be directed to OpMenu
                } else {
                    counter--;
                    System.out.println("Invalid DNI or pin. Please try again.\n" +
                            "You have " + counter + " attempts left");
                }
            } catch (SQLException sqlException) {
                System.err.println("Fatal error while trying to login");
                sqlException.printStackTrace();
            }
            // Exhausted login attempts
            if (counter == 0) {
                System.out.println("You have exhausted your attempts. Please try again later");
                System.out.println("Shutting down the application...");
                System.exit(-1);
            }
        }
    }

    private String promptDNI() {
        System.out.print("DNI: ");
        return ScannerCreator.nextLine();
    }

    private String promptPIN() {
        System.out.print("PIN: ");
        return ScannerCreator.nextLine();
    }
}