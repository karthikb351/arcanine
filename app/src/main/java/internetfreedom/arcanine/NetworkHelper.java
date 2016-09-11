package internetfreedom.arcanine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import internetfreedom.arcanine.model.NetworkMetaData;
import internetfreedom.arcanine.model.RequestEntity;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public class NetworkHelper {

    public static int IS_WIFI = 10;
    public static int IS_MOBILE = 11;
    public static int IS_DISCONNECTED = 12;

    public static String getIPFromDomain(String url) {
        String ip = null;
        try {

            InetAddress addr = InetAddress.getByName(getDomainName(url));
            ip = addr.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            ip = null;
        }
        return ip;
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        return domain;
    }

    public static RequestEntity getRequestEntity(String url) throws IOException {
        RequestEntity requestEntity = new RequestEntity();

        OkHttpClient client = new OkHttpClient().newBuilder()
                .followRedirects(false)
                .followSslRedirects(false)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();

        requestEntity.setCode(String.valueOf(response.code()));

        requestEntity.setBody(response.body().string());
        List<String> keys = new ArrayList<>();
        List<String> vals = new ArrayList<>();

        Headers responseHeaders = response.headers();
        for (int i = 0; i < responseHeaders.size(); i++) {
            keys.add(responseHeaders.name(i));
            vals.add(responseHeaders.value(i));
        }
        requestEntity.setHeaderKeys(keys);
        requestEntity.setHeaderValues(vals);

        return requestEntity;

    }

    public static String getMCCMNC(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getNetworkOperator();
    }

    public static int isWifiOrMobile(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();
        if(activeNetworkInfo!=null) {
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return IS_WIFI;
            }

            else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                return IS_MOBILE;
            }

            else {
                // Not sure what's happening
                return IS_DISCONNECTED;
            }
        }
        else {
            // not connected to the internet
            return IS_DISCONNECTED;
        }
    };

}
