package com.github.chrisbanes.photoview;

import ohos.multimodalinput.event.TouchEvent;

public interface OnDoubleTapListener {
    boolean onSingleTapConfirmed(TouchEvent e);

    /**
     * Notified when a double-tap occurs. Triggered on the down event of second tap.
     *
     * @param e The down motion event of the first tap of the double-tap.
     * @return true if the event is consumed, else false
     */
    boolean onDoubleTap(TouchEvent e);

    /**
     * Notified when an event within a double-tap gesture occurs, including
     * the down, move, and up events.
     *
     * @param e The motion event that occurred during the double-tap gesture.
     * @return true if the event is consumed, else false
     */
    boolean onDoubleTapEvent(TouchEvent e);
}
