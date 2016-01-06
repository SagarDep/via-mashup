package cvut.arenaq.mashup;

import java.util.List;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by DZCPK7D on 5.1.2016.
 */
public interface GitHubService {
    @GET("/users/{user}/repos")
    Call<List<Repo>> listRepos(@Path("user") String user);
}
