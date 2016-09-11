package internetfreedom.arcanine.model;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public class NetworkMetaData extends RealmObject {

    @Ignore
    public static int WIFI = 1;

    @Ignore
    public static int MOBILE = 2;

    String lat;
    String lng;
    int type;
    String MCCMNC;
    String wifiProviderID;

    public String getMCCMNC() {
        return MCCMNC;
    }

    public void setMCCMNC(String MCCMNC) {
        this.MCCMNC = MCCMNC;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getWifiProviderID() {
        return wifiProviderID;
    }

    public void setWifiProviderID(String wifiProviderID) {
        this.wifiProviderID = wifiProviderID;
    }
}
