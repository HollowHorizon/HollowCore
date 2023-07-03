# HollowCore
HollowCore is a minecraft modding library by [HollowHorizon](https://github.com/HollowHorizon). It is designed to be a lightweight, easy to use, and powerful library for more easily creating minecraft mods. It is designed to be used with Forge.

## Features
- [x] KotlinScript-based (.kts) scripting engine with support for remapping when running on a production run. (Using official mappings)
- [x] Easier registration of blocks, items, and tile entities using annotations. (also automatic model generation and renderers registration is required)
- [x] NBT serialization and deserialization of objects using Kotlinx.Serialization.
- [x] A simple and easy to creating gui's using special layout. (also easy to posing objects, and some widgets, for example lists, buttons, and navigation fields)
- [x] A simple and easy to use networking system, without any serialization or deserialization required.
- [x] Easier capabilities' system, without serialization and auto-generated providers for Entities, Tiles, Worlds and Chunks.
- [x] GLTF model loading and rendering. (Deprecated)
- [x] Automatic configuration file generation and loading with annotations. (maybe in future it will be possible to use it with gui and Kotlinx.Serialization)
- [x] Automatic sounds.json generation.

## Work In Progress
- [ ] Assimp model loading (gltf, fbx, obj and some more)
- [ ] Post Processing, Shaders (visual effects, particles)
- [ ] Gui Framework (May be something like JCEF)
