Ex Machina is a power API mod for the Neoforge modloader that uses JSON data to assign power properties to blocks.

It was primarily created for the purpose of adding compatibility to the [More Red](https://github.com/Commoble/morered) mod,
though any mods which uses the Ex Machina API will be compatible with each other even if More Red is itself not present.

Mod creators can use the API in their gradle projects by adding this information to their build.gradle:

```
repositories {
	maven { url "maven.commoble.net" }
}

dependencies {
	implementation "net.commoble.exmachina:exmachina:${exmachina_version}
}
```

As of MC 1.21.9, Ex Machina versions follow the MCMINOR.MCPATCH.MODVERSION schema, e.g. exmachina 21.9.0 is the first release of Ex Machina for MC 1.21.9.

You can view the available versions in [the maven](https://maven.commoble.net/net/commoble/exmachina/exmachina).

Documentation WIP, see More Red for usage examples
