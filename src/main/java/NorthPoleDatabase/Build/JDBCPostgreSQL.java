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
    public static ResultSet getClient(String DNI, String pin) {
        try {
            String preparedStatementSQL =
                    "SELECT * FROM clientes WHERE DNI = ? AND PIN = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(preparedStatementSQL);
            preparedStatement.setString(1, DNI);
            preparedStatement.setString(2, pin);

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
