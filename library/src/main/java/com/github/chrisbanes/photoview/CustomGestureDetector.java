/*
 * Copyright 2011, 2012 Chris Banes.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.chrisbanes.photoview;

import com.github.chrisbanes.photoview.gesture.ScaleGestureDetector;

import ohos.agp.components.Component;
import ohos.agp.components.VelocityDetector;
import ohos.app.Context;
import ohos.multimodalinput.event.TouchEvent;

/**
 * Does a whole lot of gesture detecting.
 */
class CustomGestureDetector {
    private static final int INVALID_POINTER_ID = -1;

    private int mActivePointerId = INVALID_POINTER_ID;
    private int mActivePointerIndex = 0;
    private final ScaleGestureDetector mDetector;

    private VelocityDetector mVelocityTracker;
    private boolean mIsDragging;
    private float mLastTouchX;
    private float mLastTouchY;
    private final float mTouchSlop;
    private final float mMinimumVelocity;
    private OnGestureListener mListener;

    CustomGestureDetector(Context context, OnGestureListener listener) {
        mMinimumVelocity = 50;
        mTouchSlop = 8;

        mListener = listener;
        ScaleGestureDetector.OnScaleGestureListener mScaleListener =
                new ScaleGestureDetector.OnScaleGestureListener() {
                    @Override
                    public boolean onScale(ScaleGestureDetector detector) {
                        float scaleFactor = detector.getScaleFactor();

                        if (Float.isNaN(scaleFactor) || Float.isInfinite(scaleFactor)) return false;

                        if (scaleFactor >= 0) {
                            mListener.onScale(scaleFactor, detector.getFocusX(), detector.getFocusY());
                        }
                        return true;
                    }

                    @Override
                    public boolean onScaleBegin(ScaleGestureDetector detector) {
                        return true;
                    }

                    @Override
                    public void onScaleEnd(ScaleGestureDetector detector) {
                        // NO-OP
                    }
                };
        mDetector = new ScaleGestureDetector(context, mScaleListener);
    }

    private float getActiveX(TouchEvent ev,Component component) {
        try {
            return Compat.getTouchX(ev,mActivePointerIndex,component);
        } catch (Exception e) {
            return Compat.getTouchX(ev,0,component);
        }
    }

    private float getActiveY(TouchEvent ev,Component component) {
        try {
            return Compat.getTouchY(ev,mActivePointerIndex,component);
        } catch (Exception e) {
            return Compat.getTouchY(ev,0,component);
        }
    }

    public boolean isScaling() {
        return mDetector.isInProgress();
    }

    public boolean isDragging() {
        return mIsDragging;
    }

    public boolean onTouchEvent(TouchEvent ev, Component component) {
        try {
            mDetector.onTouchEvent(ev,component);
            return processTouchEvent(ev,component);
        } catch (IllegalArgumentException e) {
            // Fix for support lib bug, happening when onDestroy is called
            return true;
        }
    }

    private boolean processTouchEvent(TouchEvent ev,Component component) {
        final int action = ev.getAction();
        switch (action) {
            case TouchEvent.PRIMARY_POINT_DOWN:
                mActivePointerId = ev.getPointerId(0);

                mVelocityTracker = VelocityDetector.obtainInstance();
                if (null != mVelocityTracker) {
                    mVelocityTracker.addEvent(ev);
                }

                mLastTouchX = getActiveX(ev,component);
                mLastTouchY = getActiveY(ev,component);
                mIsDragging = false;
                break;
            case TouchEvent.POINT_MOVE:
                final float x = getActiveX(ev,component);
                final float y = getActiveY(ev,component);
                final float dx = x - mLastTouchX, dy = y - mLastTouchY;

                if (!mIsDragging) {
                    // Use Pythagoras to see if drag length is larger than
                    // touch slop
                    mIsDragging = Math.sqrt((dx * dx) + (dy * dy)) >= mTouchSlop;
                }

                if (mIsDragging) {
                    mListener.onDrag(dx, dy);
                    mLastTouchX = x;
                    mLastTouchY = y;

                    if (null != mVelocityTracker) {
                        mVelocityTracker.addEvent(ev);
                    }
                }
                break;
            case TouchEvent.CANCEL:
                mActivePointerId = INVALID_POINTER_ID;
                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.clear();
                    mVelocityTracker = null;
                }
                break;
            case TouchEvent.PRIMARY_POINT_UP:
                mActivePointerId = INVALID_POINTER_ID;
                if (mIsDragging) {
                    if (null != mVelocityTracker) {
                        mLastTouchX = getActiveX(ev,component);
                        mLastTouchY = getActiveY(ev,component);

                        // Compute velocity within the last 1000ms
                        mVelocityTracker.addEvent(ev);
                        mVelocityTracker.calculateCurrentVelocity(1000);

                        final float vX = mVelocityTracker.getHorizontalVelocity(),
                                vY = mVelocityTracker.getVerticalVelocity();

                        // If the velocity is greater than minVelocity, call
                        // listener
                        if (Math.max(Math.abs(vX), Math.abs(vY)) >= mMinimumVelocity) {
                            mListener.onFling(mLastTouchX, mLastTouchY, -vX, -vY);
                        }
                    }
                }

                // Recycle Velocity Tracker
                if (null != mVelocityTracker) {
                    mVelocityTracker.clear();
                    mVelocityTracker = null;
                }
                break;
            case TouchEvent.OTHER_POINT_UP:
                final int pointerIndex = ev.getIndex();
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = Compat.getTouchX(ev,newPointerIndex,component);
                    mLastTouchY = Compat.getTouchY(ev,newPointerIndex,component);
                }
                break;
        }

        mActivePointerIndex = mActivePointerId != INVALID_POINTER_ID ? mActivePointerId : 0;
        return true;
    }
}
