package cn.edu.xjtlu.testapp.api;

import cn.edu.xjtlu.testapp.domain.response.Result;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("floor/campus")
    Observable<Result<String>> getCampusInfo();

    @GET("floor/")
    Observable<Result<String>> getFloorInfo(@Query("buildingId") Integer buildingId, @Query("floorId") Integer floorId);

    @GET("place/")
    Observable<Result<String>> getPlaceInfo(@Query("id") Integer id, @Query("location") String location);
}
