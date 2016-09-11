package internetfreedom.arcanine;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public class AuthService {

    private SharedPreferences pref;

    public static AuthService authService;

    public static AuthService getAuthService(Context context){
        if(authService==null){
            authService = new AuthService(context);
        }
        return authService;
    }

    public AuthService(Context ctx) {
        pref = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public void addToPref(String key, String value) {
        pref.edit().putString(key, value).apply();
    }

    public String getFromPref(String key) {
        return pref.getString(key, null);
    }

    public void deleteFromPref(String key) {
        pref.edit().remove(key).apply();
    }

    public void saveUserToken(String access_token) {
        addToPref("access_token", access_token);
    }

    public void deleteUserToken() {
        deleteFromPref("access_token");
    }

    public boolean isLoggedIn() {
        if(getFromPref("access_token")!=null) {
            return true;
        }
        return false;
    }

    public String getAuthHeader() {
        if(isLoggedIn()) {
            return "Bearer "+getFromPref("access_token");
        }
        return "";
    }
}
