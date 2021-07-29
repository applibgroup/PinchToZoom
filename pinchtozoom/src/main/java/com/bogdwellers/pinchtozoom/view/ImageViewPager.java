package com.bogdwellers.pinchtozoom.view;

import ohos.agp.components.AttrSet;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.PageSlider;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Matrix;
import ohos.app.Context;
import ohos.multimodalinput.event.TouchEvent;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;



/**
 * <p><code>ViewPager</code> implementation that allows sideways scrolling only when current image is not zoomed in.</p>
 * Created by Martin on 16-10-2016.
 */
public class ImageViewPager extends PageSlider implements Component.TouchEventListener {


    private static final float DEFAULT_SCALE_THRESHOLD = 1.2f;

    /*
     * Attributes
     */

    private float scaleThreshold;
    private int pointerCount;

    /*
     * Constructor(s)
     */

    /**
     * ImageViewPager.
     *
     * @param context context.
     */
    public ImageViewPager(Context context) {
        super(context);
        init();

    }

    /**
     *  ImageViewPager.
     *
     * @param context context
     * @param attrSet set
     * @param styleName Name
     */
    public ImageViewPager(Context context, AttrSet attrSet, String styleName) {
        super(context, attrSet, styleName);
        init();

    }

    public ImageViewPager(Context context, AttrSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        this.scaleThreshold = DEFAULT_SCALE_THRESHOLD;
        setTouchEventListener(this::onTouchEvent);
    }

    /*
     * Class methods
     */

    public float getScaleThreshold() {
        return this.scaleThreshold;
    }

    /**
     * <p>Sets the scale threshold.</p>
     *
     * @param scaleThreshold - scaleThreshold
     */
    public void setScaleThreshold(float scaleThreshold) {
        this.scaleThreshold = scaleThreshold;
    }

    /*
     * Overrides
     */

    @Override
    public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
        onInterceptTouchEvent(touchEvent);
        return true;
    }

    public boolean onInterceptTouchEvent(TouchEvent ev) {
        pointerCount = ev.getPointerCount();
        return false;
    }


    protected boolean can_Scroll(Component v, boolean checkV, float dx, float x, float y) {
        if (v instanceof PhotoView) {
            PhotoView iv = (PhotoView) v;
            Element drawable = iv.getImageElement();

            if (drawable != null) {
                float vw = iv.getWidth();
                float vh = iv.getHeight();
                float dw = drawable.getWidth();
                float dh = drawable.getHeight();

                Matrix matrix = iv.getImageMatrix();
                matrix.getElements(VALUES);
                float tx = VALUES[MatrixEx.MTRANS_X] + dx;
                float sdw = dw * VALUES[MatrixEx.MSCALE_X];

                return VALUES[MatrixEx.MSCALE_X] / centerInsideScale(vw, vh, dw, dh) > scaleThreshold
                        && !translationExceedsBoundary(tx, vw, sdw) && sdw > vw && pointerCount == 1;
                // Assumes mx-my scales are equal
            }
        }
        if (v instanceof ComponentContainer) {
            ComponentContainer group = (ComponentContainer) v;
            float scrollX = v.getContentPositionX();
            float scrollY = v.getContentPositionY();
            int count = group.getChildCount();

            for (int i = count - 1; i >= 0; --i) {
                Component child = group.getComponentAt(i);
                if (x + scrollX >= child.getLeft() && x + scrollX < child.getRight()
                        && y + scrollY >= child.getTop() && y + scrollY < child.getBottom()
                        && this.can_Scroll(child, true, dx,
                        x + scrollX - child.getLeft(), y + scrollY - child.getTop())) {
                    return true;
                }
            }
        }

        return checkV && v.canScroll((int) -dx);
    }


    /*
     * Static methods
     */

    /**
     * NOT Thread safe! (But it all happens on the UI thread anyway).
     */
    private static final float[] VALUES = new float[9];

    /**
     * <p>Returns the scale ratio between view and drawable for the longest side.</p>
     *
     * @param vw - viewWidth.
     * @param vh - viewHeight.
     * @param dw - drawable/element width.
     * @param dh - drawable/element height.
     * @return float.
     */
    public static final float centerInsideScale(float vw, float vh, float dw, float dh) {
        return vw / vh <= dw / dh ? vw / dw : vh / dh;
    }

    /**
     * <p>Determines whether a translation makes the view exceed the boundary of a drawable.</p>
     *
     * @param tx float
     * @param vw float
     * @param dw float
     * @return boolean
     */
    public static final boolean translationExceedsBoundary(float tx, float vw, float dw) {
        return dw >= vw && (tx > 0 || tx < vw - dw);
    }

}