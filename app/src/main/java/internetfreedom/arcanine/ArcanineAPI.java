package internetfreedom.arcanine;

import java.util.List;

import internetfreedom.arcanine.model.JobEntity;
import internetfreedom.arcanine.model.RegistrationEntity;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Author: @karthikb351
 * Project: arcanine
 */
public interface ArcanineAPI {

    @GET("/jobs")
    Call<List<JobEntity>> queryJobs(@Query("network") String network, @Query("mccmnc") String mccmnc, @Query("type") String type, @Query("lat") String lat, @Query("lng") String lng);


    @POST("/register")
    Call<RegistrationEntity> register();
}
