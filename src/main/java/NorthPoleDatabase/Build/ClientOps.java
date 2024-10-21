package NorthPoleDatabase.Build;

import java.sql.SQLException;

public interface ClientOps {
    void withdrawMoney() throws ExitException;
    void depositMoney() throws ExitException;
}