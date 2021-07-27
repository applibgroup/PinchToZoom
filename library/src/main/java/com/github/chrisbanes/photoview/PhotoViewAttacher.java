package com.github.chrisbanes.photoview;

import com.github.chrisbanes.photoview.gesture.GestureDetector;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.PageSlider;
import ohos.agp.components.ScrollHelper;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.multimodalinput.event.TouchEvent;

public class PhotoViewAttacher implements Component.TouchEventListener {

    private static final String TAG = "PhotoViewAttach";
    private static float DEFAULT_MAX_SCALE = 3.0f;
    private static float DEFAULT_MID_SCALE = 1.75f;
    private static float DEFAULT_MIN_SCALE = 1.0f;
    private static int DEFAULT_ZOOM_DURATION = 200;

    private static final int HORIZONTAL_EDGE_NONE = -1;
    private static final int HORIZONTAL_EDGE_LEFT = 0;
    private static final int HORIZONTAL_EDGE_RIGHT = 1;
    private static final int HORIZONTAL_EDGE_BOTH = 2;
    private static final int VERTICAL_EDGE_NONE = -1;
    private static final int VERTICAL_EDGE_TOP = 0;
    private static final int VERTICAL_EDGE_BOTTOM = 1;
    private static final int VERTICAL_EDGE_BOTH = 2;
    private static int SINGLE_TOUCH = 1;

    //    private Interpolator mInterpolator = new AccelerateDecelerateInterpolator();
    private int mZoomDuration = DEFAULT_ZOOM_DURATION;
    private float mMinScale = DEFAULT_MIN_SCALE;
    private float mMidScale = DEFAULT_MID_SCALE;
    private float mMaxScale = DEFAULT_MAX_SCALE;

    private boolean mAllowParentInterceptOnEdge = true;
    private boolean mBlockParentIntercept = false;

    private PhotoView mImageView;

    // Gesture Detectors
    private GestureDetector mGestureDetector;
    private CustomGestureDetector mScaleDragDetector;

    // These are set so we don't keep allocating them on the heap
    private final Matrix mBaseMatrix = new Matrix();
    private final Matrix mDrawMatrix = new Matrix();
    private final Matrix mSuppMatrix = new Matrix();
    private final RectFloat mDisplayRect = new RectFloat();
    private final float[] mMatrixValues = new float[9];

    // Listeners
    private OnMatrixChangedListener mMatrixChangeListener;
    private OnPhotoTapListener mPhotoTapListener;
    private OnOutsidePhotoTapListener mOutsidePhotoTapListener;
    private OnViewTapListener mViewTapListener;
    private Component.ClickedListener mOnClickListener;
    private Component.LongClickedListener mLongClickListener;
    private OnScaleChangedListener mScaleChangeListener;
    private OnSingleFlingListener mSingleFlingListener;
    private OnViewDragListener mOnViewDragListener;

    private FlingRunnable mCurrentFlingRunnable;
    private int mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
    private int mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
    private float mBaseRotation;

    private boolean mZoomEnabled = true;
    private Image.ScaleMode mScaleType = Image.ScaleMode.ZOOM_CENTER;

    private PageSlider pageSlider;

    public void setPageSlider(PageSlider pageSlider) {
        this.pageSlider = pageSlider;
    }

    private final EventHandler mHandler = new EventHandler(EventRunner.getMainEventRunner());

    private final OnGestureListener onGestureListener = new OnGestureListener() {
        @Override
        public void onDrag(float dx, float dy) {
            if (mScaleDragDetector.isScaling()) {
                return; // Do not drag if we are already scaling
            }
            if (mOnViewDragListener != null) {
                mOnViewDragListener.onDrag(dx, dy);
            }
            mSuppMatrix.postTranslate(dx, dy);
            checkAndDisplayMatrix();

            /*
             * Here we decide whether to let the ImageView's parent to start taking
             * over the touch event.
             *
             * First we check whether this function is enabled. We never want the
             * parent to take over if we're scaling. We then check the edge we're
             * on, and the direction of the scroll (i.e. if we're pulling against
             * the edge, aka 'overscrolling', let the parent take over).
             */
//            ComponentParent parent = mImageView.getComponentParent();
            if (mAllowParentInterceptOnEdge && !mScaleDragDetector.isScaling() && !mBlockParentIntercept) {
                if (mHorizontalScrollEdge == HORIZONTAL_EDGE_BOTH
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_LEFT && dx >= 1f)
                        || (mHorizontalScrollEdge == HORIZONTAL_EDGE_RIGHT && dx <= -1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_TOP && dy >= 1f)
                        || (mVerticalScrollEdge == VERTICAL_EDGE_BOTTOM && dy <= -1f)) {
                    if (pageSlider != null){
                        pageSlider.setEnabled(true);
                    }
                }
            } else {
                if (pageSlider != null){
                    pageSlider.setEnabled(false);
                }
//                if (parent != null) {
//                    parent.requestDisallowInterceptTouchEvent(true);
//                }
            }
        }

        @Override
        public void onFling(float startX, float startY, float velocityX, float velocityY) {
            mCurrentFlingRunnable = new FlingRunnable();
            mCurrentFlingRunnable.fling(getImageViewWidth(mImageView),
                    getImageViewHeight(mImageView), (int) velocityX, (int) velocityY);
            mHandler.postTask(mCurrentFlingRunnable);
        }

        @Override
        public void onScale(float scaleFactor, float focusX, float focusY) {
            if (getScale() < mMaxScale || scaleFactor < 1f) {
                if (mScaleChangeListener != null) {
                    mScaleChangeListener.onScaleChange(scaleFactor, focusX, focusY);
                }
                mSuppMatrix.postScale(scaleFactor, scaleFactor, focusX, focusY);
                checkAndDisplayMatrix();
            }
        }
    };

    public PhotoViewAttacher(PhotoView imageView) {
        mImageView = imageView;
        imageView.setTouchEventListener(this);
        imageView.setLayoutRefreshedListener(new Component.LayoutRefreshedListener() {
            @Override
            public void onRefreshed(Component component) {
                updateBaseMatrix(mImageView.getImageElement());
            }
        });

        mBaseRotation = 0.0f;
        // Create Gesture Detectors...
        mScaleDragDetector = new CustomGestureDetector(imageView.getContext(), onGestureListener);
        mGestureDetector = new GestureDetector(imageView.getContext(), new GestureDetector.SimpleOnGestureListener() {

            // forward long click listener
            @Override
            public void onLongPress(TouchEvent e) {
                if (mLongClickListener != null) {
                    mLongClickListener.onLongClicked(mImageView);
                }
            }

            @Override
            public boolean onFling(TouchEvent e1, TouchEvent e2,
                                   float velocityX, float velocityY) {
                if (mSingleFlingListener != null) {
                    if (getScale() > DEFAULT_MIN_SCALE) {
                        return false;
                    }
                    if (e1.getPointerCount() > SINGLE_TOUCH
                            || e2.getPointerCount() > SINGLE_TOUCH) {
                        return false;
                    }
                    return mSingleFlingListener.onFling(e1, e2, velocityX, velocityY);
                }
                return false;
            }
        });
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(TouchEvent e) {
                if (mOnClickListener != null) {
                    mOnClickListener.onClick(mImageView);
                }
                final RectFloat displayRect = getDisplayRect();
                final float x = e.getPointerPosition(0).getX(), y = e.getPointerPosition(0).getY();
                if (mViewTapListener != null) {
                    mViewTapListener.onViewTap(mImageView, x, y);
                }
                if (displayRect != null) {
                    // Check to see if the user tapped on the photo
                    if (contains(displayRect,x, y)) {
                        float xResult = (x - displayRect.left)
                                / displayRect.getWidth();
                        float yResult = (y - displayRect.top)
                                / displayRect.getHeight();
                        if (mPhotoTapListener != null) {
                            mPhotoTapListener.onPhotoTap(mImageView, xResult, yResult);
                        }
                        return true;
                    } else {
                        if (mOutsidePhotoTapListener != null) {
                            mOutsidePhotoTapListener.onOutsidePhotoTap(mImageView);
                        }
                    }
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(TouchEvent ev) {
                try {
                    float scale = getScale();
                    float x = ev.getPointerPosition(0).getX();
                    float y = ev.getPointerPosition(0).getY();
                    if (scale < getMediumScale()) {
                        setScale(getMediumScale(), x, y, true);
                    } else if (scale >= getMediumScale() && scale < getMaximumScale()) {
                        setScale(getMaximumScale(), x, y, true);
                    } else {
                        setScale(getMinimumScale(), x, y, true);
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    // Can sometimes happen when getX() and getY() is called
                }
                return true;
            }

            @Override
            public boolean onDoubleTapEvent(TouchEvent e) {
                // Wait for the confirmed onDoubleTap() instead
                return false;
            }
        });
    }
    private boolean contains(RectFloat rectFloat,float x, float y) {
        return rectFloat.left <  rectFloat.right &&  rectFloat.top <  rectFloat.bottom  // check for empty first
                && x >=  rectFloat.left && x <  rectFloat.right && y >=  rectFloat.top && y <  rectFloat.bottom;
    }
    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener newOnDoubleTapListener) {
        this.mGestureDetector.setOnDoubleTapListener(newOnDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangeListener) {
        this.mScaleChangeListener = onScaleChangeListener;
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        this.mSingleFlingListener = onSingleFlingListener;
    }

    @Deprecated
    public boolean isZoomEnabled() {
        return mZoomEnabled;
    }

    public RectFloat getDisplayRect() {
        checkMatrixBounds();
        return getDisplayRect(getDrawMatrix());
    }

    public boolean setDisplayMatrix(Matrix finalMatrix) {
        if (finalMatrix == null) {
            throw new IllegalArgumentException("Matrix cannot be null");
        }
        if (mImageView.getImageElement() == null) {
            return false;
        }
        mSuppMatrix.setMatrix(finalMatrix);
        checkAndDisplayMatrix();
        return true;
    }

    public void setBaseRotation(final float degrees) {
        mBaseRotation = degrees % 360;
        update();
        setRotationBy(mBaseRotation);
        checkAndDisplayMatrix();
    }

    public void setRotationTo(float degrees) {
        mSuppMatrix.setRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public void setRotationBy(float degrees) {
        mSuppMatrix.postRotate(degrees % 360);
        checkAndDisplayMatrix();
    }

    public float getMinimumScale() {
        return mMinScale;
    }

    public float getMediumScale() {
        return mMidScale;
    }

    public float getMaximumScale() {
        return mMaxScale;
    }

    public float getScale() {
        return (float) Math.sqrt((float) Math.pow(getValue(mSuppMatrix, 0), 2) + (float) Math.pow
                (getValue(mSuppMatrix, 3), 2));
    }
    /**
     * Helper method that 'unpacks' a Matrix and returns the required value
     *
     * @param matrix     Matrix to unpack
     * @param whichValue Which value from Matrix.M* to return
     * @return returned value
     */
    private float getValue(Matrix matrix, int whichValue) {
        float[] data = matrix.getData();
        System.arraycopy(data, 0, mMatrixValues, 0, 9);
        return mMatrixValues[whichValue];
    }
    public Image.ScaleMode getScaleType() {
        return mScaleType;
    }

    @Override
    public boolean onTouchEvent(Component v, TouchEvent ev) {
        boolean handled = false;
        if (mZoomEnabled && Util.hasDrawable((PhotoView) v)) {
            switch (ev.getAction()) {
                case TouchEvent.PRIMARY_POINT_DOWN:
//                    disableParent();
                    if (pageSlider != null){
                        pageSlider.setEnabled(false);
                    }
                    cancelFling();
                    break;
                case TouchEvent.CANCEL:
                case TouchEvent.PRIMARY_POINT_UP:
                    // If the user has zoomed less than min scale, zoom back
                    // to min scale
                    if (getScale() < mMinScale) {
                        RectFloat rect = getDisplayRect();
                        if (rect != null) {
                            mHandler.postTask(new AnimatedZoomRunnable(getScale(), mMinScale,
                                    rect.getHorizontalCenter(), rect.getVerticalCenter()));
                            handled = true;
                        }
                    } else if (getScale() > mMaxScale) {
                        RectFloat rect = getDisplayRect();
                        if (rect != null) {
                            mHandler.postTask(new AnimatedZoomRunnable(getScale(), mMaxScale,
                                    rect.getHorizontalCenter(), rect.getVerticalCenter()));
                            handled = true;
                        }
                    }
                    break;
            }
            // Try the Scale/Drag detector
            if (mScaleDragDetector != null) {
                boolean wasScaling = mScaleDragDetector.isScaling();
                boolean wasDragging = mScaleDragDetector.isDragging();
                handled = mScaleDragDetector.onTouchEvent(ev,mImageView);
                boolean didntScale = !wasScaling && !mScaleDragDetector.isScaling();
                boolean didntDrag = !wasDragging && !mScaleDragDetector.isDragging();
                mBlockParentIntercept = didntScale && didntDrag;
            }
            // Check to see if the user double tapped
            if (mGestureDetector != null && mGestureDetector.onTouchEvent(ev,mImageView)) {
                handled = true;
            }

        }
        return handled;
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        mAllowParentInterceptOnEdge = allow;
    }

    public void setMinimumScale(float minimumScale) {
        Util.checkZoomLevels(minimumScale, mMidScale, mMaxScale);
        mMinScale = minimumScale;
    }

    public void setMediumScale(float mediumScale) {
        Util.checkZoomLevels(mMinScale, mediumScale, mMaxScale);
        mMidScale = mediumScale;
    }

    public void setMaximumScale(float maximumScale) {
        Util.checkZoomLevels(mMinScale, mMidScale, maximumScale);
        mMaxScale = maximumScale;
    }

    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        Util.checkZoomLevels(minimumScale, mediumScale, maximumScale);
        mMinScale = minimumScale;
        mMidScale = mediumScale;
        mMaxScale = maximumScale;
    }

    public void setOnLongClickListener(Component.LongClickedListener listener) {
        mLongClickListener = listener;
    }

    public void setOnClickListener(Component.ClickedListener listener) {
        mOnClickListener = listener;
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        mMatrixChangeListener = listener;
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        mPhotoTapListener = listener;
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener mOutsidePhotoTapListener) {
        this.mOutsidePhotoTapListener = mOutsidePhotoTapListener;
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        mViewTapListener = listener;
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        mOnViewDragListener = listener;
    }

    public void setScale(float scale) {
        setScale(scale, false);
    }

    public void setScale(float scale, boolean animate) {
        setScale(scale,
                (mImageView.getRight()) / 2,
                (mImageView.getBottom()) / 2,
                animate);
    }

    public void setScale(float scale, float focalX, float focalY,
                         boolean animate) {
        // Check to see if the scale is within bounds
        if (scale < mMinScale || scale > mMaxScale) {
            throw new IllegalArgumentException("Scale must be within the range of minScale and maxScale");
        }
        if (animate) {
            mHandler.postTask(new AnimatedZoomRunnable(getScale(), scale,
                    focalX, focalY));
        } else {
            mSuppMatrix.setScale(scale, scale, focalX, focalY);
            checkAndDisplayMatrix();
        }
    }


    public void setScaleType(Image.ScaleMode scaleType) {
        if (Util.isSupportedScaleType(scaleType) && scaleType != mScaleType) {
            mScaleType = scaleType;
            update();
        }
    }

    public boolean isZoomable() {
        return mZoomEnabled;
    }

    public void setZoomable(boolean zoomable) {
        mZoomEnabled = zoomable;
        update();
    }

    public void update() {
        if (mZoomEnabled) {
            // Update the base matrix using the current drawable
            updateBaseMatrix(mImageView.getImageElement());
        } else {
            // Reset the Matrix...
            resetMatrix();
        }
    }

    /**
     * Get the display matrix
     *
     * @param matrix target matrix to copy to
     */
    public void getDisplayMatrix(Matrix matrix) {
        matrix.setMatrix(getDrawMatrix());
    }

    /**
     * Get the current support matrix
     */
    public void getSuppMatrix(Matrix matrix) {
        matrix.setMatrix(mSuppMatrix);
    }

    private Matrix getDrawMatrix() {
        mDrawMatrix.setMatrix(mBaseMatrix);
        mDrawMatrix.postConcat(mSuppMatrix);
        return mDrawMatrix;
    }

    public Matrix getImageMatrix() {
        return mDrawMatrix;
    }

    public void setZoomTransitionDuration(int milliseconds) {
        this.mZoomDuration = milliseconds;
    }



    /**
     * Resets the Matrix back to FIT_CENTER, and then displays its contents
     */
    private void resetMatrix() {
        mSuppMatrix.reset();
        setRotationBy(mBaseRotation);
        setImageViewMatrix(getDrawMatrix());
        checkMatrixBounds();
    }

    private void setImageViewMatrix(Matrix matrix) {

        mImageView.setImageMatrix(matrix);

        // Call MatrixChangedListener if needed
        if (mMatrixChangeListener != null) {
            RectFloat displayRect = getDisplayRect(matrix);
            if (displayRect != null) {
                mMatrixChangeListener.onMatrixChanged(displayRect);
            }
        }
    }

    /**
     * Helper method that simply checks the Matrix, and then displays the result
     */
    private void checkAndDisplayMatrix() {
        if (checkMatrixBounds()) {
            setImageViewMatrix(getDrawMatrix());
        }
    }

    /**
     * Helper method that maps the supplied Matrix to the current Drawable
     *
     * @param matrix - Matrix to map Drawable against
     * @return RectF - Displayed Rectangle
     */
    private RectFloat getDisplayRect(Matrix matrix) {
        Element d = mImageView.getImageElement();
        if (d != null) {
            mDisplayRect.modify(0, 0, d.getWidth(),
                    d.getHeight());
            matrix.mapRect(mDisplayRect);
            return mDisplayRect;
        }
        return null;
    }

    /**
     * Calculate Matrix for FIT_CENTER
     *
     * @param drawable - Drawable being displayed
     */
    private void updateBaseMatrix(Element drawable) {
        if (drawable == null) {
            return;
        }
        final float viewWidth = getImageViewWidth(mImageView);
        final float viewHeight = getImageViewHeight(mImageView);
        final int drawableWidth = drawable.getWidth();
        final int drawableHeight = drawable.getHeight();
        mBaseMatrix.reset();
        final float widthScale = viewWidth / drawableWidth;
        final float heightScale = viewHeight / drawableHeight;
        if (mScaleType == Image.ScaleMode.CENTER) {
            mBaseMatrix.postTranslate((viewWidth - drawableWidth) / 2F,
                    (viewHeight - drawableHeight) / 2F);

        } else if (mScaleType == Image.ScaleMode.CLIP_CENTER) {
            float scale = Math.max(widthScale, heightScale);
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else if (mScaleType == Image.ScaleMode.INSIDE) {
            float scale = Math.min(1.0f, Math.min(widthScale, heightScale));
            mBaseMatrix.postScale(scale, scale);
            mBaseMatrix.postTranslate((viewWidth - drawableWidth * scale) / 2F,
                    (viewHeight - drawableHeight * scale) / 2F);

        } else {
            RectFloat mTempSrc = new RectFloat(0, 0, drawableWidth, drawableHeight);
            RectFloat mTempDst = new RectFloat(0, 0, viewWidth, viewHeight);
            if ((int) mBaseRotation % 180 != 0) {
                mTempSrc = new RectFloat(0, 0, drawableHeight, drawableWidth);
            }
            switch (mScaleType) {
                case ZOOM_CENTER:
                    setRectToRect(mBaseMatrix,mTempSrc, mTempDst, Matrix.ScaleToFit.CENTER);
                    break;
                case ZOOM_START:
                    setRectToRect(mBaseMatrix,mTempSrc, mTempDst, Matrix.ScaleToFit.START);
                    break;
                case ZOOM_END:
                    setRectToRect(mBaseMatrix,mTempSrc, mTempDst, Matrix.ScaleToFit.END);
                    break;
                case STRETCH:
                    setRectToRect(mBaseMatrix,mTempSrc, mTempDst, Matrix.ScaleToFit.FILL);
                    break;
                default:
                    break;
            }
        }
        resetMatrix();
    }
    private boolean setRectToRect(Matrix matrix,RectFloat src, RectFloat dst,Matrix.ScaleToFit stf){
        if (matrix == null){
            return false;
        }
        float[] mValues = matrix.getData();
        if (src.isEmpty()){
            reset(mValues);
            return false;
        }
        if (dst.isEmpty()){
            mValues[0] = mValues[1] = mValues[2] = mValues[3] = mValues[4] = mValues[5] = mValues[6]
                    = mValues[7] = 0;
            mValues[8] = 1;
        }else {
            float tx,sx = dst.getWidth() / src.getWidth();
            float ty,sy = dst.getHeight() / src.getHeight();
            boolean xLarger = false;
            if (stf != Matrix.ScaleToFit.FILL){
                if (sx > sy){
                    xLarger = true;
                    sx = sy;
                }else {
                    sy = sx;
                }
            }

            tx = dst.left - src.left * sx;
            ty = dst.top - src.top * sy;
            if (stf == Matrix.ScaleToFit.CENTER || stf == Matrix.ScaleToFit.END){
                float diff;

                if (xLarger){
                    diff = dst.getWidth() - src.getWidth() * sy;
                }else {
                    diff = dst.getHeight() - src.getHeight() * sy;
                }

                if (stf == Matrix.ScaleToFit.CENTER){
                    diff = diff / 2;
                }

                if (xLarger){
                    tx += diff;
                }else {
                    ty += diff;
                }
            }

            mValues[0] = sx;
            mValues[4] = sy;
            mValues[2] = tx;
            mValues[5] = ty;
            mValues[1] = mValues[3] = mValues[6] = mValues[7] = 0;
        }
        mValues[8] = 1;
        matrix.setElements(mValues);
        return true;
    }

    private void reset(float[] mtx){
        for (int i = 0, k = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++,k++) {
                mtx[k] = ((i == j) ? 1 : 0);
            }
        }
    }

    private boolean checkMatrixBounds() {
        final RectFloat rect = getDisplayRect(getDrawMatrix());
        if (rect == null) {
            return false;
        }
        final float height = rect.getHeight(), width = rect.getWidth();
        float deltaX = 0, deltaY = 0;
        final int viewHeight = getImageViewHeight(mImageView);
        if (height <= viewHeight) {
            switch (mScaleType) {
                case ZOOM_START:
                    deltaY = -rect.top;
                    break;
                case ZOOM_END:
                    deltaY = viewHeight - height - rect.top;
                    break;
                default:
                    deltaY = (viewHeight - height) / 2 - rect.top;
                    break;
            }
            mVerticalScrollEdge = VERTICAL_EDGE_BOTH;
        } else if (rect.top > 0) {
            mVerticalScrollEdge = VERTICAL_EDGE_TOP;
            deltaY = -rect.top;
        } else if (rect.bottom < viewHeight) {
            mVerticalScrollEdge = VERTICAL_EDGE_BOTTOM;
            deltaY = viewHeight - rect.bottom;
        } else {
            mVerticalScrollEdge = VERTICAL_EDGE_NONE;
        }
        final int viewWidth = getImageViewWidth(mImageView);
        if (width <= viewWidth) {
            switch (mScaleType) {
                case ZOOM_START:
                    deltaX = -rect.left;
                    break;
                case ZOOM_END:
                    deltaX = viewWidth - width - rect.left;
                    break;
                default:
                    deltaX = (viewWidth - width) / 2 - rect.left;
                    break;
            }
            mHorizontalScrollEdge = HORIZONTAL_EDGE_BOTH;
        } else if (rect.left > 0) {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_LEFT;
            deltaX = -rect.left;
        } else if (rect.right < viewWidth) {
            deltaX = viewWidth - rect.right;
            mHorizontalScrollEdge = HORIZONTAL_EDGE_RIGHT;
        } else {
            mHorizontalScrollEdge = HORIZONTAL_EDGE_NONE;
        }
        // Finally actually translate the matrix
        mSuppMatrix.postTranslate(deltaX, deltaY);
        return true;
    }

    private int getImageViewWidth(PhotoView imageView) {
        return imageView.getWidth() - imageView.getPaddingLeft() - imageView.getPaddingRight();
    }

    private int getImageViewHeight(PhotoView imageView) {
        return imageView.getHeight() - imageView.getPaddingTop() - imageView.getPaddingBottom();
    }

    private void cancelFling() {
        if (mCurrentFlingRunnable != null) {
            mCurrentFlingRunnable.cancelFling();
            mCurrentFlingRunnable = null;
        }
    }

    private class AnimatedZoomRunnable implements Runnable {

        private final float mFocalX, mFocalY;
        private final long mStartTime;
        private final float mZoomStart, mZoomEnd;

        public AnimatedZoomRunnable(final float currentZoom, final float targetZoom,
                                    final float focalX, final float focalY) {
            mFocalX = focalX;
            mFocalY = focalY;
            mStartTime = System.currentTimeMillis();
            mZoomStart = currentZoom;
            mZoomEnd = targetZoom;
        }

        @Override
        public void run() {
            float t = interpolate();
            float scale = mZoomStart + t * (mZoomEnd - mZoomStart);
            float deltaScale = scale / getScale();
            onGestureListener.onScale(deltaScale, mFocalX, mFocalY);
            // We haven't hit our target scale yet, so post ourselves again
            if (t < 1f) {
                mHandler.postTask(this,10);
            }
        }

        private float interpolate() {
            float t = 1f * (System.currentTimeMillis() - mStartTime) / mZoomDuration;
            t = Math.min(1f, t);
            t = getInterpolation(t);
            return t;
        }
        private float getInterpolation(float input) {
            return (float)(Math.cos((input + 1) * Math.PI) / 2.0f) + 0.5f;
        }

    }

    private class FlingRunnable implements Runnable {

        private final ScrollHelper mScroller;
        private int mCurrentX, mCurrentY;

        public FlingRunnable() {
            mScroller = new ScrollHelper();
        }

        public void cancelFling() {
//            mScroller.forceFinished(true);
        }

        public void fling(int viewWidth, int viewHeight, int velocityX,
                          int velocityY) {
            final RectFloat rect = getDisplayRect();
            if (rect == null) {
                return;
            }
            final int startX = Math.round(-rect.left);
            final int minX, maxX, minY, maxY;
            if (viewWidth < rect.getWidth()) {
                minX = 0;
                maxX = Math.round(rect.getWidth() - viewWidth);
            } else {
                minX = maxX = startX;
            }
            final int startY = Math.round(-rect.top);
            if (viewHeight < rect.getHeight()) {
                minY = 0;
                maxY = Math.round(rect.getHeight() - viewHeight);
            } else {
                minY = maxY = startY;
            }
            mCurrentX = startX;
            mCurrentY = startY;
            // If we actually can move, fling the scroller
            if (startX != maxX || startY != maxY) {
                mScroller.doFling(startX, startY, velocityX, velocityY, minX,
                        maxX, minY, maxY);
            }
        }

        @Override
        public void run() {
            if (mScroller.isFinished()) {
                return; // remaining post that should not be handled
            }
            if (mScroller.updateScroll()) {
                final int newX = mScroller.getCurrValue(ScrollHelper.AXIS_X);
                final int newY = mScroller.getCurrValue(ScrollHelper.AXIS_Y);
                mSuppMatrix.postTranslate(mCurrentX - newX, mCurrentY - newY);
                checkAndDisplayMatrix();
                mCurrentX = newX;
                mCurrentY = newY;
                // Post On animation
                mHandler.postTask(this,10);
            }
        }
    }
}
