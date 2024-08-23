# Shadow Gadgets

A utility library for Android with various tools to help remedy a couple of
shortcomings in the native material shadows.

<br />

**Visual artifacts**

Unsightly draw defects are visible on `View`s and `Composable`s with see-through
backgrounds.

<p align="center">
<img src="images/intro_clip_broken.png"
alt="Examples of various translucent UI elements showing the artifacts."
width="50%" />
</p>

The clip tools use the same classes and methods that the native framework uses
to render shadows, simply replacing the originals with clipped copies.

<p align="center">
<img src="images/intro_clip_fixed.png"
alt="The above examples with the clip fix applied to each."
width="50%" />
</p>

**Color support**

Shadow colors were not added to the SDK until API level 28 (Pie). Prior to that,
only the alpha values of plain black hues could be manipulated.

Like the clip feature, color compat uses the same native classes and methods,
replacing the originals with tinted copies. Only one color can be applied with
this technique, however; it's not possible to separate the ambient and spot
shadows at this level.

<p align="center">
<img src="images/intro_color_compat.png"
alt="Two shadows, one with native colors, the other tinted with color compat."
width="30%" />
</p>

Though the differences are noticeable when compared side by side, the compat
results are likely sufficient for many cases.

<br />

### Before getting started…

Please note that clipping that shadow artifact is not necessary if the region
behind the UI element is a single solid color. In that case, it is preferable to
simply calculate the opaque color that results from compositing the translucent
one over the solid, and set that as the element's background instead.

- Compose already has the [`compositeOver()`][ComposeComposite] function in its
  `androidx.compose.ui.graphics.Color` class that can do the necessary
  calculations internally.

- The View framework has no such function out of the box, but [this
  extension][ViewsComposite] from the androidx test source shows how to do the
  math for `android.graphics.Color`.

<br />

## Contents

- [**Views**](#views)

  The `view` package contains several extension properties and helper classes to
  apply the library's clip fix and color compat functionalities in Android's
  native framework.

- [**Compose**](#compose)

  For the analogous features in the modern UI toolkit, the `compose` package
  comprises just two functions (and one overload) as direct replacements for the
  inbuilt shadow.

- [**Project notes**](#project-notes)

  Important details, caveats, release notes, etc.

- [**Download**](#download)

  Available through JitPack.

- [**Documentation**][Documentation]

  Note that inherited members are suppressed to prevent, for example, all of
  `ViewGroup`s visible members being listed for each `ShadowsViewGroup`.

<br />

## Views

<details>
  <summary>Subsections</summary>

- [Artifact removal](#artifact-removal)
- [Limitations and recourses](#limitations-and-recourses)
  - [Overlapping sibling Views](#overlapping-sibling-views)
  - [Irregular shapes on Android R+](#irregular-shapes-on-android-r)
  - [Parent matrix on Android N-P](#parent-matrix-on-android-n-p)
- [Color compat](#color-compat)
- [ViewGroups](#viewgroups)
- [Drawable](#drawable)
- [Notes](#notes)
</details>

### Artifact removal

Nobody wants to mess with a whole library for such a small thing that should've
been handled already in the native UI framework, so this was designed to be as
simple and familiar as possible:

```kotlin
view.clipOutlineShadow = true
```

That's it. Unless your setup requires that a sibling `View` overlap a target of
the fix, or it involves a target with an irregular shape on Android R and above,
that's possibly all you need.

The `Boolean`-value [`View.clipOutlineShadow`][clipOutlineShadow] extension
property is basically a switch to toggle the fix on `View`s individually, and it
was designed to mimic an intrinsic property as much as possible. Though the
shadow is actually being handled and drawn in the parent `ViewGroup`, the
property can be set on the target `View` at any time, even while it's
unattached, so there's no need to worry about timing. Additionally, the clipped
shadow automatically animates and transforms along with its target, and it will
handle moving itself to any new parents, should the target be moved.

It is hoped that that simple usage should cover most cases. For those setups
that might be problematic, the library offers a few other configuration
properties as possible fixes.

### Limitations and recourses

There are currently three particular situations that might require further
settings.

- #### Overlapping sibling Views

  To accomplish its effect, the library disables a target's intrinsic shadow and
  draws a clipped replacement in its parent `ViewGroup`'s overlay, by default,
  in front of all of the parent's children. This can cause a problem when a
  sibling with a higher elevation overlaps the target.

  <p align="center">
  <img src="images/plane_foreground_broken.png"
  alt="A target's clipped shadow incorrectly drawn on top of its higher sibling View."
  width="15%" />
  </p>

  As a remedy, the [`ShadowPlane`][ShadowPlane] enum and its corresponding
  [`View.shadowPlane`][shadowPlaneProperty] property are available to move the
  shadow to behind all of the children instead, or, with a couple of extra
  layout settings, to draw right along with the target itself, interleaved
  between siblings.

  Details for the specific enum values and their respective behaviors and
  requirements are given on [the ShadowPlane wiki page][ShadowPlaneWiki].

- #### Irregular shapes on Android R+

  `View`s that are not shaped as circles, plain rectangles, or single-radius
  rounded rectangles have their outlines defined by a `Path` field that became
  inaccessible starting with API level 30. Such targets using
  `clipOutlineShadow` on those newer versions require that the user provide the
  `Path`. This is done with the library's [`ViewPathProvider`][ViewPathProvider]
  interface and its corresponding extension property,
  [`View.pathProvider`][pathProvider]. Details and examples of this feature are
  discussed on [its wiki page][ViewPathProviderWiki].

- #### Parent matrix on Android N-P

  On API levels 24 through 28 (Nougat, Oreo, and Pie), differences in some of
  the low-level graphics operations can give rise to a misalignment in the clip
  region if the parent `ViewGroup` has been transformed by, for example, a
  running animation.

  <p align="center">
  <img src="images/parent_matrix_defect.png"
  alt="A misaligned clip region is shown in a parent scaled by an animation."
  width="15%" />
  </p>

  The exact underlying cause is currently unknown, and any targets using
  `clipOutlineShadow` on API levels 24..28 that are children of parents that
  will have a non-identity matrix applied, by any means, should be tested for
  this glitch. If found, the [`View.forceShadowLayer`][forceShadowLayer]
  property can be used to mitigate, as explained on [its wiki
  page][forceShadowLayerWiki].

### Color compat

This feature can apply an extrinsic tint to the native shadows, allowing for
color shadows on older API levels, though with a somewhat rudimentary
implementation, since it uses a single color in place of the two native ones. As
with the clip, this was designed to be easy and straightforward:

```kotlin
view.outlineShadowColorCompat = Color.BLUE
```

The [`View.outlineShadowColorCompat`][outlineShadowColorCompat] property takes
any `@ColorInt` value, and it's accompanied by an (optional) [helper
class][ShadowColorsBlender] that can proportionally blend the ambient and spot
colors a target uses on newer API levels into a single value for use with the
compat property.

By default, the color compat value is applied only on API levels 27 and below.
The [`View.forceOutlineShadowColorCompat`][forceOutlineShadowColorCompat]
property can be used to enable it on newer versions.

Color compat can be used with or without the clip functionality. When used on
its own, a more performant shadow implementation is substituted, allowing it to
skip expensive clip operations if that area is going to be covered by an opaque
background anyway.

Important information regarding performance and overhead, along with further
details on the feature's behavior and helpers, can be found on [its wiki
page][ViewColorCompatWiki].

### ViewGroups

Several specialized subclasses of common `ViewGroup`s are included mainly as
helpers that allow shadow properties to be set on `View`s from attributes in
layout XML, without the need for extra code. They all implement a [common
interface][ShadowsViewGroup] with a few properties that are mostly conveniences
for setting a single library value on all child `View`s.

The library's features work rather well in Android Studio's layout preview, so
even if you don't intend to use them at runtime, these groups may still be
useful during design.

<p align="center">
<img src="images/layout_editor.png"
alt="Screenshot of Android Studio editing layout XML, showing a custom group that's automatically fixed a child's shadow in the design view."
width="40%" />
</p>

Information on the two general types of groups – Regular and Recycling – along
with descriptions of their behaviors and usage in layout XML can be found on the
[ViewGroups wiki page][ViewGroupsWiki].

### Drawable

[`ShadowDrawable`][ShadowDrawable] is a thin wrapper around the core classes
that allows these shadows to be drawn manually without having to work with the
`core` module directly. As with the other tools, this class requires a
hardware-accelerated `Canvas` to draw.

Details on requirements and usage, and links to examples, can be found on the
[Drawable wiki page][DrawableWiki].

### Notes

- If you only need the clip fix for `View`s in a simple static setup or two, you
  might prefer to put something together from the core techniques demonstrated
  in [this Stack Overflow answer][SOViewAnswer]. If that core solution is
  sufficient, you probably don't want the overhead here.

- The library's particular technique causes a target's parent `ViewGroup` to be
  invalidated more that it normally would. For static shadows, it's just an
  extra time or two here and there. For animated shadows, though, the parent is
  invalidated on each step, no matter the type of animation, effectively
  defeating some of the optimizations that hardware acceleration brings. Button
  presses, for example, aren't "free" when the button has an active library
  shadow.

  It's not great, but it's likely not a huge deal for most setups, especially
  considering that many animations will cause the same invalidations anyway. The
  app in the `demo` module has several different arrangements that translate and
  rotate and such, so you can investigate the effects, if curious.

- To disable the target's inherent shadow, its `ViewOutlineProvider` is wrapped
  in a custom implementation. This has the possibility of breaking something if
  some function or component is expecting the `View` to have one of the static
  platform implementations; i.e., `BACKGROUND`, `BOUNDS`, or `PADDED_BOUNDS`.
  This shouldn't cause a fatal error, or anything – it's no different than
  anything else that uses a custom provider – but you might need to rework some
  background drawables or the like.

<br />

## Compose

<details>
  <summary>Subsections</summary>

- [Artifact removal](#artifact-removal-1)
- [Color compat](#color-compat-1)
- [Notes](#notes-1)

</details>

<br />

Details and examples for both functions can be found on the [Compose wiki
page][ComposeWiki].

### Artifact removal

The base [`clippedShadow`][clippedShadow] is a drop-in replacement for Compose's
[`shadow`][shadow] function, with the exact same signature and defaults, and the
exact same usage. For example:

```kotlin
Box(
    Modifier
        .clippedShadow(
            elevation = 10.dp,
            shape = CircleShape
        )
        …
)
```

### Color compat

The Composables handle color compat through additional parameters; e.g., using
`clippedShadow`'s overload to add some color:

```kotlin
Box(
    Modifier
        .clippedShadow(
            elevation = 10.dp,
            shape = CircleShape,
            ambientColor = Color.Blue,
            spotColor = Color.Cyan,
            colorCompat = Color.Blue
        )
        …
)
```

On API level 27 and below, the ambient and spot colors are ignored, and the
normally black shadow will be tinted with `colorCompat`.

For those cases where you need color compat but the clipping is unnecessary,
[`shadowCompat`][shadowCompat] is a more performant option. It has the exact
same signature and color behavior as the `clippedShadow` overload above, just
without the shadow clipping.

```kotlin
Box(
    Modifier
        .shadowCompat(
            elevation = 10.dp,
            shape = CircleShape,
            ambientColor = Color.Blue,
            spotColor = Color.Cyan,
            colorCompat = Color.Unspecified
        )
        …
)
```

### Notes

- If you only need the clip fix in Compose for a relatively simple setup or two,
  you might prefer to try something like the stacked `Composable` solution
  demonstrated in [this Stack Overflow answer][SOComposeAnswer]. The primary
  benefits of the library's Compose version are user convenience, and access to
  the color compat functionality. If those aren't concerns, you might be able to
  avoid the library overhead with just a custom `Layout` and some wrapper
  functions.

- Color compat here is currently accomplished similarly to how `Inline` shadows
  are handled for Views, meaning the same internal requirements and overhead
  apply to this, for the time being. Please refer to [the Performance and
  overhead section][PerformanceOverhead] on the Color Compat wiki page.

- Compose's color compat currently requires `@OptIn`, as work is still being
  done internally to cut down on overhead. The public API is locked, however,
  and the feature is as stable and robust as the clip.

<br />

## Project notes

- The native ambient and spot shadow colors are supported on Pie and above,
  technically. They absolutely do work for Q+, but I cannot get the native
  shadow colors to work _at all_ on Pie itself, with or without this library
  involved. All of the relevant methods and attributes were introduced with that
  version, and the documentation indicates that they should work like normal,
  but none of the emulators I've tested on show anything but black shadows. I
  can't find mention of anyone else having the same issue, though, so I'm
  completely baffled by this.

  The demo app's Intro page has a setup that lets you fiddle with the shadow
  color, so that could be used as a quick test, if you're curious. It is set up
  to fall back to the new color compat mechanism for API levels <28, but 28
  itself uses the native ambient and spot colors.

- The demo app was put together by eye on 1080x1920 xxhdpi devices, so things
  might not look that great on other configurations. Just a heads up.

<br />

## Download

The library is available as a compiled dependency through the very handy service
[JitPack](https://jitpack.io/#zed-alpha/shadow-gadgets). To enable download in a
modern Gradle setup, add their Maven URL to the `repositories` block that's
inside the `dependencyResolutionManagement` block in the root project's
`settings.gradle[.kts]` file; e.g.:

```kotlin
dependencyResolutionManagement {
    …
    repositories {
        …
        maven { url 'https://jitpack.io' }
    }
}
```

Then add a dependency for
[the latest release](https://github.com/zed-alpha/shadow-gadgets/releases) of
whichever module is required, `view` or `compose`:

```kotlin
dependencies {
  …
  implementation 'com.github.zed-alpha.shadow-gadgets:view:[latest-release]'
  implementation 'com.github.zed-alpha.shadow-gadgets:compose:[latest-release]'
}
```

You can also get the `core` module directly, if you'd like, but there are no
examples or docs for it, and its API is liable to change drastically without
notice.

<br />

## License

MIT License

Copyright (c) 2024 zed-alpha

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.


[ComposeComposite]: https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/Color#(androidx.compose.ui.graphics.Color).compositeOver(androidx.compose.ui.graphics.Color)

[ViewsComposite]: https://github.com/androidx/androidx/blob/fcb9a89959e0bbbdd1ec63ac82e279feb8336daa/graphics/graphics-core/src/androidTest/java/androidx/graphics/surface/SurfaceControlCompatTest.kt#L1783

[Documentation]: https://zed-alpha.github.io/shadow-gadgets

[clipOutlineShadow]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/clip-outline-shadow.html

[ShadowPlane]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/-shadow-plane/index.html

[shadowPlaneProperty]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/shadow-plane.html

[ShadowPlaneWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowPlane

[ViewPathProvider]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/-view-path-provider/index.html

[pathProvider]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/path-provider.html

[ViewPathProviderWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ViewPathProvider

[forceShadowLayer]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/force-shadow-layer.html

[forceShadowLayerWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/View.forceShadowLayer

[outlineShadowColorCompat]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/outline-shadow-color-compat.html

[ShadowColorsBlender]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/-shadow-colors-blender/index.html

[forceOutlineShadowColorCompat]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/force-outline-shadow-color-compat.html

[ViewColorCompatWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Color-compat

[ShadowsViewGroup]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view.viewgroup/-shadows-view-group/index.html

[ViewGroupsWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ViewGroups

[ShadowDrawable]: https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view.drawable/-shadow-drawable/index.html

[DrawableWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Drawable

[ViewGroupsLintWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ViewGroups#lint-integration

[SOViewAnswer]: https://stackoverflow.com/a/70076301

[LayoutInflationHelpersWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Layout-inflation-helpers

[shadow]: https://developer.android.com/reference/kotlin/androidx/compose/ui/Modifier#(androidx.compose.ui.Modifier).shadow(androidx.compose.ui.unit.Dp,androidx.compose.ui.graphics.Shape,kotlin.Boolean,androidx.compose.ui.graphics.Color,androidx.compose.ui.graphics.Color)

[clippedShadow]: https://zed-alpha.github.io/shadow-gadgets/compose/com.zedalpha.shadowgadgets.compose/clipped-shadow.html

[shadowCompat]: https://zed-alpha.github.io/shadow-gadgets/compose/com.zedalpha.shadowgadgets.compose/shadow-compat.html

[ComposeWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Compose

[SOComposeAnswer]: https://stackoverflow.com/a/71868521

[PerformanceOverhead]: https://github.com/zed-alpha/shadow-gadgets/wiki/Color-compat#performance-and-overhead