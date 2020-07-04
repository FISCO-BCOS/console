package console.data.tools;

public class SafeKeeperUrl {

    private String url;
    private String server;

    public SafeKeeperUrl() {}

    public SafeKeeperUrl(String url, String server) {
        this.url = url;
        this.server = server;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
