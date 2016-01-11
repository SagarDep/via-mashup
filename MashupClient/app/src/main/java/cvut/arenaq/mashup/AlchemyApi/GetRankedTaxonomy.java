package cvut.arenaq.mashup.AlchemyApi;

import java.util.List;

public class GetRankedTaxonomy {
    private String status;
    private String language;
    private List<Taxonomy> taxonomy;

    public String getStatus() {
        return status;
    }

    public String getLanguage() {
        return language;
    }

    public List<Taxonomy> getTaxonomy() {
        return taxonomy;
    }
}
