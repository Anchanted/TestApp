package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.text.Layout;
import android.transition.Slide;
import android.transition.TransitionInflater;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import cn.edu.xjtlu.testapp.adapter.FloorListAdapter;
import cn.edu.xjtlu.testapp.adapter.MenuListAdapter;
import cn.edu.xjtlu.testapp.api.Api;
import cn.edu.xjtlu.testapp.domain.PlaceFloor;
import cn.edu.xjtlu.testapp.domain.Point;
import cn.edu.xjtlu.testapp.domain.response.Result;
import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.domain.Place;
import cn.edu.xjtlu.testapp.domain.PlainPlace;
import cn.edu.xjtlu.testapp.listener.HttpObserver;
import cn.edu.xjtlu.testapp.util.AESUtil;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import cn.edu.xjtlu.testapp.activity.BaseCommonActivity;
import cn.edu.xjtlu.testapp.util.LocationUtil;
import cn.edu.xjtlu.testapp.util.LogUtil;
import cn.edu.xjtlu.testapp.util.NetworkUtil;
import cn.edu.xjtlu.testapp.util.ResourceUtil;
import cn.edu.xjtlu.testapp.util.SensorUtil;
import cn.edu.xjtlu.testapp.widget.loading.LoadingStateManager;
import cn.edu.xjtlu.testapp.widget.loading.LoadingStateManagerInterface;
import cn.edu.xjtlu.testapp.widget.popupwindow.FloorPopupWindow;
import cn.edu.xjtlu.testapp.widget.popupwindow.MenuPopupWindow;
import io.reactivex.disposables.Disposable;
import permissions.dispatcher.NeedsPermission;
import permissions.dispatcher.OnNeverAskAgain;
import permissions.dispatcher.OnPermissionDenied;
import permissions.dispatcher.OnShowRationale;
import permissions.dispatcher.PermissionRequest;
import permissions.dispatcher.RuntimePermissions;

@RuntimePermissions
public class MainActivity extends BaseCommonActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    private LoadingStateManager activityLoadingManager;
    private LoadingStateManager canvasLoadingManager;

    @BindView(R.id.cv)
    public CanvasView cv;
    @BindView(R.id.button_campus)
    public Button campusButton;
    @BindView(R.id.tv_code)
    public TextView tvCode;
    @BindView(R.id.button_floor)
    public Button floorButton;
    private FloorPopupWindow floorPopupWindow;
    @BindView(R.id.button_menu)
    public Button menuButton;
    private MenuPopupWindow menuPopupWindow;
    private ToggleButton hideButton;
    @BindView(R.id.iv_compass)
    public ImageView ivCompass;
    @BindView(R.id.button_compass)
    public ToggleButton compassButton;
    @BindView(R.id.button_location)
    public ToggleButton locationButton;
    @BindView(R.id.button_occupation)
    public ToggleButton occupationButton;

    @BindView(R.id.button_group)
    public View buttonGroup;
    @BindView(R.id.button_group_left_top)
    public LinearLayout buttonGroupLT;
    @BindView(R.id.button_group_right_bottom)
    public LinearLayout buttonGroupRB;
    @BindView(R.id.floor_button_layout)
    public LinearLayout floorButtonLayout;
    @BindView(R.id.compass_layout)
    public FrameLayout compassLayout;

    @BindView(R.id.tv_latitude)
    public TextView tvLatitude;
    @BindView(R.id.tv_longitude)
    public TextView tvLongitude;
    @BindView(R.id.tv_orientation)
    public TextView tvOrientation;

    private final Handler mHandler = new Handler();
    private LocationUtil locationUtil;
    private SensorUtil sensorUtil;
    private NetworkUtil networkUtil;
    private Disposable placeRequestDisposable;

    private Integer floorId;
    private Integer buildingId;
    @NonNull
    private Floor currentFloor = new Floor();
    @NonNull
    private Place currentBuilding = new Place();
    private final int cachedFloorListCapacity = 30;
    private final int cachedBuildingListCapacity = 20;
    private final LinkedList<Floor> cachedFloorList = new LinkedList<>();
    private final LinkedList<Place> cachedBuildingList = new LinkedList<>();
    private Integer currentBuildingId;
    private int floorPopupWindowMaxHeight;
    private int getFloorInfoId;
    private boolean indoorMode;
    private int floorDirection;

    private final Set<String> requestingFloorSet = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floorId = (Integer) getIntent().getSerializableExtra("floorId");
        buildingId = (Integer) getIntent().getSerializableExtra("buildingId");
    }

    @SuppressLint("InflateParams")
    @Override
    protected void initViews() {
        super.initViews();

        setPageColor(Color.WHITE);

        activityLoadingManager = new LoadingStateManager.Builder(getMainActivity(), findViewById(R.id.button_group))
                .setLoadingView(null, null)
                .setNetworkErrorView(null, null, null)
                .setOnNetworkErrorRetryClickListener(null, new LoadingStateManagerInterface.OnClickListener() {
                    @Override
                    public void onClick() {
                        requestMapData();
                    }
                })
                .setErrorView(null, null)
                .setOnErrorRetryClickListener(null, new LoadingStateManagerInterface.OnClickListener() {
                    @Override
                    public void onClick() {
                        requestMapData();
                    }
                })
                .build();

        canvasLoadingManager = new LoadingStateManager.Builder(getMainActivity(), cv)
                .setLoadingView(null)
                .setNetworkErrorView(null, null)
                .setOnNetworkErrorRetryClickListener(null, new LoadingStateManagerInterface.OnClickListener() {
                    @Override
                    public void onClick() {
                        loadMainImage();
                    }
                })
                .setErrorView(null, null)
                .setOnErrorRetryClickListener(null, new LoadingStateManagerInterface.OnClickListener() {
                    @Override
                    public void onClick() {
                        loadMainImage();
                    }
                })
                .build();

//        floorAlertDialog = new FloorAlertDialog(getMainActivity(), new FloorAlertDialog.OnItemClickListener() {
//            @Override
//            public void onClick(Place building, int index) {
//                ObjectMapper mapper = new ObjectMapper();
//                Map<String, Object> extraInfo = (Map<String, Object>) building.getExtraInfo();
//                List<Floor> floorList = null;
//                try {
//                    floorList = Arrays.asList(mapper.readValue(mapper.writeValueAsString(extraInfo.get("floorList")), Floor[].class));
//                } catch (JsonProcessingException e) {
//                    e.printStackTrace();
//                }
//
//                Toast.makeText(getApplicationContext(), floorList.get(index).getName(), Toast.LENGTH_SHORT).show();
//
//                Intent intent = new Intent(MainActivity.this, MainActivity.class);
//                intent.putExtra("floorId", floorList.get(index).getId());
//                intent.putExtra("buildingId", building.getId());
//
//                startActivityAndFinish(intent);
//            }
//        });

        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Typeface iconfont = Typeface.createFromAsset(getAssets(), "font/iconfont.ttf");
        campusButton.setTypeface(iconfont);
        locationButton.setTypeface(iconfont);
        occupationButton.setTypeface(iconfont);
        if (floorId != null && buildingId != null) {
            campusButton.setVisibility(View.VISIBLE);
            locationButton.setVisibility(View.GONE);
            compassLayout.setVisibility(View.VISIBLE);
        } else {
            campusButton.setVisibility(View.GONE);
            locationButton.setVisibility(View.VISIBLE);
            compassLayout.setVisibility(View.GONE);
        }
        occupationButton.setVisibility(View.GONE);

        List<View> buttonList = new ArrayList<>();
        hideButton = (ToggleButton) LayoutInflater.from(getMainActivity()).inflate(R.layout.hide_button,null, false);
        hideButton.setVisibility(View.GONE);
        hideButton.setTypeface(iconfont);
        buttonList.add(hideButton);
        menuPopupWindow = new MenuPopupWindow(LayoutInflater.from(getMainActivity()).inflate(R.layout.floor_dropdown, null, false), getResources().getDimensionPixelSize(R.dimen.button_size), ViewGroup.LayoutParams.WRAP_CONTENT, new MenuListAdapter(getMainActivity(), buttonList));
    }

    @Override
    protected void initData() {
        super.initData();

        floorPopupWindowMaxHeight = getResources().getDimensionPixelSize(R.dimen.spinner_dropdown_max_height);
        loadMainImage();
        requestMapData();
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        networkUtil = new NetworkUtil(getApplicationContext());

        hideButton.setOnClickListener((buttonView) -> {
            cv.setDisplayVirtualButton(hideButton.isChecked());
            menuButton.performClick();
        });

        locationUtil = new LocationUtil(getApplicationContext(), location -> {
            tvLatitude.setText(String.valueOf(location.getLatitude()));
            tvLongitude.setText(String.valueOf(location.getLongitude()));
                LogUtil.d(TAG, location.getLatitude() + " " + location.getLongitude());
//                ToastUtil.shortToastSuccess(location.getLatitude() + " " + location.getLongitude());
            PointF point = LocationUtil.geoToImage(new Point(location.getLatitude(), location.getLongitude()));
            cv.setLocation(point.x, point.y);
//            cv.setLocation(600, 500);
        });

        sensorUtil = new SensorUtil(getApplicationContext(), direction -> {
//            direction = 135;
            tvOrientation.setText(String.valueOf(direction));
            if (floorId != null && buildingId != null) {
                compassButton.setRotation(direction + floorDirection);
            } else {
                cv.setDirection(direction);
            }
//            LogUtil.d(TAG, "" + direction);
        });

        cv.setOnCanvasDataUpdateListener(new CanvasView.OnCanvasDataUpdateListener() {
            @Override
            public void onPlaceSelected(PlainPlace place) {
                if (!"building".equals(place.getPlaceType())) return;
                Api.getInstance().getPlaceInfo(place.getId(), null).subscribe(new HttpObserver<Result<String>>() {
                    @Override
                    public void onSubscribe(@io.reactivex.annotations.NonNull Disposable d) {
                        super.onSubscribe(d);
                        placeRequestDisposable = d;
                        LoadingUtil.showLoading(getMainActivity(), v -> {
                            if (placeRequestDisposable != null) {
                                placeRequestDisposable.dispose();
                            }
                            LoadingUtil.hideLoading();
                        });
                    }

                    @SuppressLint("InflateParams")
                    @Override
                    public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                        placeRequestDisposable = null;
                        LoadingUtil.hideLoading();

                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String string = AESUtil.decrypt(result.getData());
                            JsonNode rootNode = mapper.readTree(string);
                            Place place = mapper.readValue(rootNode.get("place").toString(), Place.class);
                            LogUtil.d(TAG, "onSucceed: " + place);

                            if ("building".equals(place.getPlaceType())) {
//                                floorAlertDialog.updateData(place);
//                                floorAlertDialog.show();
                                Map<String, Object> extraInfo = (Map<String, Object>) place.getExtraInfo();
                                List<Floor> floorList = Arrays.asList(mapper.readValue(mapper.writeValueAsString(extraInfo.get("floorList")), Floor[].class));
//                                String[] floorArr = floorList.stream().map(Floor::getName).toArray(String[]::new);
                                List<String> floorNameList = new ArrayList<>();
                                for (Floor floor : floorList) {
                                   floorNameList.add(floor.getName());
                                }
                                ViewGroup titleLayout = (ViewGroup) LayoutInflater.from(getMainActivity()).inflate(R.layout.floor_dialog_title, null, false);
                                TextView title = titleLayout.findViewById(R.id.floor_dialog_title_text);
                                title.setText(getString(R.string.choose_floor, place.getCode()));
                                ArrayAdapter<String> adapter = new ArrayAdapter<>(getMainActivity(), R.layout.floor_dialog_item, R.id.floor_dialog_item_text, floorNameList);
                                AlertDialog dialog = new AlertDialog.Builder(MainActivity.this)
                                        .setCustomTitle(titleLayout)
                                        .setAdapter(adapter, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int index) {
//                                            Toast.makeText(getApplicationContext(), floorArr[index], Toast.LENGTH_SHORT).show();

                                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                                            intent.putExtra("floorId", floorList.get(index).getId());
                                            intent.putExtra("buildingId", place.getId());

                                            startActivityAndFinish(intent);
                                            }
                                        })
                                        .setNegativeButton(getString(R.string.button_cancel), null)
                                        .create();
                                dialog.show();
                                Button button = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
                                button.setAllCaps(false);
                                button.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
                                button.setTextColor(getResources().getColor(R.color.bs_primary));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public boolean onFailed(Result<String> data, @io.reactivex.annotations.NonNull Throwable e) {
                        e.printStackTrace();
                        placeRequestDisposable = null;
                        LoadingUtil.hideLoading();
                        return false;
                    }
                });
            }

            @Override
            public void onPlaceSelected(PointF location) {
                Api.getInstance().getPlaceInfo(null, String.format("%.1f,%.1f", location.x, location.y)).subscribe(new HttpObserver<Result<String>>() {
                    @SuppressLint("InflateParams")
                    @Override
                    public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            String string = AESUtil.decrypt(result.getData());
                            JsonNode rootNode = mapper.readTree(string);
                            Place place = mapper.readValue(rootNode.get("place").toString(), Place.class);
                            LogUtil.d(TAG, "onSucceed: " + place);

                            cv.setMarkerName((String) place.getShortName(), location);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onBuildingIdUpdate(int id) {
                if (currentBuildingId == null || currentBuildingId != id) {
                    currentBuildingId = id;
                    getFloorInfo(id, null);
                }
            }

            @Override
            public void onIndoorModeUpdate(boolean flag) {
                indoorMode = flag;
                if (!flag) {
                    floorButtonLayout.setVisibility(View.INVISIBLE);
                }
                getFloorInfo(currentBuildingId, null);
            }

            @Override
            public void onRotateUpdate(int rotate) {
                floorDirection = (currentFloor.getDirection() == null ? 0 : currentFloor.getDirection()) + rotate;
                ivCompass.setRotation(floorDirection);
                compassButton.setRotation(floorDirection);
            }

            @Override
            public void onDisplayVirtualButtonUpdate(boolean display) {
                hideButton.setChecked(display);
                buttonGroup.setVisibility(display ? View.INVISIBLE : View.VISIBLE);
            }
        });
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause");
        super.onPause();
        cv.pause();
        LogUtil.d(TAG, "locationButton.isChecked() " + locationButton.isChecked());
        if (locationButton.isChecked()) {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();
        }
        networkUtil.unregister();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cv.resume();
        if (locationButton.isChecked()) {
            locationUtil.requestLocationUpdates();
            sensorUtil.registerListener();
        }
        networkUtil.register();
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
        cv.destroy();
        if (floorPopupWindow != null) {
            floorPopupWindow.dismiss();
        }
        if (menuPopupWindow != null) {
            menuPopupWindow.dismiss();
        }
        LoadingUtil.hideLoading();
//        if (floorAlertDialog != null) {
//            floorAlertDialog.dismiss();
//        }
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

    @OnClick(R.id.button_campus)
    public void onCampusBtnClick(View v) {
        startActivityAndFinish(MainActivity.class);
    }

    @OnClick(R.id.button_floor)
    public void onFloorBtnClick(View v) {
        if (floorPopupWindow == null) return;
        if (floorPopupWindow.isShowing()) {
            floorPopupWindow.dismiss();
        } else {
            floorPopupWindow.showAsDropDown(floorButton, 0, 0);
        }
    }

    @OnClick(R.id.button_menu)
    public void onMenuBtnClick(View v) {
        if (menuPopupWindow.isShowing()) {
            menuPopupWindow.dismiss();
        } else {
            menuPopupWindow.showAsDropDown(menuButton, 0, 0);
        }
    }

    @OnCheckedChanged(R.id.button_compass)
    public void onCompassLayoutClick(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            sensorUtil.registerListener();
        } else {
            sensorUtil.unregisterListener();
            cv.setDirection(0);
            compassButton.setRotation(floorDirection);
        }
    }

    @OnCheckedChanged(R.id.button_location)
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
            locationButton.setChecked(false);
            new AlertDialog.Builder(this)
                    .setMessage("Location Service is not enabled. Do you want to go to the Setting menu?")
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
        locationButton.setChecked(false);
    }

    @OnNeverAskAgain({
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION
    })
    void showNeverAsk() {
        locationButton.setChecked(false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        MainActivityPermissionsDispatcher.onRequestPermissionsResult(this, requestCode, grantResults);
    }

    private void setPageColor(int color) {
        cv.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    public void loadMainImage() {
        canvasLoadingManager.dismiss();
        canvasLoadingManager.showLoading();
        mHandler.post(() -> {
            try {
                Bitmap mapImage = BitmapFactory.decodeResource(getResources(), R.drawable.map);
                int width = mapImage.getWidth();
                int height = mapImage.getHeight();
                int length = Math.max(width, height);
                SparseIntArray colorArray = new SparseIntArray();
                for (int i = 0; i < length; i++) {
                    int color;
                    if (i < width) {
                        color = mapImage.getPixel(i, 0) & 0xffffff;
                        colorArray.put(color, colorArray.get(color) + 1);
                        color = mapImage.getPixel(i, height - 1) & 0xffffff;
                        colorArray.put(color, colorArray.get(color) + 1);
                    }
                    if (i < height) {
                        color = mapImage.getPixel(0, i) & 0xffffff;
                        colorArray.put(color, colorArray.get(color) + 1);
                        color = mapImage.getPixel(width - 1, i) & 0xffffff;
                        colorArray.put(color, colorArray.get(color) + 1);
                    }
                }
                int color = colorArray.keyAt(0);
                int maxValue = 0;
                for (int i = 0; i < colorArray.size(); i++) {
                    if (maxValue < colorArray.valueAt(i)) {
                        color = colorArray.keyAt(i);
                        maxValue = colorArray.valueAt(i);
                    }
                }
                setPageColor(color|0xff000000);
                cv.setMapImage(mapImage);
                canvasLoadingManager.dismiss();
                hideButton.setVisibility(View.VISIBLE);
            } catch (Exception e) {
                e.printStackTrace();
                canvasLoadingManager.showError();
            }
        });
    }

    public void requestMapData() {
        activityLoadingManager.dismiss();
        activityLoadingManager.showLoading();

        Api.getInstance().getFloorInfo(null, null).subscribe(new HttpObserver<Result<String>>() {
            @SuppressLint("InflateParams")
            @Override
            public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String string = AESUtil.decrypt(result.getData());
                    JsonNode rootNode = mapper.readTree(string);
                    cv.setPlaceList(Arrays.asList(mapper.readValue(rootNode.get("placeList").toString(), PlainPlace[].class)));

                    buttonGroupRB.setVisibility(View.VISIBLE);
                    activityLoadingManager.dismiss();
                } catch (Exception e) {
                    e.printStackTrace();
                    activityLoadingManager.showError();
                }
            }

            @Override
            public boolean onFailed(Result<String> data, @io.reactivex.annotations.NonNull Throwable e) {
                e.printStackTrace();
                activityLoadingManager.showNetworkError();
                return false;
            }
        });
    }

    public void getFloorInfo(Integer buildingId, Integer floorId) {
        if (!indoorMode) return;
        if (buildingId == null && floorId == null) return;

        int methodId = getFloorInfoId + 1;
        getFloorInfoId = methodId;

        final Place[] building = {null};
        final Floor[] floor = {null};

        if (buildingId != null) {
            for (Place place : cachedBuildingList) {
                if (buildingId.equals(place.getId())) {
                    building[0] = place;
                    break;
                }
            }
        }
        if (building[0] != null) {
            boolean some = false;
            if (floorId != null) {
                for (int i = 0; i < building[0].floorList.length; i++) {
                    if (floorId.equals(building[0].floorList[i].getId())) {
                        some = true;
                        break;
                    }
                }
            }
            if (!some && building[0].currentFloorIndex >= 0 && building[0].currentFloorIndex < building[0].floorList.length) {
                floorId = building[0].floorList[building[0].currentFloorIndex].getId();
            }
        }
        if (buildingId != null) {
            if (floorId == null) {
                boolean flag = false;
                for (Place b : this.cachedBuildingList) {
                    if (b.currentFloorIndex < 0 || b.currentFloorIndex >= b.floorList.length) continue;
                    PlaceFloor[] buildingList = b.floorList[b.currentFloorIndex].getBuildingList();
                    for (int i = 0; i < buildingList.length; i++) {
                        if (buildingId.equals(buildingList[i].getPlaceId())) {
                            floorId = buildingList[i].getFloorId();
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
            }
            if (floorId == null) {
                boolean flag = false;
                for (Floor f : this.cachedFloorList) {
                    PlaceFloor[] buildingList = f.getBuildingList();
                    for (int i = 0; i < buildingList.length; i++) {
                        if (buildingId.equals(buildingList[i].getPlaceId())) {
                            floorId = buildingList[i].getFloorId();
                            flag = true;
                            break;
                        }
                    }
                    if (flag) break;
                }
            }
        }
        if (floorId != null) {
            for (Floor f : cachedFloorList) {
                if (floorId.equals(f.getId())) {
                    floor[0] = f;
                    break;
                }
            }
        }

        if (building[0] != null && floor[0] != null) {
            setFloorInfo(building[0], floor[0], methodId);
        } else  {
            String key = String.format("%d,%d", buildingId, floorId);
            if (requestingFloorSet.contains(key)) return;
            requestingFloorSet.add(key);
            if (floor[0] != null) {
                setCurrentFloor(floor[0]);
                cv.arrangeFloorList(floor[0]);
            }
            Api.getInstance().getFloorInfo(buildingId, floorId).subscribe(new HttpObserver<Result<String>>() {
                @SuppressLint("InflateParams")
                @Override
                public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        String string = AESUtil.decrypt(result.getData());
                        JsonNode rootNode = mapper.readTree(string);
//                        PlainPlace[] placeArr = mapper.readValue(rootNode.get("placeList").toString(), PlainPlace[].class);

                        if (building[0] == null) {
                            Place place = mapper.readValue(rootNode.get("building").toString(), Place.class);
                            if (place != null) {
                                Floor[] floorList = mapper.readValue(rootNode.get("floorList").toString(), Floor[].class);
                                place.floorList = floorList == null ? new Floor[0] : floorList;
                            }
                            building[0] = place;
                        }

                        if (floor[0] == null) {
                            floor[0] = mapper.readValue(rootNode.get("floor").toString(), Floor.class);
                            setFloorGraphicData(floor[0]);
                        }

                        if (building[0] != null && floor[0] != null) {
                            boolean some1 = false;
                            for (int i = 0; i < building[0].floorList.length; i++) {
                                if (Objects.equals(building[0].floorList[i].getId(), floor[0].getId())) {
                                    some1 = true;
                                    break;
                                }
                            }
                            if (!some1) {
                                throw new Exception("Floor not in the building.");
                            }
                        }

                        setFloorInfo(building[0], floor[0], methodId);

    //                    occupationButton.setVisibility((currentFloor.getHasOccupation() == null || !currentFloor.getHasOccupation()) ? View.GONE : View.VISIBLE);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    requestingFloorSet.remove(key);
                }

                @Override
                public boolean onFailed(Result<String> data, @io.reactivex.annotations.NonNull Throwable e) {
                    e.printStackTrace();
                    requestingFloorSet.remove(key);
                    return false;
                }
            });
        }
    }

    private void setFloorInfo(Place building, Floor floor, int methodId) {
        if (building == null || floor == null) return;

        if (getFloorInfoId == methodId && building.getId().equals(currentBuildingId)) {
            if (!building.getId().equals(currentBuilding.getId())) {
                currentBuilding = building;
                tvCode.setText(building.getCode());

                Floor[] floorList = building.floorList;
                if (floorPopupWindow == null) {
                    FloorListAdapter adapter = new FloorListAdapter(getMainActivity(), building);

                    floorPopupWindow = new FloorPopupWindow(LayoutInflater.from(getMainActivity()).inflate(R.layout.floor_dropdown, null, false), getResources().getDimensionPixelSize(R.dimen.button_size), floorList.length > 6 ? floorPopupWindowMaxHeight : ViewGroup.LayoutParams.WRAP_CONTENT, adapter, (parent, view, pos, id) -> {
                        LogUtil.d(TAG, "onItemSelected: " + floorList[pos]);
                        floorPopupWindow.dismiss();
//                        floorButton.performClick();
//                        adapter.setSelectedPosition(pos);
                        Integer buildingId = adapter.getBuildingId();
                        Floor selectedFloor = adapter.getItem(pos);
                        if (buildingId != null && selectedFloor != null && selectedFloor.getId() != null) {
                            getFloorInfo(buildingId, selectedFloor.getId());
                        }
                    });
                } else {
                    floorPopupWindow.getAdapter().updateList(building);
                    floorPopupWindow.setHeight(floorList.length > 6 ? floorPopupWindowMaxHeight : ViewGroup.LayoutParams.WRAP_CONTENT);
                }
            }

            boolean flag = false;
            for (int i = 0; i < building.floorList.length; i++) {
                if (floor.getId().equals(building.floorList[i].getId())) {
                    flag = true;
                    break;
                }
            }
            if (flag && !floor.getId().equals(currentFloor.getId())) {
                setCurrentFloor(floor);
                floorButton.setText(floor.getName());
                floorPopupWindow.getAdapter().setSelectedPosition(floor);
            }
            if (floorButtonLayout.getVisibility() != View.VISIBLE && indoorMode) {
                floorButtonLayout.setVisibility(View.VISIBLE);
            }
            cv.arrangeFloorList(floor);
        }

        int ii;

        int buildingIndex = -1;
        Iterator<Place> buildingIterator = cachedBuildingList.iterator();
        ii = 0;
        while (buildingIterator.hasNext()) {
            Place b = buildingIterator.next();
            if (building.getId().equals(b.getId())) {
                buildingIndex = ii;
                if (buildingIndex > 0) {
                    buildingIterator.remove();
                }
                break;
            }
            ii++;
        }
        if (buildingIndex != 0) cachedBuildingList.addFirst(building);
        while (cachedBuildingList.size() > cachedBuildingListCapacity) {
            cachedBuildingList.removeLast();
        }

        int floorIndex = -1;
        Iterator<Floor> floorIterator = cachedFloorList.iterator();
        ii = 0;
        while (floorIterator.hasNext()) {
            Floor f = floorIterator.next();
            if (floor.getId().equals(f.getId())) {
                floorIndex = ii;
                if (floorIndex > 0) {
                    floorIterator.remove();
                }
                break;
            }
            ii++;
        }
        if (floorIndex != 0) cachedFloorList.addFirst(floor);
        while (cachedFloorList.size() > cachedFloorListCapacity) {
            Floor f = cachedFloorList.removeLast();
            if (!floor.getId().equals(f.getId())) {
                cv.deleteImage(String.format("map%d", f.getId()));
            }
        }

        for (Place b : cachedBuildingList) {
            int index = -1;
            for (int i = 0; i < b.floorList.length; i++) {
                if (floor.getId().equals(b.floorList[i].getId())) {
                    index = i;
                    break;
                }
            }
            b.currentFloorIndex = index;
        }
    }

    private void setFloorGraphicData(@NonNull Floor floor) {
        if (floor.getRatio() == null || floor.getRatio() <= 0) {
            floor.setRatio(1f);
        }
        if (floor.getRefCoords() == null) return;
        Float[][][] refCoords = floor.getRefCoords();
        refCoords[1][0][1] *= floor.getRatio();
        refCoords[1][1][1] *= floor.getRatio();
        double degree = getDegree(refCoords[0][0][0], refCoords[0][0][1], refCoords[0][1][0], refCoords[0][1][1], refCoords[1][0][0], refCoords[1][0][1], refCoords[1][1][0], refCoords[1][1][1]);
        degree += (degree < -Math.PI / 4) ? Math.PI : 0;
        floor.degree = (float) Math.toDegrees(degree);
        floor.scale = getDistance(refCoords[0][0][0], refCoords[0][0][1], refCoords[0][1][0], refCoords[0][1][1]) / getDistance(refCoords[1][0][0], refCoords[1][0][1], refCoords[1][1][0], refCoords[1][1][1]);
        PointF offset = getRotatedPoint(refCoords[1][0][0], refCoords[1][0][1], degree);
        floor.origin.x = floor.getRefCoords()[0][0][0] - floor.scale * offset.x;
        floor.origin.y = floor.getRefCoords()[0][0][1] - floor.scale * offset.y;
    }

    private void setCurrentFloor(@NonNull Floor floor) {
        if (floor.getImgUrl() == null) return;
        String key = String.format("map%d", floor.getId());
        if (!cv.hasImage(key)) {
            mHandler.post(() -> {
                try {
                    String imgUrl = floor.getImgUrl().startsWith("http") ? floor.getImgUrl() : ResourceUtil.resourceUri(floor.getImgUrl());
                    LogUtil.d("loadFloorImage", floor.getImgUrl());
                    RequestBuilder<Bitmap> requestBuilder = Glide.with(getMainActivity()).asBitmap().load(imgUrl);

                    requestBuilder.listener(new RequestListener<Bitmap>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                            LogUtil.d(TAG, "onLoadFailed");
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                            LogUtil.d(TAG, "onResourceReady1");
                            return false;
                        }
                    }).into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            LogUtil.d(TAG, "onResourceReady2");
                            if (floor.getRefCoords() != null) {
                                float[][] bounds = {
                                        {0, 0},
                                        {resource.getWidth(), 0},
                                        {resource.getWidth(), resource.getHeight()},
                                        {0, resource.getHeight()}
                                };
                                for (int i = 0; i < bounds.length; i++) {
                                    PointF point = getRotatedPoint(bounds[i][0], bounds[i][1] * floor.getRatio(), Math.toRadians(floor.degree));
                                    point.x *= floor.scale;
                                    point.y *= floor.scale;
                                    point.x += floor.origin.x;
                                    point.y += floor.origin.y;
                                    bounds[i][0] = point.x;
                                    bounds[i][1] = point.y;
                                }
                                float minX = bounds[0][0];
                                float maxX = bounds[0][0];
                                float minY = bounds[0][1];
                                float maxY = bounds[0][1];
                                for (int i = 0; i < bounds.length; i++) {
                                    if (minX > bounds[i][0]) {
                                        minX = bounds[i][0];
                                    }
                                    if (maxX < bounds[i][0]) {
                                        maxX = bounds[i][0];
                                    }
                                    if (minY > bounds[i][1]) {
                                        minY = bounds[i][1];
                                    }
                                    if (maxY < bounds[i][1]) {
                                        maxY = bounds[i][1];
                                    }
                                }
                                floor.envelope = new PointF[]{new PointF(minX, minY), new PointF(maxX, maxY)};
                            }

//                            cv.addImage(key, resource);

                            Bitmap newImage = resource.copy(Bitmap.Config.ARGB_8888, true);
                            Canvas canvas = new Canvas(newImage);
                            Path path = new Path();
                            path.setFillType(Path.FillType.EVEN_ODD);
                            boolean flag = false;
                            if (floor.getBuildingList() != null) {
                                for (int n = 0; n < floor.getBuildingList().length; n++) {
                                    PlaceFloor pf = floor.getBuildingList()[n];
                                    if (pf.getAreaCoords() == null) continue;
                                    flag = true;
                                    List<List<List<Point>>> areaCoords = pf.getAreaCoords();
                                    for (int i = 0; i < areaCoords.size(); i++) {
                                        List<List<Point>> polygon = areaCoords.get(i);
                                        for (int j = 0; j < polygon.size(); j++) {
                                            List<Point> pointList = polygon.get(j);
                                            for (int k = 0; k < pointList.size(); k++) {
                                                double x = pointList.get(k).getX();
                                                double y = pointList.get(k).getY();
                                                x -= floor.origin.x;
                                                y -= floor.origin.y;
                                                x /= floor.scale;
                                                y /= floor.scale;
                                                PointF point = getRotatedPoint((float) x, (float) y, -Math.toRadians(floor.degree));
                                                if (k == 0) path.moveTo(Math.round(point.x), Math.round(point.y / floor.getRatio()));
                                                else path.lineTo(Math.round(point.x), Math.round(point.y / floor.getRatio()));
                                            }
                                        }
                                    }
                                }
                            }
                            Paint paint = new Paint();
                            paint.setColor(Color.RED);
                            paint.setAntiAlias(true);
                            paint.setStyle(Paint.Style.FILL);
                            if (flag) {
                                paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT));
                                canvas.drawPath(path, paint);
                            }
                            canvas.drawBitmap(resource, 0, 0, paint);
                            cv.addImage(key, newImage);
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {
                            LogUtil.d(TAG, "onLoadCleared");
                        }
                    });
//                        .submit()
//                        .get();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
        currentFloor = floor;
    }

    public double getDegree(float x11, float y11, float x12, float y12, float x21, float y21, float x22, float y22) {
        double a1 = Math.atan((y11 - y12) / (x11 - x12));
        double a2 = Math.atan((y21 - y22) / (x21 - x22));
        return a1 - a2;
    }

    public float getDistance(float x1, float y1, float x2, float y2) {
        return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    public PointF getRotatedPoint(float x, float y, double degree) {
        return new PointF((float) (x * Math.cos(degree) - y * Math.sin(degree)), (float) (x * Math.sin(degree) + y * Math.cos(degree)));
    }
}