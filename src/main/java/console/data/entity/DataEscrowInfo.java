package console.data.entity;

public class DataEscrowInfo {

    private String dataID;
    private String cipherText1;
    private String cipherText2;

    public DataEscrowInfo() {
        super();
    }

    public DataEscrowInfo(String dataID, String cipherText1, String cipherText2) {
        super();
        this.dataID = dataID;
        this.cipherText1 = cipherText1;
        this.cipherText2 = cipherText2;
    }

    public String getDataID() {
        return dataID;
    }

    public String getCipherText1() {
        return cipherText1;
    }

    public String getCipherText2() {
        return cipherText2;
    }

    public void setDataId(String dataID) {
        this.dataID = dataID;
    }

    public void setCipherText1(String cipherText1) {
        this.cipherText1 = cipherText1;
    }

    public void setCipherText2(String cipherText2) {
        this.cipherText2 = cipherText2;
    }
}
