package cn.edu.xjtlu.testapp;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.text.DynamicLayout;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.xjtlu.testapp.domain.Floor;
import cn.edu.xjtlu.testapp.graphic.AnimationTransform;
import cn.edu.xjtlu.testapp.graphic.AnimationTransformEvaluator;
import cn.edu.xjtlu.testapp.graphic.BBox;
import cn.edu.xjtlu.testapp.graphic.BCircle;
import cn.edu.xjtlu.testapp.graphic.GraphicPlace;
import cn.edu.xjtlu.testapp.domain.PlainPlace;
import cn.edu.xjtlu.testapp.graphic.GridIndex;
import cn.edu.xjtlu.testapp.graphic.Marker;
import cn.edu.xjtlu.testapp.graphic.TextPosition;
import cn.edu.xjtlu.testapp.util.JsonAssetsReader;
import cn.edu.xjtlu.testapp.util.LogUtil;

public class CanvasView extends SurfaceView implements SurfaceHolder.Callback, Runnable, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = CanvasView.class.getSimpleName();

    private Context mContext;
    private Thread surfaceThread;
    private SurfaceHolder surfaceHolder;
    private boolean doDrawing;
    private final Rect boundingClientRect = new Rect();
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean doubleTapDown;
    private boolean doubleTapMove;
    private final PointF doubleTapDownMotionEventPoint = new PointF(0, 0);
    private boolean scaling;
    private float lastDoubleTapMotionEventDistance;
    private final TextPaint textPaint = new TextPaint();
    private final Paint drawPaint = new Paint();
    private GraphicPlace[] graphicPlaceArray = new GraphicPlace[0];
    private OnCanvasDataUpdateListener mListener;
    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private Runnable tapEvent;
    private final AnimationTransformEvaluator animationTransformEvaluator = new AnimationTransformEvaluator();

    private Map<String, Object> iconSpriteInfo;
    private Map<String, Object> markerSpriteInfo;
    private final Map<String, Bitmap> markerSpritePool = new HashMap<>();

    private float canvasWidth;
    private float canvasHeight;
    private int imgWidth;
    private int imgHeight;
    private final AnimationTransform transformAdaption = new AnimationTransform();
    private final AnimationTransform transform = new AnimationTransform();
    private final PointF focusedPoint = new PointF(0, 0);
    private int backgroundColor = Color.WHITE;
    private Marker lastMarker;
    private Marker currentMarker;
    private Marker fromDirectionMarker;
    private Marker toDirectionMarker;
    private PointF location;
    private float iconSize;
    private float halfIconSize;
    private float locationIconSize;
    private float halfLocationIconSize;
    private float markerSize;
    private float halfMarkerSize;
    private final PointF virtualButtonPosition = new PointF(100, 100);
    private int virtualButtonSize;
    private boolean virtualButtonSelected;
    private boolean indoorMode;
    @NonNull
    private ValueAnimator mapAnimator = new ValueAnimator();
    private ValueAnimator backgroundColorAnimator;
    @NonNull
    private ValueAnimator lastMarkerAnimator = new ValueAnimator();
    @NonNull
    private ValueAnimator currentMarkerAnimator = new ValueAnimator();
    private AnimationTransform lastMapAnimationTransform;
    @NonNull
    private final Matrix mapMatrix = new Matrix();
    @NonNull
    private final Matrix locationMatrix = new Matrix();
    private boolean labelComplete;
    private boolean imageComplete;
    private boolean layoutComplete;

    @NonNull
    private final Map<String, Bitmap> imageMap = new HashMap<>();
    private int clientWidth;
    private int clientHeight;
    private int rotate;
    private float mapRotationCenter;
    private PlainPlace[] placeArray;
    private PlainPlace[] buildingArray;
    private List<Floor> floorList = new ArrayList<>(20);
    private boolean locationActivated;
    private Integer deviceDirection;
    private boolean displayVirtualButton;

    public CanvasView(Context context) {
        super(context);
        this.init(context);
    }

    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.clientWidth = getWidth();
        this.clientHeight = getHeight();
        LogUtil.d(TAG, "onLayout: " + getWidth() + " " + getHeight());

        this.boundingClientRect.left = getLeft();
        this.boundingClientRect.top = getTop();
        this.boundingClientRect.right = getRight();
        this.boundingClientRect.bottom = getBottom();

        if (this.imageComplete) {
            try {
                this.resetLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.layoutComplete = true;
    }

//    @Override
//    protected void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            LogUtil.d(TAG, "onConfigurationChanged: ORIENTATION_LANDSCAPE");
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//            LogUtil.d(TAG, "onConfigurationChanged: ORIENTATION_PORTRAIT");
//        }
//    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        LogUtil.d(TAG, "surfaceCreated");
        this.doDrawing = true;
        this.surfaceThread = new Thread(this);
        this.surfaceThread.start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        LogUtil.d(TAG, "surfaceChanged");
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        LogUtil.d(TAG, "surfaceDestroyed");
        this.doDrawing = false;
        try {
            this.surfaceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        long frameTime = 1000 / 60;
        Canvas canvas;
        while (this.doDrawing) {
            if (!this.surfaceHolder.getSurface().isValid()) continue;
            if (!this.labelComplete) continue;
//            if ((canvas = this.surfaceHolder.lockCanvas()) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                canvas = this.surfaceHolder.lockHardwareCanvas();
            } else {
                canvas = this.surfaceHolder.lockCanvas();
            }
            if (canvas != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                long startTime = System.currentTimeMillis();
                this.drawMapInfo(canvas);
                long timeCost = System.currentTimeMillis() - startTime;
//                LogUtil.d(TAG, "" + timeCost);
                if (timeCost < frameTime) {
                    try {
                        Thread.sleep(frameTime - timeCost);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            this.surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
//        LogUtil.d(TAG, "onTouchEvent " + event.getAction());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                this.scaling = false;
                if (this.displayVirtualButton) {
                    this.virtualButtonSelected = false;
                    Object element = this.isPointInItem(event.getX(), event.getY());
                    if (element instanceof Integer && (int) element == 4) {
                        this.virtualButtonSelected = true;
                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() != 1 && this.virtualButtonSelected) {
                    this.virtualButtonSelected = false;
                }
                if (this.doubleTapDown && !this.mapAnimator.isRunning()) {
                    float distance = event.getY() - this.doubleTapDownMotionEventPoint.y;
//                    LogUtil.d(TAG, String.format("%f %f", Math.abs(event.getX() - this.doubleTapDownMotionEventPoint.x), Math.abs(distance)));
                    if (!this.doubleTapMove && (Math.abs(event.getX() - this.doubleTapDownMotionEventPoint.x) > 5 || Math.abs(distance) > 5)) {
                        this.doubleTapMove = true;
                    }
                    if (this.doubleTapMove) {
                        float scale = (this.lastDoubleTapMotionEventDistance - distance) / 300;
                        this.manipulateMap(0, 0, scale, scale);
    //                    LogUtil.d("TAG", String.format("%f %f %f %f", this.doubleTapDownMotionEventPoint.y, event.getY(), distance, dd));
                        this.refreshTextPosition();
                        this.refreshIconDisplay();
                    }
                    this.lastDoubleTapMotionEventDistance = distance;
                }
                break;
        }
        return (this.gestureDetector.onTouchEvent(event) | this.scaleGestureDetector.onTouchEvent(event)) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
//        LogUtil.d(TAG, "onDown");
        return true;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return true;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
//        LogUtil.d(TAG, "onScroll");
        if (this.scaling) return true;
        if (e2.getPointerCount() > 1) return true;
        if (this.virtualButtonSelected) {
            final PointF touchPoint = this.getTouchPoint(e2.getX(), e2.getY());
            final float offset = this.virtualButtonSize / 2f;
            float posX = touchPoint.x - offset;
            float posY = touchPoint.y - offset;
            if (touchPoint.x + offset > this.canvasWidth) {
                posX = this.canvasWidth - this.virtualButtonSize;
            } else if (posX < 0) {
                posX = 0;
            }
            if (touchPoint.y + offset > this.canvasHeight) {
                posY = this.canvasHeight - this.virtualButtonSize;
            } else if (posY < 0) {
                posY = 0;
            }
            this.virtualButtonPosition.x = posX;
            this.virtualButtonPosition.y = posY;
        } else {
            this.manipulateMap(-distanceX, -distanceY, 0, 0);
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {
        LogUtil.d(TAG, "onLongPress");
        if (this.doubleTapDown) return;
        this.mHandler.removeCallbacks(this.tapEvent);
        this.chooseItem(e.getX(), e.getY(), true);
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        if (this.displayVirtualButton && this.virtualButtonSelected) {
            setDisplayVirtualButton(false);
        } else {
            this.mHandler.postDelayed(this.tapEvent = () -> chooseItem(e.getX(), e.getY(), false), 500);
        }
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
//        LogUtil.d(TAG, "onDoubleTap");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
//        LogUtil.d(TAG, "onDoubleTapEvent" + e.getAction());
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
//                LogUtil.d(TAG, "onDoubleTapEvent ACTION_DOWN");
                this.doubleTapDownMotionEventPoint.x = e.getX();
                this.doubleTapDownMotionEventPoint.y = e.getY();
                this.doubleTapDown = true;
                this.doubleTapMove = false;
                this.lastDoubleTapMotionEventDistance = 0;

                PointF focusedPoint = this.getTouchPoint(e.getX(), e.getY());
                this.focusedPoint.x = focusedPoint.x;
                this.focusedPoint.y = focusedPoint.y;
                break;
//            case MotionEvent.ACTION_MOVE:
//                LogUtil.d(TAG, String.format("onDoubleTapEvent ACTION_MOVE %f %f", Math.abs(this.doubleTapDownMotionEventPoint.x - e.getX()), Math.abs(this.doubleTapDownMotionEventPoint.y - e.getY())));
//                if (!(!this.doubleTapMove && this.doubleTapDownMotionEventPoint.x == e.getX() && this.doubleTapDownMotionEventPoint.y == e.getY())) {
//                    this.doubleTapMove = true;
//                }
//                break;
            case MotionEvent.ACTION_UP:
                this.doubleTapDown = false;

                if (!this.doubleTapMove) {
                    if (!this.mapAnimator.isRunning()) {
                        this.mapAnimator.start();
                    }
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
//        LogUtil.d(TAG, "onScale");
        if (this.doubleTapDown) return true;
        if (this.mapAnimator.isRunning()) return true;
        if (!this.scaling) this.scaling = true;
        float zoom = detector.getScaleFactor() - 1f;
        PointF focusPoint = this.getTouchPoint(detector.getFocusX(), detector.getFocusY());
        this.focusedPoint.x = focusPoint.x;
        this.focusedPoint.y = focusPoint.y;
        this.manipulateMap(0, 0, zoom * 2, zoom * 2);
        this.refreshTextPosition();
        this.refreshIconDisplay();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void init(Context context) {
        this.mContext = context;

        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        // 画布透明处理
        setZOrderOnTop(false);
//        this.surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

        setFocusable(true);
        setKeepScreenOn(false);
        setFocusableInTouchMode(true);

        this.gestureDetector = new GestureDetector(this.mContext, this);
        this.gestureDetector.setOnDoubleTapListener(this);
        this.scaleGestureDetector = new ScaleGestureDetector(this.mContext, this);

        float dpi = getResources().getDisplayMetrics().density;
        LogUtil.d(TAG, "dpi: " + dpi);

        this.iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
        this.halfIconSize = this.iconSize / 2;
        this.locationIconSize = this.iconSize * 1.5f;
        this.halfLocationIconSize = this.iconSize * 0.75f;
        this.markerSize = this.iconSize * 2;
        this.halfMarkerSize = this.iconSize;

        this.imageMap.put("icon", BitmapFactory.decodeResource(getResources(), R.drawable.icon_sprite));
        this.imageMap.put("marker", BitmapFactory.decodeResource(getResources(), R.drawable.marker_sprite));
        Matrix matrix = new Matrix();
        Bitmap originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_probe);
        matrix.postScale(this.locationIconSize / originalBitmap.getWidth(), this.locationIconSize / originalBitmap.getHeight());
        this.imageMap.put("locationProbe", Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true));
        originalBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.location_marker);
        this.imageMap.put("locationMarker", Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true));

        Drawable vectorDrawable = getResources().getDrawable(R.drawable.ic_display_button);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        this.imageMap.put("displayButton", bitmap);

        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size));
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setStrokeWidth(4);
//        this.textPaint.setShadowLayer(1, 0, 0, 0xffffffff);
        this.textPaint.setFakeBoldText(true);

        this.drawPaint.setAntiAlias(true);
        this.drawPaint.setStrokeWidth(6);
        this.drawPaint.setStrokeJoin(Paint.Join.ROUND);
        this.drawPaint.setStrokeCap(Paint.Cap.ROUND);

        ValueAnimator mapAnimator = ValueAnimator.ofObject(this.animationTransformEvaluator, new AnimationTransform(0, 0, 0, 0), new AnimationTransform(0, 0, 0.5f, 0.5f));
        mapAnimator.setDuration(100);
        mapAnimator.setInterpolator(new LinearInterpolator());
        mapAnimator.addUpdateListener(animation -> {
            AnimationTransform currentAnimation = (AnimationTransform) animation.getAnimatedValue();
            if (currentAnimation != null) {
                if (this.lastMapAnimationTransform == null) {
                    manipulateMap(currentAnimation.translateX, currentAnimation.translateY, currentAnimation.scaleX, currentAnimation.scaleY);
                } else {
                    manipulateMap(currentAnimation.translateX - this.lastMapAnimationTransform.translateX, currentAnimation.translateY - this.lastMapAnimationTransform.translateY, currentAnimation.scaleX - this.lastMapAnimationTransform.scaleX, currentAnimation.scaleY - this.lastMapAnimationTransform.scaleY);
                }
                this.lastMapAnimationTransform = currentAnimation;
            }
        });
        mapAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                lastMapAnimationTransform = null;
            }
        });
        this.mapAnimator = mapAnimator;

        ValueAnimator currentMarkerAnimator = ValueAnimator.ofFloat(this.iconSize / this.markerSize, 1);
        currentMarkerAnimator.setDuration(400);
        currentMarkerAnimator.setInterpolator(new OvershootInterpolator(3f));
        currentMarkerAnimator.addUpdateListener(animation -> {
            if (this.currentMarker != null) {
                this.currentMarker.scaleFactor = (Float) animation.getAnimatedValue();
            }
        });
        this.currentMarkerAnimator = currentMarkerAnimator;

        ValueAnimator lastMarkerAnimator = ValueAnimator.ofFloat(1, 0.2f);
        lastMarkerAnimator.setDuration(200);
        lastMarkerAnimator.setInterpolator(new DecelerateInterpolator());
        lastMarkerAnimator.addUpdateListener(animation -> {
            if (this.lastMarker != null) {
                float value = (Float) animation.getAnimatedValue();
                this.lastMarker.scaleFactor = value;
                this.lastMarker.displayAlpha = Math.round(value * 255);
            }
        });
        this.lastMarkerAnimator = lastMarkerAnimator;

        Thread thread = new Thread(() -> {
            ObjectMapper mapper = new ObjectMapper();
            try {
                this.iconSpriteInfo = mapper.readValue(JsonAssetsReader.getJsonString("json/iconSpriteInfo.json", mContext), Map.class);
                this.markerSpriteInfo = mapper.readValue(JsonAssetsReader.getJsonString("json/markerSpriteInfo.json", mContext), Map.class);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        });
        thread.start();
    }

    public void pause() {
        this.doDrawing = false;
        try {
            this.surfaceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void resume() {
        this.doDrawing = true;
        this.surfaceThread = new Thread(this);
        this.surfaceThread.start();
    }

    public void destroy() {
        this.mHandler.removeCallbacksAndMessages(null);
        this.doDrawing = false;
        try {
            this.surfaceThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//        if (this.mapAnimator.isRunning()) this.mapAnimator.end();
//        if (this.backgroundColorAnimator.isRunning()) this.backgroundColorAnimator.end();
//        if (this.currentMarkerAnimator.isRunning()) this.currentMarkerAnimator.end();
//        if (this.lastMarkerAnimator.isRunning()) this.lastMarkerAnimator.end();
//        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
//            if (this.graphicPlaceArray[i].displayAnimator.isRunning()) {
//                this.graphicPlaceArray[i].displayAnimator.end();
//            }
//        }
    }

    public void setOnCanvasDataUpdateListener(OnCanvasDataUpdateListener listener) {
        this.mListener = listener;
    }

    private void drawMapInfo(Canvas canvas) {
        final PointF sharedPoint = new PointF();

        canvas.save();

        canvas.drawColor(this.backgroundColor);

        float scaleX = this.transform.scaleX * this.transformAdaption.scaleX;
        float scaleY = this.transform.scaleY * this.transformAdaption.scaleY;

        if (this.imageMap.get("map") != null) {
            this.mapMatrix.reset();

            if (this.rotate != 0) {
                this.mapMatrix.postRotate(this.rotate, this.mapRotationCenter, this.mapRotationCenter);
                this.mapMatrix.postScale(scaleY, scaleX);
            } else {
                this.mapMatrix.postScale(scaleX, scaleY);
            }

            float posX = this.transform.translateX + this.transformAdaption.translateX;
            float posY = this.transform.translateY + this.transformAdaption.translateY;
            this.mapMatrix.postTranslate(posX, posY);
            canvas.drawBitmap(this.imageMap.get("map"), this.mapMatrix, this.drawPaint);

//            this.drawImage(canvas, this.imageMap.get("map"), this.mapMatrix, 0, 0, 0, 0, false, null);
        }
        
        if (this.indoorMode && this.floorList.size() > 0) {
            List<Floor> floorList = this.floorList;
            for (int i = 0; i < floorList.size(); i++) {
                Floor floor = floorList.get(0);
                Bitmap image = this.imageMap.get(String.format("map%d", floor.getId()));
                if (image == null) continue;
                if (floor.envelope != null) {
                    getImageToCanvasPoint(sharedPoint, floor.envelope[0].x, floor.envelope[0].y);
                    float minX = sharedPoint.x;
                    float minY = sharedPoint.y;
                    getImageToCanvasPoint(sharedPoint, floor.envelope[1].x, floor.envelope[1].y);
                    float maxX = sharedPoint.x;
                    float maxY = sharedPoint.y;
                    if (!(minX <= this.canvasWidth && minY <= this.canvasHeight && maxX >= 0 && maxY >= 0)) continue;
                }
                floor.matrix.reset();

                this.getImageToCanvasPoint(sharedPoint, floor.origin.x, floor.origin.y);
                floor.matrix.postScale(scaleX * floor.scale, scaleY * floor.scale * floor.getRatio());
                floor.matrix.postRotate(this.rotate + floor.degree, this.mapRotationCenter, this.mapRotationCenter);
                floor.matrix.postTranslate(sharedPoint.x, sharedPoint.y);

                canvas.drawBitmap(image, floor.matrix, this.drawPaint);
            }
        }

        if (this.currentMarker != null) {
            this.drawArea(canvas, sharedPoint, this.currentMarker.areaCoords);
        }

        if (this.graphicPlaceArray != null && this.graphicPlaceArray.length > 0) {
            for (int i = this.graphicPlaceArray.length - 1; i >= 0; i--) {
                GraphicPlace place = this.graphicPlaceArray[i];
                // selected place
                if (this.currentMarker != null && place.id == this.currentMarker.id) continue;
                // direction markers
                if (this.fromDirectionMarker != null && place.id == this.fromDirectionMarker.id) continue;
                if (this.toDirectionMarker != null && place.id == this.toDirectionMarker.id) continue;
                // place not to display
                if (place.displayAlpha <= 0) continue;
                this.drawPaint.setAlpha(place.displayAlpha);

                this.getImageToCanvasPoint(sharedPoint, place.location.x, place.location.y);
                this.drawImage(canvas, place.iconImg, place.matrix, sharedPoint.x, sharedPoint.y, this.halfIconSize, this.halfIconSize, true, null);

                if (place.displayName && place.textPosition != null) {
                    canvas.save();
                    switch (place.textPosition) {
                        case BOTTOM:
                            canvas.translate(sharedPoint.x, sharedPoint.y + this.halfIconSize);
                            break;
                        case LEFT:
                            canvas.translate(sharedPoint.x + this.halfIconSize, sharedPoint.y - place.halfTextHeight);
                            break;
                        case RIGHT:
                            canvas.translate(sharedPoint.x - this.halfIconSize, sharedPoint.y - place.halfTextHeight);
                            break;
                        case TOP:
                            canvas.translate(sharedPoint.x, sharedPoint.y - this.halfIconSize - place.textHeight);
                            break;
                    }
                    this.textPaint.setTextAlign(place.textPosition.align);
                    this.textPaint.setColor(Color.WHITE);
                    this.textPaint.setStyle(Paint.Style.STROKE);
                    this.textPaint.setAlpha(place.displayAlpha);
                    place.staticLayout.draw(canvas);
                    this.textPaint.setColor(place.iconColor);
                    this.textPaint.setStyle(Paint.Style.FILL);
                    this.textPaint.setAlpha(place.displayAlpha);
                    place.staticLayout.draw(canvas);
                    canvas.restore();
                }
            }
            this.drawPaint.setAlpha(255);
        }

        if (this.lastMarkerAnimator.isRunning()) {
            this.getImageToCanvasPoint(sharedPoint, this.lastMarker.location.x, this.lastMarker.location.y);
            this.drawPaint.setAlpha(this.lastMarker.displayAlpha);
            this.drawImage(canvas, this.lastMarker.iconImg, this.lastMarker.matrix, sharedPoint.x, sharedPoint.y, this.halfMarkerSize * this.lastMarker.scaleFactor, this.markerSize * this.lastMarker.scaleFactor, true, this.lastMarker.scaleFactor);
            this.drawPaint.setAlpha(255);
        }
        if (this.currentMarker != null) {
            this.getImageToCanvasPoint(sharedPoint, this.currentMarker.location.x, this.currentMarker.location.y);
            this.drawImage(canvas, this.currentMarker.iconImg, this.currentMarker.matrix, sharedPoint.x, sharedPoint.y, this.halfMarkerSize * this.currentMarker.scaleFactor, this.markerSize * this.currentMarker.scaleFactor, true, this.currentMarker.scaleFactor);
            canvas.save();
            canvas.translate(sharedPoint.x, sharedPoint.y);
            this.textPaint.setTextAlign(Paint.Align.CENTER);
            this.textPaint.setColor(Color.WHITE);
            this.textPaint.setStyle(Paint.Style.STROKE);
            this.currentMarker.staticLayout.draw(canvas);
            this.textPaint.setColor(Color.BLACK);
            this.textPaint.setStyle(Paint.Style.FILL);
            this.currentMarker.staticLayout.draw(canvas);
            canvas.restore();
        }

        if (this.locationActivated && this.location != null) {
            this.getImageToCanvasPoint(sharedPoint, this.location.x, this.location.y);
            if (this.deviceDirection != null) {
//                this.locationMatrix.reset();
//                this.locationMatrix.postRotate(30, this.imageMap.get("locationProbe").getWidth() / 2f, this.imageMap.get("locationProbe").getHeight() / 2f);
//                float dx = sharedPoint.x - this.halfLocationIconSize;
//                float dy = sharedPoint.y - this.halfLocationIconSize;
//                this.locationMatrix.postTranslate(dx, dy);
//                canvas.drawBitmap(this.imageMap.get("locationProbe"), this.locationMatrix, this.drawPaint);
                this.drawImage(canvas, this.imageMap.get("locationProbe"), this.locationMatrix, sharedPoint.x, sharedPoint.y, this.halfLocationIconSize, this.halfLocationIconSize, true, null, this.deviceDirection);
            }
            this.drawImage(canvas, this.imageMap.get("locationMarker"), this.locationMatrix, sharedPoint.x, sharedPoint.y, this.halfLocationIconSize, this.halfLocationIconSize, true, null);
        }

        if (this.displayVirtualButton) {
            canvas.restore();
            canvas.drawBitmap(this.imageMap.get("displayButton"), this.virtualButtonPosition.x, this.virtualButtonPosition.y, this.drawPaint);
            canvas.save();
        }

//        PointF[][][] areaCoords = {
//                {
//                        {new PointF(0, 0), new PointF(200, 0), new PointF(200, 200), new PointF(0, 200), new PointF(0, 0)},
//                        {new PointF(50, 50), new PointF(150, 50), new PointF(150, 150), new PointF(50, 150), new PointF(50, 50)}
//                },
//                {
//                        {new PointF(180, 0), new PointF(500, 0), new PointF(500, 200), new PointF(180, 200), new PointF(180, 0)}
//                }
//        };
//        this.drawArea(canvas, sharedPoint, areaCoords);

        canvas.restore();
    }

    /**
     * drawImage with 8 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param scaleFactor
     */
    private void drawImage(Canvas canvas, Bitmap image, Matrix matrix, float x, float y, float imgOffsetX, float imgOffsetY, boolean fixSize, Float scaleFactor) {
        drawImage(canvas, image, matrix, x, y, imgOffsetX, imgOffsetY, fixSize, scaleFactor, null, null, null);
    }

    /**
     * drawImage with 9 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param scaleFactor
     * @param degree
     */
    private void drawImage(Canvas canvas, Bitmap image, Matrix matrix, float x, float y, float imgOffsetX, float imgOffsetY, boolean fixSize, Float scaleFactor, float degree) {
        drawImage(canvas, image, matrix, x, y, imgOffsetX, imgOffsetY, fixSize, scaleFactor,  degree, null, null);
    }

    /**
     * drawImage with 11 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param scaleFactor
     * @param degree
     * @param translateX
     * @param translateY
     */
    private void drawImage(Canvas canvas, Bitmap image, Matrix matrix, float x, float y, float imgOffsetX, float imgOffsetY, boolean fixSize, Float scaleFactor, Float degree, Float translateX, Float translateY) {
        if (canvas == null || image == null || matrix == null) return;

        float scaleX = fixSize ? 1 : this.transform.scaleX * this.transformAdaption.scaleX;
        float scaleY = fixSize ? 1 : this.transform.scaleY * this.transformAdaption.scaleY;

        matrix.reset();
        if (degree != null) {
            matrix.postRotate(degree + this.rotate, image.getWidth() / 2f, image.getHeight() / 2f);
            if (translateX != null && translateY != null) {
                matrix.postTranslate(translateX, translateY);
            }
        }
        if (scaleFactor != null) {
            matrix.postScale(scaleFactor, scaleFactor);
        }

        float dx, dy;
        if (!fixSize) {
            matrix.postScale(scaleX, scaleY);
        }
        dx = x - imgOffsetX * scaleX;
        dy = y - imgOffsetY * scaleX;

        matrix.postTranslate(dx, dy);
        canvas.drawBitmap(image, matrix, this.drawPaint);
    }

    private void drawArea(Canvas canvas, PointF sharedPoint, PointF[][][] areaCoords) {
        if (areaCoords == null) return;
        for (int i = 0; i < areaCoords.length; i++) {
            PointF[][] polygon = areaCoords[i];
            Path path = new Path();
            path.setFillType(Path.FillType.EVEN_ODD);
            for (int j = 0; j < polygon.length; j++) {
                PointF[] pointList = polygon[j];
                Path subPath = new Path();
                for (int k = 0; k < pointList.length; k++) {
                    this.getImageToCanvasPoint(sharedPoint, pointList[k].x, pointList[k].y);
                    if (k == 0) {
                        subPath.moveTo(sharedPoint.x, sharedPoint.y);
                    } else {
                        subPath.lineTo(sharedPoint.x, sharedPoint.y);
                    }
                }
                path.addPath(subPath);
            }
            this.drawPaint.setColor(0x3fff0000);
            this.drawPaint.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, this.drawPaint);
            this.drawPaint.setColor(0xffff0000);
            this.drawPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, this.drawPaint);
        }
    }

    public void transformPoint(PointF point, float oldX, float oldY, boolean reverse) {
        if (this.rotate == 90) {
            point.x = reverse ? oldY : this.imgWidth - oldY;
            point.y = reverse ? this.imgWidth - oldX : oldX;
        } else if (this.rotate == -90) {
            point.x = reverse ? this.imgHeight - oldY : oldY;
            point.y = reverse ? oldX : this.imgHeight - oldX;
        } else {
            point.x = oldX;
            point.y = oldY;
        }
    }

    private void getImageToCanvasPoint(PointF point, float x, float y) {
        this.transformPoint(point, x, y, false);
        point.x = point.x * this.transform.scaleX * this.transformAdaption.scaleX + this.transform.translateX + this.transformAdaption.translateX;
        point.y = point.y * this.transform.scaleY * this.transformAdaption.scaleY + this.transform.translateY + this.transformAdaption.translateY;
    }

    private void getCanvasToImagePoint(PointF point, float x, float y) {
        float newX = (x - this.transformAdaption.translateX - this.transform.translateX) / (this.transform.scaleX * this.transformAdaption.scaleX);
        float newY = (y - this.transformAdaption.translateY - this.transform.translateY) / (this.transform.scaleY * this.transformAdaption.scaleY);
        this.transformPoint(point, newX, newY, true);
    }

    private PointF getTouchPoint(float x, float y) {
        float px = x - this.boundingClientRect.left;
        float py = y - this.boundingClientRect.top;
        return new PointF(px, py);
    }

    private void validateScale(float newScaleX, float newScaleY) {
        if (newScaleX != newScaleY) return;
        newScaleX = (float) Math.ceil(newScaleX * 10000) / 10000;
        newScaleY = (float) Math.ceil(newScaleY * 10000) / 10000;

        if (newScaleX > 16) {
            newScaleX = 16;
        } else if (newScaleX < 1) {
            newScaleX = 1;
        }
        if (newScaleY > 16) {
            newScaleY = 16;
        } else if (newScaleY < 1) {
            newScaleY = 1;
        }

        if (this.transform.scaleX != newScaleX || this.transform.scaleY != newScaleY) {
            float oldScaleX = this.transform.scaleX;

            this.transform.scaleX = newScaleX;
            this.transform.scaleY = newScaleY;

            if ((oldScaleX >= 4) != (newScaleX >= 4)) {
                boolean lastIndoorMode = this.indoorMode;
                this.indoorMode = newScaleX >= 4;
                if (lastIndoorMode != this.indoorMode) {
                    mListener.onIndoorModeUpdate(this.indoorMode);
                }
            }
        }
    }

    private void validateTranslate(float newTranslateX, float newTranslateY) {
        // edges cases
        float currentWidth = this.imgWidth * this.transformAdaption.scaleX * this.transform.scaleX;
        float currentHeight = this.imgHeight * this.transformAdaption.scaleY * this.transform.scaleY;

        if (newTranslateX + currentWidth + this.transformAdaption.translateX < this.canvasWidth - this.transformAdaption.translateX) {
            newTranslateX = this.canvasWidth - 2 * this.transformAdaption.translateX - currentWidth;
        }
        if (newTranslateX > 0) {
            newTranslateX = 0;
        }

        if (newTranslateY + currentHeight + this.transformAdaption.translateY < this.canvasHeight - this.transformAdaption.translateY) {
            newTranslateY = this.canvasHeight - 2 * this.transformAdaption.translateY - currentHeight;
        }
        if (newTranslateY > 0) {
            newTranslateY = 0;
        }

        if (this.transform.translateX != newTranslateX) {
            this.transform.translateX = newTranslateX;
        }
        if (this.transform.translateY != newTranslateY) {
            this.transform.translateY = newTranslateY;
        }
    }

    private void manipulateMap(float dtranslateX, float dtranslateY, float dscaleX, float dscaleY) {
        float oldScaleX = this.transform.scaleX;
        float oldScaleY = this.transform.scaleY;
        float newScaleX = this.transform.scaleX + dscaleX;
        float newScaleY = this.transform.scaleY + dscaleY;
        this.validateScale(newScaleX, newScaleY);

        float newTranslateX = oldScaleX == this.transform.scaleX ? this.transform.translateX : (this.focusedPoint.x - this.transformAdaption.translateX - (this.focusedPoint.x - this.transformAdaption.translateX - this.transform.translateX) * this.transform.scaleX / oldScaleX);
        float newTranslateY = oldScaleY == this.transform.scaleY ? this.transform.translateY : (this.focusedPoint.y - this.transformAdaption.translateY - (this.focusedPoint.y - this.transformAdaption.translateY - this.transform.translateY) * this.transform.scaleY / oldScaleY);
        newTranslateX += dtranslateX;
        newTranslateY += dtranslateY;
        this.validateTranslate(newTranslateX, newTranslateY);

        this.checkCenteredBuilding();
    }

    private Object isPointInItem(float pointX, float pointY) {
        final PointF sharedPoint = new PointF();

        // tap on places
        PointF touchPoint = this.getTouchPoint(pointX, pointY);
        if (this.displayVirtualButton) {
            if (touchPoint.x >= this.virtualButtonPosition.x && touchPoint.x <= this.virtualButtonPosition.x + this.virtualButtonSize
                && touchPoint.y >= this.virtualButtonPosition.y && touchPoint.y <= this.virtualButtonPosition.y + this.virtualButtonSize) {
                return 4;
            }
        }

        RectF boundRect = new RectF();
        Path path = new Path();
        Region region = new Region();

        // tap on markers
        if (this.currentMarker != null) {
            path.reset();
            region.setEmpty();
            if (this.currentMarker.areaCoords != null) {
                path.addPath(this.addAreaToPath(sharedPoint, this.currentMarker.areaCoords));
            }
            this.getImageToCanvasPoint(sharedPoint, this.currentMarker.location.x , this.currentMarker.location.y);
            path.addRect(sharedPoint.x - this.halfMarkerSize, sharedPoint.y - this.markerSize, sharedPoint.x + this.halfMarkerSize, sharedPoint.y, Path.Direction.CW);
            path.addRect(this.addTextAreaToPath(sharedPoint, 0, 0, this.currentMarker.textWidth, this.currentMarker.textHeight, TextPosition.BOTTOM), Path.Direction.CW);
            path.computeBounds(boundRect, true);
            this.currentMarker.clipRegion.set((int) boundRect.left, (int) boundRect.top, (int) boundRect.right, (int) boundRect.bottom);
            region.setPath(path, this.currentMarker.clipRegion);
            if (region.contains((int) touchPoint.x, (int) touchPoint.y)) {
                return 1;
            }
        }

        // tap on places
        Object place = null;
        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
            GraphicPlace p = this.graphicPlaceArray[i];
            if (this.currentMarker != null && p.id == this.currentMarker.id) continue;
            if (p.iconLevel == 0 && p.areaCoords == null) continue;
            if (p.areaCoords == null && (this.transform.scaleX < p.iconLevel || this.transform.scaleY < p.iconLevel)) continue;
            path.reset();
            region.setEmpty();
            if (p.areaCoords != null) {
                path.addPath(this.addAreaToPath(sharedPoint, p.areaCoords));
            }
            if (p.iconLevel > 0) {
                this.getImageToCanvasPoint(sharedPoint, p.location.x , p.location.y);
                path.addRect(sharedPoint.x - this.halfIconSize, sharedPoint.y - this.halfIconSize, sharedPoint.x + this.halfIconSize, sharedPoint.y + this.halfIconSize, Path.Direction.CW);
                if (p.textPosition != null) {
                    path.addRect(this.addTextAreaToPath(sharedPoint, this.halfIconSize, this.halfIconSize, p.textWidth, p.textHeight, p.textPosition), Path.Direction.CW);
                }
            }
            path.computeBounds(boundRect, true);
            p.clipRegion.set((int) boundRect.left, (int) boundRect.top, (int) boundRect.right, (int) boundRect.bottom);
            region.setPath(path, p.clipRegion);
//                    LogUtil.d(TAG, String.format("%s %s %b", p.name, r.toShortString(), region.contains((int) touchPoint.x, (int) touchPoint.y)));
            if (region.contains((int) touchPoint.x, (int) touchPoint.y)) {
                place = p;
                break;
            }
        }
        if (place != null) return place;
        return null;
    }

    private Path addAreaToPath(PointF sharedPoint, PointF[][][] areaCoords) {
        Path area = new Path();
        for (int i = 0; i < areaCoords.length; i++) {
            PointF[] pointList = areaCoords[i][0];
            if (pointList != null) {
                for (int k = 0; k < pointList.length; k++) {
                    this.getImageToCanvasPoint(sharedPoint, pointList[k].x , pointList[k].y);
                    if (k == 0) area.moveTo(sharedPoint.x, sharedPoint.y);
                    else area.lineTo(sharedPoint.x, sharedPoint.y);
                }
            }
        }
        return area;
    }

    private RectF addTextAreaToPath(PointF point, float offsetX, float offsetY, int textWidth, int textHeight, TextPosition textPosition) {
        float halfWidth = textWidth / 2f;
        float halfHeight = textHeight / 2f;
        switch (textPosition) {
            case BOTTOM:
                return new RectF(point.x - halfWidth, point.y + offsetY, point.x + halfWidth, point.y + offsetY + textHeight);
            case LEFT:
                return new RectF(point.x + offsetX, point.y - halfHeight, point.x + offsetX + textWidth, point.y + halfHeight);
            case RIGHT:
                return new RectF(point.x - offsetX - textWidth, point.y - halfHeight, point.x - offsetX, point.y + halfHeight);
            case TOP:
                return new RectF(point.x - halfWidth, point.y - offsetY - textHeight, point.x + halfWidth, point.y - offsetY);
            default:
                return null;
        }
    }

    private void chooseItem(float pointX, float pointY, boolean longPress) {
        if (this.currentMarkerAnimator.isRunning() || this.lastMarkerAnimator.isRunning()) return;

        Object element = this.isPointInItem(pointX, pointY);
        if (element instanceof Integer && element.equals(1)) {

        } else if (longPress) {
            PointF imgPoint = new PointF();
            this.getCanvasToImagePoint(imgPoint, pointX, pointY);
            if ((imgPoint.x >= 0 && imgPoint.x <= (this.rotate == 0 ? this.imgWidth : this.imgHeight)) && (imgPoint.y >= 0 && imgPoint.y <= (this.rotate == 0 ? this.imgHeight : this.imgWidth))) {
                this.setSelectedPlace(new GraphicPlace(getResources().getString(R.string.marker_name), new PointF(imgPoint.x, imgPoint.y)));
            }
        } else if (element != null) {
            if (element instanceof GraphicPlace) {
                this.setSelectedPlace((GraphicPlace) element);
            }
        } else {
            // click on nothing
            if (this.currentMarker != null) {
                this.setSelectedPlace(null);
            }
        }
    }

    private void setSelectedPlace(GraphicPlace graphicPlace) {
        if (this.currentMarker != null && this.currentMarker.location != null) {
            this.lastMarker = this.currentMarker;
            this.lastMarkerAnimator.start();
        }

        if (graphicPlace == null) {
            this.currentMarker = null;
        } else if (this.currentMarker == null || this.currentMarker.id != graphicPlace.id || !this.currentMarker.placeType.equals(graphicPlace.placeType) || !this.currentMarker.location.equals(graphicPlace.location)) {
            this.currentMarker = new Marker(graphicPlace, this.getMarkerSpriteFromPool(graphicPlace.iconType), this.getStaticLayout(graphicPlace.name, getResources().getDimensionPixelSize(R.dimen.text_width)), this.iconSize / this.markerSize);
            if (this.currentMarker.id == 0 && "marker".equals(this.currentMarker.placeType)) {
                this.mListener.onPlaceSelected(this.currentMarker.location);
            }
            this.currentMarkerAnimator.start();
        }

        if (graphicPlace != null) {
            PlainPlace place = null;
            for (int i = 0; i < this.placeArray.length; i++) {
                if (this.placeArray[i].getId() == graphicPlace.id) {
                    place = this.placeArray[i];
                    break;
                }
            }
            if (place == null) return;
            LogUtil.d(TAG, "setSelectedPlace: " + place.getName());
            this.mListener.onPlaceSelected(place);
        }
    }

    private Bitmap getMarkerSpriteFromPool(String iconType) {
        if (!this.markerSpritePool.containsKey(iconType)) {
            Map<String, Object> value = (Map<String, Object>) this.markerSpriteInfo.get(this.markerSpriteInfo.containsKey(iconType) ? iconType : "default");
            int row = (Integer) value.get("row");
            int column = (Integer) value.get("column");
            int width = (Integer) value.get("width");
            int height = (Integer) value.get("height");
            Matrix matrix = new Matrix();
            matrix.setScale(this.markerSize / width, this.markerSize / height);
            this.markerSpritePool.put(iconType, Bitmap.createBitmap(imageMap.get("marker"), (column - 1) * width, (row - 1) * height, width, height, matrix, true));
        }
        return this.markerSpritePool.get(iconType);
    }

    private int getMapRotation(int imgWidth, int imgHeight) throws Exception {
        if (imgWidth == 0 || imgHeight == 0) {
            throw new Exception("Image size is 0.");
        }

        float clientWidth = this.clientWidth;
        float clientHeight = this.clientHeight;

        if (imgWidth <= imgHeight) {
            if (clientWidth <= clientHeight) {
                // img: portrait  screen: portrait
                return 0;
            } else {
                // img: portrait  screen: landscape
                return -90;
            }
        } else {
            if (clientWidth >= clientHeight) {
                // img: landscape  screen: landscape
                return 0;
            } else { // clientWidth < clientHeight
                //img: landscape  screen: portrait
                return 90;
            }
        }
    }

    private void resetLayout() throws Exception {
        final int imgWidth = this.imageMap.get("map").getWidth();
        final int imgHeight = this.imageMap.get("map").getHeight();

        LogUtil.d(TAG, "IMGSIZE: " + imgWidth + " " + imgHeight + " " + this.imageMap.get("map").getAllocationByteCount());
        this.rotate = getMapRotation(imgWidth, imgHeight);
        this.mListener.onRotateUpdate(this.rotate);

        this.imgWidth = this.rotate == 0 ? imgWidth : imgHeight;
        this.imgHeight = this.rotate == 0 ? imgHeight : imgWidth;

        if (this.rotate != 0) {
            this.mapRotationCenter = (((imgWidth <= imgHeight) == (this.rotate == 90)) ? Math.max(imgWidth, imgHeight) : Math.min(imgWidth, imgHeight)) / 2f;
        }

        GraphicPlace.imgWidth = this.imgWidth;
        GraphicPlace.imgHeight = this.imgHeight;
        GraphicPlace.degree = this.rotate;

        this.canvasWidth = this.clientWidth;
        this.canvasHeight = this.clientHeight;

        float scaleAdaption = Math.min(this.canvasWidth / this.imgWidth, this.canvasHeight / this.imgHeight);
        this.transformAdaption.scaleX = scaleAdaption;
        this.transformAdaption.scaleY = scaleAdaption;
        this.transformAdaption.translateX = Math.round((this.canvasWidth - this.imgWidth * this.transformAdaption.scaleX) / 2);
        this.transformAdaption.translateY = Math.round((this.canvasHeight - this.imgHeight * this.transformAdaption.scaleY) / 2);

        this.virtualButtonSize = getResources().getDimensionPixelSize(R.dimen.button_size);
        this.virtualButtonPosition.x = Math.round(this.canvasWidth * 0.98 - this.virtualButtonSize);
        this.virtualButtonPosition.y = Math.round((this.canvasHeight - this.virtualButtonSize) / 2);

        if (this.labelComplete) {
            this.updatePlaceList();
        }
    }

    private void refreshTextPosition() {
        PointF sharedPoint = new PointF();

        TextPosition[] positionArr = {TextPosition.BOTTOM, TextPosition.LEFT, TextPosition.RIGHT, TextPosition.TOP};
        float finalScaleX = this.transform.scaleX * this.transformAdaption.scaleX;
        float finalScaleY = this.transform.scaleX * this.transformAdaption.scaleY;
        float currentScaleX = this.transform.scaleX;
        float currentScaleY = this.transform.scaleY;
        GridIndex.init(this.imgWidth * finalScaleX, this.imgHeight * finalScaleY, (int) (30 * Math.floor(this.transform.scaleX)));

        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
            GraphicPlace place = this.graphicPlaceArray[i];
            if (currentScaleX < place.iconLevel || currentScaleY < place.iconLevel) continue;
            this.transformPoint(sharedPoint, place.location.x, place.location.y, false);
            sharedPoint.x *= finalScaleX;
            sharedPoint.y *= finalScaleY;
            GridIndex.insert(new Pair<>(String.valueOf(place.id), new BCircle(sharedPoint.x, sharedPoint.y, this.halfIconSize)), false);
        }

        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
            GraphicPlace place = this.graphicPlaceArray[i];

            if (place.iconLevel <= 0 || currentScaleX < place.iconLevel || currentScaleY < place.iconLevel) continue;

            this.transformPoint(sharedPoint, place.location.x, place.location.y, false);
            sharedPoint.x *= finalScaleX;
            sharedPoint.y *= finalScaleY;
            float width = place.textWidth;
            float height = place.textHeight;
            float halfWidth = width / 2;
            float halfHeight = height / 2;

            int result = -1;
            for (int j = -1; j < positionArr.length; j++) {
                TextPosition position = j >= 0 ? positionArr[j] : place.textPosition;
                if (position == null) continue;
                BBox box = null;
                switch (position) {
                    case BOTTOM:
                        box = new BBox(sharedPoint.x - halfWidth, sharedPoint.y + this.halfIconSize, width, height);
                        break;
                    case LEFT:
                        box = new BBox(sharedPoint.x + this.halfIconSize, sharedPoint.y - halfHeight, width, height);
                        break;
                    case RIGHT:
                        box = new BBox(sharedPoint.x - this.halfIconSize - width, sharedPoint.y - halfHeight, width, height);
                        break;
                    case TOP:
                        box = new BBox(sharedPoint.x - halfWidth, sharedPoint.y - this.halfIconSize - height, width, height);
                        break;
                }
                result = GridIndex.insert(new Pair<>(String.valueOf(place.id), box), true);

                if (result > -1) {
                    place.textPosition = position;
                    break;
                }
            }
            if (result == -1) {
                place.textPosition = null;
            }
        }
    }
    
    public void refreshIconDisplay() {
        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
            GraphicPlace place = this.graphicPlaceArray[i];
            if (place.iconLevel <= 0) continue;
            if (this.transform.scaleX < place.iconLevel || this.transform.scaleY < place.iconLevel) {
                if (place.displayAlpha == 255 || (place.displayAnimator.isRunning() && place.displayAnimationForward)) {
                    place.displayAnimationForward = false;
                    place.displayAnimator.end();
                    place.displayAnimator.reverse();
                }
            } else {
                if (place.displayAlpha == 0 || (place.displayAnimator.isRunning() && !place.displayAnimationForward)) {
                    place.displayAnimationForward = true;
                    place.displayAnimator.end();
                    place.displayAnimator.start();
                }
            }
        }
    }

    private void checkCenteredBuilding() {
        if (this.transform.scaleX < 4 || this.transform.scaleY < 4) return;
        float minDistance = 300 / this.transform.scaleX / this.transformAdaption.scaleX;
        PlainPlace minPlace = null;
        PointF touchPoint = this.getTouchPoint(this.canvasWidth / 2, this.canvasHeight / 2);
        this.getCanvasToImagePoint(touchPoint, touchPoint.x, touchPoint.y);
        for (int i = 0; i < this.buildingArray.length; i++) {
            PlainPlace building = this.buildingArray[i];
            float distance = this.getDistance(touchPoint.x, touchPoint.y, (float) building.getLocation().getX(), (float) building.getLocation().getY());
            if (distance < minDistance) {
                minDistance = distance;
                minPlace = building;
            }
        }
        if (minPlace != null && minPlace.getId() != null) {
            LogUtil.d("checkCenteredBuilding", minPlace.toString());
            this.mListener.onBuildingIdUpdate(minPlace.getId());
        }
    }

    public void setPlaceList(List<PlainPlace> placeList) {
        this.placeArray = placeList.toArray(new PlainPlace[0]);
        String placeType = "building";
        List<PlainPlace> buildingList = new ArrayList<>();
        for (int i = 0; i < this.placeArray.length; i++) {
            if (placeType.equals(this.placeArray[i].getPlaceType())) {
                buildingList.add(this.placeArray[i]);
            }
        }
        this.buildingArray = buildingList.toArray(new PlainPlace[0]);
//        for (PlainPlace place : placeList) {
//            LogUtil.d(TAG, place.toString());
//        }

        if (this.imageComplete) {
            this.updatePlaceList();
        }

        this.labelComplete = true;
    }

    public float getDistance(float x1, float y1, float x2, float y2) {
        float deltaX = x2 - x1;
        float deltaY = y2 - y1;
        return (float) Math.sqrt(deltaX * deltaX + deltaY * deltaY);
    }

    public void setMapImage(Bitmap resource) {
        this.imageMap.put("map", resource);

        if (this.layoutComplete) {
            try {
                this.resetLayout();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        this.imageComplete = true;
    }

    public boolean hasImage(String key) {
        return this.imageMap.containsKey(key);
    }

    public void addImage(String key, Bitmap value) {
        this.imageMap.put(key, value);
    }

    public void deleteImage(String key) {
        Bitmap image = this.imageMap.remove(key);
        if (image != null) {
            image.recycle();
            image = null;
        }
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColorAnimator = ValueAnimator.ofArgb(this.backgroundColor, color);
        this.backgroundColorAnimator.setDuration(500);
        this.backgroundColorAnimator.addUpdateListener(animation -> backgroundColor = ((Number) animation.getAnimatedValue()).intValue());
        this.mHandler.post(() -> backgroundColorAnimator.start());
    }

    public void setDisplayVirtualButton(boolean display) {
        this.mListener.onDisplayVirtualButtonUpdate(this.displayVirtualButton = display);
    }

    private void updatePlaceList() {
        int textWidth = getResources().getDimensionPixelSize(R.dimen.text_width);

        Map<String, Map<String, Object>> iconSpritePool = new HashMap<>();

        GraphicPlace[] tempPlaceArray = new GraphicPlace[this.placeArray.length];
        for (int i = 0; i < this.placeArray.length; i++) {
            PlainPlace place = this.placeArray[i];
            if (!iconSpritePool.containsKey(place.getIconType())) {
                Map<String, Object> value = (Map<String, Object>) this.iconSpriteInfo.get(place.getIconType());
                if (value == null) {
                    value = new HashMap<>();
                    value.put("color", 0x000000);
                } else {
                    int row = (Integer) value.get("row");
                    int column = (Integer) value.get("column");
                    int width = (Integer) value.get("width");
                    int height = (Integer) value.get("height");
                    Matrix matrix = new Matrix();
                    matrix.setScale(this.iconSize / width, this.iconSize / height);
                    value.put("image", Bitmap.createBitmap(imageMap.get("icon"), (column - 1) * width, (row - 1) * height, width, height, matrix, true));
                    value.put("color", Integer.valueOf((String) value.get("color"), 16));
                }
                iconSpritePool.put(place.getIconType(), value);
            }
            Map<String, Object> typeMap = iconSpritePool.get(place.getIconType());

            GraphicPlace gp = new GraphicPlace(place, (Bitmap) typeMap.get("image"), (Integer) typeMap.get("color"), this.getStaticLayout(place.getShortName(), textWidth));
            tempPlaceArray[i] = gp;
        }
        Arrays.sort(tempPlaceArray);
        this.graphicPlaceArray = tempPlaceArray;

        for (int i = 0; i < this.graphicPlaceArray.length; i++) {
            LogUtil.d(TAG, i + " " + this.graphicPlaceArray[i]);
        }

        this.refreshTextPosition();
        this.mHandler.post(this::refreshIconDisplay);
    }

    public void arrangeFloorList(@NonNull Floor floor) {
        this.floorList.clear();
        this.floorList.add(floor);
    }

    private StaticLayout getStaticLayout(String name, int textWidth) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return StaticLayout.Builder.obtain(name, 0, name.length(), this.textPaint, textWidth).build();
        } else {
            return new StaticLayout(name, this.textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
        }
    }

    public void setLocation(float x, float y) {
        LogUtil.d(TAG, String.format("location: %f, %f", x, y));
        if ((x >= 0 && x <= this.imgWidth) && (y >= 0 && y <= this.imgHeight)) {
            if (this.location == null) {
                this.location = new PointF(x, y);
            } else {
                this.location.x = x;
                this.location.y = y;
            }
        }
    }

    public void setDirection(int direction) {
        this.deviceDirection = direction;
    }

    public void setLocationActivated(boolean flag) {
        this.locationActivated = flag;
//        if (flag) {
//            this.location = new PointF(500, 500);
//        }
//        if (!flag) {
//            this.location = null;
//        }
    }

    public void setMarkerName(String name, PointF location) {
        if (this.currentMarker == null) return;
        if (!location.equals(this.currentMarker.location)) return;
        this.currentMarker.name = name;
        this.currentMarker.setStaticLayout(this.getStaticLayout(name, getResources().getDimensionPixelSize(R.dimen.text_width)));
    }

    interface OnCanvasDataUpdateListener {
        void onPlaceSelected(PlainPlace place);

        void onPlaceSelected(PointF location);

        void onBuildingIdUpdate(int id);

        void onIndoorModeUpdate(boolean indoorMode);

        void onRotateUpdate(int rotate);

        void onDisplayVirtualButtonUpdate(boolean display);
    }
}
