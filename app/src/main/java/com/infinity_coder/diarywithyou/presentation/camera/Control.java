package com.infinity_coder.diarywithyou.presentation.camera;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.Gesture;
import com.otaliastudios.cameraview.GestureAction;

/**
 * Controls that we want to display in a ControlView.
 */
public enum Control {

    WIDTH("Width", false),
    HEIGHT("Height", true),

    MODE("Mode", false),
    FLASH("Flash", false),
    WHITE_BALANCE("White balance", false),
    HDR("Hdr", true),

    GRID("Grid lines", false),
    GRID_COLOR("Grid color", true),

    // THey are a bit annoying because it's not clear what the default should be.
    VIDEO_CODEC("Video codec", false),
    AUDIO("Audio", true),

    PINCH("Pinch", false),
    HSCROLL("Horizontal scroll", false),
    VSCROLL("Vertical scroll", false),
    TAP("Single tap", false),
    LONG_TAP("Long tap", true);

    private String name;

    Control(String n, boolean l) {
        name = n;
    }

    public String getName() {
        return name;
    }

    public void applyValue(CameraView camera, Object value) {
        switch (this) {
            case WIDTH:
                camera.getLayoutParams().width = (int) value;
                camera.setLayoutParams(camera.getLayoutParams());
                break;
            case HEIGHT:
                camera.getLayoutParams().height = (int) value;
                camera.setLayoutParams(camera.getLayoutParams());
                break;
            case MODE:
            case FLASH:
            case WHITE_BALANCE:
            case GRID:
            case AUDIO:
            case VIDEO_CODEC:
            case HDR:
                camera.set((com.otaliastudios.cameraview.Control) value);
                break;
            case PINCH:
                camera.mapGesture(Gesture.PINCH, (GestureAction) value);
                break;
            case HSCROLL:
                camera.mapGesture(Gesture.SCROLL_HORIZONTAL, (GestureAction) value);
                break;
            case VSCROLL:
                camera.mapGesture(Gesture.SCROLL_VERTICAL, (GestureAction) value);
                break;
            case TAP:
                camera.mapGesture(Gesture.TAP, (GestureAction) value);
                break;
            case LONG_TAP:
                camera.mapGesture(Gesture.LONG_TAP, (GestureAction) value);
                break;
            case GRID_COLOR:
                camera.setGridColor(((GridColor) value).color);
        }
    }


    static class GridColor {
        int color;
        String name;

        GridColor(int color, String name) {
            this.color = color;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof GridColor && color == ((GridColor) obj).color;
        }
    }
}
