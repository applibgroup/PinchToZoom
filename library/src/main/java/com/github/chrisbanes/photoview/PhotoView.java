package com.github.chrisbanes.photoview;

import com.github.chrisbanes.photoview.gesture.GestureDetector;
import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.agp.components.PageSlider;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Paint;
import ohos.agp.render.PixelMapHolder;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.global.resource.Resource;
import ohos.media.image.ImageSource;
import ohos.media.image.PixelMap;

public class PhotoView extends Image {

    private Element mElement;
    private PixelMapHolder mPixelMapHolder;
    private PixelMap mPixelMap;
    private Paint mPaint;
    private ScaleMode pendingScaleType;

    private PhotoViewAttacher attach;

    private Matrix mMatrix;

    private Matrix mDrawMatrix = null;

    private boolean isChange = false;

    public PhotoView(Context context) {
        this(context, null);
    }

    public PhotoView(Context context, AttrSet attrSet) {
        this(context, attrSet, "");
    }

    public PhotoView(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init(attrSet);
    }


    private void init(AttrSet attrSet) {
        if (attrSet != null){
            attrSet.getAttr("image").ifPresent(attr -> mElement = attr.getElement());
        }
        if (mElement != null){
            isChange = true;
            mPixelMap = Compat.elementToPixelMap(mElement);
        }
        attach = new PhotoViewAttacher(this);

        mMatrix = new Matrix();
        pendingScaleType = super.getScaleMode();

        mPaint = new Paint();

        //apply the previously applied scale type
        if (pendingScaleType != null) {
            if (pendingScaleType != ScaleMode.ZOOM_CENTER){
                setScaleMode(pendingScaleType);
            }
            pendingScaleType = null;
        }


        addDrawTask(new DrawTask() {
            @Override
            public void onDraw(Component component, Canvas canvas) {
                if (mElement == null || mPixelMap == null) {
                    return; // couldn't resolve the URI
                }

                if (mDrawMatrix == null && getPaddingTop() == 0 && getPaddingLeft() == 0) {
                    mElement.drawToCanvas(canvas);
                } else {

                    canvas.save();

                    canvas.translate(getPaddingLeft(), getPaddingTop());

                    if (mDrawMatrix != null) {
                        canvas.concat(mDrawMatrix);
                    }

                    if (mPixelMapHolder == null || isChange){
                        mPixelMapHolder = new PixelMapHolder(mPixelMap);
                        isChange = false;
                    }
                    canvas.drawPixelMapHolder(mPixelMapHolder,0,0,mPaint);

                    canvas.restore();

                }
            }
        });

        setBindStateChangedListener(new BindStateChangedListener() {
            @Override
            public void onComponentBoundToWindow(Component component) {

            }

            @Override
            public void onComponentUnboundFromWindow(Component component) {
                if (mPixelMap != null){
                    mPixelMap.release();
                }
                if (mPixelMapHolder != null){
                    mPixelMapHolder.release();

                }
                mPixelMapHolder = null;
                mPixelMap = null;
                mElement = null;
            }
        });

    }
    public void setImageMatrix(Matrix matrix) {
        // collapse null and identity to just null
        if (matrix != null && matrix.isIdentity()) {
            matrix = null;
        }

        // don't invalidate unless we're actually changing our matrix
        if (matrix == null && !mMatrix.isIdentity() ||
                matrix != null && !mMatrix.equals(matrix)) {
            mMatrix.setMatrix(matrix);
            configureBounds();
            invalidate();
        }
    }
    public Matrix getImageMatrix(){
        if (attach != null){
            return attach.getImageMatrix();
        }
        return null;
    }

    public void setPageSlider(PageSlider pageSlider){
        if (attach != null){
            attach.setPageSlider(pageSlider);
        }
    }
    public PhotoViewAttacher getAttach() {
        return attach;
    }


    @Override
    public void setImageAndDecodeBounds(int resId) {
        if (resId == 0){
            return;
        }

        try {
            ImageSource.SourceOptions options = new ImageSource.SourceOptions();
            Resource resource = getContext().getResourceManager().getResource(resId);
            ImageSource imageSource = ImageSource.create(resource, options);
            ImageSource.DecodingOptions decodingOptions = new ImageSource.DecodingOptions();
            setPixelMap(imageSource.createPixelmap(decodingOptions));
        } catch (Exception exception) {
            LogUtil.d(exception.getMessage());
        }
    }

    @Override
    public void setImageElement(Element element) {
        if (element == null){
            return;
        }
        mElement = element;
        mPixelMap = Compat.elementToPixelMap(element);
        isChange = true;
        configureBounds();
        invalidate();

        if (attach != null){
            attach.update();
        }
    }

    @Override
    public Element getImageElement() {
        return mElement;
    }

    @Override
    public void setPixelMap(PixelMap pixelMap) {
        if (pixelMap == null){
            return;
        }

        mElement = new PixelMapElement(pixelMap);
        mPixelMap = pixelMap;
        isChange = true;
        configureBounds();
        invalidate();

        if (attach != null){
            attach.update();
        }

    }

    @Override
    public void setPixelMap(int resId) {
        setImageAndDecodeBounds(resId);
    }

    @Override
    public PixelMap getPixelMap() {
        return mPixelMap;
    }
    @Override
    public void setScaleMode(ScaleMode scaleMode) {
        if (attach == null) {
            pendingScaleType = scaleMode;
        } else {
            attach.setScaleType(scaleMode);
        }
    }

    @Override
    public ScaleMode getScaleMode() {
        return attach.getScaleType();
    }

    public void setRotationTo(float rotationDegree) {
        attach.setRotationTo(rotationDegree);
    }

    public void setRotationBy(float rotationDegree) {
        attach.setRotationBy(rotationDegree);
    }

    public boolean isZoomable() {
        return attach.isZoomable();
    }

    public void setZoomable(boolean zoomable) {
        attach.setZoomable(zoomable);
    }

    public RectFloat getDisplayRect() {
        return attach.getDisplayRect();
    }
    public void getDisplayMatrix(Matrix matrix) {
        attach.getDisplayMatrix(matrix);
    }

    @SuppressWarnings("UnusedReturnValue")
    public boolean setDisplayMatrix(Matrix finalRectangle) {
        return attach.setDisplayMatrix(finalRectangle);
    }

    public void getSuppMatrix(Matrix matrix) {
        attach.getSuppMatrix(matrix);
    }

    public boolean setSuppMatrix(Matrix matrix) {
        return attach.setDisplayMatrix(matrix);
    }

    public float getAttachScale() {
        return attach.getScale();
    }

    public void setAllowParentInterceptOnEdge(boolean allow) {
        attach.setAllowParentInterceptOnEdge(allow);
    }


    public void setScaleLevels(float minimumScale, float mediumScale, float maximumScale) {
        attach.setScaleLevels(minimumScale, mediumScale, maximumScale);
    }

    public void setOnMatrixChangeListener(OnMatrixChangedListener listener) {
        attach.setOnMatrixChangeListener(listener);
    }

    @Override
    public void setClickedListener(ClickedListener listener) {
        attach.setOnClickListener(listener);
    }

    @Override
    public void setLongClickedListener(LongClickedListener listener) {
        attach.setOnLongClickListener(listener);
    }

    public void setOnPhotoTapListener(OnPhotoTapListener listener) {
        attach.setOnPhotoTapListener(listener);
    }

    public void setOnOutsidePhotoTapListener(OnOutsidePhotoTapListener listener) {
        attach.setOnOutsidePhotoTapListener(listener);
    }

    public void setOnViewTapListener(OnViewTapListener listener) {
        attach.setOnViewTapListener(listener);
    }

    public void setOnViewDragListener(OnViewDragListener listener) {
        attach.setOnViewDragListener(listener);
    }

    public float getMinimumScale() {
        return attach.getMinimumScale();
    }

    public float getMediumScale() {
        return attach.getMediumScale();
    }

    public float getMaximumScale() {
        return attach.getMaximumScale();
    }

    public float getPhotoScale() {
        return attach.getScale();
    }

    public void setMinimumScale(float minimumScale) {
        attach.setMinimumScale(minimumScale);
    }

    public void setMediumScale(float mediumScale) {
        attach.setMediumScale(mediumScale);
    }

    public void setMaximumScale(float maximumScale) {
        attach.setMaximumScale(maximumScale);
    }

    public void setZoomTransitionDuration(int milliseconds) {
        attach.setZoomTransitionDuration(milliseconds);
    }

    public void setScale(float scale) {
        attach.setScale(scale);
    }

    public void setScale(float scale, boolean animate) {
        attach.setScale(scale, animate);
    }

    public void setScale(float scale, float focalX, float focalY, boolean animate) {
        attach.setScale(scale, focalX, focalY, animate);
    }


    public void setOnDoubleTapListener(GestureDetector.OnDoubleTapListener onDoubleTapListener) {
        attach.setOnDoubleTapListener(onDoubleTapListener);
    }

    public void setOnScaleChangeListener(OnScaleChangedListener onScaleChangedListener) {
        attach.setOnScaleChangeListener(onScaleChangedListener);
    }

    public void setOnSingleFlingListener(OnSingleFlingListener onSingleFlingListener) {
        attach.setOnSingleFlingListener(onSingleFlingListener);
    }

    private void configureBounds() {
        if (mElement == null) {
            return;
        }

        final int dwidth = mElement.getWidth();
        final int dheight = mElement.getHeight();

        final int vwidth = getWidth() - getPaddingLeft() - getPaddingRight();
        final int vheight = getHeight() - getPaddingTop() - getPaddingBottom();

        final boolean fits = (dwidth < 0 || vwidth == dwidth)
                && (dheight < 0 || vheight == dheight);

        if (dwidth <= 0 || dheight <= 0) {
            /* If the drawable has no intrinsic size, or we're told to
                scaletofit, then we just fill our entire view.
            */

            mElement.setBounds(0, 0, vwidth, vheight);
            mDrawMatrix = null;
        } else {
            // We need to do the scaling ourself, so have the drawable
            // use its native size.
            mElement.setBounds(0, 0, dwidth, dheight);

                if (mMatrix.isIdentity()) {
                    mDrawMatrix = null;
                } else {
                    mDrawMatrix = mMatrix;
                }


        }
    }

}
