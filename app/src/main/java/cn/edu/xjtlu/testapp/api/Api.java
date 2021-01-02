package cn.edu.xjtlu.testapp.api;

import java.io.IOException;
import java.util.Locale;

import cn.edu.xjtlu.testapp.domain.response.Result;
import cn.edu.xjtlu.testapp.util.Constant;
import cn.edu.xjtlu.testapp.util.LogUtil;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class Api {
    private static final String TAG = Api.class.getSimpleName();
    private static Api instance;

    private static Service service;

    public Api() {
        Locale locale = Locale.getDefault();

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        clientBuilder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Interceptor.Chain chain) throws IOException {
                Request original = chain.request();

                Request request = original.newBuilder()
                        .header("Content-Language", String.format("%s, en", locale.getLanguage()))
                        .method(original.method(), original.body())
                        .build();

                return chain.proceed(request);
            }
        });
        if (LogUtil.isDebug) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.level(HttpLoggingInterceptor.Level.BASIC);
            clientBuilder.addInterceptor(loggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .client(clientBuilder.build())
                .baseUrl(Constant.ENDPOINT)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        service = retrofit.create(Service.class);
    }

    public static Api getInstance() {
        if (instance == null) {
            instance = new Api();
        }
        return instance;
    }

    public Observable<Result<String>> getFloorInfo(Integer floorId, Integer buildingId) {
        if (floorId == null && buildingId == null) {
            return service.getCampusInfo()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        } else {
            return service.getFloorInfo(floorId, buildingId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    public Observable<Result<String>> getPlaceInfo(Integer id) {
        return service.getPlaceInfo(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
