package android.view;

import android.graphics.Outline;

@SuppressWarnings("EmptyMethod")
public class RenderNode {
    private RenderNode(String name, View owningView) {}

    public static RenderNode create(String name, View owningView) {
        throw new UnsupportedOperationException();
    }

    public DisplayListCanvas start(int width, int height) {
        throw new UnsupportedOperationException();
    }

    public void end(DisplayListCanvas canvas) {}

    public boolean setElevation(float lift) {
        throw new UnsupportedOperationException();
    }

    public boolean setTranslationZ(float translationZ) {
        throw new UnsupportedOperationException();
    }

    public boolean setOutline(Outline outline) {
        throw new UnsupportedOperationException();
    }

    public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
        throw new UnsupportedOperationException();
    }

    public boolean setSpotShadowColor(int color) {
        throw new UnsupportedOperationException();
    }

    public boolean setAmbientShadowColor(int color) {
        throw new UnsupportedOperationException();
    }
}