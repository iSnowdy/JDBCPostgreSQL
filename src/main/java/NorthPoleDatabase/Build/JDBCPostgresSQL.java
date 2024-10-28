package NorthPoleDatabase.Build;

import java.sql.*;

public class JDBCPostgresSQL {
    // Strings to build the valid URL
    private final String URL = "jdbc:postgresql://";
    // jdbc:postgresql://localhost:5432/postgres
    private final String HOST;
    private final String PORT = ":5432/";
    private final String DBNAME;
    private final String VALIDURL;
    // User information
    private final String USERNAME;
    private final String PASSWORD;
    // Object that will represent the connection to the DB
    // once the Driver is called (.DriverManager)
    private static Connection connection;
    // To store the return number of tuples affected after a SQL query
    // public access just in case we want to verify from outside the class
    public static int tuplesAffected; // Make it not static

    // localhost constructor
    public JDBCPostgresSQL(String USERNAME, String PASSWORD, String DBNAME) throws SQLException {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.DBNAME = DBNAME;
        this.HOST = "localhost";

        this.VALIDURL = URL + HOST + PORT + DBNAME;

        // Connection to the DB is not left in the hands of the user
        connect();
    }

    // HOST constructor
    public JDBCPostgresSQL(String USERNAME, String PASSWORD, String HOST, String DBNAME) throws SQLException {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.DBNAME = DBNAME;
        this.HOST = HOST;

        this.VALIDURL =  URL + HOST + PORT + DBNAME;

        // Connecting using a specific HOST
        connect(VALIDURL);
    }

    // Given the USERNAME and PASSWORD, connects to the DB
    private void connect() {
        try {
            this.connection = DriverManager.getConnection(this.VALIDURL, this.USERNAME, this.PASSWORD);
            System.out.println("You have been succesfully connected to the PostgresSQL database");
        } catch (SQLException sqlException) {
            System.err.println("Error connecting to PostgresSQL");
            sqlException.printStackTrace();
            System.exit(1);
        }
    }

    // When the connection is not done through localhost
    private void connect(String URL) {
        try {
            this.connection = DriverManager.getConnection(URL, this.USERNAME, this.PASSWORD);
            System.out.println("You have been succesfully connected to the PostgresSQL database");
        } catch (SQLException sqlException) {
            System.err.println("Error connecting to PostgresSQL");
            sqlException.printStackTrace();
            System.exit(1);
        }
    }

    // Checks if the connection is still valid. Probably not needed here? But should be
    // good practice. Especially later on if we use long-time connections / pools
    protected boolean validConnection() {
        try {
            if (connection != null && connection.isValid(0)) {
                System.out.println("Connection is valid");
                return true;
            }
        } catch (SQLException sqlException) {
            System.err.println("Invalid connection");
            sqlException.printStackTrace();
        }
        return false;
    }

    // Closes the connection to the DB
    protected static void disconnect() {
        try {
            System.out.println("Closing connection...");
            connection.close();
            System.out.println("Connection closed");
        } catch (SQLException sqlException) {
            System.err.println("Error closing connection");
            sqlException.printStackTrace();
        }
    }

    // Queries
    // pSt.executeQuery() == SELECT
    // pSt.executeUpdate() == INSERT, UPDATE, DELETE

    // SELECT

    // Search for a client. Only need DNI because the name and PIn
    // in reality can be repeated; which is not the case for DNI (PK)
    public static ResultSet getClient(String DNI) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM clientes WHERE dni = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);

            ResultSet resultSet = preparedStatement.executeQuery();

            // If we don't have a single register, then the client does not exist
            if (!resultSet.next()) return null;
            // Otherwise, return the ResultSet of the query
            // preparedStatement.close();
            // Cannot close the statement here because it will also close the ResultSet Object,
            // losing the return information in the process. So this preparedStatement has to be eventually
            // automatically closed
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting client from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getPerson(String DNI, String pin) {
        // UNION can only return one row even if some rows from two queries return something
        // UNION ALL, on the other hand, can result duplicates. Which we want since we are
        // querying two tables where an employee could also be a client
        try {
            //String preparedStatementSQL = "SELECT 'cliente' AS type, dni, nombre FROM clientes WHERE dni = ? AND pin = ? UNION ALL SELECT 'empleado' AS type , dni, nombre FROM empleados WHERE dni = ? AND pin = ?";

            String preparedStatementSQL =
                    "SELECT 'cliente' AS type, dni, nombre " +
                    "FROM clientes WHERE dni = ? AND pin = ? " +
                            "UNION ALL " +
                    "SELECT 'empleado' AS TYPE, dni, nombre " +
                    "FROM empleados WHERE dni = ? AND pin = ?";
            // Scroll-insensitive is necessary to be able to move backwards (.previous()) and
            // concur-read-only because we do not need to be modifying any data inside this ResultType
            PreparedStatement preparedStatement = connection.prepareStatement(
                    preparedStatementSQL,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, pin);
            preparedStatement.setString(3, DNI);
            preparedStatement.setString(4, pin);

            ResultSet resultSet = preparedStatement.executeQuery();

            // If we don't have a single register, then the client does not exist
            if (!resultSet.next()) return null;
            // We moved the cursor to the first row. If we were to just return it, the
            // state of return would be in row 1. So when dealing with it outside here,
            // we would not have any information after calling .next() again
            resultSet.previous();
            // Otherwise, return the ResultSet of the query
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting client from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAccount(int account) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cuentas " +
                    "WHERE numero = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setInt(1, account);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return null; // Does not exist
            return resultSet; // Returns all the information contained in the DB for that account number
        } catch (SQLException sqlException) {
            System.err.println("Error getting account from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAccount(String DNI) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cuentas " +
                    "WHERE dni_titular = " +
                            "(SELECT dni FROM clientes WHERE dni = ?)" +
                    "ORDER BY numero";

            PreparedStatement preparedStatement = connection.prepareStatement(
                    preparedStatementSQL,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            preparedStatement.setString(1, DNI);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return null; // Does not exist
            return resultSet; // Returns all the information contained in the DB for that account number
        } catch (SQLException sqlException) {
            System.err.println("Error getting account from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }

    }
    // Method to return the number of tuples if a client's bank
    public static int accountAmount(String DNI) {
        try {
            String preparedStatementSQL =
                    "SELECT COUNT(*) FROM cuentas " +
                    "WHERE dni_titular = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);

            ResultSet resultSet = preparedStatement.executeQuery();
            int tuplesAmount = resultSet.next() ? resultSet.getInt(1) : 0;
            preparedStatement.close();
            if (tuplesAmount > 0) {
                return tuplesAmount;
            } else {
                System.out.println("The provided client has no accounts to their name");
                return 0;
            }
        } catch (SQLException sqlException) {
            System.err.println("Error getting account amount from PostgresSQL");
            sqlException.printStackTrace();
            return 0;
        }
    }

    public static ResultSet getAllATM() {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cajeros ORDER BY id";

            PreparedStatement preparedStatement = connection.prepareStatement(
                    preparedStatementSQL,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return null;
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting all ATM's from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getATM(int choice) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cajeros " +
                    "WHERE id = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(
                    preparedStatementSQL,
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);

            preparedStatement.setInt(1, choice);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (!resultSet.next()) return null;
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting ATM from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    // Method to check if the ATM that the employee wants to UPDATE exists or not
    public static ResultSet getATM(String address, String city) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cajeros " +
                    "WHERE direccion = ? AND poblacion = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, address);
            preparedStatement.setString(2, city);

            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) return null; // Nothing found
            return resultSet; // If the query found something, return that
        } catch (SQLException sqlException) {
            System.err.println("Error validating ATM UPDATE from PostgresSQL");
            sqlException.printStackTrace();
            return null;
        }
    }
    // UPDATE
    public static boolean updateATM(int[] bills, String address, String city) throws SQLException {
        try {
            // For updates, we will enclose them in a Transaction to ensure
            // the DB ACID properties
            connection.setAutoCommit(false);

            String preparedStatementSQL =
                    "UPDATE cajeros " +
                    "SET billetes5 = billetes5 + ?, " +
                    "billetes10 = billetes10 + ?, " +
                    "billetes20 = billetes20 + ?, " +
                    "billetes50 = billetes50 + ? " +
                    "WHERE direccion = ? AND poblacion = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);

            // Updates all the ? values at once
            for (int i = 0; i < bills.length; i++) {
                preparedStatement.setInt(i + 1, bills[i]);
            }
            preparedStatement.setString(5, address);
            preparedStatement.setString(6, city);
            // PreparedStatement.executeUpdate() is used for any queries that somehow change
            // the DB (INSERT, UPDATE, DELETE); DML. It returns how many rows or tuples were
            // affected by that statement. So as long it is > 0, we know the query was
            // correctly executed
            tuplesAffected = 0; // Clears any data that may have been previously stored inside
            tuplesAffected =  preparedStatement.executeUpdate();
            preparedStatement.close(); // Frees up resources
            if (tuplesAffected > 0) {
                connection.commit();
                System.out.println("Successfully updated ATM UPDATE");
                return true;
            } else {
                connection.rollback();
                System.out.println("Failed to update ATM UPDATE");
                return false;
            }
        } catch (SQLException sqlException) {
            System.err.println("Error while trying to update the ATM");
            sqlException.printStackTrace();
            connection.rollback(); // If something went wrong...
            return false;
        } finally { // No matter what, reset it to true
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }
    // Method overloading
    public static void updateAccounts(int originAccount, int amountToTransfer) {
        updateAccounts(originAccount, -1, amountToTransfer);
    }
    // This method will handle: employee's transactions between 2 accounts and client transactions
    public static void updateAccounts(int originAccount, int destinationAccount, int amountToTransfer) {
        try {
            // Envelope the UPDATE in a Transaction
            connection.setAutoCommit(false);

            String originPreparedStatementSQL =
                    "UPDATE cuentas " +
                    "SET saldo = saldo - ? " +
                    "WHERE numero = ?";
            String destinationPreparedStatementSQL =
                    "UPDATE cuentas " +
                    "SET saldo = saldo + ? " +
                    "WHERE numero = ?";

            PreparedStatement ogPreparedStatement = connection.prepareStatement(originPreparedStatementSQL);
            PreparedStatement destPreparedStatement = null;

            // Like this we can reuse this code for money withdrawal and not only transactions
            int amountToTransferTo = (destinationAccount == -1) ? 0 : amountToTransfer;

            ogPreparedStatement.setInt(1, amountToTransfer);
            ogPreparedStatement.setInt(2, originAccount);
            if (destinationAccount != -1) {
                // System.out.println("Doing a two way transaction...");
                destPreparedStatement = connection.prepareStatement(destinationPreparedStatementSQL);
                destPreparedStatement.setInt(1, amountToTransferTo);
                destPreparedStatement.setInt(2, destinationAccount);
            }
            tuplesAffected = 0;
            int tuplesAffected2; // To store the second UPDATE results
            tuplesAffected = ogPreparedStatement.executeUpdate();
            tuplesAffected2 = (destPreparedStatement != null) ?
                    destPreparedStatement.executeUpdate() : 0;
            // Frees memory
            ogPreparedStatement.close();
            if (destPreparedStatement != null) destPreparedStatement.close();
            // If the destination account statement was not null, it means that something was executed
            // The other case is withdrawal situation, represented by a -1. This way we control both cases
            if (tuplesAffected > 0 && (destinationAccount == -1 || tuplesAffected2 > 0)) {
                connection.commit();
                System.out.println("The transaction was successful");
            } else {
                connection.rollback();
                System.out.println("Failed to do the transaction");
            }
        } catch (SQLException sqlException) {
            try {
                System.err.println("Error while trying to make the transaction");
                sqlException.printStackTrace();
                connection.rollback();
            } catch (SQLException sqlException2) {
                System.err.println("Error during rollback");
            }
        } finally { // autocommit back to true in a finally block ensures that no matter what, this will be restored
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }

    public static void depositToAccount(int account, int amount) {
        // No transaction here because the method that calls it already
        // has a transaction in it
        try {
            String preparedStatementSQL =
                    "UPDATE cuentas " +
                    "SET saldo = saldo + ? " +
                    "WHERE numero = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setInt(1, amount);
            preparedStatement.setInt(2, account);

            tuplesAffected = 0;
            tuplesAffected = preparedStatement.executeUpdate();
            preparedStatement.close();

            if (tuplesAffected > 0) {
                System.out.println("The deposit of " + amount + "€ to the account number " + account +
                        " was successful");
            } else {
                System.out.println("Failed to deposit to the account number provided");
            }
        } catch (SQLException sqlException) {
                System.err.println("Error while trying to make a deposit");
                sqlException.printStackTrace();
        }
    }
    // Method that checks if a client has enough balance to perform a withdrawal operation
    public static boolean checkBalance(String DNI, int account, int amount) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cuentas " +
                    "WHERE numero = ? AND dni_titular = " +
                        "(SELECT dni FROM clientes " +
                        " WHERE dni = ?)";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setInt(1, account);
            preparedStatement.setString(2, DNI);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                if (resultSet.getInt("saldo") > amount) {
                    System.out.println("Withdrawal operation accepted. Enough credit is available");
                    System.out.println("Proceding to withdraw " + amount + "€. Please wait...");
                    resultSet.close();
                    return true;
                }
            } else {
                System.out.println("Error while performing the operation. Exiting...");
                resultSet.close();
                return false;
            }
        } catch (SQLException sqlException) {
            System.err.println("Error while trying to check balance");
            sqlException.printStackTrace();
            return false;
        }
        return false;
    }

    public static boolean updatePIN(String DNI, String pin) {
        try {
            connection.setAutoCommit(false);

            String preparedStatementSQL =
                    "UPDATE clientes " +
                    "SET pin = ? " +
                    "WHERE dni = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, pin);
            preparedStatement.setString(2, DNI);

            tuplesAffected = 0;
            tuplesAffected = preparedStatement.executeUpdate();
            preparedStatement.close();
            if (tuplesAffected > 0) {
                connection.commit();
                System.out.println("PIN change operation was successful");
                return true;
            } else {
                connection.rollback();
                System.out.println("Error while trying to change the PIN");
                return false;
            }
        } catch (SQLException sqlException) {
            try {
                System.err.println("Error while trying to change the PIN in PostgresSQL");
                sqlException.printStackTrace();
                connection.rollback();
                return false;
            } catch (SQLException sqlException2) {
                System.err.println("Error during rollback");
                sqlException2.printStackTrace();
                return false;
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }

    public static void insertUser(String DNI, String name, String pin, Rol rol) {
        try {
            String preparedStatementSQL = "";

            switch (rol) {
                case C -> preparedStatementSQL =
                        "INSERT INTO clientes " +
                        "(dni, nombre, pin, rol) VALUES (?, ?, ?, ?)";
                case E -> preparedStatementSQL =
                        "INSERT INTO empleados " +
                        "(dni, nombre, pin, rol) VALUES (?, ?, ?, ?)";
            }

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, pin);
            preparedStatement.setString(4, rol.toString());

            tuplesAffected = 0; // Clears any data that may have been previously stored inside
            tuplesAffected = preparedStatement.executeUpdate();
            if (tuplesAffected > 0) {
                System.out.println("----------------------------------------");
                System.out.println("Client addition successful. Recap:");
                System.out.println("DNI: " + DNI);
                System.out.println("Name: " + name);
                System.out.println("PIN: " + pin);
                System.out.println("----------------------------------------");
                preparedStatement.close(); // Frees up resources

                // Automatically creates the account
                // The ID is automatically generated by PostgresSQL and the balance is set to default 0
                insertAccount(DNI);

            } else {
                System.out.println("Error. Could not add the client to the database.");
                preparedStatement.close(); // Frees up resources
            }
        } catch (SQLException sqlException) {
            System.err.println("Error inserting user from PostgresSQL");
            sqlException.printStackTrace();
        }
    }

    public static void deleteUser(String DNI, String name) {
        try {
            // Initiate transaction. Someone could be using the client's information
            // while we are trying to delete it from the DB. So a transaction is ideal
            connection.setAutoCommit(false);
            String preparedStatementSQL =
                    "DELETE FROM clientes " +
                    "WHERE dni = ? AND nombre = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, name);

            tuplesAffected = 0; // Clears any data that may have been previously stored inside
            tuplesAffected = preparedStatement.executeUpdate();
            preparedStatement.close(); // Frees up resources
            if (tuplesAffected > 0) {
                System.out.println("----------------------------------------");
                System.out.println("Client deletion successful. Recap:");
                System.out.println("DNI: " + DNI);
                System.out.println("Name: " + name);
                System.out.println("----------------------------------------");
                connection.commit();
            } else {
                System.out.println("Error. Could not delete the client from the database");
                connection.rollback();
            }
        } catch (SQLException sqlException) {
            try {
                System.err.println("Error deleting user from PostgresSQL");
                connection.rollback();
                sqlException.printStackTrace();
            } catch (SQLException sqlException2) {
                System.err.println("Error during rollback");
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }

    public static void insertAccount(String DNI) {
        try {
            String preparedStatementSQL =
                    "INSERT INTO cuentas " +
                    "(dni_titular) VALUES (?)";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);

            tuplesAffected = 0;
            tuplesAffected = preparedStatement.executeUpdate();
            if (tuplesAffected > 0) {
                System.out.println("The account for the DNI " + DNI + " has been successfully created.");
            } else {
                System.out.println("Error. Could not add the account to the database.");
            }
        } catch (SQLException sqlException) {
            System.err.println("Error inserting account from PostgresSQL");
            sqlException.printStackTrace();
        }
    }

    /*public static void deleteAccount(String DNI) {
        try {
            connection.setAutoCommit(false);
            String preparedStatementSQL =
                    "DELETE FROM cuentas " +
                    "WHERE dni_titular = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);

            tuplesAffected = 0;
            tuplesAffected = preparedStatement.executeUpdate();

            if (tuplesAffected > 0) {
                System.out.println("The account for the DNI " + DNI + " has been successfully deleted");
                connection.commit();
                connection.setAutoCommit(true);
            } else {
                System.out.println("Error. Could not delete the account from the database.");
                connection.rollback();
                connection.setAutoCommit(true);
            }
        } catch (SQLException sqlException) {
            try {
                System.err.println("Error deleting account from PostgresSQL");
                sqlException.printStackTrace();
                connection.rollback();
            } catch (SQLException sqlException2) {
                System.err.println("Error during rollback");
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }*/

    public static void deleteAccount(String DNI, int account) {
        try {
            connection.setAutoCommit(false);
            CallableStatement callableStatement = connection.prepareCall("CALL delete_Accounts(?, ?)");
            callableStatement.setString(1, DNI);
            callableStatement.setInt(2, account);

            callableStatement.executeUpdate();

            System.out.println("The account number " + account + " for the DNI " + DNI
                    + " has been successfully deleted");

            try {
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException sqlException ) {
                System.err.println("Commit failed");
                connection.rollback();
            }
        } catch (SQLException sqlException) {
            try {
                System.err.println("Error deleting account using the stored procedure from PostgresSQL");
                sqlException.printStackTrace();
                connection.rollback();
            } catch (SQLException sqlException2) {
                System.err.println("Error during rollback");
            }
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException sqlException) {
                System.err.println("Failure to set auto-commit back to true");
                sqlException.printStackTrace();
            }
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}