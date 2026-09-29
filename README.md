# Shadow Gadgets

A utility library for Android with various tools to help remedy shortcomings in
the shadow implementations for Views and Composables.

<br />

**Visual artifacts**

Unsightly draw defects in the native shadows are visible on elements with
see-through backgrounds.

<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
    <!--suppress CheckImageSize -->
    <img
        src="images/intro_clip_broken.png" 
        alt="Examples of various translucent UI elements showing the artifacts." 
        width="50%" />
</p>

The clip tools use the same classes and methods that the framework uses to
render shadows, simply replacing the originals with clipped copies.

<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
    <!--suppress CheckImageSize -->
    <img
        src="images/intro_clip_fixed.png" 
        alt="The above examples with the clip fix applied to each." 
        width="50%" />
</p>

This clip feature is also available for Compose's drop shadow modifiers.

<br />

**Color support**

Shadow colors were not added to the native shadows until API level 28 (Pie).
Before that, the only relevant adjustment available was the alpha value of plain
black.

Like the clip feature, color compat uses the same native classes and methods,
replacing the originals with tinted copies. Only one color can be applied with
this technique, however, as it's not possible to separate the ambient and spot
shadows at this level.

<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
    <!--suppress CheckImageSize -->
    <img
        src="images/intro_color_compat.png" 
        alt="A shadow with native colors, and another tinted with color compat." 
        width="25%" />
</p>

Though the differences are noticeable when compared side by side, the compat
results are likely sufficient for many cases.

<br />

## Contents

- [**Views**](#views)
- [**Compose**](#compose)
- [**Download**](#download)
- [**Documentation**<sup>↗</sup>][Documentation]
- [**Notes**][Notes]

<br />

## Views

<details>
  <summary>Subsections</summary>

- [Limitations and recourses](#limitations-and-recourses)
  - [Overlapping sibling Views](#overlapping-sibling-views)
  - [Irregular shapes on Android R+](#irregular-shapes-on-android-r)
- [ViewGroups](#viewgroups)
- [Drawable](#drawable)
- [Helpers](#helpers)
</details>

Nobody wants to mess with a whole library for such small issues that should've
been handled already in the native framework or supporting packages, so these
tools have been designed to be as simple and familiar as possible.

```kotlin
view.clipOutlineShadow = true
view.outlineShadowColorCompat = Color.BLUE
```

That's it. Unless your setup requires that a sibling `View` overlap a target of
the fix, or it involves a target with an irregular shape on Android R and above,
that's possibly all you need.

- The [`View.clipOutlineShadow: Boolean`][clipOutlineShadow] extension is simply
  a switch that toggles the clip fix on the receiver `View`. When `true`, the
  intrinsic shadow is disabled and replaced with a clipped copy.

- The [`View.outlineShadowColorCompat: Int`][outlineShadowColorCompat] property
  takes a `@ColorInt` with which to tint replacement shadows on versions before
  Pie. A separate extension is available to force it on newer versions, and it
  can be used with or without the clip feature. The particulars can be found on
  [its wiki page][ViewColorCompatWiki].

Though the library's shadow is actually being handled and drawn in the parent
`ViewGroup`, these properties can be set on the target `View` at any point, even
while it's unattached, so there's no need to worry about timing. Additionally,
the shadow automatically animates and transforms along with its target, and it
will handle moving itself to any new parents should the target be moved.

It is hoped that the base features will cover most cases. For those setups that
might be problematic, the library offers a couple of configuration properties as
potential remedies.

### Limitations and recourses

- #### Overlapping sibling Views

  To accomplish its effect, the library disables a target's intrinsic shadow and
  draws a modified replacement in its parent `ViewGroup`'s overlay by default,
  in front of all the children. This can cause a problem when a sibling with a
  higher elevation overlaps the target.

  <!--suppress HtmlDeprecatedAttribute -->
  <p align="center">
      <!--suppress CheckImageSize -->
      <img
          src="images/plane_foreground_broken.png" 
          alt="A View's clipped shadow incorrectly drawn atop a higher sibling." 
          width="20%" />
  </p>

  The [`ShadowPlane`][ShadowPlane] enum defines other options for different
  points in the hierarchy's draw routine where the library shadow can be
  inserted. Specifics and requirements are given on [its wiki
  page][ShadowPlaneWiki].

- #### Irregular shapes on Android R+

  Starting with API level 30, `View`s that are not shaped as circles, plain
  rectangles, or single-radius rounded rectangles require that the developer
  provide the outline `Path` for the clip.

  <!--suppress HtmlDeprecatedAttribute -->
  <p align="center">
      <!--suppress CheckImageSize -->
      <img
          src="images/view_path_provider.png" 
          alt="A View in the shape of a puzzle piece with its shadow clipped." 
          width="20%" />
  </p>

  This is done with the [`ViewPathProvider`][ViewPathProvider] interface,
  details and examples for which are discussed on [its wiki
  page][ViewPathProviderWiki].

### ViewGroups

Several specialized subclasses of common `ViewGroup`s are included mainly as
helpers that allow shadow properties to be set on `View`s from attributes in
layout XML, without the need for extra code.

The library's features work rather well in Android Studio's layout preview, so
even if you don't intend to use them at runtime, these groups may still be
useful during design.

<!--suppress HtmlDeprecatedAttribute -->
<p align="center">
    <!--suppress CheckImageSize -->
    <img
        src="images/layout_editor.png" 
        alt="Android Studio's layout editor showing library effects." 
        width="40%" />
</p>

Information on the two general types – Regular and Recycling – along with
descriptions of their behaviors and usage in layout XML can be found on the
[ViewGroups wiki page][ViewGroupsWiki].

### Drawable

[`ShadowDrawable`][ShadowDrawable] is a thin wrapper around the core classes
that allows these shadows to be drawn manually, or used with standard `Drawable`
APIs. Information on requirements and usage, and links to examples are available
on the [Drawable wiki page][DrawableWiki].

### Helpers

The library also offers [a `View` extension][View.updateShadowWiki] that allows
for updating multiple shadow properties at once, which can benefit performance
in recycling `Adapter`s, and help to avoid invalid states when
[`throwOnUnhandledErrors`][throwOnUnhandledErrorsWiki] is enabled.

[The object][ShadowGadgetsWiki] containing the throw property also holds a few
other flags for the (internal) draw method, and log and error behavior. To
assist with runtime error handling, an [enum and extension
function][ShadowModeWiki] are available to act as a mode change callback.

<br />

## Compose

<details>
  <summary>Subsections</summary>

- [Modifier.clippedShadow](#modifierclippedshadow)
  - [Simple](#simple)
  - [Color compat](#color-compat)
  - [Lambda](#lambda)
- [Modifier.shadowCompat](#modifiershadowcompat)
  - [Simple](#simple-1)
  - [Lambda](#lambda-1)
- [Modifier.clippedDropShadow](#modifierclippeddropshadow)
  - [Simple](#simple-2)
  - [Lambda](#lambda-2)
  </details>

Since Compose already allows shadows to be handled and manipulated as discrete
UI elements, employing the library's features here is straightforward and
routine.

There are two replacements for the inbuilt `shadow` modifier:
[`clippedShadow`][clippedShadow] and [`shadowCompat`][shadowCompat], the latter
being the more performant option when only color compat is needed.
([wiki][ComposeNativeWiki])

The last modifier, [`clippedDropShadow`][clippedDropShadow], adds the clip
feature to the new `dropShadow` modifier, which has supported color from the
start, so there's no need for a compat version here. ([wiki][ComposeDropWiki])

### Modifier.clippedShadow

- #### Simple

  The base [`clippedShadow`][clippedShadow] is a drop-in replacement for
  `shadow`, with the exact same signature and defaults, and identical usage. For
  example:

  ```kotlin
  Modifier
      .clippedShadow(
          elevation = 10.dp,
          shape = CircleShape,
          …
      )
  ```

- #### Color compat

  Color compat is handled here through additional parameters in an overload.

  ```kotlin
  Modifier
      .clippedShadow(
          elevation = 10.dp,
          shape = CircleShape,
          …
          colorCompat = Color.Blue,
          forceColorCompat = true
      )
  ```

- #### Lambda

  There is now also an overload that takes a lambda to allow for efficient
  updates of shadow properties without recomposition. This mimics the lambda
  version of `dropShadow`; elevation is taken in pixels rather than `Dp`, but
  the scope is a `Density` so conversions are trivial.

  ```kotlin
  Modifier
      .clippedShadow(shape = CircleShape) {
          elevation = animatedElevationDp.toPx()
          …
      }
  ```

### Modifier.shadowCompat

- #### Simple

  The [`shadowCompat`][shadowCompat] modifier is a more performant option for
  those cases where only color compat is needed, without the clip.

  ```kotlin
  Modifier
      .shadowCompat(
          elevation = 10.dp,
          shape = CircleShape,
          ambientColor = Color.Blue,
          spotColor = Color.Cyan,
          colorCompat = Color.Blue
      )
  ```

- #### Lambda

  `shadowCompat` also has a lambda overload that is exactly like
  `clippedShadow`'s except for the name of its scope interface, which is itself
  otherwise identical.

  ```kotlin
  Modifier
      .shadowCompat(shape = CircleShape) {
          elevation = animatedElevationDp.toPx()
          colorCompat = animatedColor
          forceColorCompat = true
          …
      }
  ```

### Modifier.clippedDropShadow

- #### Simple

  The base [`clippedDropShadow`][clippedDropShadow] requires a `Shadow`
  instance, and is a drop-in replacement for the corresponding `dropShadow`
  overload.

  ```kotlin
  Modifier
      .clippedDropShadow(
          shape = CircleShape,
          shadow = Shadow(radius = 10.dp, color = Color.Blue)
      )
  ```

- #### Lambda

  This version allows for shadow property updates without recomposition. It is a
  drop-in replacement as well.

  ```kotlin
  Modifier
      .clippedDropShadow(shape = CircleShape) {
          radius = animatedElevationDp.toPx()
          color = animatedColor
          …
      }
  ```

<br />

## Download

> [!IMPORTANT]
> Remember to check [the Notes][Notes] for anything that might be pertinent to
> your project.

Starting with version 2.5.1, the libraries will be published to Maven Central.
They'll still be available on JitPack for one or two releases more, but the
relevant configuration will eventually be removed.

A custom lint rule for dependencies has been added to both modules to alert
JitPack users of the move. Be aware that if you use both libraries in a single
project, you may get double, redundant alerts, one from each. This is only a
temporary notice, so I didn't bother trying to coordinate them.

The snippets that follow are all `kts`, they use `?.?.?` as a placeholder for
[the latest version number][latest-release], and they register the dependencies
with inline literals, though you're probably using a `libs.versions.toml` these
days.

### Versions 2.5.1+

**[Maven Central][MavenCentral]**

Any Android project created with the standard templates is already set up for
this. All that's required is a single function call in the `repositories` block
shown below, usually found in `settings.gradle[.kts]`.

```kotlin
…
dependencyResolutionManagement {
    …
    repositories {
        …
        mavenCentral()
    }
}
```

And the `dependencies`, usually in `build.gradle[.kts]`:

```kotlin
…
dependencies {
    …
    implementation("io.github.zed-alpha.shadow-gadgets:view:?.?.?")
    implementation("io.github.zed-alpha.shadow-gadgets:compose:?.?.?")
}
```

### Versions <= 2.5.0

**[JitPack][JitPack]**

This `maven` entry will have to be added manually, unless you happen to be using
it already.

```kotlin
…
dependencyResolutionManagement {
    …
    repositories {
        …
        maven { url = uri("https://jitpack.io") }
    }
}
```

The only difference below is the coordinate group's TLD; this one is `com`
instead of Maven Central's `io`.

```kotlin
…
dependencies {
    …
    implementation("com.github.zed-alpha.shadow-gadgets:view:?.?.?")
    implementation("com.github.zed-alpha.shadow-gadgets:compose:?.?.?")
}
```

### Semantic-ish Versioning

Please note that this project doesn't follow SemVer very strictly, even though
the version numbers are in that format. I prefer to handle small breaking
changes in minor versions rather than major. Sorry if that's an inconvenience.

<br />

<br />

## License

MIT License

Copyright (c) 2026 zed-alpha

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

[Notes]: https://github.com/zed-alpha/shadow-gadgets/wiki/Notes
[Documentation]: https://zed-alpha.github.io/shadow-gadgets
[Issues]: https://github.com/zed-alpha/shadow-gadgets/issues
[clipOutlineShadow]:
  https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/clip-outline-shadow.html
[outlineShadowColorCompat]:
  https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/outline-shadow-color-compat.html
[ViewColorCompatWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/Color-compat
[ShadowPlane]:
  https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/-shadow-plane/index.html
[ShadowPlaneWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowPlane
[ViewPathProvider]:
  https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view/-view-path-provider/index.html
[ViewPathProviderWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/ViewPathProvider
[ViewGroupsWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ViewGroups
[ShadowDrawable]:
  https://zed-alpha.github.io/shadow-gadgets/view/com.zedalpha.shadowgadgets.view.drawable/-shadow-drawable/index.html
[DrawableWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Drawable
[View.updateShadowWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/Multi–property-update#viewupdateshadow
[throwOnUnhandledErrorsWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowGadgets#throwOnUnhandledErrors
[ShadowGadgetsWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowGadgets
[ShadowModeWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/ShadowMode
[clippedShadow]:
  https://zed-alpha.github.io/shadow-gadgets/compose/com.zedalpha.shadowgadgets.compose/clipped-shadow.html
[shadowCompat]:
  https://zed-alpha.github.io/shadow-gadgets/compose/com.zedalpha.shadowgadgets.compose/shadow-compat.html
[clippedDropShadow]:
  https://zed-alpha.github.io/shadow-gadgets/compose/com.zedalpha.shadowgadgets.compose/clipped-drop-shadow.html
[ComposeNativeWiki]:
  https://github.com/zed-alpha/shadow-gadgets/wiki/Native-shadows
[ComposeDropWiki]: https://github.com/zed-alpha/shadow-gadgets/wiki/Drop-shadows
[MavenCentral]: https://central.sonatype.com/
[JitPack]: https://jitpack.io/#zed-alpha/shadow-gadgets
[latest-release]: https://github.com/zed-alpha/shadow-gadgets/releases/latest