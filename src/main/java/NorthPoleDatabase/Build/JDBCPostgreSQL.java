package NorthPoleDatabase.Build;

import java.sql.*;

public class JDBCPostgreSQL {
    // Strings to build the valid URL
    private final String URL = "jdbc:postgresql://";
    // jdbc:postgresql://localhost:5432/postgres
    private final String HOST;
    private final String PORT = ":5432/";
    private final String DBNAME;
    private final String validURL;
    // User information
    private final String USERNAME;
    private final String PASSWORD;
    // Object that will represent the connection to the DB
    // once the Driver is called (.DriverManager)
    private static Connection connection;

    // localhost constructor
    public JDBCPostgreSQL(String USERNAME, String PASSWORD, String DBNAME) {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.DBNAME = DBNAME;
        this.HOST = "localhost";

        this.validURL = HOST + URL + PORT + DBNAME;

        // Connection to the DB is not left in the hands of the user
        connect();
    }

    // HOST constructor
    public JDBCPostgreSQL(String USERNAME, String PASSWORD, String HOST, String DBNAME) {
        this.USERNAME = USERNAME;
        this.PASSWORD = PASSWORD;
        this.DBNAME = DBNAME;
        this.HOST = HOST;

        this.validURL = HOST + URL + PORT + DBNAME;

        // Connecting using a specific HOST
        connect(validURL);
    }

    // Given the USERNAME and PASSWORD, connects to the DB
    private void connect() {
        try {
            this.connection = DriverManager.getConnection(this.validURL, this.USERNAME, this.PASSWORD);
            System.out.println("You have been succesfully connected to the PostgreSQL database");
        } catch (SQLException sqlException) {
            System.err.println("Error connecting to PostgreSQL");
            sqlException.printStackTrace();
        }
    }

    // When the connection is not done through localhost
    private void connect(String URL) {
        try {
            this.connection = DriverManager.getConnection(URL, this.USERNAME, this.PASSWORD);
            System.out.println("You have been succesfully connected to the PostgreSQL database");
        } catch (SQLException sqlException) {
            System.err.println("Error connecting to PostgreSQL");
            sqlException.printStackTrace();
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
    protected void disconnect() {
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

    // SELECT

    // Search for a client. Only need DNI because the name and PIn
    // in reality can be repeated; which is not the case for DNI (PK)
    public static ResultSet getClient(String DNI) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM clientes WHERE DNI = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);

            ResultSet resultSet = preparedStatement.executeQuery(preparedStatementSQL);

            // If we don't have a single register, then the client does not exist
            if (!resultSet.first()) return null;
            // Otherwise, return the ResultSet of the query
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting client from PostgreSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getEmployee(String DNI, String pin) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM empleados WHERE DNI = ? AND PIN = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, pin);

            ResultSet resultSet = preparedStatement.executeQuery(preparedStatementSQL);

            // If we don't have a single register, then the client does not exist
            if (!resultSet.first()) return null;
            // Otherwise, return the ResultSet of the query
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting employee from PostgreSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    public static ResultSet getPerson(String DNI, String pin) {
        // UNION can only return one row even if some rows from two queries return something
        // UNION ALL, on the other hand, can result duplicates. Which we want since we are
        // querying two tables where an employee could also be a client
        try {
            String preparedStatementSQL =
                    "SELECT 'cliente' AS type, DNI FROM empleados WHERE DNI = ? AND PIN = ? " +
                    "UNION ALL + " +
                    "SELECT 'empleado' AS type, DNI FROM empleados WHERE DNI = ? AND PIN = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, pin);
            preparedStatement.setString(3, DNI);
            preparedStatement.setString(4, pin);

            ResultSet resultSet = preparedStatement.executeQuery(preparedStatementSQL);

            // If we don't have a single register, then the client does not exist
            if (!resultSet.first()) return null;
            // Otherwise, return the ResultSet of the query
            return resultSet;
        } catch (SQLException sqlException) {
            System.err.println("Error getting client from PostgreSQL");
            sqlException.printStackTrace();
            return null;
        }
    }

    // Method to check if the ATM that the employee wants to UPDATE exists or not
    public static boolean validateATMUPDATE(String address, String city) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM cajeros WHERE direccion = ? AND poblacion = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, address);
            preparedStatement.setString(2, city);

            ResultSet resultSet = preparedStatement.executeQuery(preparedStatementSQL);
            return resultSet.first();
        } catch (SQLException sqlException) {
            System.err.println("Error validating ATM UPDATE from PostgreSQL");
            sqlException.printStackTrace();
            return false;
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
            int tuplesAffected =  preparedStatement.executeUpdate();
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
        }
    }

    public static void insertUser(String DNI, String name, String pin, Rol rol) {
        try {
            String preparedStatementSQL = "";
            switch (rol) {
                case C -> preparedStatementSQL =
                        "INSERT INTO clientes " +
                        "(DNI, nombre, pin, rol) VALUES (?, ?, ?, ?)";
                case E -> preparedStatementSQL =
                        "INSERT INTO empleados " +
                        "(DNI, nombre, pin, rol) VALUES (?, ?, ?, ?)";
            }

            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, pin);
            preparedStatement.setString(4, rol.toString());

            preparedStatement.executeUpdate(preparedStatementSQL);
            System.out.println("----------------------------------------");
            System.out.println("Client addition successful. Recap:");
            System.out.println("DNI: " + DNI);
            System.out.println("Name: " + name);
            System.out.println("PIN: " + pin);
            System.out.println("----------------------------------------");

        } catch (SQLException sqlException) {
            System.err.println("Error inserting user from PostgreSQL");
            sqlException.printStackTrace();
        }

    }




    // Getters and Setters
    public String getUrl() {
        return URL;
    }

    public String getUSERNAME() {
        return USERNAME;
    }

    public String getPassword() {
        return PASSWORD;
    }
}
