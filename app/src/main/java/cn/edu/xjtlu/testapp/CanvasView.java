package cn.edu.xjtlu.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.os.Build;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.xjtlu.testapp.graphic.BBox;
import cn.edu.xjtlu.testapp.graphic.BCircle;
import cn.edu.xjtlu.testapp.graphic.GraphicPlace;
import cn.edu.xjtlu.testapp.domain.PlainPlace;
import cn.edu.xjtlu.testapp.graphic.GridIndex;
import cn.edu.xjtlu.testapp.listener.OnPlaceSelectedListener;
import cn.edu.xjtlu.testapp.util.JsonAssetsReader;
import cn.edu.xjtlu.testapp.util.LogUtil;

public class CanvasView extends SurfaceView implements SurfaceHolder.Callback, Runnable, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = CanvasView.class.getName();

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
    private OnPlaceSelectedListener onPlaceSelectedListener;

    private final Map<String, Map<String, Object>> iconSpriteInfo = new HashMap<>();
    private final Map<String, Map<String, Object>> markerSpriteInfo = new HashMap<>();

    private float canvasWidth;
    private float canvasHeight;
    private float imgWidth;
    private float imgHeight;
    private float scaleAdaption;
    private final PointF positionAdaption = new PointF();
    private final PointF position = new PointF(0, 0);
    private final PointF scale = new PointF(1, 1);
    private final PointF focusedPoint = new PointF(0, 0);
    private int backgroundColor;
    private PlainPlace selectedPlace;
    private PlainPlace fromDirectionMarker;
    private PlainPlace toDirectionMarker;
    private PointF location;
    private float iconSize;
    private float halfIconSize;
    private float locationIconSize;
    private float halfLocationIconSize;
    private MapAnimation mapAnimation;
    private boolean labelComplete;
    private boolean imageComplete;

    private final Map<String, Bitmap> imageMap = new HashMap<>();
    private float clientWidth;
    private float clientHeight;
    private int rotate;
    private List<PlainPlace> placeList;
    private boolean locationActivated;
    private Integer direction;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CanvasView(Context context) {
        super(context);
        this.init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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
            if (!this.labelComplete || !this.imageComplete) continue;
            if ((canvas = this.surfaceHolder.lockCanvas()) != null) {
                if (this.mapAnimation != null) {
                    long t = Math.min(System.currentTimeMillis(), this.mapAnimation.endTime);
                    long lt = this.mapAnimation.currentTime;
                    float deltaT = t - lt;
                    float deltaTranslateX = deltaT / this.mapAnimation.duration * this.mapAnimation.deltaTranslateX;
                    float deltaTranslateY = deltaT / this.mapAnimation.duration * this.mapAnimation.deltaTranslateY;
                    float deltaScale = deltaT / this.mapAnimation.duration * this.mapAnimation.deltaScale;
                    this.manipulateMap(deltaTranslateX, deltaTranslateY, deltaScale);
                    this.mapAnimation.currentTime = t;
                    if (t >= this.mapAnimation.endTime) this.mapAnimation = null;
                }

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
            case MotionEvent.ACTION_MOVE:
                if (this.doubleTapDown) {
                    float distance = event.getY() - this.doubleTapDownMotionEventPoint.y;
                    this.manipulateMap((distance - this.lastDoubleTapMotionEventDistance) / 400);
                    this.lastDoubleTapMotionEventDistance = distance;
                    this.refreshTextPosition();
                }
                break;
        }
        return this.gestureDetector.onTouchEvent(event) || this.scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
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
        this.manipulateMap(-distanceX, -distanceY);
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        this.chooseItem(e.getX(), e.getY());
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
                    this.mapAnimation = new MapAnimation(0, 0, 0.5f, 100);
                }
                break;
        }
        return true;
    }

    @Override
    public boolean onScale(ScaleGestureDetector detector) {
//        LogUtil.d(TAG, "onScale");
        float zoom = detector.getScaleFactor() - 1f;
        PointF focusPoint = this.getTouchPoint(detector.getFocusX(), detector.getFocusY());
        this.focusedPoint.x = focusPoint.x;
        this.focusedPoint.y = focusPoint.y;
        this.manipulateMap(zoom * 2);
        this.refreshTextPosition();
        return true;
    }

    @Override
    public boolean onScaleBegin(ScaleGestureDetector detector) {
        return true;
    }

    @Override
    public void onScaleEnd(ScaleGestureDetector detector) {

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
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

        this.iconSize = getResources().getDimensionPixelSize(R.dimen.icon_size);
        this.halfIconSize = this.iconSize / 2;
        this.locationIconSize = this.iconSize * 1.5f;
        this.halfLocationIconSize = this.iconSize * 1.5f;

        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.text_size));
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setColor(0xff0d6efd);
        this.textPaint.setStrokeWidth(4);
//        this.textPaint.setShadowLayer(1, 0, 0, 0xffffffff);
        this.textPaint.setFakeBoldText(true);

        this.drawPaint.setAntiAlias(true);
        this.drawPaint.setStrokeWidth(6);
        this.drawPaint.setStrokeJoin(Paint.Join.ROUND);
        this.drawPaint.setStrokeCap(Paint.Cap.ROUND);

        new AssetsJsonThread().start();
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

    public void setOnPlaceSelectedListener(OnPlaceSelectedListener listener) {
        this.onPlaceSelectedListener = listener;
    }

    private void drawMapInfo(Canvas canvas) {
        canvas.save();

        this.drawPaint.setColor(this.backgroundColor);
        this.drawPaint.setStyle(Paint.Style.FILL);
        canvas.drawRect(this.boundingClientRect, this.drawPaint);
        this.drawImage(canvas, this.imageMap.get("map"), 0, 0, this.imgWidth, this.imgHeight, 0, 0, false, false);

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
                if (place.iconLevel == 0 || (this.scale.x < place.iconLevel || this.scale.y < place.iconLevel)) continue;
//                this.drawImage(canvas, this.imageMap.get("icon"), (float) place.getLocation().getX(), (float) place.getLocation().getY(), size, size, size/2, size/2, true, true,
//                        (iconSpriteInfo.get(place.iconType).get("column") - 1) * iconSpriteInfo.get(place.iconType).get("width"), (iconSpriteInfo.get(place.iconType).get("row") - 1) * iconSpriteInfo.get(place.iconType).get("height"), iconSpriteInfo.get(place.iconType).get("width"), iconSpriteInfo.get(place.iconType).get("height"));
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
                    place.staticLayout.draw(canvas);
                    this.textPaint.setColor(0xff0d6efd);
                    this.textPaint.setStyle(Paint.Style.FILL);
                    place.staticLayout.draw(canvas);
                    canvas.restore();
                }
            }
        }

        if (this.locationActivated && this.location != null) {
            if (this.direction != null) {
                this.drawImage(canvas, this.imageMap.get("locationProbe"), this.location.x, this.location.y, this.locationIconSize, this.locationIconSize, this.locationIconSize / 2f, this.locationIconSize / 2f, true, false, this.direction);
            }
            this.drawImage(canvas, this.imageMap.get("locationMarker"), this.location.x, this.location.y, this.locationIconSize, this.locationIconSize, this.locationIconSize / 2f, this.locationIconSize / 2f, true, false);
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

        float scaleX = this.scale.x * this.scaleAdaption;
        float scaleY = this.scale.y * this.scaleAdaption;
        Rect sRect = (sx != null && sy != null && sWidth != null && sHeight != null) ? new Rect(sx, sy, sx + sWidth, sy + sHeight) : null;
        RectF dRect;
        if (!fixSize) {
            PointF canvasPoint = this.getImageToCanvasPoint(x - imgOffsetX, y - imgOffsetY);
            float dx = Math.round(canvasPoint.x);
            float dy = Math.round(canvasPoint.y);
            dRect = new RectF(dx, dy, dx + sizeX * scaleX, dy + sizeY * scaleY);
        } else {
            PointF canvasPoint = this.getImageToCanvasPoint(x, y);
            float dx = Math.round(canvasPoint.x - imgOffsetX);
            float dy = Math.round(canvasPoint.y - imgOffsetY);
            dRect = new RectF(dx, dy, dx + sizeX, dy + sizeY);
        }
        canvas.drawBitmap(image, sRect, dRect, null);
    }

    private PointF getImageToCanvasPoint(float x, float y) {
        return new PointF(x * this.scale.x * this.scaleAdaption + this.position.x + this.positionAdaption.x, y * this.scale.y * this.scaleAdaption + this.position.y + this.positionAdaption.y);
    }

    private PointF getCanvasToImagePoint(float x, float y) {
        return new PointF((x - this.positionAdaption.x - this.position.x) / (this.scale.x * this.scaleAdaption), (y - this.positionAdaption.y - this.position.y) / (this.scale.y * this.scaleAdaption));
    }

    private PointF getTouchPoint (float x, float y) {
        float px = x - this.boundingClientRect.left;
        float py = y - this.boundingClientRect.top;
        return new PointF(px, py);
    }

    private void validateScale(float newScale) {
        newScale = (float) Math.ceil(newScale * 10000) / 10000;

        if (newScale > 6) {
            newScale = 6;
        } else if (newScale < 1) {
            newScale = 1;
        }

        if (this.scale.x != newScale && this.scale.x == this.scale.y) {
            this.scale.x = newScale;
            this.scale.y = newScale;
        }
    }

    private void validatePosition(float newPosX, float newPosY) {
        // edges cases
        float currentWidth = this.imgWidth * this.scaleAdaption * this.scale.x;
        float currentHeight = this.imgHeight * this.scaleAdaption * this.scale.y;

        if (newPosX + currentWidth + this.positionAdaption.x < this.canvasWidth - this.positionAdaption.x) {
            newPosX = this.canvasWidth - 2 * this.positionAdaption.x - currentWidth;
        }
        if (newPosX > 0) {
            newPosX = 0;
        }

        if (newPosY + currentHeight + this.positionAdaption.y < this.canvasHeight - this.positionAdaption.y) {
            newPosY = this.canvasHeight - 2 * this.positionAdaption.y - currentHeight;
        }
        if (newPosY > 0) {
            newPosY = 0;
        }

        if (this.position.x != newPosX) {
            this.position.x = newPosX;
        }
        if (this.position.y != newPosY) {
            this.position.y = newPosY;
        }
    }

    private void manipulateMap(float deltaScale) {
        manipulateMap(0, 0, deltaScale);
    }

    private void manipulateMap(float deltaX, float deltaY) {
        manipulateMap(deltaX, deltaY, 0);
    }

    private void manipulateMap(float deltaX, float deltaY, float deltaScale) {
        float oldScale = this.scale.x;
        float newScale = this.scale.x + deltaScale;
        this.validateScale(newScale);

        float newPosX = oldScale == this.scale.x ? this.position.x : (this.focusedPoint.x - this.positionAdaption.x - (this.focusedPoint.x - this.positionAdaption.x - this.position.x) * this.scale.x / oldScale);
        float newPosY = oldScale == this.scale.y ? this.position.y : (this.focusedPoint.y - this.positionAdaption.y - (this.focusedPoint.y - this.positionAdaption.y - this.position.y) * this.scale.y / oldScale);
        newPosX += deltaX;
        newPosY += deltaY;
        this.validatePosition(newPosX, newPosY);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private Object isPointInItem(float pointX, float pointY) {
        // click on places
        PointF touchPoint = this.getTouchPoint(pointX, pointY);
        int iconSizeSquared = (int) (this.iconSize * this.iconSize);
        RectF boundRect = new RectF();
        Object place = this.graphicPlaceList.stream()
                .filter(p -> {
                    if (this.selectedPlace != null && p.id == this.selectedPlace.getId()) return false;
                    Region region = new Region();
                    Path path = new Path();
                    if (p.areaCoords == null) {
                        if (p.iconLevel <= 0 || (this.scale.x < p.iconLevel || this.scale.y < p.iconLevel)) return false;
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
//                        return Math.pow(touchPoint.x - point.x, 2) + Math.pow(touchPoint.y - point.y, 2) <= iconSizeSquared;
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
                    return region.contains((int) touchPoint.x, (int) touchPoint.y);
                })
                .findFirst()
                .orElse(null);
        if (place != null) return place;
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void chooseItem(float pointX, float pointY) {
        Object element = this.isPointInItem(pointX, pointY);
        if (element != null) {
            // route is not direction
            if (element instanceof GraphicPlace) {
                this.setSelectedPlace((GraphicPlace) element);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void setSelectedPlace(GraphicPlace graphicPlace) {
        PlainPlace place = this.placeList.stream()
                .filter(p -> p.getId() == graphicPlace.id)
                .findFirst()
                .orElse(null);
        if (place != null) {
            LogUtil.d(TAG, "setSelectedPlace: " + place.getName());
            this.onPlaceSelectedListener.onPlaceSelected(place);
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

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void resetLayout() throws Exception {
        LogUtil.d(TAG, "setMapImage: " + this.resourceImg.getWidth() + " " + this.resourceImg.getHeight());
        this.rotate = getMapRotation(this.resourceImg.getWidth(), this.resourceImg.getHeight());
        GraphicPlace.imgWidth = this.resourceImg.getWidth();
        GraphicPlace.imgHeight = this.resourceImg.getHeight();
        GraphicPlace.degree = this.rotate;

        Matrix matrix = new Matrix();
        if (this.rotate != 0) {
            matrix.setRotate(this.rotate, this.resourceImg.getWidth() / 2f, this.resourceImg.getHeight() / 2f);
        }
        Bitmap newImg = Bitmap.createBitmap(this.resourceImg, 0, 0, this.resourceImg.getWidth(), this.resourceImg.getHeight(), matrix, true);

        this.imageMap.put("map", newImg);
        this.imgWidth = newImg.getWidth();
        this.imgHeight = newImg.getHeight();

        this.canvasWidth = this.clientWidth;
        this.canvasHeight = this.clientHeight;

        this.scaleAdaption = Math.min(this.canvasWidth / this.imgWidth, this.canvasHeight / this.imgHeight);

        this.positionAdaption.x = Math.round((this.canvasWidth - this.imgWidth * this.scaleAdaption) / 2);
        this.positionAdaption.y = Math.round((this.canvasHeight - this.imgHeight * this.scaleAdaption) / 2);

        if (this.labelComplete) {
            this.updatePlaceList();
        }
    }

    private void refreshTextPosition() {
        GraphicPlace.TextPosition[] positionArr = {GraphicPlace.TextPosition.BOTTOM, GraphicPlace.TextPosition.LEFT, GraphicPlace.TextPosition.RIGHT, GraphicPlace.TextPosition.TOP};
        float finalScale = this.scale.x * this.scaleAdaption;
        float currentScale = this.scale.x;
        GridIndex gi = new GridIndex(this.imgWidth * finalScale, this.imgHeight * finalScale, (int) (30 * Math.floor(this.scale.x)));

        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.iconLevel > currentScale) continue;
            PointF canvasPoint = new PointF((float) place.location.x * finalScale, (float) place.location.y * finalScale);
                    this.getImageToCanvasPoint(place.location.x, place.location.y);
            gi.insert(new Pair<>(String.valueOf(place.id), new BCircle(canvasPoint.x, canvasPoint.y, this.halfIconSize)), false);
        }

        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.iconLevel <= 0 || place.iconLevel > currentScale) continue;

            PointF canvasPoint = new PointF((float) place.location.x * finalScale, (float) place.location.y * finalScale);
            float width = place.textWidth;
            float height = place.textHeight;
            float halfWidth = width / 2;
            float halfHeight = height / 2;

            int result = -1;
            for (GraphicPlace.TextPosition position : positionArr) {
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

    @RequiresApi(api = Build.VERSION_CODES.N)
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

    @RequiresApi(api = Build.VERSION_CODES.N)
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
    public void setBackgroundColor(int backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updatePlaceList() {
        int textWidth = getResources().getDimensionPixelSize(R.dimen.text_width);

        List<GraphicPlace> tempPlaceList = new ArrayList<>();
        for (PlainPlace place : this.placeList) {
            Map<String, Object> typeMap = this.iconSpriteInfo.get(place.getIconType());
            GraphicPlace gp = new GraphicPlace(place, (Bitmap) typeMap.get("image"), StaticLayout.Builder.obtain(place.getShortName(), 0, place.getShortName().length(), this.textPaint, textWidth).build());
            tempPlaceList.add(gp);
        }
//        tempPlaceList.sort((o1, o2) -> Float.compare(o1.iconLevel, o2.iconLevel));
        Collections.sort(tempPlaceList);
        this.graphicPlaceList.clear();
        this.graphicPlaceList.addAll(tempPlaceList);

//        for (GraphicPlace place : this.graphicPlaceList) {
//            LogUtil.d(TAG, place.toString());
//        }

        this.refreshTextPosition();
    }

    public void setLocation(PointF point) {
        if ((point.x >= 0 && point.x <= this.imgWidth) && (point.y >= 0 && point.y <= this.imgHeight)) {
            this.location = point;
        }
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public void setLocationActivated(boolean flag) {
        this.locationActivated = flag;
    }

    class MapAnimation {
        public final int duration;
        public final long startTime;
        public final long endTime;
        public final float deltaTranslateX;
        public final float deltaTranslateY;
        public final float deltaScale;
        public long currentTime;

        public MapAnimation(float deltaTranslateX, float deltaTranslateY, float deltaScale, int duration) {
            this.duration = duration;
            this.startTime = System.currentTimeMillis();
            this.endTime = this.startTime + this.duration;
            this.deltaTranslateX = deltaTranslateX;
            this.deltaTranslateY = deltaTranslateY;
            this.deltaScale = deltaScale;
            this.currentTime = this.startTime;
        }
    }

    class AssetsJsonThread extends Thread {
        @Override
        public void run() {
            super.run();
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
    }
}
