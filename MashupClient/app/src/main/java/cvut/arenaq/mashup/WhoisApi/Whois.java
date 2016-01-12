package cvut.arenaq.mashup.WhoisApi;

public class Whois {
    private String registrar;
    private String[] nameServer;
    private String updated;
    private String created;
    private String expired;

    public String getRegistrar() {
        return registrar;
    }

    public String[] getNameServer() {
        return nameServer;
    }

    public String getUpdated() {
        return updated;
    }

    public String getCreated() {
        return created;
    }

    public String getExpired() {
        return expired;
    }
}
