package NorthPoleDatabase.Build;

import java.sql.SQLException;

public interface EmpOps {
    void refillBank() throws ExitException;
    void addClient() throws ExitException;
    void deleteClient() throws ExitException;
    void addAccount() throws ExitException;
    void deleteAccount() throws ExitException;
}
