# ModularUI target module

This directory embeds the complete `ModularUI-Modern` `1.21.1` source tree as
an independent Gradle project. The source baseline is upstream commit
`c13e141b830c70922c3540ea245ecf5d29e1029d` from the read-only upstream
repository:

`https://github.com/brachy84/ModularUI-Modern/tree/1.21.1`

The root build includes this project as `:modularui`. GTM consumes it through
the normal project dependency boundary in the root `dependencies.gradle`:

```groovy
jarJar(implementation(project(':modularui')))
```

The module target is Minecraft `26.1.2`, NeoForge `26.1.2.103`, ModDevGradle
`2.0.146`, and Java `25`. Its Maven identity remains
`brachy.modularui:modularui-mc26.1.2`, preserving the `brachy.modularui` Java
package API for GTM consumers.

EMI source is excluded because the port plan explicitly cuts the unverified
EMI target. JEI, REI, Curios, screen, widget, drawable, value-sync, menu,
recipe-viewer, networking, and loader integration source remains bounded in
this module and is not copied into GTM.
