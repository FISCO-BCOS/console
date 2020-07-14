package console.data.entity;

public class DataEscrowInfo {

    private String dataEntityId;
    private String creatorCipherText;
    private String userCipherText;

    public DataEscrowInfo() {
        super();
    }

    public DataEscrowInfo(String dataEntityId, String creatorCipherText, String userCipherText) {
        super();
        this.dataEntityId = dataEntityId;
        this.creatorCipherText = creatorCipherText;
        this.userCipherText = userCipherText;
    }

    public String getDataEntityId() {
        return dataEntityId;
    }

    public String getCreatorCipherText() {
        return creatorCipherText;
    }

    public String getUserCipherText() {
        return userCipherText;
    }

    public void setDataEntityId(String dataEntityId) {
        this.dataEntityId = dataEntityId;
    }

    public void setCreatorCipherText(String creatorCipherText) {
        this.creatorCipherText = creatorCipherText;
    }

    public void setUserCipherText(String userCipherText) {
        this.userCipherText = userCipherText;
    }
}
