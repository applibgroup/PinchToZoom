package com.bogdwellers.pinchtozoom.animation;


import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;
import ohos.agp.animation.AnimatorValue;
import ohos.agp.utils.Matrix;

/**
 * Created by Martin on 12-10-2016.
 */

public class ScaleAnimatorHandler extends AbsCorrectorAnimatorHandler {


    public float getPx() {
        return px;
    }

    public void setPx(float px) {
        this.px = px;
    }

    public float getPy() {
        return py;
    }

    public void setPy(float py) {
        this.py = py;
    }

    public boolean isTranslate() {
        return translate;
    }

    public void setTranslate(boolean translate) {
        this.translate = translate;
    }

    private float px;
    private float py;
    private boolean translate;

    public ScaleAnimatorHandler(ImageMatrixCorrector corrector) {
        super(corrector);
        this.translate = false;
    }

    public ScaleAnimatorHandler(ImageMatrixCorrector corrector, float px, float py) {
        super(corrector);
        this.px = px;
        this.py = py;
        this.translate = true;
    }

    @Override
    public void onUpdate(AnimatorValue animation, float v) {
        ImageMatrixCorrector corrector = getCorrector();
        PhotoView photoView = corrector.getImageView();
        if (photoView.getImageElement() != null) {
            Matrix matrix = photoView.getImageMatrix();
            float[] values = getValues();
            matrix.getElements(values);

            float sx = v;
            sx = corrector.correctAbsolute(MatrixEx.MSCALE_X, sx) / values[MatrixEx.MSCALE_X];

            if (translate) {
                matrix.postScale(sx, sx, px, py);
            } else {
                matrix.postScale(sx, sx);
            }
            corrector.performAbsoluteCorrections();
            photoView.setImageMatrix(matrix);
            photoView.invalidate();
        }

    }




}
