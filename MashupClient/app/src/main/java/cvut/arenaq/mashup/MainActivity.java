package cvut.arenaq.mashup;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;

import cvut.arenaq.mashup.AlchemyApi.AlchemyApiService;
import cvut.arenaq.mashup.AlchemyApi.GetRankedTaxonomy;
import cvut.arenaq.mashup.AlchemyApi.Taxonomy;
import cvut.arenaq.mashup.IpApi.IpApiModel;
import cvut.arenaq.mashup.IpApi.IpApiService;
import cvut.arenaq.mashup.WhoisApi.WhoisApiService;
import cvut.arenaq.mashup.WhoisApi.WhoisWrapper;
import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;


public class MainActivity extends ActionBarActivity {

    public static final String WHOIS_API_URL = "http://arenaq-mashup.duke-hq.net/";
    public static final String IP_API_URL = "http://ip-api.com/";
    public static final String ALCHEMY_API_URL = "http://gateway-a.watsonplatform.net/";
    public static final String ALCHEMY_API_KEY = "e9b2175fdaa36af4febe23ec32b3b9ad47154727";
    WhoisApiService whoisApiService;
    IpApiService ipApiService;
    AlchemyApiService alchemyApiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(IP_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ipApiService = retrofit.create(IpApiService.class);

        retrofit = new Retrofit.Builder()
                .baseUrl(ALCHEMY_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        alchemyApiService = retrofit.create(AlchemyApiService.class);

        retrofit = new Retrofit.Builder()
                .baseUrl(WHOIS_API_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        whoisApiService = retrofit.create(WhoisApiService.class);
    }

    public void getInfo(View view) {
        EditText text = (EditText) findViewById(R.id.editDomain);
        final String url = text.getText().toString();

        new AsyncTask<Void, Void, WhoisWrapper>() {
            @Override
            protected WhoisWrapper doInBackground(Void... params) {
                final Call<WhoisWrapper> call = whoisApiService.whois(url);

                try {
                    return call.execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(WhoisWrapper response) {
                super.onPostExecute(response);

                if (response == null) return;

                TextView owner = (TextView) findViewById(R.id.textOwner);
                owner.setText(response.getWhois().getRegistrar());
            }
        }.execute();

        new AsyncTask<Void, Void, IpApiModel>() {
            @Override
            protected IpApiModel doInBackground(Void... params) {
                final Call<IpApiModel> call = ipApiService.lookup(url);

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

                TextView location = (TextView) findViewById(R.id.textLocation);
                location.setText(response.getCity()+", "+response.getCountry()+", "+response.getRegionName());
            }
        }.execute();

        new AsyncTask<Void, Void, GetRankedTaxonomy>() {
            @Override
            protected GetRankedTaxonomy doInBackground(Void... params) {
                final Call<GetRankedTaxonomy> call = alchemyApiService.getRankedTaxonomy(ALCHEMY_API_KEY, "json", url);

                try {
                    return call.execute().body();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return null;
            }

            @Override
            protected void onPostExecute(GetRankedTaxonomy response) {
                super.onPostExecute(response);

                if (response == null) return;

                String taxonomy = "";

                if (response.getTaxonomy() == null) {
                    taxonomy += response.getStatus();
                } else {
                    for (Taxonomy keyword : response.getTaxonomy()) taxonomy += keyword.getLabel()+" ";
                }

                TextView content = (TextView) findViewById(R.id.textContent);
                content.setText(taxonomy);
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
