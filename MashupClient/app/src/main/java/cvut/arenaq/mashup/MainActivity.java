package cvut.arenaq.mashup;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
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


public class MainActivity extends Activity {

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
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem item = menu.findItem(R.id.action_lookup);
        View v = (View) item.getActionView();
        final EditText txtSearch = ( EditText ) v.findViewById(R.id.lookup);
        txtSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                getInfo(v);
                return false;
            }
        });

        item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                txtSearch.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
                return true;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_settings) {
            return true;
        } if (id == R.id.action_lookup) {

        }

        return super.onOptionsItemSelected(item);
    }
}
