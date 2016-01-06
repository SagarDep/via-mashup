package cvut.arenaq.mashup.AlchemyApi;

import java.util.List;

public class GetRankedKeywords {
    private String status;
    private String language;
    private List<Keyword> keywords;

    public String getStatus() {
        return status;
    }

    public String getLanguage() {
        return language;
    }

    public List<Keyword> getKeywords() {
        return keywords;
    }
}
