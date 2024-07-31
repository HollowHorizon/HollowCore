# HollowCore
HollowCore is a minecraft modding library by [HollowHorizon](https://github.com/HollowHorizon). It is an easy to use and powerful library for easier creation of minecraft mods for Fabric, Forge and NeoForge.

## Features
### Integration with ModLoaders
- [x] Universal Registration system to register blocks, items, block entities, etc. using kotlin delegates.
- [x] Universal Packet system with automatic serialization and registration using annotations.
- [x] Universal Capabilities system to storing nbt in entities, block entities and worlds. 
- [x] Universal EventBus system for all modloaders and basic events.
### Easier development
- [x] Automatic models generation for blocks and items using embed resourcepack.
- [x] NBT serialization and deserialization of any objects using Kotlinx.Serialization.
- [x] Toml Config system based on KotlinX Serialization.
- [x] Automatic sounds.json generation.
### Graphics
- [x] ImGui based framework: basic elements, items, entities, containers, slots, etc. 
- [x] GLTF models support includes skeletal animations, skinning, morph targets, PBR materials with Iris/Oculus support.
- [x] Effekseer particles support.
