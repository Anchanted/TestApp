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
import android.view.animation.LinearInterpolator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.xjtlu.testapp.graphic.AnimationTransform;
import cn.edu.xjtlu.testapp.graphic.AnimationTransformEvaluator;
import cn.edu.xjtlu.testapp.graphic.BBox;
import cn.edu.xjtlu.testapp.graphic.BCircle;
import cn.edu.xjtlu.testapp.graphic.GraphicPlace;
import cn.edu.xjtlu.testapp.domain.PlainPlace;
import cn.edu.xjtlu.testapp.graphic.GridIndex;
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
    private PointF doubleTapDownMotionEventPoint;
    private float lastDoubleTapMotionEventDistance;
    private Bitmap resourceImg;
    private final TextPaint textPaint = new TextPaint();
    private final Paint drawPaint = new Paint();
    private final List<GraphicPlace> graphicPlaceList = new ArrayList<>();
    private OnCanvasDataUpdateListener mListener;
    @NonNull
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private final Map<String, Map<String, Object>> iconSpriteInfo = new HashMap<>();
    private final Map<String, Map<String, Object>> markerSpriteInfo = new HashMap<>();

    private float canvasWidth;
    private float canvasHeight;
    private float imgWidth;
    private float imgHeight;
    private final AnimationTransform transformAdaption = new AnimationTransform();
    private final AnimationTransform transform = new AnimationTransform(0, 0, 1, 1);
    private final PointF focusedPoint = new PointF(0, 0);
    private int backgroundColor = Color.WHITE;
    private PlainPlace selectedPlace;
    private PlainPlace fromDirectionMarker;
    private PlainPlace toDirectionMarker;
    private PointF location;
    private float iconSize;
    private float halfIconSize;
    private float locationIconSize;
    private float halfLocationIconSize;
    private final PointF virtualButtonPosition = new PointF(100, 100);
    private int virtualButtonSize;
    private boolean virtualButtonSelected;
    @NonNull
    private ValueAnimator mapAnimator = new ValueAnimator();
    private ValueAnimator backgroundColorAnimator;
    private final Matrix drawMatrix = new Matrix();
    private AnimationTransform lastMapAnimationTransform;
    private boolean labelComplete;
    private boolean imageComplete;

    @NonNull
    private final Map<String, Bitmap> imageMap = new HashMap<>();
    private Bitmap mapImage;
    private float clientWidth;
    private float clientHeight;
    private int rotate;
    private List<PlainPlace> placeList;
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
        Canvas canvas;
        while (this.doDrawing) {
            if (!this.surfaceHolder.getSurface().isValid()) continue;
//            if (!this.labelComplete || !this.imageComplete) continue;
            if (!this.labelComplete) continue;
            if ((canvas = this.surfaceHolder.lockCanvas()) != null) {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                this.drawMapInfo(canvas);
            }
            this.surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
//        LogUtil.d(TAG, "onTouchEvent " + event.getAction());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
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
                    float scale = (distance - this.lastDoubleTapMotionEventDistance) / 300;
                    this.manipulateMap(new AnimationTransform(0, 0, scale, scale));
                    this.lastDoubleTapMotionEventDistance = distance;
                    this.refreshTextPosition();
                    this.refreshIconDisplay();
                }
                break;
        }
        if (event.getPointerCount() > 1) {
            return this.scaleGestureDetector.onTouchEvent(event);
        } else {
            return this.gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
        }
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
            this.manipulateMap(new AnimationTransform(-distanceX, -distanceY, 0, 0));
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

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
            this.chooseItem(e.getX(), e.getY());
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
//        LogUtil.d(TAG, "onDoubleTapEvent");
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
//                LogUtil.d(TAG, "onDoubleTapEvent ACTION_DOWN");
                this.doubleTapDown = true;
                this.doubleTapMove = false;
                this.doubleTapDownMotionEventPoint = new PointF(e.getX(), e.getY());
                this.lastDoubleTapMotionEventDistance = 0;

                PointF focusedPoint = this.getTouchPoint(e.getX(), e.getY());
                this.focusedPoint.x = focusedPoint.x;
                this.focusedPoint.y = focusedPoint.y;
                break;
            case MotionEvent.ACTION_MOVE:
//                LogUtil.d(TAG, "onDoubleTapEvent ACTION_MOVE");
                this.doubleTapMove = true;
                break;
            case MotionEvent.ACTION_UP:
                this.doubleTapDown = false;

                if (!this.doubleTapMove) {
                    ValueAnimator valueAnimator = ValueAnimator.ofObject(new AnimationTransformEvaluator(), new AnimationTransform(0, 0, 0, 0), new AnimationTransform(0, 0, 0.5f, 0.5f));
                    valueAnimator.setDuration(100);
                    valueAnimator.setInterpolator(new LinearInterpolator());
                    valueAnimator.addUpdateListener(animation -> {
                        AnimationTransform currentAnimation = (AnimationTransform) animation.getAnimatedValue();
                        manipulateMap(AnimationTransform.minus(currentAnimation, lastMapAnimationTransform));
                        lastMapAnimationTransform = currentAnimation;
                    });
                    valueAnimator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            super.onAnimationStart(animation);
                            lastMapAnimationTransform = null;
                        }
                    });
                    this.mapAnimator = valueAnimator;
                    this.mapAnimator.start();
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
//        LogUtil.d(TAG, "onScale");
        if (this.mapAnimator.isRunning()) return true;
        float zoom = detector.getScaleFactor() - 1f;
        PointF focusPoint = this.getTouchPoint(detector.getFocusX(), detector.getFocusY());
        this.focusedPoint.x = focusPoint.x;
        this.focusedPoint.y = focusPoint.y;
        this.manipulateMap(new AnimationTransform(0, 0, zoom * 2, zoom * 2));
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

//        setFocusable(true);
//        setKeepScreenOn(true);
//        setFocusableInTouchMode(true);

        this.gestureDetector = new GestureDetector(this.mContext, this);
        this.gestureDetector.setOnDoubleTapListener(this);
        this.scaleGestureDetector = new ScaleGestureDetector(this.mContext, this);

        float dpi = getResources().getDisplayMetrics().density;
        LogUtil.d(TAG, "dpi: " + dpi);

        this.imageMap.put("icon", BitmapFactory.decodeResource(getResources(), R.drawable.icon_sprite));
        this.imageMap.put("locationProbe", BitmapFactory.decodeResource(getResources(), R.drawable.location_probe));
        this.imageMap.put("locationMarker", BitmapFactory.decodeResource(getResources(), R.drawable.location_marker));

        Drawable vectorDrawable = getResources().getDrawable(R.drawable.ic_display_button);
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        this.imageMap.put("displayButton", bitmap);

        this.iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
        this.halfIconSize = this.iconSize / 2;
        this.locationIconSize = this.iconSize * 1.5f;
        this.halfLocationIconSize = this.iconSize * 0.75f;

//        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size));
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setColor(0xff0d6efd);
        this.textPaint.setStrokeWidth(4);
//        this.textPaint.setShadowLayer(1, 0, 0, 0xffffffff);
        this.textPaint.setFakeBoldText(true);

//        this.drawPaint.setAntiAlias(true);
        this.drawPaint.setStrokeWidth(6);
        this.drawPaint.setStrokeJoin(Paint.Join.ROUND);
        this.drawPaint.setStrokeCap(Paint.Cap.ROUND);

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                ObjectMapper mapper = new ObjectMapper();
                try {
                    Map<String, Object> map = mapper.readValue(JsonAssetsReader.getJsonString("json/iconSpriteInfo.json", mContext), Map.class);
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        Map<String, Object> value = (Map<String, Object>) entry.getValue();
                        int row = (Integer) value.get("row");
                        int column = (Integer) value.get("column");
                        int width = (Integer) value.get("width");
                        int height = (Integer) value.get("height");
                        value.put("image", Bitmap.createBitmap(imageMap.get("icon"),(column - 1) * width, (row - 1) * height, width, height));
                        iconSpriteInfo.put(entry.getKey(), value);
                    }
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
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
        if (this.mapAnimator.isRunning()) {
            this.mapAnimator.end();
        }
        if (this.backgroundColorAnimator.isRunning()) {
            this.backgroundColorAnimator.end();
        }
        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.displayAnimator.isRunning()) {
                place.displayAnimator.end();
            }
        }
    }

    public void setOnCanvasDataUpdateListener(OnCanvasDataUpdateListener listener) {
        this.mListener = listener;
    }

    private void drawMapInfo(Canvas canvas) {
        canvas.save();

        canvas.drawColor(this.backgroundColor);

        if (this.imageMap.get("map") != null) {
    //        this.drawImage(canvas, this.imageMap.get("map"), 0, 0, this.imgWidth, this.imgHeight, 0, 0, false, false);

            float scaleX = this.transform.scaleX * this.transformAdaption.scaleX;
            float scaleY = this.transform.scaleY * this.transformAdaption.scaleY;
            PointF p = this.getImageToCanvasPoint(0, 0);
            this.drawMatrix.setScale(scaleX, scaleY);
            this.drawMatrix.postTranslate(p.x, p.y);
    //        RectF dRect = new RectF(p.x, p.y, p.x + this.imgWidth * scaleX, p.y + this.imgHeight * scaleY);
            canvas.drawBitmap(this.mapImage, this.drawMatrix, this.drawPaint);
    //        this.drawPaint.setColor(Color.RED);
    //        canvas.drawRect(dRect, this.drawPaint);
        }

        if (this.graphicPlaceList != null && this.graphicPlaceList.size() > 0) {
//            for (GraphicPlace place : this.graphicPlaceList) {
//                if (place.areaCoords == null) continue;
//                Path path = new Path();
//                path.setFillType(Path.FillType.EVEN_ODD);
//                for (List<Point> pointList : place.areaCoords) {
//                    Path subPath = new Path();
//                    for (int i = 0; i < pointList.size(); i++) {
//                        PointF canvasPoint = this.getImageToCanvasPoint(pointList.get(i));
//                        if (i == 0) {
//                            subPath.moveTo(canvasPoint.x, canvasPoint.y);
//                        } else {
//                            subPath.lineTo(canvasPoint.x, canvasPoint.y);
//                        }
//                    }
////                    subPath.moveTo(0, 0);
////                    subPath.lineTo(0, 200);
////                    subPath.lineTo(200, 200);
////                    subPath.lineTo(200, 0);
////                    subPath.lineTo(0, 0);
//                    path.addPath(subPath);
//                }
//
//                this.drawPaint.setColor(0x3fff0000);
//                this.drawPaint.setStyle(Paint.Style.FILL);
//                canvas.drawPath(path, this.drawPaint);
//                this.drawPaint.setColor(0xffff0000);
//                this.drawPaint.setStyle(Paint.Style.STROKE);
//                canvas.drawPath(path, this.drawPaint);
//            }

            for (GraphicPlace place : this.graphicPlaceList) {
                // selected place
                if (this.selectedPlace != null && place.id == this.selectedPlace.getId()) continue;
                // direction markers
                if (this.fromDirectionMarker != null && place.id == this.fromDirectionMarker.getId()) continue;
                if (this.toDirectionMarker != null && place.id == this.toDirectionMarker.getId()) continue;
                // place not to display
                if (place.displayAlpha == 0) continue;
//                this.drawImage(canvas, this.imageMap.get("icon"), (float) place.getLocation().getX(), (float) place.getLocation().getY(), size, size, size/2, size/2, true, true,
//                        (iconSpriteInfo.get(place.iconType).get("column") - 1) * iconSpriteInfo.get(place.iconType).get("width"), (iconSpriteInfo.get(place.iconType).get("row") - 1) * iconSpriteInfo.get(place.iconType).get("height"), iconSpriteInfo.get(place.iconType).get("width"), iconSpriteInfo.get(place.iconType).get("height"));
                this.drawPaint.setAlpha(place.displayAlpha);
                this.drawImage(canvas, place.iconImg, (float) place.location.x, (float) place.location.y, this.iconSize, this.iconSize, this.halfIconSize, this.halfIconSize, true, true);

                if (place.displayName && place.textPosition != null) {
                    PointF canvasPoint = this.getImageToCanvasPoint(place.location.x, place.location.y);
                    canvas.save();
                    switch (place.textPosition) {
                        case BOTTOM:
                            canvas.translate(canvasPoint.x, canvasPoint.y + this.halfIconSize);
                            break;
                        case LEFT:
                            canvas.translate(canvasPoint.x + this.halfIconSize, canvasPoint.y - place.halfTextHeight);
                            break;
                        case RIGHT:
                            canvas.translate(canvasPoint.x - this.halfIconSize, canvasPoint.y - place.halfTextHeight);
                            break;
                        case TOP:
                            canvas.translate(canvasPoint.x, canvasPoint.y - this.halfIconSize - place.textHeight);
                            break;
                    }
                    this.textPaint.setTextAlign(place.textPosition.align);
                    this.textPaint.setColor(Color.WHITE);
                    this.textPaint.setStyle(Paint.Style.STROKE);
                    this.textPaint.setAlpha(place.displayAlpha);
                    place.staticLayout.draw(canvas);
                    this.textPaint.setColor(0xff0d6efd);

                    this.textPaint.setStyle(Paint.Style.FILL);
                    this.textPaint.setAlpha(place.displayAlpha);
                    place.staticLayout.draw(canvas);
                    canvas.restore();
                }
            }
            this.drawPaint.setAlpha(255);
        }

        if (this.locationActivated && this.location != null) {
            if (this.deviceDirection != null) {
                this.drawImage(canvas, this.imageMap.get("locationProbe"), this.location.x, this.location.y, this.locationIconSize, this.locationIconSize, this.halfLocationIconSize, this.halfLocationIconSize, true, false, this.deviceDirection);
            }
            this.drawImage(canvas, this.imageMap.get("locationMarker"), this.location.x, this.location.y, this.locationIconSize, this.locationIconSize, this.halfLocationIconSize, this.halfLocationIconSize, true, false);
        }

        if (this.displayVirtualButton) {
            canvas.save();
            canvas.drawBitmap(this.imageMap.get("displayButton"), this.virtualButtonPosition.x, this.virtualButtonPosition.y, this.drawPaint);
            canvas.restore();
        }

        canvas.restore();
    }

    /**
     * drawImage with 10 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate) {
        drawImage(canvas, image, x, y, sizeX, sizeY, imgOffsetX, imgOffsetY, fixSize, selfRotate, null, null, null, null, null, null, null);
    }

    /**
     * drawImage with 11 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     * @param degree
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate, float degree) {
        drawImage(canvas, image, x, y, sizeX, sizeY, imgOffsetX, imgOffsetY, fixSize, selfRotate, null, null, null, null, degree, null, null);
    }

    /**
     * drawImage with 13 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     * @param degree
     * @param translateX
     * @param translateY
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate, float degree, float translateX, float translateY) {
        drawImage(canvas, image, x, y, sizeX, sizeY, imgOffsetX, imgOffsetY, fixSize, selfRotate, null, null, null, null, degree, translateX, translateY);
    }

    /**
     * drawImage with 14 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     * @param sx
     * @param sy
     * @param sWidth
     * @param sHeight
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate, int sx, int sy, int sWidth, int sHeight) {
        drawImage(canvas, image, x, y, sizeX, sizeY, imgOffsetX, imgOffsetY, fixSize, selfRotate, sx, sy, sWidth, sHeight, null, null, null);
    }

    /**
     * drawImage with 15 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     * @param sx
     * @param sy
     * @param sWidth
     * @param sHeight
     * @param degree
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate, int sx, int sy, int sWidth, int sHeight, float degree) {
        drawImage(canvas, image, x, y, sizeX, sizeY, imgOffsetX, imgOffsetY, fixSize, selfRotate, sx, sy, sWidth, sHeight, degree, null, null);
    }

    /**
     * drawImage with 17 parameters
     * @param canvas
     * @param image
     * @param x
     * @param y
     * @param sizeX
     * @param sizeY
     * @param imgOffsetX
     * @param imgOffsetY
     * @param fixSize
     * @param selfRotate
     * @param sx
     * @param sy
     * @param sWidth
     * @param sHeight
     * @param degree
     * @param translateX
     * @param translateY
     */
    private void drawImage(Canvas canvas, Bitmap image, float x, float y, float sizeX, float sizeY, float imgOffsetX, float imgOffsetY, boolean fixSize, boolean selfRotate, Integer sx, Integer sy, Integer sWidth, Integer sHeight, Float degree, Float translateX, Float translateY) {
        if (degree != null) {
            PointF tPoint = this.getImageToCanvasPoint(x, y);
            canvas.save();
            canvas.translate(tPoint.x, tPoint.y);
            canvas.rotate(degree);
            canvas.translate(-tPoint.x, -tPoint.y);
            if (translateX != null && translateY != null) {
                canvas.translate(translateX, translateY);
            }
        }

        float scaleX = this.transform.scaleX * this.transformAdaption.scaleX;
        float scaleY = this.transform.scaleY * this.transformAdaption.scaleY;
        Rect sRect = (sx != null && sy != null && sWidth != null && sHeight != null) ? new Rect(sx, sy, sx + sWidth, sy + sHeight) : null;
        RectF dRect;
        if (!fixSize) {
            PointF canvasPoint = this.getImageToCanvasPoint(x - imgOffsetX, y - imgOffsetY);
            float dx = canvasPoint.x;
            float dy = canvasPoint.y;
            dRect = new RectF(dx, dy, dx + sizeX * scaleX, dy + sizeY * scaleY);
        } else {
            PointF canvasPoint = this.getImageToCanvasPoint(x, y);
            float dx = canvasPoint.x - imgOffsetX;
            float dy = canvasPoint.y - imgOffsetY;
            dRect = new RectF(dx, dy, dx + sizeX, dy + sizeY);
        }
        canvas.drawBitmap(image, sRect, dRect, drawPaint);
    }

    private PointF getImageToCanvasPoint(float x, float y) {
        return new PointF(x * this.transform.scaleX * this.transformAdaption.scaleX + this.transform.translateX + this.transformAdaption.translateX, y * this.transform.scaleY * this.transformAdaption.scaleY + this.transform.translateY + this.transformAdaption.translateY);
    }

    private PointF getCanvasToImagePoint(float x, float y) {
        return new PointF((x - this.transformAdaption.translateX - this.transform.translateX) / (this.transform.scaleX * this.transformAdaption.scaleX), (y - this.transformAdaption.translateY - this.transform.translateY) / (this.transform.scaleY * this.transformAdaption.scaleY));
    }

    private PointF getTouchPoint (float x, float y) {
        float px = x - this.boundingClientRect.left;
        float py = y - this.boundingClientRect.top;
        return new PointF(px, py);
    }

    private void validateScale(float newScaleX, float newScaleY) {
        if (newScaleX != newScaleY) return;
        newScaleX = (float) Math.ceil(newScaleX * 10000) / 10000;
        newScaleY = (float) Math.ceil(newScaleY * 10000) / 10000;

        if (newScaleX > 6) {
            newScaleX = 6;
        } else if (newScaleX < 1) {
            newScaleX = 1;
        }
        if (newScaleY > 6) {
            newScaleY = 6;
        } else if (newScaleY < 1) {
            newScaleY = 1;
        }

        if (this.transform.scaleX != newScaleX || this.transform.scaleY != newScaleY) {
            this.transform.scaleX = newScaleX;
            this.transform.scaleY = newScaleY;
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

    private void manipulateMap(AnimationTransform deltaTransform) {
        if (deltaTransform == null) return;

        float oldScaleX = this.transform.scaleX;
        float oldScaleY = this.transform.scaleY;
        float newScaleX = this.transform.scaleX + deltaTransform.scaleX;
        float newScaleY = this.transform.scaleY + deltaTransform.scaleY;
        this.validateScale(newScaleX, newScaleY);

        float newTranslateX = oldScaleX == this.transform.scaleX ? this.transform.translateX : (this.focusedPoint.x - this.transformAdaption.translateX - (this.focusedPoint.x - this.transformAdaption.translateX - this.transform.translateX) * this.transform.scaleX / oldScaleX);
        float newTranslateY = oldScaleY == this.transform.scaleY ? this.transform.translateY : (this.focusedPoint.y - this.transformAdaption.translateY - (this.focusedPoint.y - this.transformAdaption.translateY - this.transform.translateY) * this.transform.scaleY / oldScaleY);
        newTranslateX += deltaTransform.translateX;
        newTranslateY += deltaTransform.translateY;
        this.validateTranslate(newTranslateX, newTranslateY);
    }

    private Object isPointInItem(float pointX, float pointY) {
        // click on places
        PointF touchPoint = this.getTouchPoint(pointX, pointY);
        if (this.displayVirtualButton) {
            if (touchPoint.x >= this.virtualButtonPosition.x && touchPoint.x <= this.virtualButtonPosition.x + this.virtualButtonSize
                && touchPoint.y >= this.virtualButtonPosition.y && touchPoint.y <= this.virtualButtonPosition.y + this.virtualButtonSize) {
                return 4;
            }
        }

        int iconSizeSquared = (int) (this.iconSize * this.iconSize);
        RectF boundRect = new RectF();
        Object place = null;
        for (GraphicPlace p : graphicPlaceList) {
            if (this.selectedPlace != null && p.id == this.selectedPlace.getId()) continue;
            Region region = new Region();
            Path path = new Path();
            if (p.areaCoords == null) {
                if (p.iconLevel <= 0 || (this.transform.scaleX < p.iconLevel || this.transform.scaleY < p.iconLevel)) continue;
                PointF point = this.getImageToCanvasPoint(p.location.x , p.location.y);
                path.addCircle(point.x, point.y, this.halfIconSize, Path.Direction.CW);
                if (p.textPosition != null) {
                    float halfWidth = p.textWidth / 2f;
                    float halfHeight = p.textHeight / 2f;
                    switch (p.textPosition) {
                        case BOTTOM:
                            path.addRect(point.x - halfWidth, point.y + this.halfIconSize, point.x + halfWidth, point.y + this.halfIconSize + p.textHeight, Path.Direction.CW);
                            break;
                        case LEFT:
                            path.addRect(point.x + this.halfIconSize, point.y - halfHeight, point.x + this.halfIconSize + p.textWidth, point.y + halfHeight, Path.Direction.CW);
                            break;
                        case RIGHT:
                            path.addRect(point.x - this.halfIconSize - p.textWidth, point.y - halfHeight, point.x - this.halfIconSize, point.y + halfHeight, Path.Direction.CW);
                            break;
                        case TOP:
                            path.addRect(point.x - halfWidth, point.y - this.halfIconSize - p.textHeight, point.x + halfWidth, point.y - this.halfIconSize, Path.Direction.CW);
                            break;
                    }
                }
//                if (Math.pow(touchPoint.x - point.x, 2) + Math.pow(touchPoint.y - point.y, 2) <= iconSizeSquared) {
//                    place = p;
//                    break;
//                }
            } else {
                List<Point> pointList = p.areaCoords.get(0) == null ? new ArrayList<>() : p.areaCoords.get(0);
                for (int i = 0; i < pointList.size(); i++) {
                    PointF point = this.getImageToCanvasPoint(pointList.get(i).x , pointList.get(i).y);
                    if (i == 0) path.moveTo(point.x, point.y);
                    else path.lineTo(point.x, point.y);
                }
            }
            path.computeBounds(boundRect, true);
            region.setPath(path, new Region((int) boundRect.left,(int) boundRect.top,(int) boundRect.right,(int) boundRect.bottom));
//                    LogUtil.d(TAG, String.format("%s %s %b", p.name, r.toShortString(), region.contains((int) touchPoint.x, (int) touchPoint.y)));
            if (region.contains((int) touchPoint.x, (int) touchPoint.y)) {
                place = p;
                break;
            }
        }
        if (place != null) return place;
        return null;
    }

    private void chooseItem(float pointX, float pointY) {
        Object element = this.isPointInItem(pointX, pointY);
        if (element != null) {
            // route is not direction
            if (element instanceof GraphicPlace) {
                this.setSelectedPlace((GraphicPlace) element);
            }
        }
    }

    private void setSelectedPlace(GraphicPlace graphicPlace) {
        PlainPlace place = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            place = this.placeList.stream()
                    .filter(p -> p.getId() == graphicPlace.id)
                    .findFirst()
                    .orElse(null);
        } else {
            for (PlainPlace p : placeList) {
                if (p.getId() == graphicPlace.id) {
                    place = p;
                    break;
                }
            }
        }
        if (place != null) {
            LogUtil.d(TAG, "setSelectedPlace: " + place.getName());
            this.mListener.onPlaceSelected(place);
        }
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
        LogUtil.d(TAG, "setMapImage: " + this.resourceImg.getWidth() + " " + this.resourceImg.getHeight());
        this.rotate = getMapRotation(this.resourceImg.getWidth(), this.resourceImg.getHeight());
        this.mListener.onRotateUpdate(this.rotate);
        GraphicPlace.imgWidth = this.resourceImg.getWidth();
        GraphicPlace.imgHeight = this.resourceImg.getHeight();
        GraphicPlace.degree = this.rotate;

        Matrix matrix = new Matrix();
        if (this.rotate != 0) {
            matrix.setRotate(this.rotate, this.resourceImg.getWidth() / 2f, this.resourceImg.getHeight() / 2f);
        }
        Bitmap newImg = Bitmap.createBitmap(this.resourceImg, 0, 0, this.resourceImg.getWidth(), this.resourceImg.getHeight(), matrix, true);

        this.imageMap.put("map", newImg);
        this.mapImage = newImg;
        this.imgWidth = newImg.getWidth();
        this.imgHeight = newImg.getHeight();

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
        GraphicPlace.TextPosition[] positionArr = {GraphicPlace.TextPosition.BOTTOM, GraphicPlace.TextPosition.LEFT, GraphicPlace.TextPosition.RIGHT, GraphicPlace.TextPosition.TOP};
        float finalScaleX = this.transform.scaleX * this.transformAdaption.scaleX;
        float finalScaleY = this.transform.scaleX * this.transformAdaption.scaleY;
        float currentScaleX = this.transform.scaleX;
        float currentScaleY = this.transform.scaleY;
        GridIndex gi = new GridIndex(this.imgWidth * finalScaleX, this.imgHeight * finalScaleY, (int) (30 * Math.floor(this.transform.scaleX)));

        for (GraphicPlace place : this.graphicPlaceList) {
            if (currentScaleX < place.iconLevel || currentScaleY < place.iconLevel) continue;
            PointF canvasPoint = new PointF((float) place.location.x * finalScaleX, (float) place.location.y * finalScaleY);
            this.getImageToCanvasPoint(place.location.x, place.location.y);
            gi.insert(new Pair<>(String.valueOf(place.id), new BCircle(canvasPoint.x, canvasPoint.y, this.halfIconSize)), false);
        }

        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.iconLevel <= 0 || currentScaleX < place.iconLevel || currentScaleY < place.iconLevel) continue;

            PointF canvasPoint = new PointF((float) place.location.x * finalScaleX, (float) place.location.y * finalScaleY);
            float width = place.textWidth;
            float height = place.textHeight;
            float halfWidth = width / 2;
            float halfHeight = height / 2;

            int result = -1;
            for (int i = -1; i < positionArr.length; i++) {
                GraphicPlace.TextPosition position = i >= 0 ? positionArr[i] : place.textPosition;
                if (position == null) continue;
                BBox box = null;
                switch (position) {
                    case BOTTOM:
                        box = new BBox(canvasPoint.x - halfWidth, canvasPoint.y + this.halfIconSize, width, height);
                        break;
                    case LEFT:
                        box = new BBox(canvasPoint.x + this.halfIconSize, canvasPoint.y - halfHeight, width, height);
                        break;
                    case RIGHT:
                        box = new BBox(canvasPoint.x - this.halfIconSize - width, canvasPoint.y - halfHeight, width, height);
                        break;
                    case TOP:
                        box = new BBox(canvasPoint.x - halfWidth, canvasPoint.y - this.halfIconSize - height, width, height);
                        break;
                }
                result = gi.insert(new Pair<>(String.valueOf(place.id), box), true);

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
        for (GraphicPlace place : graphicPlaceList) {
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

    public void setPlaceList(List<PlainPlace> placeList) {
        this.placeList = placeList;
//        for (PlainPlace place : placeList) {
//            LogUtil.d(TAG, place.toString());
//        }

        if (this.imageComplete) {
            this.updatePlaceList();
        }

        this.labelComplete = true;
    }

    public void setMapImage(Bitmap resource) {
        this.resourceImg = resource;

//        this.backgroundColor = resource.getPixel(2, 2);

//        post(new Runnable() {
//            @Override
//            public void run() {
//                setBackgroundColor(resource.getPixel(2, 2));
//            }
//        });

        try {
            this.resetLayout();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.imageComplete = true;
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

        List<GraphicPlace> tempPlaceList = new ArrayList<>();
        for (PlainPlace place : this.placeList) {
            Map<String, Object> typeMap = this.iconSpriteInfo.get(place.getIconType());
            StaticLayout sl;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sl = StaticLayout.Builder.obtain(place.getShortName(), 0, place.getShortName().length(), this.textPaint, textWidth).build();
            } else {
                sl = new StaticLayout(place.getShortName(), this.textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1f, 0f, true);
            }
            GraphicPlace gp = new GraphicPlace(place, (Bitmap) typeMap.get("image"), sl);
            tempPlaceList.add(gp);
        }
        Collections.sort(tempPlaceList);
        this.graphicPlaceList.clear();
        this.graphicPlaceList.addAll(tempPlaceList);

//        for (int i = 0; i < this.graphicPlaceList.size(); i++) {
//            LogUtil.d(TAG, i + " " + this.graphicPlaceList.get(i));
//        }

        this.refreshTextPosition();
        this.mHandler.post(this::refreshIconDisplay);
    }

    public void setLocation(PointF point) {
        if ((point.x >= 0 && point.x <= this.imgWidth) && (point.y >= 0 && point.y <= this.imgHeight)) {
            this.location = point;
        }
    }

    public void setDirection(int direction) {
        this.deviceDirection = direction;
    }

    public void setLocationActivated(boolean flag) {
        this.locationActivated = flag;
    }

    interface OnCanvasDataUpdateListener {
        void onPlaceSelected(PlainPlace place);

        void onRotateUpdate(int rotate);

        void onDisplayVirtualButtonUpdate(boolean display);
    }
}
