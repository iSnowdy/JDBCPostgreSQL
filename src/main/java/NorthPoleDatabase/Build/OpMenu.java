package NorthPoleDatabase.Build;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OpMenu {
    private final String DNI;
    private String pin;
    private Rol rol;

    private String name;

    public OpMenu(String DNI, String pin, Rol rol) throws SQLException {
        this.DNI = DNI;
        this.pin = pin;
        this.rol = rol;

        ResultSet resultSet = null;
        resultSet = JDBCPostgreSQL.getEmployee(DNI, pin);

        this.name = resultSet.getString("nombre");

        System.out.println("Welcome " + this.name + "!");
    }


}
