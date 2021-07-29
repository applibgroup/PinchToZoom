package com.bogdwellers.pinchtozoom.animation;

import ohos.agp.animation.AnimatorValue;
import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;

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
