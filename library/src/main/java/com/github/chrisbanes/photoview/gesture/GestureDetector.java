package com.github.chrisbanes.photoview.gesture;

import com.github.chrisbanes.photoview.Compat;
import ohos.agp.components.Component;
import ohos.agp.components.VelocityDetector;
import ohos.agp.utils.MimeData;
import ohos.app.Context;
import ohos.eventhandler.EventHandler;
import ohos.eventhandler.EventRunner;
import ohos.eventhandler.InnerEvent;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

public class GestureDetector {
    public interface OnGestureListener {
        boolean onDown(TouchEvent e);

        void onShowPress(TouchEvent e);

        boolean onSingleTapUp(TouchEvent e);

        boolean onScroll(TouchEvent e1, TouchEvent e2, float distanceX, float distanceY);

        void onLongPress(TouchEvent e);

        boolean onFling(TouchEvent e1, TouchEvent e2, float velocityX, float velocityY);
    }

    public interface OnDoubleTapListener {
        boolean onSingleTapConfirmed(TouchEvent e);

        boolean onDoubleTap(TouchEvent e);

        boolean onDoubleTapEvent(TouchEvent e);
    }


    private static String TAG = "GestureDetector";

    /**
     * Button constant: Secondary button (right mouse button).
     */
    private static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__LONG_PRESS = 1;

    private static final int TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__DEEP_PRESS = 2;

    private int mTouchSlopSquare;
    private int mDoubleTapTouchSlopSquare;
    private int mDoubleTapSlopSquare;

    private int mMinimumFlingVelocity;
    private int mMaximumFlingVelocity;

    private static final int LONGPRESS_TIMEOUT = 500;
    private static final int TAP_TIMEOUT = 100;
    private static final int DOUBLE_TAP_TIMEOUT = 300;
    private static final int DOUBLE_TAP_MIN_TIME = 40;

    // constants for Message.what used by GestureHandler below
    private static final int SHOW_PRESS = 1;
    private static final int LONG_PRESS = 2;
    private static final int TAP = 3;

    private final EventHandler mHandler;

    private final OnGestureListener mListener;
    private OnDoubleTapListener mDoubleTapListener;

    private boolean mStillDown;
    private boolean mDeferConfirmSingleTap;
    private boolean mInLongPress;
    private boolean mInContextClick;

    private boolean mAlwaysInTapRegion;
    private boolean mAlwaysInBiggerTapRegion;
    private boolean mIgnoreNextUpEvent;

    private TouchEvent mCurrentDownEvent;

    /**
     * True when the user is still touching for the second tap (down, move, and
     * up events). Can only be true if there is a double tap listener attached.
     */
    private boolean mIsDoubleTapping;

    private float mLastFocusX;
    private float mLastFocusY;
    private float mDownFocusX;
    private float mDownFocusY;

    private boolean mIsLongpressEnabled;

    private long mDownTime;
    private long mLastUpTime;

    private MmiPoint mDownPoint;
    private MmiPoint mLastUpPoint;
    /**
     * Determines speed during touch scrolling
     */
    private VelocityDetector mVelocityDetector;

    private class GestureHandler extends EventHandler {
        GestureHandler() {
            super(EventRunner.current());
        }

        GestureHandler(EventHandler handler) {
            super(handler.getEventRunner());
        }

        @Override
        public void processEvent(InnerEvent msg) {
            switch (msg.eventId) {
                case SHOW_PRESS:
                    mListener.onShowPress(mCurrentDownEvent);
                    break;

                case LONG_PRESS:
                    dispatchLongPress();
                    break;

                case TAP:
                    // If the user's finger is still down, do not count it as a tap
                    if (mDoubleTapListener != null) {
                        if (!mStillDown) {
                            mDoubleTapListener.onSingleTapConfirmed(mCurrentDownEvent);
                        } else {
                            mDeferConfirmSingleTap = true;
                        }
                    }
                    break;

                default:
                    throw new RuntimeException("Unknown message " + msg); // never
            }
        }
    }

    public static class SimpleOnGestureListener
            implements OnGestureListener, OnDoubleTapListener {
        public boolean onSingleTapUp(TouchEvent e) {
            return false;
        }

        public void onLongPress(TouchEvent e) {}

        public boolean onScroll(TouchEvent e1, TouchEvent e2, float distanceX, float distanceY) {
            return false;
        }

        public boolean onFling(TouchEvent e1, TouchEvent e2, float velocityX, float velocityY) {
            return false;
        }

        public void onShowPress(TouchEvent e) {}

        public boolean onDown(TouchEvent e) {
            return false;
        }

        public boolean onDoubleTap(TouchEvent e) {
            return false;
        }

        public boolean onDoubleTapEvent(TouchEvent e) {
            return false;
        }

        public boolean onSingleTapConfirmed(TouchEvent e) {
            return false;
        }

    }

    public GestureDetector(Context context, OnGestureListener listener) {
        this(context, listener, null);
    }

    public GestureDetector(Context context, OnGestureListener listener, EventHandler handler) {
        if (handler != null) {
            mHandler = new GestureHandler(handler);
        } else {
            mHandler = new GestureHandler();
        }
        mListener = listener;
        if (listener instanceof OnDoubleTapListener) {
            setOnDoubleTapListener((OnDoubleTapListener) listener);
        }

        init(context);
    }



    private void init(Context context) {
        if (mListener == null) {
            throw new NullPointerException("OnGestureListener must not be null");
        }
        mIsLongpressEnabled = true;

        // Fallback to support pre-donuts releases
        int touchSlop, doubleTapSlop, doubleTapTouchSlop;
        touchSlop = 8;
        doubleTapTouchSlop = 8;
        doubleTapSlop = 100;
        mMinimumFlingVelocity = 50;
        mMaximumFlingVelocity = 5000;

        mTouchSlopSquare = touchSlop * touchSlop;
        mDoubleTapTouchSlopSquare = doubleTapTouchSlop * doubleTapTouchSlop;
        mDoubleTapSlopSquare = doubleTapSlop * doubleTapSlop;
    }

    public void setOnDoubleTapListener(OnDoubleTapListener onDoubleTapListener) {
        mDoubleTapListener = onDoubleTapListener;
    }


    public boolean onTouchEvent(TouchEvent ev, Component component) {
        final int action = ev.getAction();

        if (mVelocityDetector == null) {
            mVelocityDetector = VelocityDetector.obtainInstance();
        }
        mVelocityDetector.addEvent(ev);

        final boolean pointerUp = (action & 0xff) == TouchEvent.OTHER_POINT_UP;
        final int skipIndex = pointerUp ? ev.getIndex() : -1;

        float sumX = 0, sumY = 0;
        final int count = ev.getPointerCount();
        for (int i = 0; i < count; i++) {
            if (skipIndex == i) continue;
            sumX += Compat.getTouchX(ev,i,component);
            sumY += Compat.getTouchY(ev,i,component);
        }
        final int div = pointerUp ? count - 1 : count;
        final float focusX = sumX / div;
        final float focusY = sumY / div;
        boolean handled = false;

        switch (action & 0xff) {
            case TouchEvent.OTHER_POINT_DOWN:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                cancelTaps();
                break;
            case TouchEvent.OTHER_POINT_UP:
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;

                break;
            case TouchEvent.PRIMARY_POINT_UP:
                mStillDown = false;

                if (mIsDoubleTapping) {
                    // Finally, give the up event of the double-tap
                    handled = mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (mInLongPress) {
                    mHandler.removeEvent(TAP);
                    mInLongPress = false;
                } else if (mAlwaysInTapRegion && !mIgnoreNextUpEvent) {
                    handled = mListener.onSingleTapUp(ev);
                    if (mDeferConfirmSingleTap && mDoubleTapListener != null) {
                        mDoubleTapListener.onSingleTapConfirmed(ev);
                    }
                } else if (!mIgnoreNextUpEvent) {
                    mVelocityDetector.calculateCurrentVelocity(1000, mMaximumFlingVelocity, mMaximumFlingVelocity);
                    float velocityX = mVelocityDetector.getHorizontalVelocity();
                    float velocityY = mVelocityDetector.getVerticalVelocity();

                    if ((Math.abs(velocityY) > mMinimumFlingVelocity)
                            || (Math.abs(velocityX) > mMinimumFlingVelocity)) {
                        handled = mListener.onFling(mCurrentDownEvent, ev, velocityX, velocityY);
                    }
                }

                mLastUpTime = ev.getOccurredTime();
                mLastUpPoint = Compat.getTouchPoint(ev,0,component);
                if (mVelocityDetector != null) {
                    // This may have been cleared when we called out to the
                    // application above.
                    mVelocityDetector.clear();
                    mVelocityDetector = null;
                }
                mIsDoubleTapping = false;
                mDeferConfirmSingleTap = false;
                mIgnoreNextUpEvent = false;
                mHandler.removeEvent(SHOW_PRESS);
                mHandler.removeEvent(LONG_PRESS);
                break;
            case TouchEvent.PRIMARY_POINT_DOWN:
                if (mDoubleTapListener != null) {
                    boolean hadTapMessage = mHandler.hasInnerEvent(TAP);
                    if (hadTapMessage) mHandler.removeEvent(TAP);
                    if ((mDownPoint != null)
                            && (mLastUpPoint != null)
                            && hadTapMessage
                            && isConsideredDoubleTap(mDownPoint, mDownTime, mLastUpPoint, mLastUpTime)) {
                        // This is a second tap
                        mIsDoubleTapping = true;
                        // Give a callback with the first tap of the double-tap
                        handled |= mDoubleTapListener.onDoubleTap(mCurrentDownEvent);
                        // Give a callback with down event of the double-tap
                        handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                    } else {
                        // This is a first tap
                        mHandler.sendEvent(TAP, DOUBLE_TAP_TIMEOUT);
                    }
                }
                mDownFocusX = mLastFocusX = focusX;
                mDownFocusY = mLastFocusY = focusY;
                if (mCurrentDownEvent != null) {
                    mCurrentDownEvent = null;
                }
                mDownTime = ev.getOccurredTime();
                mDownPoint = Compat.getTouchPoint(ev,0,component);
                mCurrentDownEvent = ev;

                mAlwaysInTapRegion = true;
                mAlwaysInBiggerTapRegion = true;
                mStillDown = true;
                mInLongPress = false;
                mDeferConfirmSingleTap = false;

                if (mIsLongpressEnabled) {
                    mHandler.removeEvent(LONG_PRESS);
                    mHandler.sendTimingEvent(
                            InnerEvent.get(LONG_PRESS, TOUCH_GESTURE_CLASSIFIED__CLASSIFICATION__LONG_PRESS),
                            ev.getOccurredTime() + 500);
                }
                mHandler.sendTimingEvent(InnerEvent.get(SHOW_PRESS), ev.getOccurredTime() + TAP_TIMEOUT);
                handled |= mListener.onDown(ev);
                break;
            case TouchEvent.POINT_MOVE:
                if (mInLongPress || mInContextClick) {
                    break;
                }

                final float scrollX = mLastFocusX - focusX;
                final float scrollY = mLastFocusY - focusY;

                if (mIsDoubleTapping) {
                    handled |= mDoubleTapListener.onDoubleTapEvent(ev);
                } else if (mAlwaysInTapRegion) {
                    final int deltaX = (int) (focusX - mDownFocusX);
                    final int deltaY = (int) (focusY - mDownFocusY);
                    int distance = (deltaX * deltaX) + (deltaY * deltaY);
                    if (distance > mTouchSlopSquare) {
                        handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                        mLastFocusX = focusX;
                        mLastFocusY = focusY;
                        mAlwaysInTapRegion = false;
                        mHandler.removeEvent(SHOW_PRESS);
                        mHandler.removeEvent(LONG_PRESS);
                        mHandler.removeEvent(TAP);
                    }
                    if (distance > mDoubleTapTouchSlopSquare) {
                        mAlwaysInBiggerTapRegion = false;
                    }
                } else if ((Math.abs(scrollX) >= 1) || (Math.abs(scrollY) >= 1)) {
                    handled = mListener.onScroll(mCurrentDownEvent, ev, scrollX, scrollY);
                    mLastFocusX = focusX;
                    mLastFocusY = focusY;
                }

                break;

            case TouchEvent.CANCEL:
                cancel();
                break;
            default:
                break;
        }

        return handled;
    }

    private void cancel() {
        mHandler.removeEvent(SHOW_PRESS);
        mHandler.removeEvent(LONG_PRESS);
        mHandler.removeEvent(TAP);
        mVelocityDetector.clear();
        mVelocityDetector = null;
        mIsDoubleTapping = false;
        mStillDown = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        mInLongPress = false;
        mInContextClick = false;
        mIgnoreNextUpEvent = false;
    }

    private void cancelTaps() {
        mHandler.removeEvent(SHOW_PRESS);
        mHandler.removeEvent(LONG_PRESS);
        mHandler.removeEvent(TAP);
        mIsDoubleTapping = false;
        mAlwaysInTapRegion = false;
        mAlwaysInBiggerTapRegion = false;
        mDeferConfirmSingleTap = false;
        mInLongPress = false;
        mInContextClick = false;
        mIgnoreNextUpEvent = false;
    }

    private boolean isConsideredDoubleTap(MmiPoint firstDown, long firstUp, MmiPoint secondDown, long secondDownTime) {
        if (!mAlwaysInBiggerTapRegion) {
            return false;
        }
        final long deltaTime = secondDownTime - firstUp;
        if (deltaTime > DOUBLE_TAP_TIMEOUT || deltaTime < DOUBLE_TAP_MIN_TIME) {
            return false;
        }

        int deltaX = (int) firstDown.getX() - (int) secondDown.getX();
        int deltaY = (int) firstDown.getY() - (int) secondDown.getY();
        int slopSquare = mDoubleTapSlopSquare;
        return (deltaX * deltaX + deltaY * deltaY < slopSquare);
    }

    /**
     * Analyzes the given generic motion event and if applicable triggers the
     * appropriate callbacks on the {@link OnGestureListener} supplied.
     *
     * @param ev The current motion event.
     *
     * @return true if the {@link OnGestureListener} consumed the event,
     *              else false.
     *
     */
    public boolean onGenericMotionEvent(TouchEvent ev) {
        return false;
    }

    private void dispatchLongPress() {
        mHandler.removeEvent(TAP);
        mDeferConfirmSingleTap = false;
        mInLongPress = true;
        mListener.onLongPress(mCurrentDownEvent);
    }
}
