package android.view;

import android.graphics.Matrix;
import android.graphics.Outline;

import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
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

    public boolean isValid() {
        throw new UnsupportedOperationException();
    }

    public float getAlpha() {
        throw new UnsupportedOperationException();
    }

    public boolean setAlpha(@FloatRange(from = 0.0, to = 1.0) float alpha) {
        throw new UnsupportedOperationException();
    }

    public float getCameraDistance() {
        throw new UnsupportedOperationException();
    }

    public boolean setCameraDistance(float distance) {
        throw new UnsupportedOperationException();
    }

    public float getElevation() {
        throw new UnsupportedOperationException();
    }

    public boolean setElevation(float lift) {
        throw new UnsupportedOperationException();
    }

    public float getPivotX() {
        throw new UnsupportedOperationException();
    }

    public boolean setPivotX(float pivotX) {
        throw new UnsupportedOperationException();
    }

    public float getPivotY() {
        throw new UnsupportedOperationException();
    }

    public boolean setPivotY(float pivotY) {
        throw new UnsupportedOperationException();
    }

    public float getRotation() {
        throw new UnsupportedOperationException();
    }

    public boolean setRotation(float rotation) {
        throw new UnsupportedOperationException();
    }

    public float getRotationX() {
        throw new UnsupportedOperationException();
    }

    public boolean setRotationX(float rotationX) {
        throw new UnsupportedOperationException();
    }

    public float getRotationY() {
        throw new UnsupportedOperationException();
    }

    public boolean setRotationY(float rotationY) {
        throw new UnsupportedOperationException();
    }

    public float getScaleX() {
        throw new UnsupportedOperationException();
    }

    public boolean setScaleX(float scaleX) {
        throw new UnsupportedOperationException();
    }

    public float getScaleY() {
        throw new UnsupportedOperationException();
    }

    public boolean setScaleY(float scaleY) {
        throw new UnsupportedOperationException();
    }

    public float getTranslationX() {
        throw new UnsupportedOperationException();
    }

    public boolean setTranslationX(float translationX) {
        throw new UnsupportedOperationException();
    }

    public float getTranslationY() {
        throw new UnsupportedOperationException();
    }

    public boolean setTranslationY(float translationY) {
        throw new UnsupportedOperationException();
    }

    public float getTranslationZ() {
        throw new UnsupportedOperationException();
    }

    public boolean setTranslationZ(float translationZ) {
        throw new UnsupportedOperationException();
    }

    public int getAmbientShadowColor() {
        throw new UnsupportedOperationException();
    }

    public boolean setAmbientShadowColor(@ColorInt int color) {
        throw new UnsupportedOperationException();
    }

    public int getSpotShadowColor() {
        throw new UnsupportedOperationException();
    }

    public boolean setSpotShadowColor(@ColorInt int color) {
        throw new UnsupportedOperationException();
    }

    public boolean setOutline(@Nullable Outline outline) {
        throw new UnsupportedOperationException();
    }

    public boolean setLeftTopRightBottom(int left, int top, int right, int bottom) {
        throw new UnsupportedOperationException();
    }

    public boolean hasIdentityMatrix() {
        throw new UnsupportedOperationException();
    }

    public void getMatrix(@NonNull Matrix outMatrix) {}

    public boolean setProjectBackwards(boolean shouldProject) {
        throw new UnsupportedOperationException();
    }

    public boolean setProjectionReceiver(boolean shouldReceive) {
        throw new UnsupportedOperationException();
    }
}