package com.bogdwellers.pinchtozoom;

import com.bogdwellers.pinchtozoom.util.MatrixEx;
import com.github.chrisbanes.photoview.PhotoView;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Matrix;

/**
 * 
 * @author Martin
 *
 */
public abstract class ImageMatrixCorrector extends MatrixCorrector {
	
	/*
	 * Attributes
	 */

	private PhotoView photoView;
	private float scaledImageWidth;
	private float scaledImageHeight;

	/*
	 * Overrides
	 */

	@Override
	public void setMatrix(Matrix matrix) {
		super.setMatrix(matrix);
		updateScaledImageDimensions();
	}

	/*
	 * Class methods
	 */
	
	/**
	 * <p>Sets the <code>ImageView</code>. This also sets its inner image matrix as this corrector's matrix automatically.</p>
	 * @param imageView
	 */
	public void setImageView(PhotoView imageView) {

		this.photoView = imageView;
		if(imageView != null) {
			Matrix matrix=imageView.getImageMatrix();
			setMatrix(matrix);
		}
	}

	/**
	 *
	 * @return
     */
	public PhotoView getImageView() {
		return photoView;
	}

	/**
	 *
	 * @return
     */
	public float getInnerFitScale() {
		Element drawable = photoView.getImageElement();
		float widthRatio = (float) drawable.getWidth() / photoView.getWidth();
		float heightRatio = (float) drawable.getHeight() / photoView.getHeight();
		if(widthRatio > heightRatio) {
			return 1f / widthRatio;
		} else {
			return 1f / heightRatio;
		}
	}
	
	/**
	 * <p>(Re)calculates the image's current dimensions.</p>
	 */
	protected void updateScaledImageDimensions() {
		float[] values = getValues();
		Element drawable = photoView.getImageElement();
		if(drawable != null) {
			scaledImageWidth = values[MatrixEx.MSCALE_X] * drawable.getWidth();
			scaledImageHeight = values[MatrixEx.MSCALE_Y] * drawable.getHeight();
		} else {
			scaledImageWidth = scaledImageHeight = 0f;
		}
	}

	/**
	 * <p>Returns the width of the scaled image.</p>
	 * @return
     */
	protected float getScaledImageWidth() { return scaledImageWidth; }

	/**
	 * * <p>Returns the height of the scaled image.</p>
	 * @return
     */
	protected float getScaledImageHeight() { return scaledImageHeight; }

	public abstract float correctAbsolute(int vector, float x);
}
