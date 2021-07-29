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
 * Point.
 */
public class Point {
    public int mx;
    public int my;

    public Point() {}


    public Point(int mx, int my) {
        this.mx = mx;
        this.my = my;
    }

    public Point(Point src) {
        this.mx = src.mx;
        this.my = src.my;
    }

    /**
     * Set the point's mx and my coordinates.
     *
     * @param x x
     * @param y y
     */
    public void set(int x, int y) {
        this.mx = x;
        this.my = y;
    }

    /**
     * Negate the point's coordinates.
     */
    public final void negate() {
        mx = -mx;
        my = -my;
    }

    /**
     * Offset the point's coordinates by dx, dy.
     *
     * @param dx -
     * @param dy -
     */
    public final void offset(int dx, int dy) {
        mx += dx;
        my += dy;
    }

    /**
     * Returns true if the point's coordinates equal (mx,my).
     *
     * @param x x
     * @param y y
     * @return boolean
     */
    public final boolean equals(int x, int y) {
        return this.mx == x && this.my == y;
    }
}