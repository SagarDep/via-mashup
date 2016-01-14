package cvut.arenaq.mashup.IpApi;

import cvut.arenaq.mashup.IpApi.IpApiModel;
import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

public interface IpApiService {
    @GET("/json/{ip}")
    Call<IpApiModel> lookup(@Path("ip") String ip);
}
