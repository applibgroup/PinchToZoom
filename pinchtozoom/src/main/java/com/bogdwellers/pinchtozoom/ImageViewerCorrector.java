package com.bogdwellers.pinchtozoom;
import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;


/**
 * <p>This <code>MatrixCorrector</code> implementation defines the default behavior for an image viewer.</p>
 * <p>It works properly only if the following two conditions are met:</p>
 * <ol>
 * <li>There are no rotations</li>
 * <li>The scaling is uniform: <code>sx</code> and <code>sy</code> are always the same value</li>
 * </ol>
 * @author Martin
 *
 */
public class ImageViewerCorrector extends ImageMatrixCorrector {

	public static final String TAG = ImageViewerCorrector.class.getSimpleName();
	
	/*
	 * Attributes
	 */
	
	private float maxScale;
	private boolean maxScaleRelative;
	
	/*
	 * Constructor(s)
	 */
	
	public ImageViewerCorrector() {
		this(null, 4f);
	}
	
	public ImageViewerCorrector(PhotoView imageView, float maxScale) {
		super();
		if(imageView != null) setImageView(imageView);
		this.maxScale = maxScale;
	}

	/*
	 * Class methods
	 */

	/**
	 * <p>Returns the maximum allowed scale.</p>
	 * @return
     */
	public float getMaxScale() {
		return maxScale;
	}

	/**
	 * <p>Sets the maximum allowed scale.</p>
	 * @param maxScale
     */
	public void setMaxScale(float maxScale) {
		this.maxScale = maxScale;
	}

	/**
	 * <p>Indicates whether the maximum scale should be relative to the inner fit scale.</p>
	 * @return
     */
	public boolean isMaxScaleRelative() {
		return maxScaleRelative;
	}

	/**
	 * <p>Sets whether the maximum scale should be relative to the inner fit scale.</p>
	 * @param maxScaleRelative
     */
	public void setMaxScaleRelative(boolean maxScaleRelative) {
		this.maxScaleRelative = maxScaleRelative;
	}

	/*
	 * Overrides
	 */
	
	@Override
	public void performAbsoluteCorrections() {
		super.performAbsoluteCorrections();
		
		// Calculate the image's new dimensions
		updateScaledImageDimensions();

		// Correct scale

		// Correct the translations
		float[] values = getValues();
		values[MatrixEx.MTRANS_X] = correctAbsolute(MatrixEx.MTRANS_X, values[MatrixEx.MTRANS_X]);
		values[MatrixEx.MTRANS_Y] = correctAbsolute(MatrixEx.MTRANS_Y, values[MatrixEx.MTRANS_Y]);

		// Update the matrix
		getMatrix().setElements(values);
	}

	@Override
	public float correctAbsolute(int vector, float x) {
		switch(vector) {
			case MatrixEx.MTRANS_X:
				return correctTranslation(x, getImageView().getWidth(), getScaledImageWidth());
			case MatrixEx.MTRANS_Y:
				return correctTranslation(x, getImageView().getHeight(), getScaledImageHeight());
			case MatrixEx.MSCALE_X:
			case MatrixEx.MSCALE_Y:
				float innerFitScale = getInnerFitScale();
				float maxScal = maxScaleRelative ? innerFitScale * this.maxScale : this.maxScale;
				return Math.max(Math.min(x, maxScal), innerFitScale);
			default:
				throw new IllegalArgumentException("Vector not supported");
		}
	}

	/*
	 * Static methods
	 */

	/**
	 * <p>Corrects the translation so that it does not exceed the allowed bounds.</p>
	 * @param translation
	 * @param viewDim
	 * @param imgDim
	 * @return
	 */
	public static final float correctTranslation(float translation, float viewDim, float imgDim) {
		if(imgDim < viewDim) {
			// Must center
			translation = (viewDim / 2) - (imgDim / 2);
		} else {
			float diff = imgDim - viewDim;
			translation = Math.max(Math.min(0, translation), -diff);
		}
		return translation;
	}
}
