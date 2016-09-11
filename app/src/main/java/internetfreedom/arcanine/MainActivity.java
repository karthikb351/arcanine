package internetfreedom.arcanine;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import internetfreedom.arcanine.model.JobEntity;
import internetfreedom.arcanine.model.NetworkMetaData;
import internetfreedom.arcanine.model.RegistrationEntity;
import internetfreedom.arcanine.model.RequestEntity;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements LocationListener {

    private LocationManager locationManager;
    public NetworkMetaData networkMetaData;
    double lat;
    double lng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RealmConfiguration config = new RealmConfiguration.Builder(getApplicationContext()).build();
        Realm.setDefaultConfiguration(config);
        Dexter.initialize(getApplicationContext());

        Dexter.checkPermission(new PermissionListener() {
            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                l("Permission granted");
                try {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            2000,
                            10, MainActivity.this);
                    onLocationChanged(locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER));
                    getJobs();
                }
                catch (SecurityException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                l("Permission denied");
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

            }
        }, Manifest.permission.ACCESS_COARSE_LOCATION);


    }

    @Override
    public void onLocationChanged(Location location) {
        lat = location.getLatitude();
        lng = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Please enable location services",
                Toast.LENGTH_SHORT).show();
    }

    public void l(String msg) {
        Toast.makeText(MainActivity.this, ""+msg, Toast.LENGTH_SHORT).show();
        Log.d("MainActivity", msg+"");
    }

    public void getJobs() {
        final AuthService authService = AuthService.getAuthService(getApplicationContext());
        if(authService.isLoggedIn()) {
            new NetworkMetaDataTask(getApplicationContext(), lat, lng).execute();
        }
        else {
            APIService.getApiService().api.register().enqueue(new Callback<RegistrationEntity>() {
                @Override
                public void onResponse(Call<RegistrationEntity> call, Response<RegistrationEntity> response) {
                    if (response.isSuccessful())
                        authService.saveUserToken(response.body().getToken());
                    new NetworkMetaDataTask(getApplicationContext(), lat, lng).execute();
                }

                @Override
                public void onFailure(Call<RegistrationEntity> call, Throwable t) {
                    l("Something when wrong");
                    t.printStackTrace();
                }
            });
        }
    }

    public class NetworkMetaDataTask extends AsyncTask<Object, Object, NetworkMetaData> {

        Context context;
        double lat;
        double lng;
        NetworkMetaDataTask(Context context, double lat, double lng) {
            this.context = context;
            this.lat = lat;
            this.lng = lng;
        }
        @Override
        protected NetworkMetaData doInBackground(Object[] objects) {
            NetworkMetaData networkMetaData = new NetworkMetaData();
            int type = NetworkHelper.isWifiOrMobile(context);
            if (type == NetworkHelper.IS_DISCONNECTED)
                return null;
            else if (type == NetworkHelper.IS_MOBILE)
                networkMetaData.setType(NetworkMetaData.MOBILE);
            else if (type == NetworkHelper.IS_WIFI)
                networkMetaData.setType(NetworkMetaData.WIFI);

            networkMetaData.setMCCMNC(NetworkHelper.getMCCMNC(context));
            networkMetaData.setLat(String.valueOf(lat));
            networkMetaData.setLng(String.valueOf(lng));
            return networkMetaData;
        }

        @Override
        protected void onPostExecute(NetworkMetaData networkMetaData) {
            l("Doing in background");
            MainActivity.this.networkMetaData = networkMetaData;
            doJobs();
            String type = networkMetaData.getType()==NetworkMetaData.MOBILE ? "mobile" : "wifi";
            APIService.getApiService().api.queryJobs("1", networkMetaData.getMCCMNC(), type, networkMetaData.getLat(), networkMetaData.getLng()).enqueue(new Callback<List<JobEntity>>() {
                @Override
                public void onResponse(Call<List<JobEntity>> call, Response<List<JobEntity>> response) {
                    Realm realm = Realm.getDefaultInstance();
                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(response.body());
                    realm.commitTransaction();
                }

                @Override
                public void onFailure(Call<List<JobEntity>> call, Throwable t) {
                    l("Something whent wrong");
                    t.printStackTrace();
                }
            });
        }
    }

    private void doJobs() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<JobEntity> jobs = realm.where(JobEntity.class).equalTo("completed", false).findAll();
        jobs.addChangeListener(new RealmChangeListener<RealmResults<JobEntity>>() {
            @Override
            public void onChange(RealmResults<JobEntity> element) {
                performTask(element);
            }
        });
        performTask(jobs);
    }

    private void performTask(RealmResults<JobEntity> jobs) {
        for(final JobEntity j:jobs) {
            if (!j.isCompleted()) {

                RequestEntity httpRequestEntity;
                try {
                    httpRequestEntity = NetworkHelper.getRequestEntity("http://"+j.getUrl());
                } catch (Exception e) {
                    continue;
                }

                RequestEntity httpsRequestEntity;
                try {
                    httpsRequestEntity = NetworkHelper.getRequestEntity("https://"+j.getUrl());
                } catch (Exception e) {
                    continue;
                }

                String ip = NetworkHelper.getIPFromDomain(j.getUrl());
                OkHttpClient client = new OkHttpClient();

                JSONObject obj = new JSONObject();
                try {
                    obj.put("network", "1");
                    obj.put("mccmnc", networkMetaData.getMCCMNC());
                    obj.put("type", networkMetaData.getType()==NetworkMetaData.MOBILE ? "mobile" : "wifi");
                    obj.put("lat", String.valueOf(lat));
                    obj.put("lng", String.valueOf(lng));
                    obj.put("dns", ip);
                    obj.put("tcp", ip==null ? false : true);

                    JSONObject http = new JSONObject();
                    http.put("body", httpRequestEntity==null ? null : httpRequestEntity.getBody());
                    http.put("code", httpRequestEntity==null ? null : httpRequestEntity.getCode());
                    JSONObject httpHeaders = new JSONObject();
                    for (int i = 0; i<httpRequestEntity.getHeaderKeys().size(); i++){
                        httpHeaders.put(httpRequestEntity.getHeaderKeys().get(i), httpRequestEntity.getHeaderValues().get(i));
                    }
                    http.put("headers", httpHeaders);

                    obj.put("http", http);

                    JSONObject https = new JSONObject();
                    https.put("body", httpsRequestEntity==null ? null : httpsRequestEntity.getBody());
                    https.put("code", httpsRequestEntity==null ? null : httpsRequestEntity.getCode());
                    JSONObject httpsHeaders = new JSONObject();
                    for (int i = 0; i<httpsRequestEntity.getHeaderKeys().size(); i++){
                        httpsHeaders.put(httpsRequestEntity.getHeaderKeys().get(i), httpsRequestEntity.getHeaderValues().get(i));
                    }
                    https.put("headers", httpsHeaders);

                    obj.put("https", https);


                } catch (Exception e) {

                }


                String json = obj.toString();
                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"),json);
                Request request = new Request.Builder()
                        .addHeader("Authentication", AuthService.getAuthService(getApplicationContext()).getAuthHeader())
                        .url("https://secure-river-57103.herokuapp.com/job/"+j.getId()+"/submit")
                        .post(body)
                        .build();

                client.newCall(request).enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {

                    }

                    @Override
                    public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                        j.setCompleted(true);
                    }
                });
            }



        }
    }

}
