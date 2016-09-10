package internetfreedom.arcanine;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

import java.net.InetAddress;

/**
 * Author: @karthikb351
 * Project: Cuckoo
 */
public class NetworkHelper {

    public static int IS_WIFI = 10;
    public static int IS_MOBILE = 11;
    public static int IS_DISCONNECTED = 12;

    public static String getIPFromDomain(String domain) {
        String ip = null;
        try {

            InetAddress addr = InetAddress.getByName(domain);
            ip = addr.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            ip = null;
        }
        return ip;
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
