package com.bogdwellers.pinchtozoom;

import ohos.agp.utils.Matrix;
import com.bogdwellers.pinchtozoom.util.MatrixEx;

/**
 * <p>The <code>MatrixCorrector</code> enforces boundaries in the transformation of a <code>Matrix</code>.</p>
 *
 * @author Martin
 *
 */
public abstract class MatrixCorrector {
    /*
     * Attributes
     */
    private Matrix matrix;
    private float[] values;
    
    /*
     * Constructor(s)
     */
    
    protected MatrixCorrector() {
        this(null);
    }
    
    protected MatrixCorrector(Matrix matrix) {
        this.matrix = matrix;
        this.values = new float[9];
    }
    
    /*
     * Class methods
     */

    /**
     * <p>Does corrections AFTER matrix operations have been applied.</p>
     * <p>This implementation only copies the values of the matrix into its float array <code>values</code>.</p>
     */
    public void performAbsoluteCorrections() {

    }

    /**
     * <p>Returns the corrected value of the given relative vector.</p>
     *
     * @param vector int.
     * @param x int.
     * @return float.
     */
    public float correctRelative(int vector, float x) {
        float v = getValues()[vector];
        switch (vector) {
            case MatrixEx.MTRANS_X:
            case MatrixEx.MTRANS_Y:
                return correctAbsolute(v + x) - v;
            case MatrixEx.MSCALE_X:
            case MatrixEx.MSCALE_Y:
                return correctAbsolute(v * x) / v;
            default:
                throw new IllegalArgumentException("Vector not supported");
        }
    }

    /**
     * Returns the corrected value of the given absolute vector.
     *
     * @param x - x.
     * @return - returns.
     */
    public float correctAbsolute(float x) {
        return x;
    }

    /**
     * Returns the matrix.
     *
     * @return - returns.
     */
    public Matrix getMatrix() {
        return matrix;
    }

    /**
     * Sets the matrix.
     *
     * @param matrix - matrix.
     */
    public void setMatrix(Matrix matrix) {
        this.matrix = matrix;
    }

    /**
     * Returns the matrix values.
     *
     * @return - returns.
     */
    protected float[] getValues() {
        matrix.getElements(values);
        return values;
    }
}
