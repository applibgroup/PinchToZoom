package com.bogdwellers.pinchtozoom;

import ohos.agp.animation.Animator.CurveType;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.components.Component;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.Point;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;
import com.bogdwellers.pinchtozoom.animation.FlingAnimatorHandler;
import com.bogdwellers.pinchtozoom.animation.ScaleAnimatorHandler;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.bogdwellers.pinchtozoom.util.MyValueAnimator;
import com.github.chrisbanes.photoview.LogUtil;
import com.github.chrisbanes.photoview.PhotoView;
import com.github.chrisbanes.photoview.gesture.GestureDetector;

/**
 * The ImageMatrixTouchHandler enables pinch-zoom, pinch-rotate and dragging on an ImageView.
 * Registering an instance of this class to an ImageView is the only thing you need to do.
 *
 * @author Martin
 *
 */
public class ImageMatrixTouchHandler extends MultiTouchListener {

    public static final int NONE = 0;
    public static final int DRAG = 1;
    public static final int PINCH = 2;
    public static final int MORPH = 4;
    private static final float MIN_PINCH_DIST_PIXELS = 10f;
    public static final String TAG = ImageMatrixTouchHandler.class.getSimpleName();

    private ImageMatrixCorrector corrector;
    private Matrix savedMatrix;
    private int mode;

    public float getPinchVelocity() {
        return pinchVelocity;
    }

    public float getDoubleTapZoomOutFactor() {
        return doubleTapZoomOutFactor;
    }

    public long getDoubleTapZoomDuration() {
        return doubleTapZoomDuration;
    }

    public long getFlingDuration() {
        return flingDuration;
    }

    public long getZoomReleaseDuration() {
        return zoomReleaseDuration;
    }

    public long getPinchVelocityWindow() {
        return pinchVelocityWindow;
    }

    public float getDoubleTapZoomFactor() {
        return doubleTapZoomFactor;
    }

    public float getFlingExaggeration() {
        return flingExaggeration;
    }

    public float getZoomReleaseExaggeration() {
        return zoomReleaseExaggeration;
    }

    private Point startMid;
    private Point mid;
    private float startSpacing;
    private float startAngle;
    private float pinchVelocity;
    private boolean rotateEnabled;
    private boolean scaleEnabled;
    private boolean translateEnabled;
    private boolean dragOnPinchEnabled;
    private long doubleTapZoomDuration;
    private long flingDuration;
    private long zoomReleaseDuration;
    private long pinchVelocityWindow;
    private float doubleTapZoomFactor;
    private float doubleTapZoomOutFactor;
    private float flingExaggeration;
    private float zoomReleaseExaggeration;
    private boolean updateTouchState;

    private GestureDetector gestureDetector;
    private AnimatorValue valueAnimator;

    /**
     * ImageTouchHandler.
     *
      * @param context - context.
     */
    public ImageMatrixTouchHandler(Context context) {
        this(context, new ImageViewerCorrector());

    }

    /**
     * ImageMatrixTouchHandler.
     *
     * @param context - context.
     * @param corrector - corrector.
     */
    public ImageMatrixTouchHandler(Context context, ImageMatrixCorrector corrector) {
        
        this.corrector = corrector;
        this.savedMatrix = new Matrix();
        this.mode = NONE;
        this.startMid = new Point();
        this.mid = new Point();
        this.startSpacing = 1f;
        this.startAngle = 0f;
        this.rotateEnabled = false;
        this.scaleEnabled = true;
        this.translateEnabled = true;
        this.dragOnPinchEnabled = true;
        this.pinchVelocityWindow = 100;
        this.doubleTapZoomDuration = 200;
        this.flingDuration = 200;
        this.zoomReleaseDuration = 200;
        this.zoomReleaseExaggeration = 1.337f;
        this.flingExaggeration = 0.1337f;
        this.doubleTapZoomFactor = 2.5f;
        this.doubleTapZoomOutFactor = 1.4f;
        ImageGestureListener imageGestureListener = new ImageGestureListener();
        this.gestureDetector = new com.github.chrisbanes.photoview.gesture.GestureDetector(context, 
                imageGestureListener, new EventHandler(EventRunner.getMainEventRunner()));
        this.gestureDetector.setOnDoubleTapListener(imageGestureListener);

    }



    
    /*
     * Class methods
     */
    
    /**
     * <p>Returns the mode the handler is currently in.</p> 
     *
     * @return - return.
     */
    public int getMode() {
        return mode;
    }

    /**
     * <p>Returns the <code>ImageMatrixCorrector</code> that corrects the image matrix when altered.</p>
     *
     * @return - return.
     */
    public ImageMatrixCorrector getImageMatrixCorrector() {
        return corrector;
    }

    /**
     * <p>Updates the touch state during a touch event. That is, when touch mode is not {@link #NONE} .</p>
     * <p>Use this when the image in the <code>ImageView</code> or its matrix has been changed.</p>
     */
    public void updateTouchState() {
        updateTouchState = true;
    }

    /**
     * <p>Indicates whether rotation is enabled.</p>
     *
     * @return - return.
     */
    public boolean isRotateEnabled() {
        return this.rotateEnabled;
    }

    /**
     * <p>Sets whether rotation is enabled.</p>
     *
     * @param rotateEnabled - rotateEnabled.
     */
    public void setRotateEnabled(boolean rotateEnabled) {
        this.rotateEnabled = rotateEnabled;
    }

    /**
     * <p>Indicates whether scaling is enabled.</p>
     *
     * @return - return.
     */
    public boolean isScaleEnabled() {
        return scaleEnabled;
    }

    /**
     * <p>Sets whether scaling is enabled.</p>
     *
     * @param scaleEnabled - scaleEnabled.
     */
    public void setScaleEnabled(boolean scaleEnabled) {
        this.scaleEnabled = scaleEnabled;
    }

    /**
     * <p>Indicates whether translation is enabled.</p>
     *
     * @return - return.
     */
    public boolean isTranslateEnabled() {
        return translateEnabled;
    }

    /**
     * <p>Sets whether translation is enabled.</p>
     *
     * @param translateEnabled - translateEnabled. 
     */
    public void setTranslateEnabled(boolean translateEnabled) {
        this.translateEnabled = translateEnabled;
    }

    /**
     * <p>Indicates whether drag-on-pinch is enabled.</p>
     *
     * @return - return.
     */
    public boolean isDragOnPinchEnabled() {
        return dragOnPinchEnabled;
    }

    /**
     * <p>Sets whether drag-on-pinch is enabled.</p>
     *
     * @param dragOnPinchEnabled - dragOnPinchEnabled.
     */
    public void setDragOnPinchEnabled(boolean dragOnPinchEnabled) {
        this.dragOnPinchEnabled = dragOnPinchEnabled;
    }

    /**
     * <p>Sets the pinch velocity window in milliseconds for determining the pinch velocity.</p>
     * <p><b>Note:</b> Only touch events in this temporal window are used to calculate pinch velocity.</p>
     *
     * @param pinchVelocityWindow - PinchVelocityWindow.
     */
    public void setPinchVelocityWindow(long pinchVelocityWindow) {
        this.pinchVelocityWindow = pinchVelocityWindow;
    }

    /**
     * <p>Sets the double tap zoom animation duration. 
     * Setting the duration to <code>0</code> disables the animation altogether.</p>
     *
     * @param doubleTapZoomDuration - doubleTapZoomDuration. 
     */
    public void setDoubleTapZoomDuration(long doubleTapZoomDuration) {
        this.doubleTapZoomDuration = doubleTapZoomDuration;
    }

    /**
     * <p>Sets the fling animation duration. 
     * Setting the duration to <code>0</code> disables the animation altogether.</p>
     *
     * @param flingDuration - flingDuration. 
     */
    public void setFlingDuration(long flingDuration) {
        this.flingDuration = flingDuration;
    }

    /**
     * <p>Sets the zoom release animation duration. 
     * Setting the duration to <code>0</code> disables the animation altogether.</p>
     *
     * @param zoomReleaseDuration - zoomReleaseDuration 
     */
    public void setZoomReleaseDuration(long zoomReleaseDuration) {
        this.zoomReleaseDuration = zoomReleaseDuration;
    }

    /**
     * <p>Sets the double tap zoom factor.</p>
     *
     * @param doubleTapZoomFactor - doubleTapZoomFactor. 
     */
    public void setDoubleTapZoomFactor(float doubleTapZoomFactor) {
        this.doubleTapZoomFactor = doubleTapZoomFactor;
    }

    /**
     * <p>Sets the minimum scale factor when double tapping zooms back out instead of in.</p>
     *
     * @param doubleTapZoomOutFactor - doubleTapZoomOutFactor. 
     */
    public void setDoubleTapZoomOutFactor(float doubleTapZoomOutFactor) {
        this.doubleTapZoomOutFactor = doubleTapZoomOutFactor;
    }

    /**
     * <p>Sets the fling animation exaggeration factor.</p>
     *
     * @param flingExaggeration - flingExaggeration. 
     */
    public void setFlingExaggeration(float flingExaggeration) {
        this.flingExaggeration = flingExaggeration;
    }

    /**
     * <p>Sets the zoom release animation exaggeration factor.</p>
     *
     * @param zoomReleaseExaggeration - zoomReleaseExaggeration. 
     */
    public void setZoomReleaseExaggeration(float zoomReleaseExaggeration) {
        this.zoomReleaseExaggeration = zoomReleaseExaggeration;
    }

    /**
     * <p>Indicates whether the image is being animated.</p>
     *
     * @return - return.
     */
    public boolean isAnimating() {
        return valueAnimator != null && valueAnimator.isRunning();
    }

    /**
     * <p>Cancels any running animations.</p>
     */
    public void cancelAnimation() {
        if (isAnimating()) {
            valueAnimator.cancel();
        }
    }

    /**
     * <p>Evaluates the touch state.</p>
     *
     * @param event - event.
     * @param matrix - matrix.
     */
    private void evaluateTouchState(TouchEvent event, Matrix matrix) {

        // Save the starting points
        updateStartPoints(event);
        savedMatrix.setMatrix(matrix);


        // Update the mode
        int touchCount = getTouchCount();
        if (touchCount == 0) {
            mode = NONE;
        } else {
            if (isAnimating()) {
                valueAnimator.cancel();
            }
            if (touchCount == 1 && mode == PINCH && zoomReleaseDuration > 0 && !isAnimating()) {
                // Animate zoom release
                float scale = (float) Math.pow(Math.pow(Math.pow(pinchVelocity, 1d / 1000d), zoomReleaseDuration), 
                        zoomReleaseExaggeration);
                animateZoom(scale, zoomReleaseDuration, mid.getPointX(), mid.getPointY(), CurveType.DECELERATE);
                mode = DRAG;
            } else if (touchCount > 1) {
                mode = PINCH;
                // Calculate the start distance
                startSpacing = spacing(event, getId(0), getId(1));
                pinchVelocity = 0f;

                if (startSpacing > MIN_PINCH_DIST_PIXELS) {
                    midPoint(startMid, event, getId(0), getId(1));
                    startAngle = angle(event, getId(0), getId(1), startedLower(getStartPoint(0), getStartPoint(1)));
                }
            }
        }
    }

    /*
     * Interface implementations
     */
    private void helperOnTouchEvent(TouchEvent event, Matrix matrix) {
        if (mode == DRAG && translateEnabled) {
            // Get the start point
            Point start = getStartPoint(0);
            int index = event.getPointerId(event.getIndex());
            MmiPoint point1 = event.getPointerPosition(index);
            final float eventx = point1.getX();
            final float eventy = point1.getY();
            float dx = eventx - start.getPointX();
            dx = corrector.correctRelative(MatrixEx.MTRANS_X, dx);
            float dy = eventy - start.getPointY();
            dy = corrector.correctRelative(MatrixEx.MTRANS_Y, dy);
            matrix.postTranslate(dx, dy);
        } else if (mode == PINCH) {
            // Get the new midpoint
            midPoint(mid, event, getId(0), getId(1));
            // Rotate
            if (rotateEnabled) {
                float deg = startAngle - angle(event, getId(0), getId(1), 
                        startedLower(getStartPoint(0), getStartPoint(1)));
                matrix.postRotate(deg, mid.getPointX(), mid.getPointY());
            }
            if (scaleEnabled) {
                // Scale
                float spacing = spacing(event, getId(0), getId(1));
                float sx = spacing / startSpacing;
                sx = corrector.correctRelative(MatrixEx.MSCALE_X, sx);
                matrix.postScale(sx, sx, mid.getPointX(), mid.getPointY());
                if (event.getPointerCount() > 0) {
                    pinchVelocity = pinchVelocity(event, getId(0), getId(1), pinchVelocityWindow);
                }
            }
            if (dragOnPinchEnabled && translateEnabled) {
                // Translate
                float dx = mid.getPointX() - startMid.getPointX();
                float dy = mid.getPointY() - startMid.getPointY();
                matrix.postTranslate(dx, dy);
            }
            corrector.performAbsoluteCorrections();
        }

    }

    @Override
    public boolean onTouchEvent(Component view, TouchEvent event) {
        super.onTouchEvent(view, event);
        gestureDetector.onTouchEvent(event, view);
        PhotoView photoView;
        try {
            photoView = (PhotoView) view;
        } catch (ClassCastException e) {
            throw new IllegalStateException("View must be an instance of ImageView", e);
        }
        // Get the matrix
        Matrix matrix = photoView.getImageMatrix();
        // Sets the image view
        if (corrector.getImageView() != photoView) {
            corrector.setImageView(photoView);
        } else {
            corrector.setMatrix(matrix);
        }
        int actionMasked = event.getAction();
        switch (actionMasked) {
            case TouchEvent.PRIMARY_POINT_UP:
            case TouchEvent.OTHER_POINT_UP:
            case TouchEvent.PRIMARY_POINT_DOWN:
            case TouchEvent.OTHER_POINT_DOWN:
                evaluateTouchState(event, matrix);
                break;
            case TouchEvent.POINT_MOVE:
                if (updateTouchState) {
                    evaluateTouchState(event, matrix);
                    updateTouchState = false;
                }
                // Reuse the saved matrix
                matrix.setMatrix(savedMatrix);
                helperOnTouchEvent(event, matrix);

                photoView.invalidate();
                break;
            default:
                break;
        }
        return true; // indicate event was handled
    }

    /**
     * ImageGestureListener.
     */
    public class ImageGestureListener extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(TouchEvent e1, TouchEvent e2, float velocityX, float velocityY) {
            if (mode == DRAG && flingDuration > 0 && !isAnimating()) {
                //float factor = (flingDuration / 1000f) * flingExaggeration;
                //float[] values = corrector.getValues();
                // float dx =  ((velocityX * factor) * values[MatrixEx.MSCALE_X]);
                // float dy =  ((velocityY * factor) * values[MatrixEx.MSCALE_Y]);

                //PropertyValuesHolder flingX = PropertyValuesHolder.ofFloat(FlingAnimatorHandler.PROPERTY_TRANSLATE_X,
                //                values[Matrix.MTRANS_X], values[Matrix.MTRANS_X] + dx);
                //PropertyValuesHolder flingY = PropertyValuesHolder.ofFloat(FlingAnimatorHandler.PROPERTY_TRANSLATE_Y,
                //                values[Matrix.MTRANS_Y], values[Matrix.MTRANS_Y] + dy);
                //valueAnimator = ValueAnimator.ofPropertyValuesHolder(flingX, flingY);
                //valueAnimator.setDuration(flingDuration);
                //valueAnimator.addUpdateListener(new FlingAnimatorHandler(corrector));
                //valueAnimator.setInterpolator(new DecelerateInterpolator());

                valueAnimator.start();
                valueAnimator.setValueUpdateListener(new FlingAnimatorHandler(corrector));
            }
            return super.onFling(e1, e2, velocityX, velocityY);
        }

        @Override
        public boolean onDoubleTapEvent(TouchEvent e) {
            if (doubleTapZoomFactor > 0 && !isAnimating()) {
                LogUtil.d("Gowtham : Inside onDoubleTapEvent");
                float sx = corrector.getValues()[MatrixEx.MSCALE_X];
                float innerFitScale = corrector.getInnerFitScale();
                float reversalScale = innerFitScale * doubleTapZoomOutFactor;
                ScaleAnimatorHandler scaleAnimatorHandler = new ScaleAnimatorHandler(corrector,
                        e.getPointerPosition(e.getIndex()).getX(), e.getPointerPosition(e.getIndex()).getY());
                float scaleTo = sx > reversalScale ? innerFitScale : sx * doubleTapZoomFactor;
                animateZoom(sx, scaleTo, doubleTapZoomDuration, scaleAnimatorHandler, null);
            }
            return super.onDoubleTapEvent(e);

        }

    }

    /**
     * <p>Performs a zoom animation using the given <code>zoomFactor</code>.</p>
     *
     * @param zoomFactor - zoomFactor. 
     * @param duration - duration.
     */
    public void animateZoom(float zoomFactor, long duration) {
        float sx = corrector.getValues()[MatrixEx.MSCALE_X];
        animateZoom(sx, sx * zoomFactor, duration, new ScaleAnimatorHandler(corrector), null);
    }

    /**
     * <p>Performs a zoom animation using the given <code>zoomFactor</code> and centerpoint coordinates.</p>
     *
     * @param zoomFactor - zoomFactor. 
     * @param duration - duration.
     * @param x - x.
     * @param y - y.
     */
    public void animateZoom(float zoomFactor, long duration, float x, float y) {
        animateZoom(zoomFactor, duration, x, y, null);
    }

    /**
     * <p>Performs a zoom animation from <code>scaleFrom</code> to <code>scaleTo</code> 
     * using the given <code>ScaleAnimatorHandler</code>.</p>
     *
     * @param scaleFrom - scalefrom.
     * @param scaleTo - scaleTo.
     * @param duration - duration.
     * @param scaleAnimatorHandler - scaleAnimatorHandler. 
     * @param interpolator - interpolator.
     */
    private void animateZoom(float scaleFrom, float scaleTo, long duration,
                             ScaleAnimatorHandler scaleAnimatorHandler, Integer interpolator) {
        if (isAnimating()) {
            throw new IllegalStateException("An animation is currently running; Check isAnimating() first!");
        }

        LogUtil.d("Gowtham : Inside animateZoom : " + scaleFrom + " : " + scaleTo);

        valueAnimator = MyValueAnimator.ofFloat(scaleFrom, scaleTo);
        valueAnimator.setDuration(duration);
        valueAnimator.setValueUpdateListener(scaleAnimatorHandler);
        if (interpolator != null) {
            valueAnimator.setCurveType(interpolator);
        }
        valueAnimator.start();
    }

    /**
     * <p>Performs a zoom animation using the given <code>zoomFactor</code> and centerpoint coordinates.</p>
     *
     * @param zoomFactor - zoomfactor.
     * @param duration - duration.
     * @param x - x.
     * @param y - y.
     * @param interpolator - interpolator.
     */
    public void animateZoom(float zoomFactor, long duration, float x, float y, Integer interpolator) {
        float sx = corrector.getValues()[MatrixEx.MSCALE_X];
        animateZoom(sx, sx * zoomFactor, duration, new ScaleAnimatorHandler(corrector, x, y),  interpolator);
    }

    /**
     * <p>Performs a zoom out animation so that the image entirely fits within the view.</p>
     *
     * @param duration - duration.
     */
    public void animateZoomOutToFit(long duration) {
        float sx = corrector.getValues()[MatrixEx.MSCALE_X];
        animateZoom(sx, corrector.getInnerFitScale(), duration, new ScaleAnimatorHandler(corrector), null);
    }

    /**
     * <p>Performs a zoom out animation 
     * so that the image entirely fits within the view using centerpoint coordinates.</p>
     *
     * @param duration - duration.
     * @param x - x.
     * @param y - y.
     */
    public void animateZoomOutToFit(long duration, float x, float y) {
        float sx = corrector.getValues()[MatrixEx.MSCALE_X];
        animateZoom(sx, corrector.getInnerFitScale(), duration, new ScaleAnimatorHandler(corrector, x, y), null);
    }
    
   
}
