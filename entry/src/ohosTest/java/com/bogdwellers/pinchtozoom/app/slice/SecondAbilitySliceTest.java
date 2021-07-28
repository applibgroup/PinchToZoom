package com.bogdwellers.pinchtozoom.app.slice;

import com.bogdwellers.pinchtozoom.*;
import com.bogdwellers.pinchtozoom.animation.ScaleAnimatorHandler;
import com.bogdwellers.pinchtozoom.view.ImageViewPager;
import ohos.aafwk.ability.delegation.AbilityDelegatorRegistry;
import ohos.agp.utils.Matrix;
import ohos.app.Context;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class SecondAbilitySliceTest {

    Context mContext;
    ImageMatrixTouchHandler imageMatrixTouchHandler;
    @Before
    public void setUp() {
        mContext = AbilityDelegatorRegistry.getAbilityDelegator().getAppContext();
        imageMatrixTouchHandler = new ImageMatrixTouchHandler(mContext);
    }

    @Test
    public void getTouchCount() {
        List<Integer> pointerIds = new ArrayList<Integer>();
        pointerIds.add(1);
        pointerIds.add(3);
        pointerIds.add(5);
        MultiTouchListener multiTouchListener = new MultiTouchListener();
        multiTouchListener.setPointerIds(pointerIds);
        assertEquals(3,multiTouchListener.getTouchCount());
    }

    @Test
    public void isTouchingTrue() {
        List<Integer> pointerIds = new ArrayList<Integer>();
        pointerIds.add(1);
        pointerIds.add(3);
        pointerIds.add(5);
        MultiTouchListener multiTouchListener = new MultiTouchListener();
        multiTouchListener.setPointerIds(pointerIds);
        assertFalse(pointerIds.isEmpty());
    }

    @Test
    public void getId() {
        List<Integer> pointerIds = new ArrayList<Integer>();
        pointerIds.add(1);
        pointerIds.add(3);
        pointerIds.add(5);
        MultiTouchListener multiTouchListener = new MultiTouchListener();
        multiTouchListener.setPointerIds(pointerIds);

        assertEquals(3, multiTouchListener.getId(1));
    }

    @Test
    public void rotatedEnabledTrue() {

        imageMatrixTouchHandler.setRotateEnabled(true);
        assertTrue(imageMatrixTouchHandler.isRotateEnabled());
    }

    @Test
    public void scaleEnabled() {

        imageMatrixTouchHandler.setScaleEnabled(false);
        assertFalse(imageMatrixTouchHandler.isScaleEnabled());
    }

    @Test
    public void testTranslateEnabled() {

        imageMatrixTouchHandler.setTranslateEnabled(false);
        assertFalse(imageMatrixTouchHandler.isTranslateEnabled());
    }


    @Test
    public void testDragOnPinchEnabled() {

        imageMatrixTouchHandler.setDragOnPinchEnabled(true);
        assertTrue(imageMatrixTouchHandler.isDragOnPinchEnabled());
    }

    @Test
    public void testPinchVelocityWindow() {

        imageMatrixTouchHandler.setPinchVelocityWindow(10);
        assertEquals(10, imageMatrixTouchHandler.getPinchVelocityWindow());
    }


    @Test
    public void testDoubleTapZoomDuration() {

        imageMatrixTouchHandler.setDoubleTapZoomDuration(100);
        assertEquals(100, imageMatrixTouchHandler.getDoubleTapZoomDuration());
    }

    @Test
    public void testFlingDuration() {

        imageMatrixTouchHandler.setFlingDuration(55);
        assertEquals(55, imageMatrixTouchHandler.getFlingDuration());
    }

    @Test
    public void testZoomRelease() {

        imageMatrixTouchHandler.setZoomReleaseDuration(50);
        assertEquals(50, imageMatrixTouchHandler.getZoomReleaseDuration());
    }

    @Test
    public void testZoomReleaseExaggeration() {

        imageMatrixTouchHandler.setZoomReleaseExaggeration(50);
        assertEquals(50, (int) imageMatrixTouchHandler.getZoomReleaseExaggeration());
    }

    @Test
    public void testDoubleTapZoomFactor() {

        imageMatrixTouchHandler.setDoubleTapZoomFactor(50);
        assertEquals(50, (int) imageMatrixTouchHandler.getDoubleTapZoomFactor());
    }

    @Test
    public void testDoubleTapZoomOutFactor() {

        imageMatrixTouchHandler.setDoubleTapZoomOutFactor(50);
        assertEquals(50, (int) imageMatrixTouchHandler.getDoubleTapZoomOutFactor());
    }

    @Test
    public void testMatrixCorrector() {

        MatrixCorrector matrixCorrector = new MatrixCorrector() {
            @Override
            public void performAbsoluteCorrections() {
                super.performAbsoluteCorrections();
            }

            @Override
            public float correctRelative(int vector, float x) {
                return super.correctRelative(vector, x);
            }

            @Override
            public float correctAbsolute(float x) {
                return super.correctAbsolute(x);
            }

            @Override
            public Matrix getMatrix() {
                return super.getMatrix();
            }

            @Override
            public void setMatrix(Matrix matrix) {
                super.setMatrix(matrix);
            }

            @Override
            protected float[] getValues() {
                return super.getValues();
            }
        };

        Matrix matrix = new Matrix();
        matrixCorrector.setMatrix(matrix);
        assertEquals(matrix, matrixCorrector.getMatrix());

    }

    @Test
    public void maxScale() {
        ImageViewerCorrector imageViewerCorrector = new ImageViewerCorrector();
        imageViewerCorrector.setMaxScale(20);
        assertEquals(20, (int) imageViewerCorrector.getMaxScale());
    }

    @Test
    public void isMaxScaleRelative() {
        ImageViewerCorrector imageViewerCorrector = new ImageViewerCorrector();
        imageViewerCorrector.setMaxScaleRelative(true);
        assertTrue(imageViewerCorrector.isMaxScaleRelative());
    }

    @Test
    public void testTranslate() {
        ScaleAnimatorHandler scaleAnimatorHandler = new ScaleAnimatorHandler(new ImageMatrixCorrector() {
            @Override
            public float correctAbsolute(int vector, float x) {
                return 0;
            }
        });
        scaleAnimatorHandler.setTranslate(true);
        assertTrue(scaleAnimatorHandler.isTranslate());
    }

    @Test
    public void testScaleAnimatorHanderPx() {
        ScaleAnimatorHandler scaleAnimatorHandler = new ScaleAnimatorHandler(new ImageMatrixCorrector() {
            @Override
            public float correctAbsolute(int vector, float x) {
                return 0;
            }
        });
        scaleAnimatorHandler.setPx(12);
        assertEquals(12, (int) scaleAnimatorHandler.getPx());
    }

    @Test
    public void testScaleAnimatorHandlerPy() {
        ScaleAnimatorHandler scaleAnimatorHandler = new ScaleAnimatorHandler(new ImageMatrixCorrector() {
            @Override
            public float correctAbsolute(int vector, float x) {
                return 0;
            }
        });
        scaleAnimatorHandler.setPy(20);
        assertEquals(20, (int) scaleAnimatorHandler.getPy());
    }

    @Test
    public void testScaleThreshold() {
        ImageViewPager viewPager = new ImageViewPager(mContext);
        viewPager.setScaleThreshold(10);
        assertEquals(10, (int) viewPager.getScaleThreshold());

    }



}