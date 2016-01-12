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
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;

import cvut.arenaq.mashup.AlchemyApi.AlchemyApiService;
import cvut.arenaq.mashup.AlchemyApi.GetRankedTaxonomy;
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

    TextView domain, ip, owner, created, expire, isp, nameservers, language, taxonomy, location;
    GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_table);

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

        domain = (TextView) findViewById(R.id.domain);
        ip = (TextView) findViewById(R.id.ip);
        owner = (TextView) findViewById(R.id.owner);
        created = (TextView) findViewById(R.id.created);
        expire = (TextView) findViewById(R.id.expire);
        isp = (TextView) findViewById(R.id.isp);
        nameservers = (TextView) findViewById(R.id.nameservers);
        language = (TextView) findViewById(R.id.language);
        taxonomy = (TextView) findViewById(R.id.taxonomy);
        location = (TextView) findViewById(R.id.location);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
    }

    public void getInfo(final String url) {
        domain.setText(url);
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

                created.setText(response.getWhois().getCreated());
                expire.setText(response.getWhois().getExpired());
                nameservers.setText(response.getWhois().getNameServer()[0]);
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

                ip.setText(response.getQuery());
                owner.setText(response.getOrg());
                isp.setText(response.getIsp());
                location.setText(response.getCity() + ", " + response.getRegion() + ", " + response.getCountry());
                if (map != null) {
                    LatLng pos = new LatLng(Double.parseDouble(response.getLat()), Double.parseDouble(response.getLon()));
                    map.addMarker(new MarkerOptions().position(pos))
                            .setTitle(response.getCity());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 10));
                    map.animateCamera(CameraUpdateFactory.zoomTo(14), 2000, null);
                }
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

                language.setText(response.getLanguage());

                if (response.getTaxonomy() == null) {
                    taxonomy.setText(response.getTaxonomy().get(0).getLabel());
                }
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
                getInfo(v.getText().toString());
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
