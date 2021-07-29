package com.bogdwellers.pinchtozoom;

import ohos.agp.components.element.Element;
import ohos.agp.utils.Matrix;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * ImageMatrixCorrector.
 *
 * @author Martin - Martin.
 *
 */
public abstract class ImageMatrixCorrector extends MatrixCorrector {

    private PhotoView photoView;
    private float scaledImageWidth;
    private float scaledImageHeight;

    @Override
    public void setMatrix(Matrix matrix) {
        super.setMatrix(matrix);
        updateScaledImageDimensions();
    }

    /**
     * Sets the ImageView. This also sets its inner image matrix as this corrector's matrix automatically.
     *
     * @param imageView - imageView.
     */
    public void setImageView(PhotoView imageView) {

        this.photoView = imageView;
        if (imageView != null) {
            Matrix matrix = imageView.getImageMatrix();
            setMatrix(matrix);
        }
    }

    /**
     * PhotoView.
     *
     * @return - returns.
     */
    public PhotoView getImageView() {
        return photoView;
    }

    /**
     * getInnerFirScale.
     *
     * @return - return.
     */
    public float getInnerFitScale() {
        Element drawable = photoView.getImageElement();
        float widthRatio = (float) drawable.getWidth() / photoView.getWidth();
        float heightRatio = (float) drawable.getHeight() / photoView.getHeight();
        if (widthRatio > heightRatio) {
            return 1f / widthRatio;
        } else {
            return 1f / heightRatio;
        }
    }

    /**
     * (Re)calculates the image's current dimensions.
     */
    protected void updateScaledImageDimensions() {
        float[] values = getValues();
        Element drawable = photoView.getImageElement();
        if (drawable != null) {
            scaledImageWidth = values[MatrixEx.MSCALE_X] * drawable.getWidth();
            scaledImageHeight = values[MatrixEx.MSCALE_Y] * drawable.getHeight();
        } else {
            scaledImageWidth = scaledImageHeight = 0f;
        }
    }

    /**
     * Returns the width of the scaled image.
     *
     * @return - return.
     */
    protected float getScaledImageWidth() {
        return scaledImageWidth;
    }

    /**
     * Returns the height of the scaled image.
     *
     * @return - return.
     */
    protected float getScaledImageHeight() {
        return scaledImageHeight;
    }

    public abstract float correctAbsolute(int vector, float x);
}
