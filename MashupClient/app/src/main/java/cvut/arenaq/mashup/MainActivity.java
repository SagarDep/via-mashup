package cvut.arenaq.mashup;

import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


public class MainActivity extends ActionBarActivity {

    public static final String API_BASE_URL = "https://api.github.com";
    public static final String IP_API_URL = "http://ip-api.com/";
    GitHubService service;
    IpApiService ipApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(API_BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(GitHubService.class);

        retrofit = new Retrofit.Builder()
                .baseUrl(IP_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ipApiService = retrofit.create(IpApiService.class);
    }

    public void getRepos(View view) {
        new AsyncTask<Void, Void, List<Repo>>() {
            @Override
            protected List<Repo> doInBackground(Void... params) {
                EditText text = (EditText) findViewById(R.id.editText);
                final Call<List<Repo>> call = service.listRepos(text.getText().toString());

                try {
                    return call.execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<Repo> repos) {
                super.onPostExecute(repos);

                if (repos == null) return;

                List<String> names = new ArrayList<String>();
                for (Repo repo : repos) names.add(String.valueOf(repo.owner.login)+":"+repo.id+":"+repo.name);

                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
            }
        }.execute();
    }

    public void getInfo(View view) {
        new AsyncTask<Void, Void, IpApiModel>() {
            @Override
            protected IpApiModel doInBackground(Void... params) {
                EditText text = (EditText) findViewById(R.id.editText);
                final Call<IpApiModel> call = ipApiService.lookup(text.getText().toString());

                try {
                    return call.execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(IpApiModel response) {
                super.onPostExecute(response);

                if (response == null) return;

                List<String> names = new ArrayList<String>();
                names.add(response.city+", "+response.country+", "+response.regionName);

                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, names);
                ListView listView = (ListView) findViewById(R.id.listView);
                listView.setAdapter(adapter);
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
