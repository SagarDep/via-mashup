package cvut.arenaq.mashup.AlchemyApi;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;
import retrofit.http.Query;

public interface AlchemyApiService {
    @GET("/calls/url/URLGetRankedKeywords")
    Call<GetRankedKeywords> getRankedKeywords(
            @Query("apikey") String apikey,
            @Query("outputMode") String outputMode,
            @Query("url") String url);

    @GET("/calls/url/URLGetRankedConcepts")
    Call<GetRankedConcepts> getRankedConcepts(
            @Query("apikey") String apikey,
            @Query("outputMode") String outputMode,
            @Query("url") String url);
}
