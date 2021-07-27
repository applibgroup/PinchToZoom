package com.github.chrisbanes.photoview;

import ohos.agp.components.Component;

/**
 * Callback when the user tapped outside of the photo
 */
public interface OnOutsidePhotoTapListener {
    /**
     * The outside of the photo has been tapped
     * @param imageView Component
     */
    void onOutsidePhotoTap(Component imageView);
}
