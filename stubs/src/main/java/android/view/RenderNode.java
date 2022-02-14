package android.view;

import android.graphics.Outline;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class RenderNode {
    private RenderNode(String name, View owningView) {}

    @NonNull
    public static RenderNode create(@Nullable String name, @Nullable View owningView) {
        throw new UnsupportedOperationException();
    }

    @NonNull
    public DisplayListCanvas start(int width, int height) {
        throw new UnsupportedOperationException();
    }

    public void end(@NonNull DisplayListCanvas canvas) {}

    public boolean setElevation(float lift) {
        throw new UnsupportedOperationException();
    }

    public boolean setTranslationZ(float translationZ) {
        throw new UnsupportedOperationException();
    }

    public boolean setOutline(@Nullable Outline outline) {
        throw new UnsupportedOperationException();
    }

    public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
        throw new UnsupportedOperationException();
    }
}