package console.key;

public class KMSService {

    public KMSService(String url) {
        this.url = url;
    }

    public KMSService() {}

    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
