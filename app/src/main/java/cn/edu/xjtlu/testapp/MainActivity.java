package cn.edu.xjtlu.testapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PointF;
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
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.w3c.dom.Text;

import java.util.ArrayList;
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
import cn.edu.xjtlu.testapp.util.AESUtil;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import cn.edu.xjtlu.testapp.activity.BaseCommonActivity;
import cn.edu.xjtlu.testapp.util.LocationUtil;
import cn.edu.xjtlu.testapp.util.LogUtil;
import cn.edu.xjtlu.testapp.util.ResourceUtil;
import cn.edu.xjtlu.testapp.util.SensorUtil;
import cn.edu.xjtlu.testapp.util.ToastUtil;
import cn.edu.xjtlu.testapp.widget.FloorAlertDialog;
import cn.edu.xjtlu.testapp.widget.loading.LoadingStateManager;
import cn.edu.xjtlu.testapp.widget.loading.LoadingStateManagerInterface;
import cn.edu.xjtlu.testapp.widget.popupwindow.FloorPopupWindow;
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
    @BindView(R.id.iv_compass)
    public ImageView ivCompass;
    @BindView(R.id.button_compass)
    public ToggleButton compassButton;
    @BindView(R.id.button_location)
    public ToggleButton locationButton;
    @BindView(R.id.button_occupation)
    public ToggleButton occupationButton;

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
    private Thread mThread;
    private LocationUtil locationUtil;
    private SensorUtil sensorUtil;
    private FloorAlertDialog floorAlertDialog;
    private Disposable placeRequestDisposable;

    private Integer floorId;
    private Integer buildingId;
    @NonNull
    private Floor currentFloor = new Floor();
    private int floorDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        floorId = (Integer) getIntent().getSerializableExtra("floorId");
        buildingId = (Integer) getIntent().getSerializableExtra("buildingId");
    }

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
                        loadImage();
                    }
                })
                .setErrorView(null, null)
                .setOnErrorRetryClickListener(null, new LoadingStateManagerInterface.OnClickListener() {
                    @Override
                    public void onClick() {
                        loadImage();
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
    }

    @Override
    protected void initData() {
        super.initData();

        requestMapData();
    }

    @Override
    protected void initListeners() {
        super.initListeners();

        locationUtil = new LocationUtil(getMainActivity(), new LocationUtil.MyLocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                tvLatitude.setText(String.valueOf(location.getLatitude()));
                tvLongitude.setText(String.valueOf(location.getLongitude()));
//                LogUtil.d(TAG, location.getLatitude() + " " + location.getLongitude());
//                ToastUtil.shortToastSuccess(location.getLatitude() + " " + location.getLongitude());
//                cv.setLocation(LocationUtil.geoToImage(new Point(location.getLatitude(), location.getLongitude())));
                cv.setLocation(new PointF(600, 500));
            }
        });

        sensorUtil = new SensorUtil(getMainActivity(), direction -> {
            tvOrientation.setText(String.valueOf(direction));
            if (floorId != null && buildingId != null) {
                int floorDirection = currentFloor.getDirection() == null ? 0 : currentFloor.getDirection();
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
                Api.getInstance().getPlaceInfo(place.getId()).subscribe(new HttpObserver<Result<String>>() {
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
                                ViewGroup titleLayout = (ViewGroup) LayoutInflater.from(getMainActivity()).inflate(R.layout.floor_dialog_title, null);
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
            public void onRotateUpdate(int rotate) {
                floorDirection = (currentFloor.getDirection() == null ? 0 : currentFloor.getDirection()) + rotate;
                ivCompass.setRotation(floorDirection);
                compassButton.setRotation(floorDirection);
            }
        });
    }

    @Override
    protected void onPause() {
        LogUtil.d(TAG, "onPause");
        super.onPause();
        cv.pause();
        if (locationButton.isChecked()) {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        cv.resume();
        if (locationButton.isChecked()) {
            locationUtil.requestLocationUpdates();
            sensorUtil.registerListener();
        }
    }

    @Override
    protected void onDestroy() {
        LogUtil.d(TAG, "onDestroy");
        mHandler.removeCallbacksAndMessages(null);
//        mHandler.removeCallbacks(mThread);
        super.onDestroy();
        if (floorPopupWindow != null) {
            floorPopupWindow.dismiss();
        }
        if (locationButton.isChecked()) {
            locationUtil.removeUpdates();
            sensorUtil.unregisterListener();
        }
        cv.destroy();
        LoadingUtil.hideLoading();
//        if (floorAlertDialog != null) {
//            floorAlertDialog.dismiss();
//        }
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
        if (floorPopupWindow.isShowing()) {
            floorPopupWindow.dismiss();
        } else {
            floorPopupWindow.showAsDropDown(floorButton, 0, 0);
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

    private void setPageColor(int color) {
        cv.setBackgroundColor(color);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.setStatusBarColor(color);
            window.setNavigationBarColor(color);
        }
    }

    public void loadImage() {
        canvasLoadingManager.dismiss();
        canvasLoadingManager.showLoading();
        mHandler.post(() -> {
            try {
                RequestBuilder<Bitmap> requestBuilder = Glide.with(getMainActivity()).asBitmap();
                if (floorId == null) {
                    requestBuilder = requestBuilder.load(R.drawable.map);
                } else {
                    String url = currentFloor.getImgUrl();
                    String imgUrl = url.startsWith("http") ? url : ResourceUtil.resourceUri(url);
                    requestBuilder = requestBuilder.load(imgUrl);
                }
                requestBuilder.listener(new RequestListener<Bitmap>() {
                            @Override
                            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                                LogUtil.d(TAG, "onLoadFailed");
                                canvasLoadingManager.showNetworkError();
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                                LogUtil.d(TAG, "onResourceReady1");
                                return false;
                            }
                        })
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                LogUtil.d(TAG, "onResourceReady2");
                                cv.setMapImage(resource);
                                setPageColor(resource.getPixel(2, 2));
                                canvasLoadingManager.dismiss();
//                                Palette.from(resource)
//                                        .generate(new Palette.PaletteAsyncListener() {
//                                            @Override
//                                            public void onGenerated(@Nullable Palette palette) {
//                                                Palette.Swatch swatch = palette.getMutedSwatch();
//
//                                                if (swatch != null) {
//                                                    int rgb = swatch.getRgb();
//                                                    setPageColor(rgb);
//                                                }
//                                            }
//                                        });
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                                LogUtil.d(TAG, "onLoadCleared");
                            }
                        });
//                                .submit()
//                                .get();

            } catch (Exception e) {
                e.printStackTrace();
                canvasLoadingManager.showError();
            }
        });
    }

    public void requestMapData() {
        activityLoadingManager.dismiss();
        activityLoadingManager.showLoading();

        Api.getInstance().getFloorInfo(this.floorId, this.buildingId).subscribe(new HttpObserver<Result<String>>() {
            @Override
            public void onSucceed(@io.reactivex.annotations.NonNull Result<String> result) {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    String string = AESUtil.decrypt(result.getData());
                    JsonNode rootNode = mapper.readTree(string);
                    cv.setPlaceList(Arrays.asList(mapper.readValue(rootNode.get("placeList").toString(), PlainPlace[].class)));

                    if (floorId == null) {
                        loadImage();
                    } else {
                        currentFloor = mapper.readValue(rootNode.get("selectedFloor").toString(), Floor.class);
                        floorButton.setText(currentFloor.getName());
                        loadImage();

                        Place building = mapper.readValue(rootNode.get("building").toString(), Place.class);
                        tvCode.setText(building.getCode());

                        occupationButton.setVisibility((currentFloor.getHasOccupation() == null || !currentFloor.getHasOccupation()) ? View.GONE : View.VISIBLE);

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
                        floorPopupWindow = new FloorPopupWindow(getMainActivity(), R.layout.floor_dropdown, ddWidth, (int) Math.ceil(ddHeight * (floorList.size() > 6 ? 6.5 : floorList.size())), adapter, (parent, view, position1, id) -> {
                            LogUtil.d(TAG, "onItemSelected: " + floorList.get(position1));
                            adapter.setSelectedPosition(position1);
                            floorPopupWindow.dismiss();
                            Floor floor = floorList.get(position1);
                            if (floor == null) return;
                            if (floor.getId().equals(floorId)) return;
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.putExtra("floorId", floorList.get(position1).getId());
                            intent.putExtra("buildingId", buildingId);

                            startActivityAndFinish(intent);
//                                getWindow().setExitTransition(TransitionInflater.from(getMainActivity()).inflateTransition(R.transition.slide_up));
//                                getWindow().setEnterTransition(TransitionInflater.from(getMainActivity()).inflateTransition(R.transition.slide_up));
//                                startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(getMainActivity()).toBundle());
//                                finish();
                        });
                        floorButtonLayout.setVisibility(View.VISIBLE);
                    }
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
}