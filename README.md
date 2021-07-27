[![Build](https://github.com/applibgroup/MaterialCalendar/actions/workflows/main.yml/badge.svg)](https://github.com/applibgroup/MaterialCalendar/actions/workflows/main.yml)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=applibgroup_MaterialCalendar&metric=alert_status)](https://sonarcloud.io/dashboard?id=applibgroup_MaterialCalendar)

# PinchToZoom
OHOS's ImageView/PhotoView pinch-to-zoom made easy

## Get it

    Download the Library and Add it into ur Application

Add these line to your *build.gradle*'s dependencies:
```
implementation project(path: ':pinchtozoom')
implementation 'io.openharmony.tpc.thirdlib:PhotoView:1.1.1'
```

## Overview
PinchToZoom for HarmonyOs is a simple yet feature complete library for adding pinch-to-zoom functionality to an *ImageView*. It has sleek easing animations that make it stand out in quality and ease of use.

### Features
* Pinch-to-zoom
* Double-tap to quickly zoom-in and out
* Drag while zoomed in
* Animated drag & zoom release easing
* Does not extend the *ImageView* class so is usable with custom *ImageView* implementations
* Fully customizable

## Integrate
Adding pinch-to-zoom functionality to your *ImageView* is easy as this:
```java
PhotoView photoView = (PhotoView) view.findComponentById(ResourceTable.Id_image);
photoView.setTouchEventListener(new ImageMatrixTouchHandler(getContext());
```

## Customization
The *ImageMatrixTouchHandler* class has multiple getter/setter methods that allow for changing the behavior and animation settings.

