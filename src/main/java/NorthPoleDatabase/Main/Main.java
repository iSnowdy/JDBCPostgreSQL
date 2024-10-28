package NorthPoleDatabase.Main;

import NorthPoleDatabase.Build.JDBCPostgresSQL;
import NorthPoleDatabase.Build.LoginMenu;
import NorthPoleDatabase.Build.ScannerCreator;
import org.postgresql.util.PSQLException;

import java.sql.SQLException;

public class Main {
    public static void main(String[] args) {
        String username;
        String password;
        String dbname;
        int answerConnection = 0;

        System.out.println("Starting JDBC Driver...");
        System.out.println("Please provide the following information");

        System.out.print("Username: ");
        username = ScannerCreator.nextLine(); // postgres
        //username = "postgres";
        System.out.print("Password: ");
        password = ScannerCreator.nextLine(); // 12345
        //password = "12345";
        System.out.print("Database name: ");
        dbname = ScannerCreator.nextLine(); // SouthPoleBank
        //dbname = "SouthPoleBank";
        boolean valid = false;

        while (!valid) {
            System.out.println("Would you like to connect locally (1) or remotely (2)?");
            answerConnection = ScannerCreator.nextInt();
            if (answerConnection == 1 || answerConnection == 2) valid = true;

        }
        // To avoid abnormal termination of the program if the IP is not correctly provided
        try {
            switch(answerConnection) {
                case 1 -> {
                    System.out.println("Connecting to database using localhost...");
                    new JDBCPostgresSQL(username, password, dbname);

                }
                case 2 -> {
                    ScannerCreator.nextLine();
                    System.out.println("Specify the IP address: ");
                    String host = ScannerCreator.nextLine();
                    System.out.println("Connecting to database using remote database...");
                    new JDBCPostgresSQL(username, password, host, dbname);
                }
                default -> System.out.println("Invalid answer. Please try again.");
            }
        } catch (PSQLException psqlException) {
            System.err.println("Error connecting to the database. Exiting program...");
            System.exit(1);
        } catch (SQLException sqlException) {
            System.err.println("Error connecting to the database. Exiting program...");
            System.exit(1);
        }

        ScannerCreator.nextLine();
        new LoginMenu();

        /*
        Empleado
        78901234G
        7890

        Cliente
        12345678A
        1234
        */
    }
}