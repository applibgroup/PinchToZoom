package com.bogdwellers.pinchtozoom.util;

import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.RectFloat;
import ohos.media.image.PixelMap;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Martin on 15-10-2016.
 */

public class ImageViewUtils {

    /**
     * ImageViewUtils.
     */
    private ImageViewUtils() {

    }


    /**
     * updateImageViewMatrix.
     *
     * @param photoView image
     * @param bitmap hash
     */
    public static final void updateImageViewMatrix(PhotoView photoView, PixelMap bitmap) {
        updateImageViewMatrix(photoView, bitmap.getImageInfo().size.width, bitmap.getImageInfo().size.height);
    }

    /**
     * updateImageViewMatrix.
     *
     * @param photoView image
     * @param bitmapDrawable hashElement
     */
    public static final void updateImageViewMatrix(PhotoView photoView, PixelMapElement bitmapDrawable) {
        updateImageViewMatrix(photoView, bitmapDrawable.getWidth(), bitmapDrawable.getHeight());
    }

    /**
     * updateImageViewMatrix.
     *
     * @param photoView - image.
     * @param width - width.
     * @param height - height.
     */
    public static final void updateImageViewMatrix(PhotoView photoView, float width, float height) {
        Element drawable = photoView.getImageElement();
        if (drawable == null) {
            throw new NullPointerException("ImageView drawable is null");
        }

        Matrix matrix = photoView.getImageMatrix();
        if (!matrix.isIdentity()) {
            float[] values = new float[9];
            matrix.getElements(values);

            RectFloat src = new RectFloat();
            src.left = 0;
            src.top = 0;
            src.right = width;
            src.bottom = height;

            RectFloat dst = new RectFloat();
            dst.left = values[MatrixEx.MTRANS_X];
            dst.top = values[MatrixEx.MTRANS_Y];
            dst.right = dst.left + (drawable.getWidth() * values[MatrixEx.MSCALE_X]);
            dst.bottom = dst.top + (drawable.getHeight() * values[MatrixEx.MSCALE_Y]);

            matrix.setRectToRect(src, dst, Matrix.ScaleToFit.CENTER);
        }
    }
}
