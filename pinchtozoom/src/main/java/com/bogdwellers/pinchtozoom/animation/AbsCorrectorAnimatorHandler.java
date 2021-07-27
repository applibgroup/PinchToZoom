package com.bogdwellers.pinchtozoom.animation;


import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;
import ohos.agp.animation.AnimatorValue;

/**
 * Created by Martin on 13-10-2016.
 */

public abstract class AbsCorrectorAnimatorHandler implements AnimatorValue.ValueUpdateListener {

    private ImageMatrixCorrector corrector;
    private float[] values;

    protected AbsCorrectorAnimatorHandler(ImageMatrixCorrector corrector) {
        this.corrector = corrector;
        this.values = new float[9];
    }

    public ImageMatrixCorrector getCorrector() {
        return corrector;
    }

    protected float[] getValues() {
        return values;
    }
}
