package cvut.arenaq.mashup.WhoisApi;

import java.util.List;

public class Whois {
    private List<String> registrar;
    private List<String> nameServer;
    private String updated;
    private String created;
    private String expired;

    public List<String> getRegistrar() {
        return registrar;
    }

    public List<String> getNameServer() {
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
