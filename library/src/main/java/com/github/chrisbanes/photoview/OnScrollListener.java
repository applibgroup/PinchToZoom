package com.github.chrisbanes.photoview;

import ohos.multimodalinput.event.TouchEvent;

public interface OnScrollListener {
    boolean onScroll(TouchEvent e1, TouchEvent e2, float distanceX, float distanceY);
}
