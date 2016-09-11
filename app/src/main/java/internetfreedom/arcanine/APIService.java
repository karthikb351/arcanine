package internetfreedom.arcanine;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.RealmObject;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public class APIService {

    public static APIService apiService;
    public static ArcanineAPI api;

    public static APIService getApiService() {
        if (apiService == null){
            apiService = new APIService();
            apiService.api = apiService.createService();
        }
        return apiService;
    }

    private static ArcanineAPI createService() {
        Gson gson = new GsonBuilder().setExclusionStrategies(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return f.getDeclaringClass() == RealmObject.class;
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        }).create();

        Retrofit.Builder builder = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl("https://secure-river-57103.herokuapp.com/");
        return builder.build().create(ArcanineAPI.class);
    }

}
