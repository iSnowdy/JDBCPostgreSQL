package NorthPoleDatabase.Build;

public interface ClientOps {
    void withdrawMoney() throws ExitException;
    void depositMoney() throws ExitException;
}