-dontwarn android.view.DisplayListCanvas
-dontwarn android.view.HardwareCanvas
-dontwarn android.view.RenderNode

-keepclassmembers class com.zedalpha.shadowgadgets.view.internal.BaseView {
    public void damageInParent();
}

-keepclassmembers class com.zedalpha.shadowgadgets.view.internal.Painter {
    public void damageInParent();
}

-if class androidx.constraintlayout.motion.widget.KeyFrames
-keep class androidx.constraintlayout.motion.widget.KeyAttributes

-if class androidx.constraintlayout.motion.widget.KeyFrames
-keep class androidx.constraintlayout.motion.widget.KeyPosition

-if class androidx.constraintlayout.motion.widget.KeyFrames
-keep class androidx.constraintlayout.motion.widget.KeyCycle

-if class androidx.constraintlayout.motion.widget.KeyFrames
-keep class androidx.constraintlayout.motion.widget.KeyTimeCycle

-if class androidx.constraintlayout.motion.widget.KeyFrames
-keep class androidx.constraintlayout.motion.widget.KeyTrigger