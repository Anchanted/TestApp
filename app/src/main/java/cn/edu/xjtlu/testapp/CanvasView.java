package cn.edu.xjtlu.testapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Pair;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.Request;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.SizeReadyCallback;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.edu.xjtlu.testapp.api.Api;
import cn.edu.xjtlu.testapp.api.Result;
import cn.edu.xjtlu.testapp.graphic.BBox;
import cn.edu.xjtlu.testapp.graphic.BCircle;
import cn.edu.xjtlu.testapp.graphic.GraphicPlace;
import cn.edu.xjtlu.testapp.bean.PlainPlace;
import cn.edu.xjtlu.testapp.graphic.GridIndex;
import cn.edu.xjtlu.testapp.util.AESUtil;
import cn.edu.xjtlu.testapp.util.JsonAssetsReader;
import cn.edu.xjtlu.testapp.util.LoadingUtil;
import cn.edu.xjtlu.testapp.util.UnitConverter;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import okhttp3.HttpUrl;
import okhttp3.Response;
import retrofit2.HttpException;

public class CanvasView extends SurfaceView implements SurfaceHolder.Callback, Runnable, GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener, ScaleGestureDetector.OnScaleGestureListener {
    private static final String TAG = CanvasView.class.getName();

    private SurfaceHolder surfaceHolder;
    private boolean doDrawing;
    private final Rect boundingClientRect = new Rect();
    private GestureDetector gestureDetector;
    private ScaleGestureDetector scaleGestureDetector;
    private boolean doubleTapDown;
    private boolean doubleTapMove;
    private PointF doubleTapDownMotionEventPoint;
    private float lastDoubleTapMotionEventDistance;
    private final TextPaint textPaint = new TextPaint();
    private final List<GraphicPlace> graphicPlaceList = new ArrayList<>();

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
    private PlainPlace selectedPlace;
    private PlainPlace fromDirectionMarker;
    private PlainPlace toDirectionMarker;
    private int iconSize;
    private MapAnimation mapAnimation;
    private boolean labelComplete;
    private boolean imageComplete;

    private final Map<String, Bitmap> imageMap = new HashMap<>();
    private float clientWidth;
    private float clientHeight;
    private int rotate;
    private List<PlainPlace> placeList;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CanvasView(Context context) {
        super(context);
        this.init();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public CanvasView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        this.clientWidth = getWidth();
        this.clientHeight = getHeight();
        Log.d(TAG, getWidth() + " " + getHeight());

        this.boundingClientRect.left = getLeft();
        this.boundingClientRect.top = getTop();
        this.boundingClientRect.right = getRight();
        this.boundingClientRect.bottom = getBottom();

//        this.resizeWindow();
    }

//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//
//        if (!this.init) {
//            float scaleRatio = this.canvasWidth > this.canvasHeight ? this.scale.x : this.scale.y;
//            this.scale.x = scaleRatio;
//            this.scale.y = scaleRatio;
//            this.init = true;
//        }
//
//        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//
//        this.drawMapInfo(canvas);
//    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        this.doDrawing = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        this.doDrawing = false;
    }

    @Override
    public void run() {
        while (this.doDrawing) {
            if (!this.labelComplete || !this.imageComplete) continue;
            Canvas canvas;
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
//        Log.d(TAG, "onTouchEvent");
//        Log.d(TAG, "onTouchEvent " + event.getAction());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "onTouchEvent ACTION_MOVE");
                if (this.doubleTapDown) {
                    float distance = event.getY() - this.doubleTapDownMotionEventPoint.y;
                    this.manipulateMap((distance - this.lastDoubleTapMotionEventDistance) / 400);
                    this.lastDoubleTapMotionEventDistance = distance;
                }
                break;
        }
        return this.gestureDetector.onTouchEvent(event) || this.scaleGestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    @Override
    public boolean onDown(MotionEvent e) {
//        Log.d(TAG, "onDown");
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

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        Log.d(TAG, "onSingleTapConfirmed");
        return true;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
//        Log.d(TAG, "onDoubleTap");
        return true;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
//        Log.d(TAG, "onDoubleTapEvent");
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
//                Log.d(TAG, "onDoubleTapEvent ACTION_DOWN");
                this.doubleTapDown = true;
                this.doubleTapMove = false;
                this.doubleTapDownMotionEventPoint = new PointF(e.getX(), e.getY());
                this.lastDoubleTapMotionEventDistance = 0;

                PointF focusedPoint = this.getTouchPoint(e.getX(), e.getY(), true);
                this.focusedPoint.x = focusedPoint.x;
                this.focusedPoint.y = focusedPoint.y;
                break;
            case MotionEvent.ACTION_MOVE:
//                Log.d(TAG, "onDoubleTapEvent ACTION_MOVE");
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
//        Log.d(TAG, "onScale");
        float zoom = detector.getScaleFactor() - 1f;
        PointF focusPoint = this.getTouchPoint(detector.getFocusX(), detector.getFocusY(), true);
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
    private void init() {
        this.surfaceHolder = getHolder();
        this.surfaceHolder.addCallback(this);
        // 画布透明处理
        setZOrderOnTop(true);
        this.surfaceHolder.setFormat(PixelFormat.TRANSLUCENT);

//        setFocusable(true);
//        setKeepScreenOn(true);
//        setFocusableInTouchMode(true);

        this.gestureDetector = new GestureDetector(getContext(), this);
        this.gestureDetector.setOnDoubleTapListener(this);
        this.scaleGestureDetector = new ScaleGestureDetector(getContext(), this);

        float dpi = getResources().getDisplayMetrics().density;
        Log.d(TAG, "dpi: " + dpi);

        this.imageMap.put("icon", BitmapFactory.decodeResource(getResources(), R.drawable.icon_sprite));

        this.iconSize = Math.round(UnitConverter.dp2px(getContext(), 40));

        this.textPaint.setAntiAlias(true);
        this.textPaint.setTextSize(Math.round(UnitConverter.dp2px(getContext(), 20)));
        this.textPaint.setTextAlign(Paint.Align.CENTER);
        this.textPaint.setStyle(Paint.Style.FILL);
        this.textPaint.setColor(0xff0d6efd);
        this.textPaint.setStrokeWidth(4);
//        this.textPaint.setShadowLayer(1, 0, 0, 0xffffffff);
        this.textPaint.setFakeBoldText(true);

        new AssetsJsonThread().start();
    }

    private void drawMapInfo(Canvas canvas) {
        canvas.save();
        if (this.rotate != 0) {
            canvas.translate(this.canvasHeight, 0);
            canvas.rotate(90);
        }

        this.drawImage(canvas, this.imageMap.get("map"), 0, 0, this.imgWidth, this.imgHeight, 0, 0, false, false);

        if (this.graphicPlaceList != null && this.graphicPlaceList.size() > 0) {
            for (GraphicPlace place : this.graphicPlaceList) {
                // selected place
                if (this.selectedPlace != null && place.id == this.selectedPlace.getId()) continue;
                // direction markers
                if (this.fromDirectionMarker != null && place.id == this.fromDirectionMarker.getId()) continue;
                if (this.toDirectionMarker != null && place.id == this.toDirectionMarker.getId()) continue;
                // place not to display
                if (place.iconLevel == 0 || (this.scale.x < place.iconLevel || this.scale.y < place.iconLevel)) continue;
                int size = this.iconSize;
//                this.drawImage(canvas, this.imageMap.get("icon"), (float) place.getLocation().getX(), (float) place.getLocation().getY(), size, size, size/2, size/2, true, true,
//                        (iconSpriteInfo.get(place.iconType).get("column") - 1) * iconSpriteInfo.get(place.iconType).get("width"), (iconSpriteInfo.get(place.iconType).get("row") - 1) * iconSpriteInfo.get(place.iconType).get("height"), iconSpriteInfo.get(place.iconType).get("width"), iconSpriteInfo.get(place.iconType).get("height"));
                this.drawImage(canvas, place.iconImg, (float) place.location.x, (float) place.location.y, size, size, size/2, size/2, true, true);

                if (place.displayName && place.textPosition != null) {
                    PointF canvasPoint = this.getImageToCanvasPoint(new PointF((float) place.location.x, (float) place.location.y));
    //                canvas.drawText(place.getShortName(), canvasPoint.x + this.iconSize / 2, canvasPoint.y, this.strokeTextPaint);
    //                canvas.drawText(place.getShortName(), canvasPoint.x + this.iconSize / 2, canvasPoint.y, this.fillTextPaint);
                    canvas.save();
                    switch (place.textPosition) {
                        case BOTTOM:
                            canvas.translate(canvasPoint.x, canvasPoint.y + this.iconSize / 2f);
                            break;
                        case LEFT:
                            canvas.translate(canvasPoint.x + this.iconSize / 2f, canvasPoint.y - place.staticLayout.getHeight() / 2f);
                            break;
                        case RIGHT:
                            canvas.translate(canvasPoint.x - this.iconSize / 2f, canvasPoint.y - place.staticLayout.getHeight() / 2f);
                            break;
                        case TOP:
                            canvas.translate(canvasPoint.x, canvasPoint.y - this.iconSize / 2f - place.staticLayout.getHeight() / 2f);
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
            PointF tPoint = this.getImageToCanvasPoint(new PointF(x, y));
            canvas.save();
            canvas.translate(tPoint.x, tPoint.y);
            canvas.rotate(degree);
            canvas.translate(-tPoint.x, -tPoint.y);
            canvas.translate(translateX, translateY);
        }

        float scaleX = this.scale.x * this.scaleAdaption;
        float scaleY = this.scale.y * this.scaleAdaption;
        Rect sRect = (sx != null && sy != null && sWidth != null && sHeight != null) ? new Rect(sx, sy, sx + sWidth, sy + sHeight) : null;
        RectF dRect;
        if (this.rotate != 0 && selfRotate) {
            canvas.restore();
            if (!fixSize) {
                PointF canvasPoint = this.getImageToCanvasPoint(new PointF(x - imgOffsetY, y + imgOffsetX));
                float dx = Math.round(this.canvasHeight - canvasPoint.y);
                float dy = Math.round(canvasPoint.x);
                dRect = new RectF(dx, dy, dx + sizeX * scaleY, dy + sizeY * scaleX);
            } else {
                PointF canvasPoint = this.getImageToCanvasPoint(new PointF(x, y));
                float dx = Math.round(this.canvasHeight - (canvasPoint.y + imgOffsetX));
                float dy = Math.round(canvasPoint.x - imgOffsetY);
                dRect = new RectF(dx, dy, dx + sizeX, dy + sizeY);
            }
            canvas.drawBitmap(image, sRect, dRect, null);
            canvas.save();
            canvas.translate(this.canvasHeight, 0);
            canvas.rotate(90);
        } else {
            if (!fixSize) {
                PointF canvasPoint = this.getImageToCanvasPoint(new PointF(x - imgOffsetX, y - imgOffsetY));
                float dx = Math.round(canvasPoint.x);
                float dy = Math.round(canvasPoint.y);
                dRect = new RectF(dx, dy, dx + sizeX * scaleX, dy + sizeY * scaleY);
            } else {
                PointF canvasPoint = this.getImageToCanvasPoint(new PointF(x, y));
                float dx = Math.round(canvasPoint.x - imgOffsetX);
                float dy = Math.round(canvasPoint.y - imgOffsetY);
                dRect = new RectF(dx, dy, dx + sizeX, dy + sizeY);
            }
            canvas.drawBitmap(image, sRect, dRect, null);
        }

        if (degree != null) {
            canvas.restore();
        }
    }

    private PointF getImageToCanvasPoint(PointF point) {
        return new PointF(point.x * this.scale.x * this.scaleAdaption + this.position.x + this.positionAdaption.x, point.y * this.scale.y * this.scaleAdaption + this.position.y + this.positionAdaption.y);
    }

    private PointF getCanvasToImagePoint(PointF point) {
        return new PointF((point.x - this.positionAdaption.x - this.position.x) / (this.scale.x * this.scaleAdaption), (point.y - this.positionAdaption.y - this.position.y) / (this.scale.y * this.scaleAdaption));
    }

    private PointF getTouchPoint (float x, float y, boolean followRotation) {
        float px = (this.rotate == 0 || !followRotation) ? x - this.boundingClientRect.left : y - this.boundingClientRect.top;
        float py = (this.rotate == 0 || !followRotation) ? y - this.boundingClientRect.top : this.boundingClientRect.right - 2 - x;
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

    private void resetMapFactors() {
//        float clientWidth = this.clientWidth;
//        float clientHeight = this.clientHeight;
//
//        if (this.imgWidth <= this.imgHeight) {
//            if (clientWidth <= clientHeight) {
//                // img: portrait  screen: portrait
//                this.canvasWidth = clientWidth;
//                this.canvasHeight = clientHeight;
//                this.rotate = 0;
//            } else {
//                // img: portrait  screen: landscape
//                this.canvasWidth = clientHeight;
//                this.canvasHeight = clientWidth;
//                this.rotate = -90;
//            }
//        } else {
//            if (clientWidth >= clientHeight) {
//                // img: landscape  screen: landscape
//                this.canvasWidth = clientWidth;
//                this.canvasHeight = clientHeight;
//                this.rotate = 0;
//            } else { // clientWidth < clientHeight
//                //img: landscape  screen: portrait
//                this.canvasWidth = clientHeight;
//                this.canvasHeight = clientWidth;
//                this.rotate = 90;
//            }
//        }
        this.canvasWidth = this.rotate == 0 ? this.clientWidth : this.clientHeight;
        this.canvasHeight = this.rotate == 0 ? this.clientHeight : this.clientWidth;

        this.scaleAdaption = Math.min(this.canvasWidth / this.imgWidth, this.canvasHeight / this.imgHeight);

        this.positionAdaption.x = Math.round((this.canvasWidth - this.imgWidth * this.scaleAdaption) / 2);
        this.positionAdaption.y = Math.round((this.canvasHeight - this.imgHeight * this.scaleAdaption) / 2);
    }

    private void refreshTextPosition() {
        GraphicPlace.TextPosition[] positionArr = {GraphicPlace.TextPosition.BOTTOM, GraphicPlace.TextPosition.LEFT, GraphicPlace.TextPosition.RIGHT, GraphicPlace.TextPosition.TOP};
        float finalScale = this.scale.x * this.scaleAdaption;
        float currentScale = this.scale.x;
        float halfIconSize = this.iconSize / 2f;
        GridIndex gi = new GridIndex(this.imgWidth * finalScale, this.imgHeight * finalScale, (int) (30 * Math.floor(this.scale.x)));
//        Log.d(TAG, String.format("%f %f", this.imgWidth * finalScale, this.imgHeight * finalScale));

        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.iconLevel > currentScale) continue;
            PointF canvasPoint = new PointF((float) place.location.x * finalScale, (float) place.location.y * finalScale);
                    this.getImageToCanvasPoint(new PointF((float) place.location.x, (float) place.location.y));
            gi.insert(new Pair<>(String.valueOf(place.id), new BCircle(canvasPoint.x, canvasPoint.y, halfIconSize)), false);
        }

        for (GraphicPlace place : this.graphicPlaceList) {
            if (place.iconLevel > currentScale) continue;

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
                        box = new BBox(canvasPoint.x - halfWidth, canvasPoint.y + halfIconSize, width, height);
                        break;
                    case LEFT:
                        box = new BBox(canvasPoint.x + halfIconSize, canvasPoint.y - halfHeight, width, height);
                        break;
                    case RIGHT:
                        box = new BBox(canvasPoint.x - halfIconSize - width, canvasPoint.y - halfHeight, width, height);
                        break;
                    case TOP:
                        box = new BBox(canvasPoint.x - halfWidth, canvasPoint.y - halfIconSize - height, width, height);
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
        for (PlainPlace place : placeList) {
            Log.d(TAG, place.toString());
        }

        if (this.imageComplete) {
            this.updatePlaceList();
        }

        this.labelComplete = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setMapImage(Bitmap resource) throws Exception {
//        this.imageMap.put("map", resource);
//        this.imgWidth = this.imageMap.get("map").getWidth();
//        this.imgHeight = this.imageMap.get("map").getHeight();
        this.rotate = getMapRotation(resource.getWidth(), resource.getHeight());

        Matrix matrix = new Matrix();
        if (this.rotate != 0) {
            matrix.setRotate(this.rotate, resource.getWidth() / 2f, resource.getHeight() / 2f);
        }
        Bitmap newImg = Bitmap.createBitmap(resource, 0, 0, resource.getWidth(), resource.getHeight(), matrix, true);

        this.imageMap.put("map", newImg);
        this.imgWidth = newImg.getWidth();
        this.imgHeight = newImg.getHeight();

//        this.resetMapFactors();
        this.canvasWidth = this.clientWidth;
        this.canvasHeight = this.clientHeight;
        this.scaleAdaption = Math.min(this.canvasWidth / this.imgWidth, this.canvasHeight / this.imgHeight);
        this.positionAdaption.x = Math.round((this.canvasWidth - this.imgWidth * this.scaleAdaption) / 2);
        this.positionAdaption.y = Math.round((this.canvasHeight - this.imgHeight * this.scaleAdaption) / 2);

        if (this.labelComplete) {
            this.updatePlaceList();
        }

        this.imageComplete = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void updatePlaceList() {
        int rotate = this.rotate;
        Log.d(TAG, "updatePlaceList: " + rotate);

        int textWidth = Math.round(UnitConverter.dp2px(getContext(), 200));

        List<GraphicPlace> tempPlaceList = new ArrayList<>();
        for (PlainPlace place : placeList) {
            if (place.getIconLevel() <= 0) continue;
            Map<String, Object> typeMap = iconSpriteInfo.get(place.getIconType());
            // Addresses of Bitmap in same icon are different
            GraphicPlace gp = new GraphicPlace(place, (Bitmap) typeMap.get("image"), StaticLayout.Builder.obtain(place.getShortName(), 0, place.getShortName().length(), textPaint, textWidth).build());
            if (rotate == 90) {
                double oldX = gp.location.x;
                double oldY = gp.location.y;
                gp.location.x = this.imgWidth - oldY;
                gp.location.y = oldX;
            } else if (rotate == -90) {
                double oldX = gp.location.x;
                double oldY = gp.location.y;
                gp.location.x = oldY;
                gp.location.y = this.imgHeight - oldX;
            }
            tempPlaceList.add(gp);
        }
        tempPlaceList.sort((o1, o2) -> Float.compare(o1.iconLevel, o2.iconLevel));
        graphicPlaceList.addAll(tempPlaceList);

        this.rotate = 0;

        refreshTextPosition();
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
                Map<String, Object> map = mapper.readValue(JsonAssetsReader.getJsonString("json/iconSpriteInfo.json", getContext()), Map.class);
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
