package NorthPoleDatabase.Build;

public class ATM {
    // PK and NOT NULL attributes are defined as final
    private final int id;
    private final String address;
    private final String city;

    // Bills
    private int bill5;
    private int bill10;
    private int bill20;
    private int bill50;

    public ATM(int id, String address, String city) {
        this.id = id;
        this.address = address;
        this.city = city;
    }

    public ATM(int id, String address, String city, int bill5, int bill10, int bill20, int bill50) {
        this.id = id;
        this.address = address;
        this.city = city;
        this.bill5 = bill5;
        this.bill10 = bill10;
        this.bill20 = bill20;
        this.bill50 = bill50;
    }


    // Getters and Setters
    public int getId() {
        return id;
    }

    public String getAddress() {
        return address;
    }

    public String getCity() {
        return city;
    }

    public int getBill5() {
        return bill5;
    }
    public void setBill5(int bill5) {
        this.bill5 = bill5;
    }

    public int getBill10() {
        return bill10;
    }
    public void setBill10(int bill10) {
        this.bill10 = bill10;
    }

    public int getBill20() {
        return bill20;
    }
    public void setBill20(int bill20) {
        this.bill20 = bill20;
    }

    public int getBill50() {
        return bill50;
    }
    public void setBill50(int bill50) {
        this.bill50 = bill50;
    }
}
