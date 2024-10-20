Ex Machina is a power API mod for the Neoforge modloader that uses JSON data to assign power properties to blocks.

It was primarily created for the purpose of adding compatibility to the [More Red](https://github.com/Commoble/morered) mod,
though any mods which uses the Ex Machina API will be compatible with each other even if More Red is itself not present.

Mod creators can use the API in their gradle projects by adding this information to their build.gradle:

```
repositories {
	maven { url "maven.commoble.net" }
}

dependencies {
	implementation "net.commoble.exmachina:exmachina-${mc_version}:${exmachina_version}
}
```
where ${mc_version} is (for example) 1.21.1 and ${exmachina_version} is the 4-digit version number of exmachina.

You can view the available versions in [the maven](https://maven.commoble.net/net/commoble/exmachina/).

Documentation WIP, see More Red for usage examples
