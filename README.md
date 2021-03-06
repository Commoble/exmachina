Ex Machina is a power API mod for the Forge modloader that uses JSON data to assign power properties to blocks.

This API is still in alpha and both the mod, its API, this page, and the documentation are still under construction, and any evidence that the mod actually works is therefore purely coincidental.

Mod creators can use the API in their gradle projects by adding this information to their build.gradle:

```
// This goes in the main repositories block, not the buildscript one
repositories {
  maven { url "https://cubicinterpolation.net/maven/" }
}

dependencies {
  compileOnly fg.deobf("commoble.exmachina:exmachina-${mc_version}:${exmachina_version}:api")
  runtimeOnly fg.deobf("commoble.exmachina:exmachina-${mc_version}:${exmachina_version}")
}
```
where ${mc_version} is (for example) 1.16.2 and ${exmachina_version} is the 4-digit version number of exmachina.

You may optionally use :debug instead of :api for the full sources, allowing the use of the full sources.


JSON specification for block properties coming soon. See Ex Machina Essentials for an example of a mod that uses this API:
https://github.com/Commoble/exmachina-essentials

Mods that use this API can register additional JSON deserializers by creating a class that is annotated with commoble.exmachina.api.AutoPlugin and implements commoble.exmachina.api.Plugin. Any class that implements these will be instantiated and invoked during mod init.
