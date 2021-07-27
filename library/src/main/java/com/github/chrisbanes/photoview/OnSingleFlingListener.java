package com.github.chrisbanes.photoview;

import ohos.multimodalinput.event.TouchEvent;

/**
 * A callback to be invoked when the ImageView is flung with a single
 * touch
 */
public interface OnSingleFlingListener {
    /**
     * A callback to receive where the user flings on a ImageView. You will receive a callback if
     * the user flings anywhere on the view.
     *
     * @param e1        TouchEvent the user first touch.
     * @param e2        TouchEvent the user last touch.
     * @param velocityX distance of user's horizontal fling.
     * @param velocityY distance of user's vertical fling.
     * @return boolean
     */
    boolean onFling(TouchEvent e1, TouchEvent e2, float velocityX, float velocityY);
}
