package console.contract.model;

public class AbiAndBin {
    private String abi = "";
    private String bin = "";
    private String smBin = "";
    private String devdoc = "";

    public AbiAndBin() {}

    public AbiAndBin(String abi, String bin, String smBin, String devdoc) {
        this.abi = abi;
        this.bin = bin;
        this.smBin = smBin;
        this.devdoc = devdoc;
    }

    public String getSmBin() {
        return smBin;
    }

    public String getAbi() {
        return abi;
    }

    public void setAbi(String abi) {
        this.abi = abi;
    }

    public String getBin() {
        return bin;
    }

    public void setBin(String bin) {
        this.bin = bin;
    }

    public String getDevdoc() {
        return devdoc;
    }
}
