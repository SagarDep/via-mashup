package cvut.arenaq.mashup;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apmem.tools.layouts.FlowLayout;

import java.io.IOException;
import java.util.StringTokenizer;

import cvut.arenaq.mashup.AlchemyApi.AlchemyApiService;
import cvut.arenaq.mashup.AlchemyApi.GetRankedTaxonomy;
import cvut.arenaq.mashup.AlchemyApi.Taxonomy;
import cvut.arenaq.mashup.IpApi.IpApiModel;
import cvut.arenaq.mashup.IpApi.IpApiService;
import cvut.arenaq.mashup.WhoisApi.Whois;
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

    TextView domain, ip, owner, created, expire, isp, nameservers, language, location;
    FlowLayout taxonomy;
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
        taxonomy = (FlowLayout) findViewById(R.id.taxonomy);
        location = (TextView) findViewById(R.id.location);
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.getUiSettings().setScrollGesturesEnabled(false);
    }

    public void getInfo(final String url) {
        domain.setText(url);
        new AsyncTask<Void, Void, WhoisWrapper>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                created.setText("");
                expire.setText("");
                nameservers.setText("");
            }

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

                Whois whois = response.getWhois();

                if (whois != null) {
                    if (whois.getCreated() != null && whois.getCreated() != "") created.setText(whois.getCreated());
                    if (whois.getExpired() != null && whois.getCreated() != "") expire.setText(whois.getExpired());
                    if (whois.getNameServer() != null && whois.getNameServer().length > 0) {
                        String s = response.getWhois().getNameServer()[0];
                        if (response.getWhois().getNameServer().length > 1) {
                            for (int i = 1; i < response.getWhois().getNameServer().length; i++) s += "\n" + response.getWhois().getNameServer()[i];
                        }
                        nameservers.setText(s);
                    }
                }
            }
        }.execute();

        new AsyncTask<Void, Void, IpApiModel>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                ip.setText("");
                owner.setText("");
                isp.setText("");
                location.setText("");
                if (map != null) {
                    map.clear();
                }
            }

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
                String s = "";
                if (response.getCity() != null && response.getCity() != "") s += response.getCity();
                if (response.getRegion() != null && response.getRegion() != "") {
                    if (s != "") s += ", ";
                    s += response.getRegion();
                }
                if (response.getCountry() != null && response.getCountry() != "") {
                    if (s != "") s += ", ";
                    s += response.getCountry();
                }
                location.setText(s);
                if (map != null) {
                    LatLng pos = new LatLng(Double.parseDouble(response.getLat()), Double.parseDouble(response.getLon()));
                    map.addMarker(new MarkerOptions().position(pos))
                            .setTitle(response.getCity());
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 0));
                    map.animateCamera(CameraUpdateFactory.zoomTo(11), 2000, null);
                }
            }
        }.execute();

        new AsyncTask<Void, Void, GetRankedTaxonomy>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                language.setText("");
                taxonomy.removeAllViewsInLayout();
                taxonomy.addView(new TextView(MainActivity.this));
            }

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

                if (response.getTaxonomy() != null) {
                    for (Taxonomy t : response.getTaxonomy()) {
                        String line = t.getLabel();
                        StringTokenizer st = new StringTokenizer(line, "/");
                        while (st.hasMoreTokens()) {
                            String tag = st.nextToken();
                            TextView v = new TextView(MainActivity.this);

                            GradientDrawable shape =  new GradientDrawable();
                            shape.setCornerRadius(12);
                            shape.setColor(Color.parseColor("#468847"));
                            v.setBackground(shape);

                            FlowLayout.LayoutParams params = new FlowLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            params.setMargins(8, 8, 8, 8);
                            v.setLayoutParams(params);

                            v.setPadding(16, 12, 16, 12);

                            v.setTextColor(Color.WHITE);
                            v.setTypeface(null, Typeface.BOLD);
                            v.setText(tag);
                            taxonomy.addView(v);
                            //taxonomy.addView(v, llp);
                        }
                    }
                    taxonomy.invalidate();
                } else {
                    String s = "error";
                    TextView t = new TextView(MainActivity.this);
                    t.setTextColor(Color.parseColor("#ff0000"));
                    if (response.getStatusInfo() != null) {
                        s = response.getStatusInfo();
                    } else if (response.getStatus() != null) {
                        s = response.getStatus();
                    }
                    t.setText(s);
                    taxonomy.removeAllViewsInLayout();
                    taxonomy.addView(t);
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
