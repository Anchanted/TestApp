package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.edu.xjtlu.testapp.adapter.FloorListAdapter;
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
import cn.edu.xjtlu.testapp.util.LocationUtil;
import cn.edu.xjtlu.testapp.util.LogUtil;
import cn.edu.xjtlu.testapp.util.ResourceUtil;
import cn.edu.xjtlu.testapp.util.SensorUtil;
import cn.edu.xjtlu.testapp.widget.popupwindow.FloorPopupWindow;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseCommonActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.canvasView)
    public CanvasView cv;
    @BindView(R.id.campusBtn)
    public Button campusBtn;
    @BindView(R.id.locationBtn)
    public ToggleButton locationBtn;
    @BindView(R.id.codeTextView)
    public TextView codeTv;
    @BindView(R.id.floorBtn)
    public Button floorButton;
    private FloorPopupWindow floorPopupWindow;

    @BindView(R.id.button_group_left_top)
    public LinearLayout buttonGroupLT;
    @BindView(R.id.button_group_right_bottom)
    public LinearLayout buttonGroupRB;

    @BindView(R.id.tv_latitude)
    public TextView tvLatitude;
    @BindView(R.id.tv_longitude)
    public TextView tvLongitude;
    @BindView(R.id.tv_orientation)
    public TextView tvOrientation;

    private Handler mHandler;
    private Thread mThread;
    private LocationUtil locationUtil;
    private SensorUtil sensorUtil;

    private Integer floorId;
    private Integer buildingId;

    @Override
    protected void initViews() {
        super.initViews();
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface iconfont = Typeface.createFromAsset(getAssets(), "font/iconfont.ttf");
        campusBtn.setTypeface(iconfont);
        locationBtn.setTypeface(iconfont);
        if (floorId != null && buildingId != null) {
            buttonGroupLT.setVisibility(View.VISIBLE);
            buttonGroupRB.setVisibility(View.INVISIBLE);
        } else {
            buttonGroupLT.setVisibility(View.INVISIBLE);
            buttonGroupRB.setVisibility(View.VISIBLE);
        }

        LoadingUtil.showLoading(this);
        mHandler = new Handler();
        mThread = new Thread() {
            @Override
            public void run() {
                LoadingUtil.hideLoading();
            }
        };
        mHandler.postDelayed(mThread, 3000);
    }

    @Override
    protected void initData() {
        super.initData();

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
                        floorButton.setText(floor.getName());
                        loadImage(floor.getImgUrl());

                        Place building = mapper.readValue(rootNode.get("building").toString(), Place.class);
                        codeTv.setText(building.getCode());

                        List<Floor> floorList = Arrays.asList(mapper.readValue(rootNode.get("floorList").toString(), Floor[].class));
                        int position = 0;
                        for (int i = 0; i < floorList.size(); i++) {
                            if (floorId.equals(floorList.get(i).getId())) {
                                position = i;
                            }
                        }
                        FloorListAdapter adapter = new FloorListAdapter(getMainActivity(), floorList);
                        adapter.setSelectedPosition(position);
                        int ddWidth = getResources().getDimensionPixelSize(R.dimen.button_size);
                        int ddHeight = getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_item_height);
                        int size = floorList == null ? 0 : floorList.size();
                        floorPopupWindow = new FloorPopupWindow(getMainActivity(), R.layout.floor_dropdown, ddWidth, (int) Math.ceil(ddHeight * (size > 6 ? 6.5 : size)), adapter, new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                LogUtil.d(TAG, "onItemSelected: " + floorList.get(position));
                                adapter.setSelectedPosition(position);
                                floorPopupWindow.dismiss();
                                Floor floor = floorList.get(position);
                                if (floor == null) return;
                                if (floor.getId().equals(floorId)) return;
                                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                intent.putExtra("floorId", floorList.get(position).getId());
                                intent.putExtra("buildingId", buildingId);

                                startActivityAndFinish(intent);
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
    protected void initListeners() {
        super.initListeners();

        locationUtil = LocationUtil.getInstance(getMainActivity(), location -> {
            if (location != null) {
                tvLatitude.setText(String.valueOf(location.getLatitude()));
                tvLongitude.setText(String.valueOf(location.getLongitude()));
//                LogUtil.d(TAG, location.getLatitude() + " " + location.getLongitude());
//                ToastUtil.shortToastSuccess(location.getLatitude() + " " + location.getLongitude());
//                cv.setLocation(LocationUtil.geoToImage(new Point(location.getLatitude(), location.getLongitude())));
                cv.setLocation(new PointF(600, 500));
            }
        });

        sensorUtil = SensorUtil.getInstance(getMainActivity(), direction -> {
            tvOrientation.setText(String.valueOf(direction));
            cv.setDirection(-45);
//                LogUtil.d(TAG, "" + direction);
        });

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
                                        .setItems(floorArr, (dialog1, index) -> {
                                            Toast.makeText(getApplicationContext(), floorArr[index], Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                            intent.putExtra("floorId", floorList.get(index).getId());
                                            intent.putExtra("buildingId", place.getId());

                                            startActivityAndFinish(intent);
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Integer floorId = 51;
//        Integer buildingId = 12;
//        Integer floorId = null;
//        Integer buildingId = null;
        floorId = (Integer) getIntent().getSerializableExtra("floorId");
        buildingId = (Integer) getIntent().getSerializableExtra("buildingId");
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause");
        super.onPause();
        cv.pause();
        if (locationBtn.isChecked()) {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cv.resume();
        if (locationBtn.isChecked()) {
            locationUtil.requestLocationUpdates();
            sensorUtil.registerListener();
        }
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mThread);
        super.onDestroy();
        if (floorPopupWindow != null) {
            floorPopupWindow.dismiss();
        }
        if (locationBtn.isChecked()) {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();
        }
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

    @OnClick(R.id.campusBtn)
    public void onCampusBtnClick(View v) {
        startActivityAndFinish(MainActivity.class);
    }

    @OnClick(R.id.floorBtn)
    public void onFloorBtnClick(View v) {
        floorPopupWindow.showAsDropDown(floorButton, 0, 0);
    }

    @OnCheckedChanged(R.id.locationBtn)
    public void onLocationBtnClick(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            checkLocationPermission();
        } else {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();

            cv.setLocationActivated(false);
        }
    }

    private void checkLocationPermission() {
        MainActivityPermissionsDispatcher.onPermissionGrantedWithPermissionCheck(this);
    }

    @NeedsPermission({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    void onPermissionGranted() {
        if (!locationUtil.canGetLocation()) {
            new AlertDialog.Builder(this)
                    .setMessage("Location Service is no enabled. Do you want to go to the Setting menu?")
                    .setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .show();
        } else {
//            Location location = locationUtil.getLocation();
//            LogUtil.d(TAG, "" + location);
//            LogUtil.d(TAG, location.getLatitude() + " " + location.getLongitude());
            locationUtil.requestLocationUpdates();
            sensorUtil.registerListener();

            cv.setLocationActivated(true);
        }
    }

    @OnShowRationale({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    void showRequestPermission(PermissionRequest request) {
        new AlertDialog.Builder(this)
                .setMessage("我们需要权限，请同意，否则无法使用")
                .setPositiveButton("接受", (dialog, which) -> request.proceed())
                .setNegativeButton("拒绝", (dialog, which) -> request.cancel())
                .show();
    }

    @OnPermissionDenied({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    void showDenied() {

    }

    @OnNeverAskAgain({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    void showNeverAsk() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }
}