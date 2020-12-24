package cn.edu.xjtlu.testapp.api;

import cn.edu.xjtlu.testapp.bean.PlainPlace;
import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Service {
    @GET("floor/campus")
    Observable<Result<String>> getCampusInfo();

    @GET("floor/{buildingId}/{floorId}")
    Observable<Result<String>> getFloorInfo(@Path("floorId") Integer floorId, @Path("buildingId") Integer buildingId);

    @GET("place/")
    Observable<Result<String>> getPlaceInfo(@Query("id") Integer id);
}
