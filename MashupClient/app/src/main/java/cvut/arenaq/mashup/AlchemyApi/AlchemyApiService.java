package cvut.arenaq.mashup.AlchemyApi;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface AlchemyApiService {
    @GET("/calls/url/URLGetRankedKeywords?apikey=e9b2175fdaa36af4febe23ec32b3b9ad47154727&outputMode=json&url={url}")
    Call<GetRankedKeywords> getRankedKeywords(@Path("url") String url);

    @GET("/calls/url/URLGetRankedConcepts?apikey=e9b2175fdaa36af4febe23ec32b3b9ad47154727&outputMode=json&url={url}")
    Call<GetRankedConcepts> getRankedConcepts(@Path("url") String url);
}
