package cvut.arenaq.mashup.WhoisApi;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Query;

public interface WhoisApiService {
    @GET("api/whois/")
    Call<WhoisWrapper> whois(
            @Query("url") String url);
}
