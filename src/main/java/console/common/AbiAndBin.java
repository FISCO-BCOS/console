package console.common;

public class AbiAndBin {
    private String abi;
    private String bin;
    private String smBin;

    public AbiAndBin() {}

    public AbiAndBin(String abi, String bin, String smBin) {
        this.abi = abi;
        this.bin = bin;
        this.smBin = smBin;
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
}
