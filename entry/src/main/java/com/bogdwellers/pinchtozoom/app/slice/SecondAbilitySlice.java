package com.bogdwellers.pinchtozoom.app.slice;

import com.bogdwellers.pinchtozoom.ImageMatrixTouchHandler;
import com.bogdwellers.pinchtozoom.app.ResourceTable;
import com.bogdwellers.pinchtozoom.view.ImageViewPager;
import com.github.chrisbanes.photoview.PhotoView;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.PageSliderProvider;
import ohos.app.Context;
import ohos.multimodalinput.event.TouchEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * SecondAbilitySlice.
 */
public class SecondAbilitySlice extends AbilitySlice {

    public static final String TAG = SecondAbilitySlice.class.getSimpleName();
    public static final int PICK_IMAGE = 1;
    public static final String DEFAULT_IMAGES_FOLDER = "default_images";
    public static final String PICKED_IMAGES = "picked_images";

    private ImageViewPager viewPager;
    private ImageViewPagerAdapter imageViewPagerAdapter;


    @Override
    protected void onStart(Intent savedInstanceState) {
        super.onStart(savedInstanceState);
        setUIContent(ResourceTable.Layout_ability_main);

        List<Integer> drawables = new ArrayList<>();

        addDefaultImages(drawables);



        imageViewPagerAdapter = new ImageViewPagerAdapter(drawables);

        viewPager = (ImageViewPager) findComponentById(ResourceTable.Id_pager);
        viewPager.setProvider(imageViewPagerAdapter);
        viewPager.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                return viewPager.onTouchEvent(component, touchEvent);
            }
        });

    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }

    /**
     * addDefaultImages.
     *
     * @param drawables - drawables.
     */
    private void addDefaultImages(List<Integer> drawables)  {

        drawables.add(ResourceTable.Media_1_wtc61);
        drawables.add(ResourceTable.Media_2_love_couple_kissing);
        drawables.add(ResourceTable.Media_3_forest_hills);

    }

    /**
     * ImageViewPagerAdapter Class.
     */
    private static class ImageViewPagerAdapter extends PageSliderProvider {

        private List<Integer> drawables;

        public ImageViewPagerAdapter(List<Integer> drawables) {
            this.drawables = drawables;
        }

        @Override
        public Object createPageInContainer(ComponentContainer container, int position) {
            Context context = container.getContext();
            LayoutScatter layoutInflater = LayoutScatter.getInstance(context);
            Component view = layoutInflater.parse(ResourceTable.Layout_page_image, null, false);
            container.addComponent(view);

            PhotoView photoView = (PhotoView) view.findComponentById(ResourceTable.Id_image);
            photoView.setImageAndDecodeBounds(drawables.get(position).intValue());


            ImageMatrixTouchHandler imageMatrixTouchHandler = new ImageMatrixTouchHandler(context);

            photoView.setTouchEventListener(imageMatrixTouchHandler);

            return view;

        }

        @Override
        public int getPageIndex(Object object) {
            return POSITION_INVALID;
        }

        @Override
        public int getCount() {
            return drawables.size();
        }



        @Override
        public void destroyPageFromContainer(ComponentContainer container, int i, Object o) {
            Component view = (Component) o;

            PhotoView imageView = (PhotoView) view.findComponentById(ResourceTable.Id_image);

            imageView.setImageAndDecodeBounds(0);
            container.removeComponent(view);
        }

        @Override
        public boolean isPageMatchToObject(Component view, Object object) {
            return view == object;
        }

    }

}
