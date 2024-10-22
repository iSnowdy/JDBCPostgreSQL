package NorthPoleDatabase.Main;

import NorthPoleDatabase.Build.JDBCPostgresSQL;
import NorthPoleDatabase.Build.LoginMenu;
import NorthPoleDatabase.Build.ScannerCreator;

public class Main {
    public static void main(String[] args) {
        String username;
        String password;
        String dbname;

        System.out.println("Starting JDBC Driver...");
        System.out.println("Please provide the following information: ");

        System.out.print("Username: ");
        //username = ScannerCreator.nextLine(); // postgres
        username = "postgres";
        System.out.print("Password: ");
        //password = ScannerCreator.nextLine(); // 12345
        password = "12345";
        System.out.print("Database name: ");
        //dbname = ScannerCreator.nextLine(); // SouthPoleBank
        dbname = "SouthPoleBank";

        new JDBCPostgresSQL(username, password, dbname);
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

/*

List of things 'working':

    1. DB Connection.
    2. Login Menu.
    3. Operation Menu Type Employee: Refill ATM
    4. Operation Menu Type Employee: Add Client
    5. Operation Menu Type Employee: Delete Client
    6. Operation Menu Type Employee: Add Account
    7. Operation Menu Type Employee: Transaction
    8. Operation Menu Type Employee:
    9. Operation Menu Type Employee:
    10. Operation Menu Type Employee: Exit

Others:

    - ExitException
    - Scanner Int Validator
    - Random PIN Generator
    - (Y/n) verifier (blank counting as yes)

Things to improve/implement:

    - Inside the DELETE ACCOUNT method, build a logic
    to, if the client has more than one account,
    ask which of the accounts they wish to delete

 */