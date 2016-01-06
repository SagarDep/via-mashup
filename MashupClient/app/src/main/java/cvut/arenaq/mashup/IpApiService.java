package cvut.arenaq.mashup;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface IpApiService {
    @GET("/json/{ip}")
    Call<IpApiModel> lookup(@Path("ip") String ip);
}
