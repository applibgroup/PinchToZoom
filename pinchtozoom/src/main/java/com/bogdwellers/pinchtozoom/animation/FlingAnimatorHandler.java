package com.bogdwellers.pinchtozoom.animation;

import ohos.agp.animation.AnimatorValue;
import ohos.agp.utils.Matrix;
import com.bogdwellers.pinchtozoom.ImageMatrixCorrector;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;

/**
 * Created by Martin on 12-10-2016.
 */

public class FlingAnimatorHandler extends AbsCorrectorAnimatorHandler {

    public static final String PROPERTY_TRANSLATE_X = "translateX";
    public static final String PROPERTY_TRANSLATE_Y = "translateY";

    public FlingAnimatorHandler(ImageMatrixCorrector corrector) {
        super(corrector);
    }

    @Override
    public void onUpdate(AnimatorValue animation, float v) {
        ImageMatrixCorrector corrector = getCorrector();
        PhotoView photoView = corrector.getImageView();
        Matrix matrix = photoView.getImageMatrix();
        float[] values = getValues();
        matrix.getElements(values);

        float[] animValues = {0f, 1f};
        float dx = FlingAnimatorHandler.getAnimatedValue(v, animValues);
        dx = corrector.correctAbsolute(MatrixEx.MTRANS_X, dx) - values[MatrixEx.MTRANS_X];
        float dy = FlingAnimatorHandler.getAnimatedValue(v, values);
        dy = corrector.correctAbsolute(MatrixEx.MTRANS_Y, dy) - values[MatrixEx.MTRANS_Y];

        matrix.postTranslate(dx, dy);
        photoView.invalidate();

    }

    /**
     * get the animated value with fraction and values.
     *
     * @param fraction 0~1
     * @param values float array
     * @return float animated value
     */
    public static float getAnimatedValue(float fraction, float... values) {
        if (values == null || values.length == 0) {
            return 0;
        }
        if (values.length == 1) {
            return values[0] * fraction;
        } else {
            if (fraction == 1) {
                return values[values.length - 1];
            }
            float oneFraction = 1f / (values.length - 1);
            float offFraction = 0;
            for (int i = 0; i < values.length - 1; i++) {
                if (offFraction + oneFraction >= fraction) {
                    return values[i] + (fraction - offFraction) * (values.length - 1) * (values[i + 1] - values[i]);
                }
                offFraction += oneFraction;
            }
        }
        return 0;
    }

}
