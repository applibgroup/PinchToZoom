/* * Copyright (C) 2021 Huawei Device Co., Ltd.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bogdwellers.pinchtozoom.util;

/**
 * PointF.
 */
public class PointF {
    public float mx;
    public float my;

    public PointF() {
    }

    public PointF(float mx, float my) {
        this.mx = mx;
        this.my = my;
    }

    public PointF(Point p) {
        this.mx = p.mx;
        this.my = p.my;
    }

    /**
     * Set the point's mx and my coordinates.
     *
     * @param x - mx.
     * @param y - my.
     */
    public final void set(float x, float y) {
        this.mx = x;
        this.my = y;
    }

    /**
     * Set the point's mx and my coordinates to the coordinates of p.
     *
     * @param p p
     */
    public final void set(PointF p) {
        this.mx = p.mx;
        this.my = p.my;
    }

    public final void negate() {
        mx = -mx;
        my = -my;
    }

    public final void offset(float dx, float dy) {
        mx += dx;
        my += dy;
    }

    /**
     * Returns true if the point's coordinates equal (mx,my).
     *
     * @param x mx
     * @param y my
     * @return boolean
     */
    public final boolean equals(float x, float y) {
        return this.mx == x && this.my == y;
    }

    /**
     * Return the euclidian distance from (0,0) to the point.
     *
     * @return length
     */
    public final float length() {
        return length(mx, my);
    }

    /**
     * Returns the euclidian distance from (0,0) to (mx,my).
     *
     * @param x mx
     * @param y my
     * @return length
     */
    public static float length(float x, float y) {
        return (float) Math.hypot(x, y);
    }
}