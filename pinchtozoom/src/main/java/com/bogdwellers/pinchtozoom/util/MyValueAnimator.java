package com.bogdwellers.pinchtozoom.util;

import ohos.agp.animation.AnimatorValue;

public class MyValueAnimator extends AnimatorValue {
    private float start = 0;
    private float end = 1;
    private ValueUpdateListener myValueUpdateListener;

    /**
     * 获取一个自定义初始值和结束值的数值动画对象
     *
     * @param start 起始值
     * @param end   结束值
     * @return 自定义初始值和结束值的数值动画对象
     */
    public static MyValueAnimator ofFloat(float start, float end) {
        MyValueAnimator myValueAnimator = new MyValueAnimator();
        myValueAnimator.start = start;
        myValueAnimator.end = end;
        return myValueAnimator;
    }

    public MyValueAnimator() {
        super.setValueUpdateListener(new ValueUpdateListener() {
            @Override
            public void onUpdate(AnimatorValue animatorValue, float value) {
                value = value * (end - start) + start;
                if (myValueUpdateListener != null) {
                    myValueUpdateListener.onUpdate(animatorValue, value);
                }
            }
        });
    }

    /**
     * 设置数值动画的起始值和结束值
     *
     * @param start 起始值
     * @param end   结束值
     */
    public void setFloatValues(float start, float end) {
        this.start = start;
        this.end = end;
    }

    @Override
    public void setValueUpdateListener(ValueUpdateListener listener) {
        this.myValueUpdateListener = listener;
    }

}