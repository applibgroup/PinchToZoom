package com.github.chrisbanes.photoview;

import ohos.agp.components.Component;
import ohos.agp.components.element.Element;
import ohos.agp.components.element.PixelMapElement;
import ohos.agp.render.Canvas;
import ohos.agp.render.Texture;
import ohos.media.image.PixelMap;
import ohos.media.image.common.AlphaType;
import ohos.media.image.common.PixelFormat;
import ohos.media.image.common.Size;
import ohos.multimodalinput.event.MmiPoint;
import ohos.multimodalinput.event.TouchEvent;

public class Compat {
    public static PixelMap elementToPixelMap(Element element) {
        if (element instanceof PixelMapElement){
           return ((PixelMapElement) element).getPixelMap();
        }
        int w = element.getWidth();
        int h = element.getHeight();

        PixelMap.InitializationOptions options = new PixelMap.InitializationOptions();
        options.size = new Size(w,h);
        options.pixelFormat = PixelFormat.ARGB_8888;
        options.alphaType = AlphaType.UNPREMUL;
        options.editable = true;

        PixelMap pixelMap = PixelMap.create(options);

        Canvas canvas = new Canvas(new Texture(pixelMap));
        element.setBounds(0, 0, w, h);
        element.drawToCanvas(canvas);

        return pixelMap;
    }

    public static float getTouchX(TouchEvent touchEvent, int index, Component component) {
        float x = 0;
        if (touchEvent.getPointerCount() > index) {
            int[] xy = component.getLocationOnScreen();
            if (xy != null && xy.length == 2) {
                x = touchEvent.getPointerScreenPosition(index).getX() - xy[0];
            } else {
                x = touchEvent.getPointerPosition(index).getX();
            }
        }
        return x;
    }

    public static float getTouchY(TouchEvent touchEvent, int index, Component component) {
        float y = 0;
        if (touchEvent.getPointerCount() > index) {
            int[] xy = component.getLocationOnScreen();
            if (xy != null && xy.length == 2) {
                y = touchEvent.getPointerScreenPosition(index).getY() - xy[1];
            } else {
                y = touchEvent.getPointerPosition(index).getY();
            }
        }
        return y;
    }
    public static MmiPoint getTouchPoint(TouchEvent touchEvent, int index, Component component) {
        float x = 0;
        float y = 0;
        if (touchEvent.getPointerCount() > index) {
            int[] xy = component.getLocationOnScreen();
            if (xy != null && xy.length == 2) {
                x = touchEvent.getPointerScreenPosition(index).getX() - xy[0];
            } else {
                x = touchEvent.getPointerPosition(index).getX();
            }
        }
        if (touchEvent.getPointerCount() > index) {
            int[] xy = component.getLocationOnScreen();
            if (xy != null && xy.length == 2) {
                y = touchEvent.getPointerScreenPosition(index).getY() - xy[1];
            } else {
                y = touchEvent.getPointerPosition(index).getY();
            }
        }
        return new MmiPoint(x,y);
    }
}
