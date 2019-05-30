package console.common;

public class Address {

    private boolean valid;
    private String address;

    public static final int ValidLen = 42;

    public Address() {}

    public Address(boolean valid, String address) {
        this.valid = valid;
        this.address = address;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Address [valid=" + valid + ", \naddress=" + address + "]";
    }
}
