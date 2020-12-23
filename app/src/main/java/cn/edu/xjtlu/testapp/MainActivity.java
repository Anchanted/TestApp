package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import cn.edu.xjtlu.testapp.api.Api;
import cn.edu.xjtlu.testapp.api.Result;
import cn.edu.xjtlu.testapp.bean.Floor;
import cn.edu.xjtlu.testapp.bean.PlainPlace;
import cn.edu.xjtlu.testapp.listener.HttpObserver;
import cn.edu.xjtlu.testapp.util.AESUtil;
import cn.edu.xjtlu.testapp.util.Constant;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import retrofit2.HttpException;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private CanvasView cv;

    private Handler mHandler;
    private Thread mThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cv = findViewById(R.id.canvasView);

        LoadingUtil.showLoading(this);
        mHandler = new Handler();
        mThread = new Thread() {
            @Override
            public void run() {
                LoadingUtil.hideLoading();
            }
        };
        mHandler.postDelayed(mThread, 3000);

        Integer buildingId = 12;
        Integer floorId = 51;
//        Integer buildingId = null;
//        Integer floorId = null;
        Api.getInstance().getFloorInfo(buildingId, floorId).subscribe(new HttpObserver<Result<String>>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String string = AESUtil.decrypt(result.getData());
                    JsonNode rootNode = mapper.readTree(string);
                    cv.setPlaceList(Arrays.asList(mapper.readValue(rootNode.get("placeList").toString(), PlainPlace[].class)));

                    if (floorId == null) {
                        loadImage(null);
                    } else {
                        Floor floor = mapper.readValue(rootNode.get("selectedFloor").toString(), Floor.class);
                        loadImage(floor.getImgUrl());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onFailed(Result<String> data, @io.reactivex.annotations.NonNull Throwable e) {

                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mThread);
    }

    public void loadImage(String url) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bitmap resource;
                    if (url == null) {
                        resource = BitmapFactory.decodeResource(getResources(), R.drawable.map);
                    } else {
                        resource = Glide.with(getBaseContext())
                                .asBitmap()
                                .load(Constant.ENDPOINT + url.substring(1))
                                .submit()
                                .get();
                    }

                    cv.setMapImage(resource);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}