package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.Toast;

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import cn.edu.xjtlu.testapp.api.Api;
import cn.edu.xjtlu.testapp.api.Result;
import cn.edu.xjtlu.testapp.bean.Floor;
import cn.edu.xjtlu.testapp.bean.Place;
import cn.edu.xjtlu.testapp.bean.PlainPlace;
import cn.edu.xjtlu.testapp.listener.HttpObserver;
import cn.edu.xjtlu.testapp.listener.OnPlaceSelectedListener;
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

    private Integer floorId;
    private Integer buildingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Integer buildingId = 12;
//        Integer floorId = 51;
//        Integer buildingId = null;
//        Integer floorId = null;
        Integer floorId = (Integer) getIntent().getSerializableExtra("floorId");
        Integer buildingId = (Integer) getIntent().getSerializableExtra("buildingId");

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

        cv.setOnPlaceSelectedListener(new OnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(PlainPlace place) {
                Api.getInstance().getPlaceInfo(place.getId()).subscribe(new HttpObserver<Result<String>>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String string = AESUtil.decrypt(result.getData());
                            JsonNode rootNode = mapper.readTree(string);
                            Place place = mapper.readValue(rootNode.get("place").toString(), Place.class);
                            Log.d(TAG, "onSucceed: " + place);

                            if ("building".equals(place.getPlaceType())) {
                                Map<String, Object> extraInfo = (Map<String, Object>) place.getExtraInfo();
                                List<Floor> floorList = Arrays.asList(mapper.readValue(mapper.writeValueAsString(extraInfo.get("floorList")), Floor[].class));
                                String[] floorArr = floorList.stream().map(Floor::getName).toArray(String[]::new);
                                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                                    dialog.setTitle(getString(R.string.choose_floor, place.getCode()))
                                            .setItems(floorArr, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int index) {
                                                    Toast.makeText(getApplicationContext(), floorArr[index], Toast.LENGTH_SHORT).show();
                                                    
                                                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                                    intent.putExtra("floorId", floorList.get(index).getId());
                                                    intent.putExtra("buildingId", place.getId());

                                                    startActivity(intent);
                                                    finish();
                                                }
                                            })
                                            .setNegativeButton(getResources().getText(R.string.button_cancel), null)
                                            .create();
                                dialog.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public boolean onFailed(Result<String> data, @io.reactivex.annotations.NonNull Throwable e) {
                        e.printStackTrace();
                        return false;
                    }
                });
            }
        });

        Api.getInstance().getFloorInfo(floorId, buildingId).subscribe(new HttpObserver<Result<String>>() {
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
                e.printStackTrace();
                return false;
            }
        });
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mThread);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: " + newConfig.getLayoutDirection() + " " + newConfig.orientation);
        // Checks the orientation of the screen
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadImage(String url) {
        new Thread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void run() {
                try {
                    Bitmap resource;
                    if (url == null) {
                        resource = BitmapFactory.decodeResource(getResources(), R.drawable.map);
                    } else {
                        resource = Glide.with(getBaseContext())
                                .asBitmap()
                                .load(Constant.ENDPOINT + url)
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