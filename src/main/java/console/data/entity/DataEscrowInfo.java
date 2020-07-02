package console.data.entity;

public class DataEscrowInfo {

    private String dataId;
    private String cipherText1;
    private String cipherText2;

    public DataEscrowInfo() {
        super();
    }

    public DataEscrowInfo(String dataId, String cipherText1, String cipherText2) {
        super();
        this.dataId = dataId;
        this.cipherText1 = cipherText1;
        this.cipherText2 = cipherText2;
    }

    public String getdataId() {
        return dataId;
    }

    public String getCipherText1() {
        return cipherText1;
    }

    public String getCipherText2() {
        return cipherText2;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public void setCipherText1(String cipherText1) {
        this.cipherText1 = cipherText1;
    }

    public void setCipherText2(String cipherText2) {
        this.cipherText2 = cipherText2;
    }
}
