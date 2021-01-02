package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import cn.edu.xjtlu.testapp.api.Api;
import cn.edu.xjtlu.testapp.domain.response.Result;
import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.domain.Place;
import cn.edu.xjtlu.testapp.domain.PlainPlace;
import cn.edu.xjtlu.testapp.listener.HttpObserver;
import cn.edu.xjtlu.testapp.listener.OnPlaceSelectedListener;
import cn.edu.xjtlu.testapp.util.AESUtil;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import cn.edu.xjtlu.testapp.activity.BaseCommonActivity;
import cn.edu.xjtlu.testapp.util.LogUtil;
import cn.edu.xjtlu.testapp.util.ResourceUtil;

public class MainActivity extends BaseCommonActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

//    @BindView(R.id.canvasView)
    public CanvasView cv;
//    @BindView(R.id.campusBtn)
    public Button campusBtn;
//    @BindView(R.id.codeTextView)
    public TextView codeTv;
//    @BindView(R.id.floorSpinner)
    public Spinner floorSpinner;

    private Handler mHandler;
    private Thread mThread;

    private Integer floorId;
    private Integer buildingId;

    @RequiresApi(api = Build.VERSION_CODES.P)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        Integer floorId = 51;
//        Integer buildingId = 12;
//        Integer floorId = null;
//        Integer buildingId = null;
        this.floorId = (Integer) getIntent().getSerializableExtra("floorId");
        this.buildingId = (Integer) getIntent().getSerializableExtra("buildingId");

        if (this.floorId != null && this.buildingId != null) {
            this.campusBtn = findViewById(R.id.campusBtn);
            this.codeTv = findViewById(R.id.codeTextView);
            this.floorSpinner = findViewById(R.id.floorSpinner);

            Typeface iconfont = Typeface.createFromAsset(getAssets(), "font/iconfont.ttf");
            this.campusBtn.setTypeface(iconfont);
            this.campusBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityAndFinish(MainActivity.class);
                }
            });

            LinearLayout buttonGroupLT = (LinearLayout) findViewById(R.id.button_group_left_top);
            buttonGroupLT.setVisibility(View.VISIBLE);
        }

        this.cv = findViewById(R.id.canvasView);

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
                            LogUtil.d(TAG, "onSucceed: " + place);

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

                                                    startActivityAndFinish(intent);
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

        Api.getInstance().getFloorInfo(this.floorId, this.buildingId).subscribe(new HttpObserver<Result<String>>() {
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

                        Place building = mapper.readValue(rootNode.get("building").toString(), Place.class);
                        codeTv.setText(building.getCode());

                        List<Floor> floorList = Arrays.asList(mapper.readValue(rootNode.get("floorList").toString(), Floor[].class));
                        String[] floorNameArr = new String[floorList.size()];
                        int position = 0;
                        for (int i = 0; i < floorList.size(); i++) {
                            floorNameArr[i] = floorList.get(i).getName();
                            if (floorId.equals(floorList.get(i).getId())) {
                                position = i;
                            }
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, floorNameArr);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        floorSpinner.setAdapter(adapter);
                        floorSpinner.setSelection(position);
                        floorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                LogUtil.d(TAG, "onItemSelected: " + floorNameArr[position]);
                                Floor floor = floorList.get(position);
                                if (floor.getId().equals(floorId)) return;
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.putExtra("floorId", floorList.get(position).getId());
                                intent.putExtra("buildingId", buildingId);

                                startActivityAndFinish(intent);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {

                            }
                        });
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
    protected void onPause() {
        super.onPause();
        this.cv.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.cv.resume();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mThread);
        super.onDestroy();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LogUtil.d(TAG, "onConfigurationChanged: " + newConfig.getLayoutDirection() + " " + newConfig.orientation);
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
            public void run() {
                try {
                    if (url == null) {
                        Bitmap resource = BitmapFactory.decodeResource(getResources(), R.drawable.map);
                        int color = resource.getPixel(2, 2);
                        cv.setMapImage(resource);
                        cv.setBackgroundColor(color);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            Window window = getWindow();
                            window.setStatusBarColor(color);
                            window.setNavigationBarColor(color);
                        }
                    } else {
                        String imgUrl = url.startsWith("http") ? url : ResourceUtil.resourceUri(url);
                        Glide.with(getMainActivity())
                                .asBitmap()
                                .load(imgUrl)
                                .into(new CustomTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                        int color = resource.getPixel(2, 2);
                                        cv.setMapImage(resource);
                                        cv.setBackgroundColor(color);
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                            Window window = getWindow();
                                            window.setStatusBarColor(color);
                                            window.setNavigationBarColor(color);
                                        }

//                                        Palette.from(resource)
//                                                .generate(new Palette.PaletteAsyncListener() {
//                                                    @Override
//                                                    public void onGenerated(@Nullable Palette palette) {
//                                                        Palette.Swatch swatch = palette.getMutedSwatch();
//
//                                                        if (swatch != null) {
//                                                            int rgb = swatch.getRgb();
//
//                                                            cv.setBackgroundColor(rgb);
//                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                                                                Window window = getWindow();
//                                                                window.setStatusBarColor(rgb);
//                                                                window.setNavigationBarColor(rgb);
//                                                            }
//                                                        }
//                                                    }
//                                                });
                                    }

                                    @Override
                                    public void onLoadCleared(@Nullable Drawable placeholder) {

                                    }
                                });
//                                .listener(new RequestListener<Bitmap>() {
//                                    @Override
//                                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
//                                        LogUtil.d(TAG, "onLoadFailed");
//                                        return false;
//                                    }
//
//                                    @Override
//                                    public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
//                                        LogUtil.d(TAG, "onResourceReady");
//                                        return false;
//                                    }
//                                })
//                                .submit()
//                                .get();
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}